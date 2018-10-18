package eu.cqse.azure

import java.util.*
import kotlin.collections.LinkedHashMap

class AzureSigningString() {
    var verb = ""
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
    var headers: Map<String, String> = LinkedHashMap()
    var account = ""
    var container = ""
    var queryParameters: Map<String,String> = LinkedHashMap()

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
            stringToSign += "\n$headerKey:${headers[headerKey]}"
        }
        stringToSign += "\n/$account/$container"
        for(parameter in queryParameters.keys) {
            stringToSign += "\n$parameter:${queryParameters[parameter]}"
        }

        return stringToSign
    }
}