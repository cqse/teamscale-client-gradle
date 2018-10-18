package eu.cqse.azure

import org.gradle.api.Plugin
import org.gradle.api.Project

class AzureFileShareDownload : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("azureDownload", AzureDownloadTask::class.java)
    }
}