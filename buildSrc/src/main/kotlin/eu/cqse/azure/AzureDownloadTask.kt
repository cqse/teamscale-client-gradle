package eu.cqse.azure

import eu.cqse.azure.api.AzureApi
import eu.cqse.azure.api.model.File
import net.lingala.zip4j.core.ZipFile
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.IOException

/**
 * Gradle task which downloads JaCoCo coverage files from a directory specified by
 * [AzureConfig.remotePath] to [AzureConfig.downloadDir] which match the pattern [AzureConfig.fileNamePattern].
 * If [AzureConfig.deleteRemoteFiles] is set, the files will be deleted on the Azure file share.
 * If the downloaded file is a zip, it will be extracted. The task expects one file in the container called
 * "coverage.xml" which will be renamed to the name of the zip file (with xml file extension)
 */
open class AzureDownloadTask : DefaultTask() {
    companion object {
        private const val COVERAGE_FILE_NAME = "coverage.xml"
    }

    private lateinit var azureConfig: AzureConfig
    private lateinit var azure: AzureApi

    private var coverageFiles: List<File> = ArrayList()

    @TaskAction
    fun downloadAndDelete() {
        azureConfig = project.property(AzureFileShareDownload.AZURE_EXTENSION_NAME) as AzureConfig
        azure = AzureApi(azureConfig.account, azureConfig.container, azureConfig.key, azureConfig.scheme, azureConfig.url)

        coverageFiles = azure.list(azureConfig.remotePath).entries.files.filter {
            Regex(azureConfig.fileNamePattern).matches(it.name)
        }

        for (coverageFile in coverageFiles) {
            val remotePath = getFullFilePath(azureConfig.remotePath, coverageFile.name)
            val downloadPath = getFullFilePath(azureConfig.downloadDir, coverageFile.name)

            val bytes = azure.getFile(remotePath)

            val localFile = java.io.File(downloadPath)
            localFile.writeBytes(bytes)

            if (azureConfig.deleteRemoteFiles) {
                azure.deleteFile(remotePath)
            }

            if (localFile.name.endsWith(".zip")) {
                extractZip(localFile)
            }
        }
    }

    private fun getFullFilePath(path: String, fileName: String): String {
        return "$path${if (path == "" || path.endsWith("/")) "" else "/"}$fileName"
    }

    private fun extractZip(localFile: java.io.File) {
        val zipFile = ZipFile(localFile.path)
        zipFile.extractAll(localFile.parentFile.path)
        val extractedFile = java.io.File("${localFile.parentFile.path}${java.io.File.separator}$COVERAGE_FILE_NAME")
        val renamedFile = java.io.File(localFile.parentFile.path +
                java.io.File.separator +
                "${localFile.name.substring(0, localFile.name.length - 3)}xml")
        if (renamedFile.exists()) {
            throw IOException("File ${renamedFile.name} already exists")
        }
        extractedFile.renameTo(renamedFile)
    }
}