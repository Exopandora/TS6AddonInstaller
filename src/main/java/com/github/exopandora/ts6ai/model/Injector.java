package com.github.exopandora.ts6ai.model;

import java.util.List;

public interface Injector {
	String inject(String targetString, String injectionString, InjectionPoint injectionPoint, List<InstalledAddon> installedAddons);
}
