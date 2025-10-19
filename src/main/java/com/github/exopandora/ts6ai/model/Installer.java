package com.github.exopandora.ts6ai.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.exopandora.ts6ai.util.IOUtils;
import com.github.exopandora.ts6ai.util.OS;
import com.vdurmont.semver4j.Requirement;
import com.vdurmont.semver4j.Semver;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.exopandora.ts6ai.TS6AddonInstaller.VERSION;
import static com.github.exopandora.ts6ai.util.OS.MAC_OS;
import static com.github.exopandora.ts6ai.util.Util.OBJECT_MAPPER;

public class Installer {
	private static final int SCHEMA_VERSION = 2;
	private static final Pattern ADDON_START_REGEX = Pattern.compile("<!-- ADDON_START v(\\d+) ([A-Za-z_0-9]+) ([^ ]+) \"(.+)\" (?:([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}) )?-->");
	private static final String ADDON_END_STRING_V1 = "<!-- ADDON_END -->";
	private static final String CLIENT_UI_PATH = "/html/client_ui/";
	private static final String ADDON_DEFINITION_FILE_NAME = "addon.json";
	private static final String INDEX = "index.html";
	
	public static Optional<Addon> install(IAddonSource addonSource, String installDir, ConflictHandler conflictHandler) throws Exception {
		Semver ts6version = VersionIndex.findTeamSpeakVersion(installDir, OS.getOrThrow());
		addonSource.open();
		String addonJsonPath = findAddonJsonPath(addonSource);
		Addon addon;
		try {
			String addonJson = addonSource.read(addonJsonPath);
			addon = OBJECT_MAPPER.readValue(addonJson, Addon.class);
		} catch(Exception e) {
			throw new Exception("Addon does not contain a valid " + ADDON_DEFINITION_FILE_NAME, e);
		}
		if(addon.getTeamSpeakVersion().isPresent()) {
			Requirement requirement = addon.getTeamSpeakVersion().get();
			if(!ts6version.satisfies(requirement)) {
				throw new Exception("Addon " + addon.getName() + " requires TeamSpeak version " + requirement);
			}
		}
		if(addon.getInstallerVersion().isPresent()) {
			Requirement requirement = addon.getInstallerVersion().get();
			if(!new Semver(VERSION).satisfies(requirement) && !VERSION.equals("DEV")) {
				throw new Exception("Addon " + addon.getName() + " requires installer version " + requirement);
			}
		}
		Patcher.patch(installDir, ts6version);
		try {
			String addonRoot = addonJsonPath.substring(0, addonJsonPath.length() - ADDON_DEFINITION_FILE_NAME.length());
			String injectionString = addonSource.read(addonRoot + addon.getInject());
			File indexPath = new File(installDir, resolveTeamSpeakResources(OS.getOrThrow()) + INDEX);
			String index = IOUtils.readFile(indexPath);
			List<InstalledAddon> installedAddons = findInstalledAddons(index);
			Collections.reverse(installedAddons);
			Iterator<InstalledAddon> installedAddonsIterator = installedAddons.iterator();
			while(installedAddonsIterator.hasNext()) {
				InstalledAddon installedAddon = installedAddonsIterator.next();
				if(installedAddon.getId().equals(addon.getId())) {
					int comparison = installedAddon.getVersion().compareTo(addon.getVersion());
					if(!conflictHandler.resolve(addon, installedAddon, comparison)) {
						return Optional.empty();
					}
					index = removeAddonFromIndex(index, installedAddon);
					installedAddonsIterator.remove();
				}
			}
			String packedAddon = Packer.pack(addon, injectionString, addonSource, addonRoot);
			String wrappedAddon = wrapAddonInject(packedAddon, addon, UUID.randomUUID());
			index = addon.getInjectAt().inject(index, wrappedAddon, addon.getInjectionPoint(), installedAddons);
			IOUtils.writeFile(indexPath, index);
			return Optional.of(addon);
		} catch(Exception e) {
			throw new Exception("Error during installation (" + e.getMessage() + ")", e);
		}
	}
	
	public static void uninstall(InstalledAddon addon, String installDir) throws Exception {
		File indexPath = new File(installDir, resolveTeamSpeakResources(OS.getOrThrow()) + INDEX);
		String index = IOUtils.readFile(indexPath);
		index = removeAddonFromIndex(index, addon);
		IOUtils.writeFile(indexPath, index);
	}
	
	private static String resolveTeamSpeakResources(OS os) {
		if(MAC_OS.equals(os)) {
			return "/Contents/Resources/" + CLIENT_UI_PATH;
		}
		return CLIENT_UI_PATH;
	}
	
	private static String findAddonJsonPath(IAddonSource addonSource) throws IOException {
		Iterator<String> entries = addonSource.entries();
		while(entries.hasNext()) {
			String entry = entries.next();
			if(entry.endsWith(ADDON_DEFINITION_FILE_NAME)) {
				return entry;
			}
		}
		throw new IOException("Could not find addon.json");
	}
	
	public static void validateInstallationPath(String path, boolean checkWritePermission) throws Exception {
		File installDir = new File(path);
		if(!installDir.exists() || !installDir.isDirectory()) {
			throw new IOException("The installation directory does not exist");
		}
		File resourcesDir = new File(installDir, resolveTeamSpeakResources(OS.getOrThrow()));
		if(!resourcesDir.exists() || !resourcesDir.isDirectory()) {
			throw new IOException("Could not detect a valid TeamSpeak 6 installation in \"" + installDir + "\"");
		}
		if(checkWritePermission && !Files.isWritable(installDir.toPath())) {
			throw new IOException("Unable to modify current installation:\nMissing write permissions for \"" + installDir + "\"");
		}
	}
	
	private static String wrapAddonInject(String injectionString, Addon addon, UUID installId) {
		StringBuilder builder = new StringBuilder("<!-- ADDON_START v");
		builder.append(SCHEMA_VERSION);
		builder.append(" ");
		builder.append(addon.getId());
		builder.append(" ");
		builder.append(addon.getVersion());
		builder.append(" \"");
		builder.append(Base64.getEncoder().encodeToString(addon.getName().getBytes()));
		builder.append("\" ");
		builder.append(installId);
		builder.append(" -->");
		builder.append(injectionString);
		builder.append(addonEndStringV2(installId));
		return builder.toString();
	}
	
	private static String addonEndStringV2(UUID installId) {
		return "<!-- ADDON_END " + installId + " -->";
	}
	
	public static List<InstalledAddon> installedAddons(String installDir) throws Exception {
		File indexPath = new File(installDir, resolveTeamSpeakResources(OS.getOrThrow()) + INDEX);
		String index = IOUtils.readFile(indexPath);
		return findInstalledAddons(index);
	}
	
	private static List<InstalledAddon> findInstalledAddons(String index) {
		List<InstalledAddon> addons = new ArrayList<InstalledAddon>();
		Matcher matcher = ADDON_START_REGEX.matcher(index);
		while(matcher.find()) {
			try {
				int schemaVersion = Integer.parseInt(matcher.group(1));
				if(schemaVersion == 1) {
					String id = matcher.group(2);
					Semver version = new Semver(matcher.group(3));
					String name = matcher.group(4);
					int startIndex = matcher.start();
					int endIndex = index.indexOf(ADDON_END_STRING_V1, startIndex) + ADDON_END_STRING_V1.length();
					addons.add(new InstalledAddon(id, name, version, startIndex, endIndex));
				} else if(schemaVersion == 2) {
					String id = matcher.group(2);
					Semver version = new Semver(matcher.group(3));
					String name = new String(Base64.getDecoder().decode(matcher.group(4)));
					int startIndex = matcher.start();
					String addonEndString = addonEndStringV2(UUID.fromString(matcher.group(5)));
					int endIndex = index.indexOf(addonEndString, startIndex) + addonEndString.length();
					addons.add(new InstalledAddon(id, name, version, startIndex, endIndex));
				}
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return addons;
	}
	
	public static List<Entry<Semver, String>> loadVersions(URL versionUrl) throws IOException {
		Map<Semver, String> versionMap = OBJECT_MAPPER.readValue(versionUrl.openStream(), new TypeReference<Map<Semver, String>>() {});
		List<Entry<Semver, String>> versions = new ArrayList<Entry<Semver, String>>(versionMap.entrySet());
		versions.sort(Entry.comparingByKey(Comparator.reverseOrder()));
		return versions;
	}
	
	private static String removeAddonFromIndex(String index, InstalledAddon installedAddon) {
		return index.substring(0, installedAddon.getStartIndex()) + index.substring(installedAddon.getEndIndex());
	}
	
	public interface ConflictHandler {
		boolean resolve(Addon addon, InstalledAddon installedAddon, int compareResult);
	}
}
