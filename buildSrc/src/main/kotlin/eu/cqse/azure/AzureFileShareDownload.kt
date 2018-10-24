package eu.cqse.azure

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Gradle Plugin that provides functionality to download files from an azure file share
 * Configuration is handled by an extension property called "azure", see [AzureConfig] for available options
 */
class AzureFileShareDownload : Plugin<Project> {
    companion object {
        const val AZURE_EXTENSION_NAME = "azure"
    }

    override fun apply(project: Project) {
        project.extensions.extraProperties.set(AZURE_EXTENSION_NAME, AzureConfig())
        project.tasks.create("azureDownload", AzureDownloadTask::class.java)
    }
}