package com.github.exopandora.ts6ai.util;

import java.util.Optional;

public enum OS {
	WINDOWS("windows"),
	LINUX("linux"),
	MAC_OS("macos");

	public static final Optional<OS> OPERATING_SYSTEM = find();
	
	private final String name;
	
	private OS(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public static OS getOrThrow() throws Exception {
		return OPERATING_SYSTEM.orElseThrow(() -> new Exception("Unsupported operating system"));
	}
	
	private static Optional<OS> find() {
		String os = System.getProperty("os.name").toLowerCase();
		if(os.contains("win")) {
			return Optional.of(WINDOWS);
		} else if(os.contains("mac")) {
			return Optional.of(MAC_OS);
		} else if(os.contains("nux")) {
			return Optional.of(LINUX);
		}
		return Optional.empty();
	}
}