# Kilua Project Wizard

Kilua IntellJ IDEA plugin for new project creation.

Supported project types:
* Frontend project
* Fullstack project with Ktor
* Fullstack project with Spring Boot
* Fullstack project with Javalin
* Fullstack project with Jooby
* Fullstack project with Vert.x
 
## Contribution

You can contribute new project types:
* create new `TreeGenerator` subclass
* add new case to `KiluaModuleBuilder.createGenerator()`
* add new value to `KiluaModuleBuilder.supportedProjectTypes`
