package com.github.exopandora.ts6ai.model;

import com.github.exopandora.ts6ai.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public enum InjectAt implements Injector {
	HEAD {
		@Override
		public String inject(String targetString, String injectionString, InjectionPoint injectionPoint, List<InstalledAddon> installedAddons) {
			String tag = "<" + injectionPoint.getTagName() + ">";
			return InjectAt.inject(targetString, injectionString, tag, installedAddons, tag.length());
		}
	},
	TAIL {
		@Override
		public String inject(String targetString, String injectionString, InjectionPoint injectionPoint, List<InstalledAddon> installedAddons) {
			String tag = "</" + injectionPoint.getTagName() + ">";
			return InjectAt.inject(targetString, injectionString, tag, installedAddons, 0);
		}
	};
	
	private static String inject(String targetString, String injectionString, String anchor, List<InstalledAddon> installedAddons, int offset) {
		List<Integer> indexes = findInjectionIndexes(targetString, anchor, installedAddons);
		if(indexes.size() != 1) {
			throw new IllegalStateException("Could not find valid injection point");
		}
		return insert(targetString, injectionString, indexes, Util.clamp(offset, 0, anchor.length()));
	}
	
	private static List<Integer> findInjectionIndexes(String string, String part, List<InstalledAddon> installedAddons) {
		List<Integer> indexes = new LinkedList<Integer>();
		int index = string.indexOf(part);
		while(index != -1 && index + 1 < string.length()) {
			if(isOutsideAddonRegion(index, installedAddons)) {
				indexes.add(index);
			}
			index = string.indexOf(part, index + 1);
		}
		return indexes;
	}
	
	private static boolean isOutsideAddonRegion(int index, List<InstalledAddon> installedAddons) {
		for(InstalledAddon addon : installedAddons) {
			if(index >= addon.getStartIndex() && index < addon.getEndIndex()) {
				return false;
			}
		}
		return true;
	}
	
	private static String insert(String string, String snippet, List<Integer> indexes, int offset) {
		List<Integer> reversedIndexes = new ArrayList<Integer>(indexes);
		reversedIndexes.sort(Comparator.reverseOrder());
		String result = string;
		for(int index : reversedIndexes) {
			result = result.substring(0, index + offset) + snippet + result.substring(index + offset);
		}
		return result;
	}
}
