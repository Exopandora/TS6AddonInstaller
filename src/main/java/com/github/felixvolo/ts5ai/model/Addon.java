package com.github.felixvolo.ts5ai.model;

import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.felixvolo.ts5ai.util.Util;
import com.vdurmont.semver4j.Requirement;
import com.vdurmont.semver4j.Semver;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Addon {
	private final String name;
	private final String id;
	private final Semver version;
	private final String inject;
	private final InjectionPoint injectionPoint;
	private final InjectAt injectAt;
	private final String sources;
	private final Optional<Requirement> installerVersion;
	private final Optional<Requirement> teamSpeakVersion;
	
	public Addon(@JsonProperty("name") String name, @JsonProperty("id") String id, @JsonProperty("version") Semver version, @JsonProperty("inject") String inject, @JsonProperty("injection_point") InjectionPoint injectionPoint, @JsonProperty("inject_at") InjectAt injectAt, @JsonProperty("sources") String sources, @JsonProperty("installer") String installerVersion, @JsonProperty("teamspeak") String teamSpeakVersion) throws Exception {
		this.name = Objects.requireNonNull(name, "Missing addon name");
		this.id = Util.validate(Objects.requireNonNull(id, "Missing addon id"), "[A-Za-z_0-9]+");
		this.version = Objects.requireNonNull(version, "Missing addon version");
		this.inject = Objects.requireNonNull(inject, "Missing addon patches");
		this.injectionPoint = injectionPoint != null ? injectionPoint : InjectionPoint.HEAD;
		this.injectAt = injectAt != null ? injectAt : InjectAt.TAIL;
		this.sources = Objects.requireNonNull(sources, "Missing addon sources");
		this.installerVersion = Optional.ofNullable(installerVersion).map(Requirement::buildNPM);
		this.teamSpeakVersion = Optional.ofNullable(teamSpeakVersion).map(Requirement::buildNPM);
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
	
	public Optional<Requirement> getInstallerVersion() {
		return this.installerVersion;
	}
	
	public Optional<Requirement> getTeamSpeakVersion() {
		return this.teamSpeakVersion;
	}
}