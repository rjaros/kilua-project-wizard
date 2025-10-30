package dev.kilua.wizard

import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.SettingsStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import com.intellij.psi.impl.file.PsiDirectoryFactory
import dev.kilua.wizard.data.VersionApi
import dev.kilua.wizard.data.model.TemplateJooby
import dev.kilua.wizard.data.model.TemplateKtor
import dev.kilua.wizard.data.model.TemplateMicronaut
import dev.kilua.wizard.data.model.TemplateSpring
import dev.kilua.wizard.data.model.VersionData
import dev.kilua.wizard.generator.ProjectTreeGenerator
import dev.kilua.wizard.step.library_choice.LibraryChoiceStep
import dev.kilua.wizard.utils.RunConfigurationUtil
import dev.kilua.wizard.utils.backgroundTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jetbrains.plugins.gradle.service.project.GradleAutoImportAware
import org.jetbrains.plugins.gradle.service.project.open.linkAndSyncGradleProject
import java.io.File

class KiluaModuleBuilder : ModuleBuilder() {

    val wizardScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val versionData by lazy { fetchVersionData() }

    var projectType: KiluaProjectType = KiluaProjectType.FRONTEND
    var groupId: String = "com.example"
    var artifactId: String = "project"
    var kjsEnabled: Boolean = true
    var kwasmEnabled: Boolean = true
    var ssrEnabled: Boolean = true
    var viteKotlinEnabled: Boolean = false
    var testEnabled: Boolean = true
    var selectedModules: List<String> = listOf("kilua-bootstrap")
    var selectedInitializers: List<String> = listOf("BootstrapModule", "BootstrapCssModule")

    var wizardContext: WizardContext? = null

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        val root = createAndGetRoot() ?: return
        modifiableRootModel.addContentEntry(root)
        try {
            ApplicationManager.getApplication().runWriteAction {
                val manager = PsiManager.getInstance(modifiableRootModel.project)
                manager.findFile(root)?.add(
                    PsiDirectoryFactory.getInstance(manager.project)
                        .createDirectory(root.createChildDirectory(null, "webpack"))
                )
            }
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        val generator = ProjectTreeGenerator()
        modifiableRootModel.project.backgroundTask("Setting up project") {
            try {
                generator.generate(
                    projectType,
                    root,
                    artifactId,
                    groupId,
                    wizardContext?.projectName ?: artifactId,
                    selectedModules,
                    selectedInitializers,
                    versionData,
                    kjsEnabled,
                    kwasmEnabled,
                    ssrEnabled,
                    viteKotlinEnabled,
                    testEnabled
                )
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            if (ssrEnabled || projectType != KiluaProjectType.FRONTEND) {
                RunConfigurationUtil.createFullstackConfiguration(
                    modifiableRootModel.project,
                    kjsEnabled,
                    kwasmEnabled,
                    viteKotlinEnabled
                )
            } else {
                RunConfigurationUtil.createFrontendConfiguration(
                    modifiableRootModel.project,
                    kjsEnabled,
                    kwasmEnabled,
                    viteKotlinEnabled
                )
            }
            GradleAutoImportAware()
            wizardScope.launch {
                val projectFilePath = root.canonicalPath + "/build.gradle.kts"
                linkAndSyncGradleProject(modifiableRootModel.project, projectFilePath)
            }
        }
    }

    private fun createAndGetRoot(): VirtualFile? {
        val path = contentEntryPath?.let { FileUtil.toSystemIndependentName(it) } ?: return null
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(File(path).apply { mkdirs() }.absolutePath)
    }

    override fun getModuleType(): ModuleType<*> {
        return KiluaModuleType()
    }

    override fun getCustomOptionsStep(context: WizardContext?, parentDisposable: Disposable?): ModuleWizardStep {
        return LibraryChoiceStep(this, parentDisposable)
    }

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<out ModuleWizardStep?>? {
        this.wizardContext = wizardContext
        return super.createWizardSteps(wizardContext, modulesProvider)
    }

    override fun modifySettingsStep(settingsStep: SettingsStep): ModuleWizardStep? {
        settingsStep.moduleNameLocationSettings?.moduleName = artifactId
        return super.modifySettingsStep(settingsStep)
    }

    private fun fetchVersionData(): VersionData {
        return try {
            VersionApi.create().getVersionData().blockingGet()
        } catch (_: Exception) {
            VersionData(
                kilua = "0.0.29",
                kotlin = "2.3.0-Beta2",
                compose = "1.10.0-alpha03",
                coroutines = "1.10.2",
                ksp = "2.3.0",
                kiluaRpc = "0.0.39",
                logback = "1.5.20",
                gettext = "0.7.0",
                datetime = "0.7.1",
                viteKotlin = "0.6.3",
                templateJooby = TemplateJooby("4.0.10"),
                templateKtor = TemplateKtor(ktor = "3.3.1"),
                templateMicronaut = TemplateMicronaut(micronaut = "4.10.0", micronautPlugins = "4.6.0"),
                templateSpring = TemplateSpring(springBoot = "4.0.0-RC1"),
                modules = emptyList()
            )
        }
    }
}
