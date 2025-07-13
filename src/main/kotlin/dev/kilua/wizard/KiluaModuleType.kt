package dev.kilua.wizard

import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

class KiluaModuleType : ModuleType<KiluaModuleBuilder>("KILUA_WIZARD") {

    private val _icon: Icon by lazy { IconLoader.getIcon("/images/kilua.png", KiluaModuleType::class.java) }

    override fun createModuleBuilder(): KiluaModuleBuilder {
        return KiluaModuleBuilder()
    }

    override fun getName(): String {
        return "Kilua"
    }

    override fun getDescription(): String {
        return "A new project with Kilua - a composable web framework for Kotlin/Wasm and Kotlin/JS"
    }

    override fun getNodeIcon(isOpened: Boolean): Icon {
        return _icon
    }

    override fun getIcon(): Icon {
        return _icon
    }
}

enum class KiluaProjectType(val displayName: String, val code: String) {
    FRONTEND("Frontend project", "front"),
    KTOR("Fullstack project with Ktor", "ktor"),
    SPRING_BOOT("Fullstack project with Spring Boot", "spring-boot"),
    JAVALIN("Fullstack project with Javalin", "javalin"),
    JOOBY("Fullstack project with Jooby", "jooby"),
    MICRONAUT("Fullstack project with Micronaut", "micronaut"),
    VERTX("Fullstack project with Vert.x", "vertx")
}

val supportedProjectTypes = arrayOf(
    KiluaProjectType.FRONTEND,
    KiluaProjectType.KTOR,
    KiluaProjectType.SPRING_BOOT,
    KiluaProjectType.JAVALIN,
    KiluaProjectType.JOOBY,
    KiluaProjectType.MICRONAUT,
    KiluaProjectType.VERTX
)
