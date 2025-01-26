package com.github.exopandora.ts6ai.model;

import static com.github.exopandora.ts6ai.util.Util.OBJECT_MAPPER;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.exopandora.ts6ai.util.OS;
import com.github.exopandora.ts6ai.util.Util;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.Semver.SemverType;

public class VersionIndex {
	private final Map<String, String> windows;
	private final Map<String, String> linux;
	private final Map<String, String> macos;
	
	public VersionIndex(
		@JsonProperty("windows") Map<String, String> windows,
		@JsonProperty("linux") Map<String, String> linux,
		@JsonProperty("macos") Map<String, String> macos
	) {
		this.windows = windows;
		this.linux = linux;
		this.macos = macos;
	}
	
	@JsonIgnore
	public Optional<String> getVersion(OS os, String md5) {
		String result = null;
		switch(os) {
			case WINDOWS:
				result = this.windows.get(md5);
				break;
			case LINUX:
				result = this.linux.get(md5);
				break;
			case MAC_OS:
				result = this.macos.get(md5);
				break;
		}
		return Optional.ofNullable(result);
	}
	
	public static Semver findTeamSpeakVersion(String installDir, OS os) throws IOException, IllegalStateException {
		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("versions.json");
		VersionIndex index = OBJECT_MAPPER.readValue(inputStream, VersionIndex.class);
		String md5 = Util.md5sum(new File(installDir, resolveTeamSpeakExecutable(os)));
		return index.getVersion(os, md5)
			.map(version -> new Semver(version, SemverType.NPM))
			.orElseThrow(() -> new IllegalStateException("Unsupported TeamSpeak version.\nTry updating the installer."));
	}
	
	public static String resolveTeamSpeakExecutable(OS os) {
		switch(os) {
			case WINDOWS:
				return "TeamSpeak.exe";
			case LINUX:
				return "TeamSpeak";
			case MAC_OS:
				return "Contents/MacOS/TeamSpeak";
		}
		throw new IllegalArgumentException("Unknown operating system " + os);
	}
}