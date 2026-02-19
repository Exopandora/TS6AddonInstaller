package com.github.exopandora.ts6ai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.exopandora.ts6ai.util.Lazy;
import com.github.exopandora.ts6ai.util.OS;
import com.github.exopandora.ts6ai.util.Util;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.github.exopandora.ts6ai.util.Util.OBJECT_MAPPER;

public class VersionIndex {
	private final Lazy<Map<String, String>> windows;
	private final Lazy<Map<String, String>> linux;
	private final Lazy<Map<String, String>> macos;
	
	public VersionIndex(
		@JsonProperty("windows") Map<String, Set<String>> windows,
		@JsonProperty("linux") Map<String, Set<String>> linux,
		@JsonProperty("macos") Map<String, Set<String>> macos
	) {
		this.windows = new Lazy<Map<String, String>>(() -> createLookup(windows));
		this.linux = new Lazy<Map<String, String>>(() -> createLookup(linux));
		this.macos = new Lazy<Map<String, String>>(() -> createLookup(macos));
	}
	
	@JsonIgnore
	public Optional<String> getVersion(OS os, String md5) {
		String result = null;
		switch(os) {
			case WINDOWS:
				result = this.windows.get().get(md5);
				break;
			case LINUX:
				result = this.linux.get().get(md5);
				break;
			case MAC_OS:
				result = this.macos.get().get(md5);
				break;
		}
		return Optional.ofNullable(result);
	}
	
	private static Map<String, String> createLookup(Map<String, Set<String>> versionToHashes) {
		return versionToHashes.entrySet().stream()
			.flatMap(entry -> entry.getValue().stream().map(hash -> new SimpleEntry<String, String>(hash, entry.getKey())))
			.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
	}
	
	public static Semver findTeamSpeakVersion(String installDir, OS os) throws IOException, IllegalStateException {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("versions.json");
		VersionIndex index = OBJECT_MAPPER.readValue(inputStream, VersionIndex.class);
		String md5 = Util.md5sum(new File(installDir, resolveVersionDiscriminatorFile(os)));
		return index.getVersion(os, md5)
			.map(version -> new Semver(version, SemverType.NPM))
			.orElseThrow(() -> new IllegalStateException("Unsupported TeamSpeak version.\nTry updating the installer."));
	}
	
	public static String resolveVersionDiscriminatorFile(OS os) {
		switch(os) {
			case WINDOWS:
			case LINUX:
				return "html/client_ui/main.js";
			case MAC_OS:
				return "Contents/Resources/html/client_ui/main.js";
		}
		throw new IllegalArgumentException("Unknown operating system " + os);
	}
}
