package eu.cqse.azure

import eu.cqse.azure.api.AzureApi
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

open class AzureDownloadTask : DefaultTask() {
    private lateinit var azureConfig: AzureConfig
    private lateinit var azure: AzureApi

    @TaskAction
    fun download() {
        azureConfig = project.property(AzureFileShareDownload.AZURE_EXTENSION_NAME) as AzureConfig
        azure = AzureApi(azureConfig.account, azureConfig.container, azureConfig.key, azureConfig.scheme, azureConfig.url)

        val zipFiles = azure.list(azureConfig.remoteZipPath).entries.files.filter { it.name.endsWith(".zip") }
        for (zip in zipFiles) {
            val path = azureConfig.remoteZipPath +
                    (if (azureConfig.remoteZipPath == "" || azureConfig.remoteZipPath.endsWith("/")) "" else "/") +
                    zip.name
            val bytes = azure.getFile(path)

            val downloadPath = azureConfig.downloadDir +
                    (if (azureConfig.downloadDir == "" || azureConfig.downloadDir.endsWith("/")) "" else "/") +
                    zip.name
            File(downloadPath).writeBytes(bytes)
        }
    }
}