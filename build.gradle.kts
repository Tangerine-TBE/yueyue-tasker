import org.jetbrains.kotlin.gradle.dsl.copyFreeCompilerArgsToArgs

initVersions(file("project-versions.json"))

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlin_version = "1.6.21"

    extra.apply {
        set("kotlin_version", kotlin_version)
    }

    repositories {
        mavenLocal()
        //首选国外镜像加快github CI
        google()
        mavenCentral()
        maven("https://www.jitpack.io")
        maven("https://120.25.164.233:8081/nexus/content/groups/public/")
        maven("https://maven.aliyun.com/repository/central")
        google { url = uri("https://maven.aliyun.com/repository/google") }
        mavenCentral { url = uri("https://maven.aliyun.com/repository/public")
        }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath(kotlin("gradle-plugin", version = "$kotlin_version"))
        classpath("org.codehaus.groovy:groovy-json:3.0.8")
        classpath("org.greenrobot:greendao-gradle-plugin:3.3.0")  // add plugin
    }
}

allprojects {
    repositories {
        mavenLocal()
        //首选国外镜像加快github CI
        google()
        mavenCentral()
        maven("https://www.jitpack.io")
        maven("https://120.25.164.233:8081/nexus/content/groups/public/")
        maven("https://maven.aliyun.com/repository/central")
        google { url = uri("https://maven.aliyun.com/repository/google") }
        mavenCentral { url = uri("https://maven.aliyun.com/repository/public") }
    }
}

tasks.register<Delete>("clean").configure {
    delete(rootProject.buildDir)
}
