package eu.cqse.azure

import com.google.gson.Gson
import eu.cqse.azure.api.AzureApi
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileReader

open class AzureDownloadTask : DefaultTask() {
    private val azureConfig = Gson().fromJson<AzureConfig>(
            FileReader(java.io.File(project.rootDir, "azure.conf")),
            AzureConfig::class.java)
    private val azure = AzureApi(azureConfig.account, azureConfig.container, azureConfig.key, azureConfig.scheme, azureConfig.url)

    @TaskAction
    fun download() {
        val zipFiles = azure.list(azureConfig.zipPath).entries.files.filter { it.name.endsWith(".zip") }
        for (zip in zipFiles) {
            val path = azureConfig.zipPath +
                    (if (azureConfig.zipPath == "" || azureConfig.zipPath.endsWith("/")) "" else "/") +
                    zip.name

            val bytes = azure.getFile(path)
            File("tmp/${zip.name}").writeBytes(bytes)
        }
    }

}