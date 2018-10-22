package eu.cqse.azure

data class AzureConfig(
        val scheme: String,
        val url: String,
        val account: String,
        val container: String,
        val key: String,
        val zipPath: String
)