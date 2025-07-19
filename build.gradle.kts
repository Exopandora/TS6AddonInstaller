import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask

plugins {
	id("application")
	id("eclipse")
	id("idea")
	id("edu.sc.seis.launch4j") version "2.5.3"
	id("com.gradleup.shadow") version "8.3.0"
}

project.version = "3.0.1"

repositories {
	mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}

val main = "com.github.exopandora.ts6ai.TS6AddonInstaller"

application {
	mainClass = main
}

dependencies {
	implementation("commons-cli:commons-cli:1.9.0")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
	implementation("com.vdurmont:semver4j:3.1.0")
	implementation("org.dom4j:dom4j:2.1.5")
}

tasks.withType<Jar> {
	manifest {
		attributes(
			mapOf(
				"Main-Class" to main,
				"Implementation-Version" to project.version
			)
		)
	}
	
	from("LICENSE")
	
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<DefaultLaunch4jTask> {
	mainClassName = mainClassName
	jarTask = tasks.named("shadowJar").get()
	manifest = "$projectDir/${project.name}.manifest"
	copyright = "MIT License"
	companyName = "Exopandora"
	fileDescription = project.name
	productName = project.name
	internalName = project.name
	version = project.version.toString()
	textVersion = project.version.toString()
	copyConfigurable = emptyList<Any>()
}

tasks.withType<ShadowJar> {
	configurations = listOf(project.configurations.runtimeClasspath.get())
	from(sourceSets.main.get().output)
	from("LICENSE")
	exclude("META-INF/**")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<JavaCompile> {
	options.encoding = Charsets.UTF_8.name()
}

tasks.withType<AbstractArchiveTask> {
	isPreserveFileTimestamps = false
	isReproducibleFileOrder = true
}
