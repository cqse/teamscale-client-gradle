package eu.cqse.azure.api

data class AzureSigningString(
        val account: String,
        val container: String) {
    var verb: String = "GET"
    var contentEncoding = ""
    var contentLanguage = ""
    var contentLength = ""
    var contentMd5 = ""
    var contentType = ""
    var date = ""
    var ifModifiedSince = ""
    var ifMatch = ""
    var ifNoneMatch = ""
    var ifUnmodifiedSince = ""
    var range = ""
    var headers: MutableMap<String, String> = linkedMapOf(
            "x-ms-date" to "",
            "x-ms-type" to "",
            "x-ms-version" to AzureFileShareService.X_MS_VERSION)
    var path = ""
    var queryParameters: Map<String, String> = LinkedHashMap()

    override fun toString(): String {
        var stringToSign = "$verb\n" +
                "$contentEncoding\n" +
                "$contentLanguage\n" +
                "$contentLength\n" +
                "$contentMd5\n" +
                "$contentType\n" +
                "$date\n" +
                "$ifModifiedSince\n" +
                "$ifMatch\n" +
                "$ifNoneMatch\n" +
                "$ifUnmodifiedSince\n" +
                range
        for (headerKey in headers.keys) {
            if (headers[headerKey] != "") {
                stringToSign += "\n$headerKey:${headers[headerKey]}"
            }
        }
        stringToSign += "\n/$account/$container/$path"
        for (parameter in queryParameters.keys) {
            stringToSign += "\n$parameter:${queryParameters[parameter]}"
        }

        return stringToSign
    }
}