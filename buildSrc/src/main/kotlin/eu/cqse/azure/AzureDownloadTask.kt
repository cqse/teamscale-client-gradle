package eu.cqse.azure

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

open class AzureDownloadTask : DefaultTask() {
    @TaskAction
    fun startDownload() {
        println("Downloading from Azure...")
    }

    @TaskAction
    fun finalizeDownload() {
        println("Download from Azure finished")
    }
}