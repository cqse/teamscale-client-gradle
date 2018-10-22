package eu.cqse.azure

import eu.cqse.azure.api.AzureApi
import eu.cqse.azure.api.model.File
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class AzureDownloadTask : DefaultTask() {
    private lateinit var azureConfig: AzureConfig
    private lateinit var azure: AzureApi

    private var zips: List<File> = ArrayList()

    @TaskAction
    fun downloadAndDelete() {
        azureConfig = project.property(AzureFileShareDownload.AZURE_EXTENSION_NAME) as AzureConfig
        azure = AzureApi(azureConfig.account, azureConfig.container, azureConfig.key, azureConfig.scheme, azureConfig.url)

        zips = azure.list(azureConfig.remoteZipPath).entries.files.filter { it.name.endsWith(".zip") }

        for (zip in zips) {
            val remotePath = getFullFilePath(azureConfig.remoteZipPath, zip.name)
            val downloadPath = getFullFilePath(azureConfig.downloadDir, zip.name)

            val bytes = azure.getFile(remotePath)
            java.io.File(downloadPath).writeBytes(bytes)
            azure.deleteFile(remotePath)
        }
    }

    private fun getFullFilePath(path: String, fileName: String): String {
        return "$path${if (path == "" || path.endsWith("/")) "" else "/"}$fileName"
    }
}