package com.github.exopandora.ts6ai.view;

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
		
		public RemoteAddonEntry(String name, URL versionIndex) {
			super(name);
			this.versionIndex = versionIndex;
		}
		
		public URL getVersionIndex() {
			return this.versionIndex;
		}
	}
}
