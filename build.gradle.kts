import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	id("application")
	id("eclipse")
	id("idea")
	id("edu.sc.seis.launch4j") version "4.0.0"
	id("com.gradleup.shadow") version "9.3.1"
}

version = "3.4.0"
group = "com.github.exopandora"

repositories {
	mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
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
	implementation("commons-cli:commons-cli:1.11.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")
    implementation("tools.jackson.core:jackson-core:3.0.4")
    implementation("tools.jackson.core:jackson-databind:3.0.4")
	implementation("org.semver4j:semver4j:6.0.0")
	implementation("org.dom4j:dom4j:2.2.0")
    
    testImplementation("org.junit.jupiter:junit-jupiter:6.0.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:6.0.3")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaExec> {
    args("--dev")
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

launch4j {
    mainClassName = main
	setJarTask(tasks.named<ShadowJar>("shadowJar").get())
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

dependencyAnalysis {
    issues {
        all {
            onAny {
                severity("fail")
            }
        }
    }
}
