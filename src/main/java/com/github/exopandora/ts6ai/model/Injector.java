package com.github.exopandora.ts6ai.model;

public interface Injector {
	String inject(String targetString, String injectionString, InjectionPoint injectionPoint);
}
