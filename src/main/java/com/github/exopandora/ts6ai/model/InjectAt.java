package com.github.exopandora.ts6ai.model;

public enum InjectAt {
	HEAD((targetString, injectionString, injectionPoint) -> {
		return targetString.replace("<" + injectionPoint.getTagName() + ">", "<" + injectionPoint.getTagName() + ">" + injectionString);
	}),
	TAIL((targetString, injectionString, injectionPoint) -> {
		return targetString.replace("</" + injectionPoint.getTagName() + ">", injectionString + "</" + injectionPoint.getTagName() + ">");
	});
	
	private Injector injector;
	
	private InjectAt(Injector injector) {
		this.injector = injector;
	}
	
	public String inject(String targetString, String injectionString, InjectionPoint injectionPoint) {
		return this.injector.inject(targetString, injectionString, injectionPoint);
	}
}
