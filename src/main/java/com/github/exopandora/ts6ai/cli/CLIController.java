package com.github.exopandora.ts6ai.cli;

import com.github.exopandora.ts6ai.model.Addon;
import com.github.exopandora.ts6ai.model.FolderAddonSource;
import com.github.exopandora.ts6ai.model.IAddonSource;
import com.github.exopandora.ts6ai.model.InstalledAddon;
import com.github.exopandora.ts6ai.model.Installer;
import com.github.exopandora.ts6ai.model.LocalZipAddonSource;
import com.github.exopandora.ts6ai.model.Patcher;
import com.github.exopandora.ts6ai.model.RemoteZipAddonSource;
import com.github.exopandora.ts6ai.model.Signer;
import com.github.exopandora.ts6ai.model.VersionIndex;
import com.github.exopandora.ts6ai.util.OS;
import org.semver4j.Semver;

import java.io.File;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class CLIController {
	public static void install(String installDir, String addonPath, boolean force, boolean devMode) {
		try(Scanner scanner = new Scanner(System.in)) {
			Installer.validateInstallationPath(installDir, true);
			try(IAddonSource addonSource = parseAddonSource(addonPath)) {
				Installer.install(addonSource, installDir, devMode, (addon, installedAddon, compareResult) -> {
					if(force) {
						return true;
					}
					System.out.println(conflictMessage(addon, installedAddon, compareResult) + " [y/n]: ");
					boolean answer = false;
					while(scanner.hasNext()) {
						String input = scanner.next();
						if(input.equalsIgnoreCase("y") || input.equalsIgnoreCase("yes")) {
							answer = true;
							break;
						} else if(input.equalsIgnoreCase("n") || input.equalsIgnoreCase("no")) {
							break;
						}
					}
					return answer;
				}).ifPresent(addon -> System.out.println(addon.getName() + " has successfully been installed!"));
			}
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	private static String conflictMessage(Addon addon, InstalledAddon installedAddon, int compareResult) {
		if(compareResult < 0) {
			return "An older version of " + addon.getName() + " is already installed. Do you want to update? (" + installedAddon.getVersion() + " -> " + addon.getVersion() + ")";
		} else if(compareResult > 0) {
			return "A newer version of " + addon.getName() + " is already installed. Do you want to downgrade? (" + installedAddon.getVersion() + " -> " + addon.getVersion() + ")";
		}
		return "The target version of " + addon.getName() + " is already installed. Do you want to install anyway?";
	}
	
	private static IAddonSource parseAddonSource(String addonPath) throws IllegalArgumentException {
		try {
			File file = new File(addonPath);
			if(file.exists()) {
				if(file.isDirectory() && file.canRead()) {
					return new FolderAddonSource(addonPath);
				} else if(file.isFile() && file.canRead()) {
					return new LocalZipAddonSource(addonPath);
				}
			} else {
				return new RemoteZipAddonSource(new URI(addonPath).toURL());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("Invalid addon source");
	}
	
	public static void patch(String installDir) {
		try {
			Installer.validateInstallationPath(installDir, true);
			Semver ts6version = VersionIndex.findTeamSpeakVersion(installDir, OS.getOrThrow());
			Signer.unsign(installDir);
			Patcher.patch(installDir, ts6version);
			Signer.sign(installDir);
		} catch(Exception e) {
			System.err.print(e.getMessage());
			if(e.getCause() != null) {
				System.out.println(": " + e.getCause().getMessage());
			} else {
				System.out.println();
			}
		}
	}
	
	public static void uninstall(String installDir, String addon) {
		try {
			Installer.validateInstallationPath(installDir, true);
			List<InstalledAddon> installedAddons = Installer.installedAddons(installDir);
			for(InstalledAddon installedAddon : installedAddons) {
				if(installedAddon.getId().equalsIgnoreCase(addon) || installedAddon.getName().equalsIgnoreCase(addon)) {
					Installer.uninstall(installedAddon, installDir);
					System.out.println(installedAddon.getName() + " has successfully been uninstalled!");
					break;
				}
			}
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public static void listInstalledAddons(String installDir) {
		try {
			Installer.validateInstallationPath(installDir, true);
			List<InstalledAddon> installedAddons = Installer.installedAddons(installDir);
			installedAddons.sort(Comparator.comparing(InstalledAddon::getName));
			for(InstalledAddon installedAddon : installedAddons) {
				System.out.println(installedAddon.getName() + " (" + installedAddon.getId() + ") " + installedAddon.getVersion());
			}
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}
	}
}
