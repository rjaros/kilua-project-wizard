package dev.kilua.wizard.generator

import com.intellij.openapi.vfs.VirtualFile
import dev.kilua.wizard.KiluaProjectType
import dev.kilua.wizard.data.model.VersionData
import dev.kilua.wizard.utils.TemplateAttributes
import dev.kilua.wizard.utils.build
import dev.kilua.wizard.utils.dir
import dev.kilua.wizard.utils.file
import dev.kilua.wizard.utils.packages

class ProjectTreeGenerator {
    private val ideaFiles = listOf("gradle.xml")
    private val gradleWrapperFiles = listOf("gradle-wrapper.jar", "gradle-wrapper.properties")
    private val gradleFiles = listOf(
        "libs.versions.toml",
    )
    private val rootFiles = listOf(
        "build.gradle.kts",
        "gradle.properties",
        "settings.gradle.kts",
        ".gitignore",
        "gradlew",
        "gradlew.bat",
        "README.md"
    )
    private val applicationFiles = listOf("build.gradle.kts")

    private val webpackFiles = listOf(
        "bootstrap.js",
        "css.js",
        "file.js",
        "proxy.js",
        "temporal.js",
    )
    private val webpackCustomFiles = listOf(
        "webpack.js"
    )
    private val webpackCustomTailwindFiles = listOf(
        "tailwind.js",
    )
    private val webSourceFiles = listOf(
        "main.kt"
    )
    private val webResourcesFiles = listOf(
        "index.html"
    )
    private val webResourcesI18nFiles = listOf(
        "messages-en.po",
        "messages-pl.po"
    )
    private val webResourcesTailwindcssFiles = listOf(
        "tailwind.config.js",
        "tailwind.twcss"
    )
    private val webTestFiles = listOf("AppSpec.kt")

    fun generate(
        projectType: KiluaProjectType,
        root: VirtualFile,
        artifactId: String,
        groupId: String,
        modules: List<String>,
        initializers: List<String>,
        versionData: VersionData,
        isKjsEnabled: Boolean,
        isKwasmEnabled: Boolean,
        isSsrEnabled: Boolean,
        isTestEnabled: Boolean,
    ) {
        try {
            val jvmEnabled = isSsrEnabled || projectType != KiluaProjectType.FRONTEND
            val srcFrontendDirPrefix = if (jvmEnabled) {
                if (isKjsEnabled && isKwasmEnabled) {
                    "web"
                } else if (isKjsEnabled) {
                    "js"
                } else if (isKwasmEnabled) {
                    "wasmJs"
                } else {
                    null
                }
            } else if (isKjsEnabled && isKwasmEnabled) {
                "common"
            } else if (isKjsEnabled) {
                "js"
            } else if (isKwasmEnabled) {
                "wasmJs"
            } else {
                null
            }
            val packageSegments = groupId
                .split(".")
                .toMutableList()
                .apply { add(artifactId) }
                .toList()
            val attrs = generateAttributes(
                projectType,
                artifactId,
                groupId,
                modules,
                initializers,
                versionData,
                isKjsEnabled,
                isKwasmEnabled,
                isSsrEnabled,
                isTestEnabled,
                srcFrontendDirPrefix,
            )
            root.build {
                dir(".idea") {
                    ideaFiles.forEach { fileName -> file(fileName, "idea_${fileName}", attrs) }
                }
                dir("gradle") {
                    dir("wrapper") {
                        gradleWrapperFiles.forEach { fileName ->
                            file(
                                fileName,
                                "wrapper_$fileName",
                                attrs,
                                binary = true
                            )
                        }
                    }
                    gradleFiles.forEach { fileName ->
                        file(
                            fileName,
                            "gradle_$fileName",
                            attrs
                        )
                    }
                }
                if (projectType == KiluaProjectType.SPRING_BOOT) {
                    dir("application") {
                        applicationFiles.forEach { fileName ->
                            file(
                                fileName,
                                "application_$fileName",
                                attrs
                            )
                        }
                    }
                }
                rootFiles.forEach { fileName ->
                    file(
                        fileName,
                        "root_$fileName",
                        attrs,
                        binary = (fileName == "gradlew" || fileName == "gradlew.bat"),
                        executable = (fileName == "gradlew")
                    )
                }
                if (isSsrEnabled) {
                    file("kilua.yml", "root_kilua.yml", attrs)
                }
                dir("webpack.config.d") {
                    webpackFiles.forEach { fileName -> file(fileName, "webpack_${fileName}", attrs) }
                    webpackCustomFiles.forEach { fileName ->
                        file(fileName, "webpack_${fileName}", attrs)
                    }
                    if (modules.contains("kilua-tailwindcss")) {
                        webpackCustomTailwindFiles.forEach { fileName ->
                            file(fileName, "webpack_${fileName}", attrs)
                        }
                    }
                }
                if (isSsrEnabled) {
                    dir("webpack.config.ssr.d") {
                        webpackFiles.forEach { fileName -> file(fileName, "webpack_${fileName}", attrs) }
                        webpackCustomFiles.forEach { fileName ->
                            file(fileName, "webpack_ssr_${fileName}", attrs)
                        }
                        if (modules.contains("kilua-tailwindcss")) {
                            webpackCustomTailwindFiles.forEach { fileName ->
                                file(fileName, "webpack_ssr_${fileName}", attrs)
                            }
                        }
                    }
                }
                dir("src") {
                    if (srcFrontendDirPrefix != null) {
                        dir("${srcFrontendDirPrefix}Main") {
                            dir("kotlin") {
                                packages(packageSegments) {
                                    webSourceFiles.forEach { fileName ->
                                        file(
                                            fileName,
                                            "web_source_$fileName",
                                            attrs
                                        )
                                    }
                                }
                            }
                            dir("resources") {
                                webResourcesFiles.forEach { fileName ->
                                    file(fileName, "web_resources_$fileName", attrs)
                                }
                                if (modules.contains("kilua-i18n")) {
                                    dir("modules") {
                                        dir("i18n") {
                                            webResourcesI18nFiles.forEach { fileName ->
                                                file(
                                                    fileName,
                                                    "web_resources_$fileName",
                                                    attrs
                                                )
                                            }
                                        }
                                    }
                                }
                                if (modules.contains("kilua-tailwindcss")) {
                                    dir("tailwind") {
                                        webResourcesTailwindcssFiles.forEach { fileName ->
                                            file(
                                                fileName,
                                                "web_resources_$fileName",
                                                attrs
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        if (isKjsEnabled && isKwasmEnabled) {
                            dir("jsMain") {
                                dir("kotlin") {
                                    packages(packageSegments) {
                                        file("main.js.kt", "web_source_main.js.kt", attrs)
                                    }
                                }
                            }
                            dir("wasmJsMain") {
                                dir("kotlin") {
                                    packages(packageSegments) {
                                        file("main.wasmJs.kt", "web_source_main.wasmJs.kt", attrs)
                                    }
                                }
                            }
                        }
                    }
                    if (isTestEnabled) {
                        dir("${srcFrontendDirPrefix}Test") {
                            dir("kotlin") {
                                packages(packageSegments) {
                                    webTestFiles.forEach { fileName ->
                                        file(
                                            fileName,
                                            "web_test_$fileName",
                                            attrs
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (projectType != KiluaProjectType.FRONTEND) {
                        dir("commonMain") {
                            dir("kotlin") {
                                packages(packageSegments) {
                                    file("Service.kt", "common_source_Service.kt", attrs)
                                }
                            }
                        }
                    }
                    if (jvmEnabled) {
                        dir("jvmMain") {
                            dir("kotlin") {
                                packages(packageSegments) {
                                    if (projectType != KiluaProjectType.FRONTEND) {
                                        file("Service.kt", "jvm_source_Service.kt", attrs)
                                    }
                                    when (projectType) {
                                        KiluaProjectType.JAVALIN -> {
                                            file("Main.kt", "jvm_javalin_source_Main.kt", attrs)
                                        }

                                        KiluaProjectType.JOOBY -> {
                                            file("Main.kt", "jvm_jooby_source_Main.kt", attrs)
                                        }

                                        KiluaProjectType.KTOR -> {
                                            file("Main.kt", "jvm_ktor_source_Main.kt", attrs)
                                        }

                                        KiluaProjectType.SPRING_BOOT -> {
                                            file("Main.kt", "jvm_spring_source_Main.kt", attrs)
                                        }

                                        KiluaProjectType.VERTX -> {
                                            file("Main.kt", "jvm_vertx_source_Main.kt", attrs)
                                        }

                                        KiluaProjectType.FRONTEND -> {
                                            file("Main.kt", "jvm_ssr_source_Main.kt", attrs)
                                        }
                                    }
                                }
                            }
                            dir("resources") {
                                file("logback.xml", "jvm_resources_logback.xml", attrs)
                                if (projectType == KiluaProjectType.JAVALIN) {
                                    if (isSsrEnabled) {
                                        file(
                                            "application.properties",
                                            "jvm_javalin_resources_application.properties",
                                            attrs
                                        )
                                    }
                                    dir("assets") {
                                        file(".placeholder", "jvm_resources_.placeholder", attrs)
                                    }
                                } else if (projectType == KiluaProjectType.JOOBY) {
                                    if (isSsrEnabled) {
                                        file("application.conf", "jvm_jooby_resources_application.conf", attrs)
                                    }
                                } else if (projectType == KiluaProjectType.KTOR || projectType == KiluaProjectType.FRONTEND) {
                                    file("application.conf", "jvm_ktor_resources_application.conf", attrs)
                                } else if (projectType == KiluaProjectType.SPRING_BOOT) {
                                    file("application.yml", "jvm_spring_resources_application.yml", attrs)
                                } else if (projectType == KiluaProjectType.VERTX) {
                                    if (isSsrEnabled) {
                                        file(
                                            "application.properties",
                                            "jvm_vertx_resources_application.properties",
                                            attrs
                                        )
                                    }
                                }
                            }
                        }
                        if (isTestEnabled) {
                            dir("jvmTest") {
                                dir("kotlin") {
                                    packages(packageSegments) {
                                        file("ServiceSpec.kt", "jvm_test_ServiceSpec.kt", attrs)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            root.refresh(false, true)
        } catch (ex: Exception) {
            ex.printStackTrace()
            println(ex)
        }
    }

    private fun generateAttributes(
        projectType: KiluaProjectType,
        artifactId: String,
        groupId: String,
        modules: List<String>,
        initializers: List<String>,
        versionData: VersionData,
        isKjsEnabled: Boolean,
        isKwasmEnabled: Boolean,
        isSsrEnabled: Boolean,
        isTestEnabled: Boolean,
        srcFrontendDirPrefix: String?
    ): Map<String, Any> {
        return mapOf(
            TemplateAttributes.ARTIFACT_ID to artifactId,
            TemplateAttributes.GROUP_ID to groupId,
            TemplateAttributes.PACKAGE_NAME to "${groupId}.${artifactId}",
            "type" to projectType.code,
            "kilua_version" to versionData.kilua,
            "kotlin_version" to versionData.kotlin,
            "compose_version" to versionData.compose,
            "coroutines_version" to versionData.coroutines,
            "ksp_version" to versionData.ksp,
            "kilua_rpc_version" to versionData.kiluaRpc,
            "logback_version" to versionData.logback,
            "gettext_version" to versionData.gettext,
            "datetime_version" to versionData.datetime,
            "jooby_version" to versionData.templateJooby.jooby,
            "ktor_version" to versionData.templateKtor.ktor,
            "micronaut_version" to versionData.templateMicronaut.micronaut,
            "micronaut_plugins_version" to versionData.templateMicronaut.micronautPlugins,
            "spring_boot_version" to versionData.templateSpring.springBoot,
            "selected_modules" to modules,
            "selected_modules_dependencies" to modules.map { it.replace("-", ".") },
            "selected_initializers" to initializers,
            "i18n_included" to modules.contains("kilua-i18n"),
            "kjs_enabled" to isKjsEnabled,
            "kwasm_enabled" to isKwasmEnabled,
            "ssr_enabled" to isSsrEnabled,
            "rpc_enabled" to (projectType != KiluaProjectType.FRONTEND),
            "rpc_plugin_enabled" to (isSsrEnabled || projectType != KiluaProjectType.FRONTEND),
            "jvm_enabled" to (isSsrEnabled || projectType != KiluaProjectType.FRONTEND),
            "test_enabled" to isTestEnabled,
            "src_frontend_dir_prefix" to (srcFrontendDirPrefix ?: ""),
        )
    }
}
