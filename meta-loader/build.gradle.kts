import java.util.Locale

plugins {
    alias(libs.plugins.agp.app)
}

android {
    defaultConfig {
        multiDexEnabled = false
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles("proguard-rules.pro")
        }
    }
    namespace = "org.lsposed.lspatch.metaloader"
}

// AGP 8.7 在 release 构建时会运行 optimizeReleaseRes，但本模块没有 res 资源，
// aapt2 找不到 resources-release-optimize.ap_ 而失败，这里禁用该任务。
tasks.matching { it.name == "optimizeReleaseRes" }.configureEach {
    enabled = false
}

androidComponents.onVariants { variant ->
    val variantCapped = variant.name.replaceFirstChar { it.uppercase() }
    val variantLowered = variant.name.lowercase()

    task<Copy>("copyDex$variantCapped") {
        dependsOn("assemble$variantCapped")
        val dexOutPath = if (variant.buildType == "release")
            "$buildDir/intermediates/dex/$variantLowered/minify${variantCapped}WithR8" else
            "$buildDir/intermediates/dex/$variantLowered/mergeDex$variantCapped"
        from(dexOutPath)
        rename("classes.dex", "metaloader.dex")
        into("${rootProject.projectDir}/out/assets/${variant.name}/lspatch")
    }

    task("copy$variantCapped") {
        dependsOn("copyDex$variantCapped")

        doLast {
            println("Loader dex has been copied to ${rootProject.projectDir}${File.separator}out")
        }
    }
}

dependencies {
    compileOnly(projects.hiddenapi.stubs)
    implementation(projects.share.java)
    implementation(libs.hiddenapibypass)
}
