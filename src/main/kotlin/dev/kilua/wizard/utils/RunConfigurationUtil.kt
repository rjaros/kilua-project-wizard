package dev.kilua.wizard.utils

import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.impl.RunManagerImpl
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import org.jetbrains.plugins.gradle.service.execution.GradleExternalTaskConfigurationType
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration
import javax.swing.Icon

class KiluaConfigurationFactory(val task: String, private val args: String = "") :
    ConfigurationFactory(GradleExternalTaskConfigurationType.getInstance()) {
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        val conf = GradleRunConfiguration(
            project,
            GradleExternalTaskConfigurationType.getInstance().factory,
            "Run $task"
        )
        conf.settings.externalProjectPath = project.basePath
        conf.settings.taskNames = listOf(task)
        conf.settings.scriptParameters = args
        return conf
    }

    override fun getName(): String = "Run $task"

    override fun getIcon(): Icon = IconLoader.getIcon("/images/kilua.png", KiluaConfigurationFactory::class.java)
}

class RunnerComparator : Comparator<RunnerAndConfigurationSettings> {
    override fun compare(o1: RunnerAndConfigurationSettings?, o2: RunnerAndConfigurationSettings?): Int {
        return when {
            o1?.factory is KiluaConfigurationFactory -> 1
            o2?.factory is KiluaConfigurationFactory -> -1
            else -> 0
        }
    }
}

object RunConfigurationUtil {
    fun createFrontendConfiguration(
        project: Project,
        kjsEnabled: Boolean,
        kwasmEnabled: Boolean,
        viteKotlinEnabled: Boolean
    ) {
        val runManager = RunManagerImpl.getInstanceImpl(project)
        createWebpackConfiguration(kjsEnabled, kwasmEnabled, runManager, project)
        if (viteKotlinEnabled) createViteKotlinConfiguration(kjsEnabled, kwasmEnabled, runManager, project)
        runManager.setOrder(RunnerComparator())
        runManager.requestSort()
    }

    fun createFullstackConfiguration(
        project: Project,
        kjsEnabled: Boolean,
        kwasmEnabled: Boolean,
        viteKotlinEnabled: Boolean
    ) {
        val runManager = RunManagerImpl.getInstanceImpl(project)
        runManager.addConfiguration(
            RunnerAndConfigurationSettingsImpl(
                RunManagerImpl.getInstanceImpl(project),
                KiluaConfigurationFactory("jvmRun").createTemplateConfiguration(project)
            )
        )
        createWebpackConfiguration(kjsEnabled, kwasmEnabled, runManager, project)
        if (viteKotlinEnabled) createViteKotlinConfiguration(kjsEnabled, kwasmEnabled, runManager, project)
        runManager.setOrder(RunnerComparator())
        runManager.requestSort()
    }

    private fun createWebpackConfiguration(
        kjsEnabled: Boolean,
        kwasmEnabled: Boolean,
        runManager: RunManagerImpl,
        project: Project,
    ) {
        if (kjsEnabled) {
            runManager.addConfiguration(
                RunnerAndConfigurationSettingsImpl(
                    RunManagerImpl.getInstanceImpl(project),
                    KiluaConfigurationFactory("jsBrowserDevelopmentRun", "-t").createTemplateConfiguration(project)
                )
            )
        }
        if (kwasmEnabled) {
            runManager.addConfiguration(
                RunnerAndConfigurationSettingsImpl(
                    RunManagerImpl.getInstanceImpl(project),
                    KiluaConfigurationFactory("wasmJsBrowserDevelopmentRun", "-t").createTemplateConfiguration(project)
                )
            )
        }
    }

    private fun createViteKotlinConfiguration(
        kjsEnabled: Boolean,
        kwasmEnabled: Boolean,
        runManager: RunManagerImpl,
        project: Project
    ) {
        if (kjsEnabled && kwasmEnabled) {
            runManager.addConfiguration(
                RunnerAndConfigurationSettingsImpl(
                    RunManagerImpl.getInstanceImpl(project),
                    KiluaConfigurationFactory("jsViteRun").createTemplateConfiguration(project)
                )
            )
            runManager.addConfiguration(
                RunnerAndConfigurationSettingsImpl(
                    RunManagerImpl.getInstanceImpl(project),
                    KiluaConfigurationFactory("jsViteCompileDev", "-t").createTemplateConfiguration(project)
                )
            )
            runManager.addConfiguration(
                RunnerAndConfigurationSettingsImpl(
                    RunManagerImpl.getInstanceImpl(project),
                    KiluaConfigurationFactory("wasmJsViteRun").createTemplateConfiguration(project)
                )
            )
            runManager.addConfiguration(
                RunnerAndConfigurationSettingsImpl(
                    RunManagerImpl.getInstanceImpl(project),
                    KiluaConfigurationFactory("wasmJsViteCompileDev", "-t").createTemplateConfiguration(project)
                )
            )
        } else {
            runManager.addConfiguration(
                RunnerAndConfigurationSettingsImpl(
                    RunManagerImpl.getInstanceImpl(project),
                    KiluaConfigurationFactory("viteRun").createTemplateConfiguration(project)
                )
            )
            runManager.addConfiguration(
                RunnerAndConfigurationSettingsImpl(
                    RunManagerImpl.getInstanceImpl(project),
                    KiluaConfigurationFactory("viteCompileDev", "-t").createTemplateConfiguration(project)
                )
            )
        }
    }
}