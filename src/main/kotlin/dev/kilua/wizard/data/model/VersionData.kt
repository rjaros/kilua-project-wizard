package dev.kilua.wizard.data.model

import com.google.gson.annotations.SerializedName

data class VersionData(
    @SerializedName("kilua")
    val kilua: String,
    @SerializedName("kotlin")
    val kotlin: String,
    @SerializedName("compose")
    val compose: String,
    @SerializedName("coroutines")
    val coroutines: String,
    @SerializedName("ksp")
    val ksp: String,
    @SerializedName("kilua-rpc")
    val kiluaRpc: String,
    @SerializedName("logback")
    val logback: String,
    @SerializedName("gettext")
    val gettext: String,
    @SerializedName("datetime")
    val datetime: String,
    @SerializedName("vite-kotlin")
    val viteKotlin: String,
    @SerializedName("template-jooby")
    val templateJooby: TemplateJooby,
    @SerializedName("template-ktor")
    val templateKtor: TemplateKtor,
    @SerializedName("template-micronaut")
    val templateMicronaut: TemplateMicronaut,
    @SerializedName("template-spring-boot")
    val templateSpring: TemplateSpring,
    @SerializedName("modules")
    val modules: List<Module>
)

data class TemplateJooby(
    @SerializedName("jooby")
    val jooby: String
)

data class TemplateKtor(
    @SerializedName("ktor")
    val ktor: String
)

data class TemplateMicronaut(
    @SerializedName("micronaut")
    val micronaut: String,
    @SerializedName("micronaut-plugins")
    val micronautPlugins: String
)

data class TemplateSpring(
    @SerializedName("spring-boot")
    val springBoot: String
)

data class Module(
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("excludes")
    val excludes: List<String>?,
    @SerializedName("initializers")
    val initializers: List<String>?
)
