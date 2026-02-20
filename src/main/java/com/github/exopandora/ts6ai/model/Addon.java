package com.github.exopandora.ts6ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.exopandora.ts6ai.util.Util;
import org.semver4j.Semver;
import org.semver4j.range.RangeList;
import org.semver4j.range.RangeListFactory;

import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Addon {
	private final String name;
	private final String id;
	private final Semver version;
	private final String inject;
	private final InjectionPoint injectionPoint;
	private final InjectAt injectAt;
	private final String sources;
	private final Optional<RangeList> installerVersion;
	private final Optional<RangeList> teamSpeakVersion;
	
	public Addon(
		@JsonProperty("name") String name,
		@JsonProperty("id") String id,
		@JsonProperty("version") Semver version,
		@JsonProperty("inject") String inject,
		@JsonProperty("injection_point") InjectionPoint injectionPoint,
		@JsonProperty("inject_at") InjectAt injectAt,
		@JsonProperty("sources") String sources,
		@JsonProperty("installer") String installerVersion,
		@JsonProperty("teamspeak") String teamSpeakVersion
	) throws Exception {
		this.name = Objects.requireNonNull(name, "Missing addon name");
		this.id = Util.validate(Objects.requireNonNull(id, "Missing addon id"), "[A-Za-z_0-9]+");
		this.version = Objects.requireNonNull(version, "Missing addon version");
		this.inject = Objects.requireNonNull(inject, "Missing addon patches");
		this.injectionPoint = injectionPoint != null ? injectionPoint : InjectionPoint.HEAD;
		this.injectAt = injectAt != null ? injectAt : InjectAt.TAIL;
		this.sources = Objects.requireNonNull(sources, "Missing addon sources");
		this.installerVersion = Optional.ofNullable(installerVersion).map(Addon::parseRangeList);
		this.teamSpeakVersion = Optional.ofNullable(teamSpeakVersion).map(Addon::parseRangeList);
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getId() {
		return this.id;
	}
	
	public Semver getVersion() {
		return this.version;
	}
	
	public String getInject() {
		return this.inject;
	}
	
	public InjectionPoint getInjectionPoint() {
		return this.injectionPoint;
	}
	
	public InjectAt getInjectAt() {
		return this.injectAt;
	}
	
	public String getSources() {
		return this.sources;
	}
	
	public Optional<RangeList> getInstallerVersion() {
		return this.installerVersion;
	}
	
	public Optional<RangeList> getTeamSpeakVersion() {
		return this.teamSpeakVersion;
	}
	
	private static RangeList parseRangeList(String range) {
		return RangeListFactory.create(range.replaceAll("\\|+", "||"));
	}
}
