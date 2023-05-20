plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-kapt")
    id("kotlin-android-extensions")
    id("org.greenrobot.greendao")
}
val AAVersion = "4.5.2"
android {
    namespace = "cn.com.auto.thkl"
    compileSdk = versions.compile
    buildToolsVersion = versions.buildTool

    defaultConfig {
        minSdk = versions.mini
        targetSdk = versions.target
        versionCode = versions.appVersionCode
        versionName = versions.appVersionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["resourcePackageName"] = applicationId.toString()
                arguments["androidManifestFile"] = "$projectDir/src/main/AndroidManifest.xml"
            }
        }
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "armeabi-v7a"))
        }
    }
    lint {
        abortOnError = false
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0-rc01"
        kotlinCompilerVersion = "1.6.20"
    }
    splits {

        // Configures multiple APKs based on ABI.
        abi {

            // Enables building multiple APKs per ABI.
            isEnable = true

            // By default all ABIs are included, so use reset() and include to specify that we only
            // want APKs for x86 and x86_64.

            // Resets the list of ABIs that Gradle should create APKs for to none.
            reset()

            // Specifies a list of ABIs that Gradle should create APKs for.
            include("armeabi-v7a", "arm64-v8a")

            // Specifies that we do not want to also generate a universal APK that includes all ABIs.
            isUniversalApk = true
        }
    }
    signingConfigs {
        create("release") {
            keyPassword = "100344"
            keyAlias = "thkl"
            storeFile = File("./thkl.jks")
            storePassword = "100344"
        }

    }
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders.putAll(mapOf("appName" to versions.name))
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            manifestPlaceholders.putAll(mapOf("appName" to versions.name))
        }
    }

    flavorDimensions.add("channel")
    productFlavors {
        create("huawei_android_10_") {
            buildConfigField("String", "SYSTEM_VALUE", "\"HuaWeiAndroid10\"")
        }
        create("huawei_android_9_") {
            buildConfigField("String", "SYSTEM_VALUE", "\"HuaWeiAndroid9\"")
        }
    }
    android.applicationVariants.all {
        outputs.all {
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                if (this.name.contains("arm64-v8a")) {
                    this.outputFileName =
                        "yueyue_V${versionName}_v8a.apk"
                } else if (this.name.contains("armeabi-v7a")) {
                    this.outputFileName =
                        "yueyue_V${versionName}_v7a.apk"
                } else {
                    this.outputFileName =
                        "yueyue_V${versionName}_null.apk"
                }
            }
        }
    }
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("/libs")
        }
    }

    configurations.all {
        resolutionStrategy.force("com.google.code.findbugs:jsr305:3.0.1")
        exclude(group = "org.jetbrains", module = "annotations-java5")
//        exclude(group = "com.atlassian.commonmark",) module = "commonmark"
        exclude(group = "com.github.atlassian.commonmark-java", module = "commonmark")
    }
    packagingOptions {
        //ktor netty implementation("io.ktor:ktor-server-netty:2.0.1")
        resources.pickFirsts.addAll(
            listOf(
                "META-INF/io.netty.versions.properties",
                "META-INF/INDEX.LIST"
            )
        )
    }
    greendao {
        this.schemaVersion = 3
        this.daoPackage = "cn.com.auto.thkl.db"
    }


}

dependencies {
    implementation("org.greenrobot:greendao:3.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation(project(mapOf("path" to ":easyfloat")))
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    val accompanist_version = "0.24.13-rc"
    implementation("com.google.accompanist:accompanist-permissions:0.24.13-rc")
    implementation("com.google.accompanist:accompanist-pager-indicators:$accompanist_version")
    implementation("com.google.accompanist:accompanist-pager:$accompanist_version")
    implementation("com.google.accompanist:accompanist-swiperefresh:$accompanist_version")
    implementation("com.google.accompanist:accompanist-appcompat-theme:$accompanist_version")
    implementation("com.google.accompanist:accompanist-insets:$accompanist_version")
    implementation("com.google.accompanist:accompanist-insets-ui:$accompanist_version")
    implementation("com.google.accompanist:accompanist-systemuicontroller:$accompanist_version")
    implementation("com.google.accompanist:accompanist-webview:$accompanist_version")
//    implementation (files("libs/bugly-4.1.9.2.aar"))
    implementation("org.chromium.net:cronet-embedded:76.3809.111")
    implementation("androidx.preference:preference-ktx:1.2.0")
    implementation("androidx.appcompat:appcompat:1.4.2") //
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.7.0-alpha03")
    // Personal libraries
    implementation("com.github.hyb1996:MutableTheme:1.0.0")
    // Material Dialogs
    implementation("com.afollestad.material-dialogs:core:0.9.2.3") {
        exclude(group = "com.android.support")
    }
    // Common Markdown
    implementation("com.github.atlassian:commonmark-java:commonmark-parent-0.9.0")
    // Android issue reporter (a github issue reporter)
    implementation("com.heinrichreimersoftware:android-issue-reporter:1.3.1") {
        exclude(group = "com.afollestad.material-dialogs")
        exclude(group = "com.android.support")
    }
    //MultiLevelListView
    implementation("com.github.hyb1996:android-multi-level-listview:1.1")
    //Licenses Dialog
    implementation("de.psdev.licensesdialog:licensesdialog:1.9.0")
    //Expandable RecyclerView
    implementation("com.bignerdranch.android:expandablerecyclerview:3.0.0-RC1")
    //FlexibleDivider
    implementation("com.yqritc:recyclerview-flexibledivider:1.4.0")
    //???
    implementation("com.wang.avi:library:2.1.3")

    // 证书签名相关
    implementation("com.madgag.spongycastle:bcpkix-jdk15on:1.56.0.0")
    //Expandable RecyclerView
    implementation("com.thoughtbot:expandablerecyclerview:1.3")
//    implementation("org.signal.autox:apkbuilder:1.0.3")
    // RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.jakewharton.retrofit:retrofit2-rxjava2-adapter:1.0.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")
    //Glide
    implementation("com.github.bumptech.glide:glide:4.8.0") {
        exclude(group = "com.android.support")
    }
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    //joda time
    implementation("net.danlew:android.joda:2.10.14")
    // Tasker Plugin
    implementation("com.twofortyfouram:android-plugin-client-sdk-for-locale:4.0.3")
    // Flurry
    implementation("com.flurry.android:analytics:13.1.0@aar")
    // tencent
    implementation("com.tencent.bugly:crashreport:4.0.0")
    api("com.tencent.tbs:tbssdk:44181")
    // MaterialDialogCommon
    implementation("com.afollestad.material-dialogs:commons:0.9.2.3") {
        exclude(group = "com.android.support")
    }
    // WorkManager
    implementation("androidx.work:work-runtime:2.7.1")
    // Android job
    implementation("com.evernote:android-job:1.4.2")
    // Optional, if you use support library fragments:
    implementation(project(":automator"))
    implementation(project(":common"))
    implementation(project(":autojs"))
    implementation("androidx.multidex:multidex:2.0.1")
    val lifecycle_version = "2.5.0-rc01"
    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version")
    // ViewModel utilities for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycle_version")
    // Lifecycles only (without ViewModel or LiveData)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version")
    // Saved state module for ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version")
    // Annotation processor
    kapt("androidx.lifecycle:lifecycle-compiler:$lifecycle_version")
    implementation("androidx.lifecycle:lifecycle-service:$lifecycle_version")
    implementation("androidx.savedstate:savedstate-ktx:1.2.0")
    implementation("androidx.savedstate:savedstate:1.2.0")

    val ktor_version = "2.0.3"
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")
    implementation("io.ktor:ktor-client-okhttp:$ktor_version")
    implementation("io.ktor:ktor-client-core:$ktor_version")

    //qr scan
    implementation("io.github.g00fy2.quickie:quickie-bundled:1.5.0")
    //Fab button with menu, please do not upgrade, download dependencies will be error after upgrade
    //noinspection GradleDependency
    implementation("com.leinardi.android:speed-dial.compose:1.0.0-alpha03")
    //TextView markdown
    implementation("io.noties.markwon:core:4.6.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0-beta01")
    implementation("io.coil-kt:coil-compose:2.0.0-rc03")
    implementation("com.squareup.okhttp3:logging-interceptor:3.4.1")
    implementation("com.gyf.immersionbar:immersionbar:2.3.2-beta05")
    implementation("com.alibaba:fastjson:1.2.8")
    implementation("com.blankj:utilcodex:1.31.1")
    implementation("me.jessyan:autosize:1.2.1")

}