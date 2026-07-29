import java.util.Locale

plugins {
    alias(libs.plugins.agp.app)
}

android {
    defaultConfig {
        multiDexEnabled = false
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    externalNativeBuild {
        cmake {
            path("src/main/jni/CMakeLists.txt")
        }
    }
    namespace = "org.lsposed.lspatch.loader"
}

// AGP 8.7 在 release 构建时会运行 optimizeReleaseRes，但本模块没有 res 资源，
// aapt2 找不到 resources-release-optimize.ap_ 而失败，这里禁用该任务。
tasks.matching { it.name == "optimizeReleaseRes" }.configureEach {
    enabled = false
}

androidComponents.onVariants { variant ->
    val variantCapped = variant.name.replaceFirstChar { it.uppercase() }

    task<Copy>("copyDex$variantCapped") {
        dependsOn("assemble$variantCapped")
        from("$buildDir/intermediates/dex/${variant.name}/mergeDex$variantCapped/classes.dex")
        rename("classes.dex", "loader.dex")
        into("${rootProject.projectDir}/out/assets/${variant.name}/lspatch")
    }

    task<Copy>("copySo$variantCapped") {
        dependsOn("assemble$variantCapped")
        // AGP 8.7 的中间产物路径在 stripped_native_libs/<variant>/ 下增加了任务名子目录
        // strip<Variant>DebugSymbols，这里适配新路径结构。
        val stripTaskName = "strip${variantCapped}DebugSymbols"
        from(
            fileTree(
                "dir" to "$buildDir/intermediates/stripped_native_libs/${variant.name}/$stripTaskName/out/lib",
                "include" to listOf("**/liblspatch.so")
            )
        )
        into("${rootProject.projectDir}/out/assets/${variant.name}/lspatch/so")
    }

    task("copy$variantCapped") {
        dependsOn("copySo$variantCapped")
        dependsOn("copyDex$variantCapped")

        doLast {
            println("Dex and so files has been copied to ${rootProject.projectDir}${File.separator}out")
        }
    }
}

dependencies {
    compileOnly(projects.hiddenapi.stubs)
    implementation(projects.core)
    implementation(projects.hiddenapi.bridge)
    implementation(projects.services.daemonService)
    implementation(projects.share.android)
    implementation(projects.share.java)

    implementation(libs.gson)
}
