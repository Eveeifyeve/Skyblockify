import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import gg.essential.gradle.util.noServerRunConfigs

plugins {
    java
    kotlin("jvm") version "1.7.20"
    id("gg.essential.loom") version "0.10.0.+"     
		id("gg.essential.defaults") version "0.1.16"     
		id("com.github.johnrengelman.shadow") version "7.1.2" 
	}

repositories {
    mavenCentral()
    maven("https://repo.spongepowered.org/maven/")
}

@Suppress
dependencies {
    implementation("org.spongepowered:mixin:0.7.11-SNAPSHOT") 
    shadow("org.spongepowered:mixin:0.7.11-SNAPSHOT")     
		shadow("org.jetbrains.kotlin:kotlin-stdlib:1.7.20")
    shadow("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
}

group = "io.github.eveeifyeve.skyblockify"
version = "0.0.1+1.8.9"

loom {
    noServerRunConfigs()

    runs {
        getByName("client") {
            programArgs(
                "--tweakClass", "org.spongepowered.asm.launch.MixinTweaker",
                "--mixin", "mixins.json"
            )
        }
    }

   // forge {
   //     mixinConfigs("mixins.json")
   // }

    @Suppress("UnstableApiUsage")
    mixin {
        defaultRefmapName.set("mixins.refmap.json")
    }
}

sourceSets.main {
    output.setResourcesDir(file("$buildDir/classes/kotlin/main"))
}

// Java 8
java.toolchain.languageVersion.set(JavaLanguageVersion.of(8))
kotlin.jvmToolchain(jdkVersion = 8)
tasks.compileKotlin.get().kotlinOptions {
    jvmTarget = "1.8"
    languageVersion = "1.7"
}

tasks {
    processResources {
        inputs.property("MOD_VERSION", version)

        filesMatching("mcmod.info") {
            expand("MOD_VERSION" to project.version)
        }
    }

		// Shadow jar

    val relocateShadowJar by creating(ConfigureShadowRelocation::class) {
        target = shadowJar.get()
        prefix = "${project.group}.libs"
    }

    jar {
        enabled = false 
        manifest.attributes(
            "TweakClass" to "${relocateShadowJar.prefix}.org.spongepowered.asm.launch.MixinTweaker",
        )
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        mergeServiceFiles() 
        archiveClassifier.set("mapped")
        destinationDirectory.set(temporaryDir)

        dependsOn(relocateShadowJar)
        finalizedBy(remapJar)
    }

    remapJar {
        input.set(shadowJar.get().archiveFile)
		}

}



