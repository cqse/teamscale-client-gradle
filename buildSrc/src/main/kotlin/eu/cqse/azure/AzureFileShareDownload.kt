package eu.cqse.azure

import org.gradle.api.Plugin
import org.gradle.api.Project

class AzureFileShareDownload : Plugin<Project> {
    companion object {
        const val AZURE_EXTENSION_NAME = "azure"
    }

    override fun apply(project: Project) {
        project.extensions.extraProperties.set(AZURE_EXTENSION_NAME, AzureConfig())
        project.tasks.create("azureDownload", AzureDownloadTask::class.java)
    }
}