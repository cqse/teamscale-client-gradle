package eu.cqse.azure

open class AzureConfig {
    var scheme = "https"
    var url = "file.core.windows.net"
    var account = ""
    var container = ""
    var key = ""
    var remotePath = ""
    var fileNamePattern = """.*\.zip"""
    var downloadDir = ""
    var deleteRemoteFiles = true
}