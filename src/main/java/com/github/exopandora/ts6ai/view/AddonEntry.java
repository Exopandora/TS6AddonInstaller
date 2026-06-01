package com.github.exopandora.ts6ai.view;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URL;

public class AddonEntry {
	private final String name;
	
	public AddonEntry(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	
	public static class RemoteAddonEntry extends AddonEntry {
		private final URL versionIndex;
		private final URL website;
		
		public RemoteAddonEntry(
			@JsonProperty("name") String name,
			@JsonProperty("version_index") URL versionIndex,
			@JsonProperty("website") URL website
		) {
			super(name);
			this.versionIndex = versionIndex;
			this.website = website;
		}
		
		public URL getVersionIndex() {
			return this.versionIndex;
		}
		
		public URL getWebsite() {
			return this.website;
		}
	}
}
