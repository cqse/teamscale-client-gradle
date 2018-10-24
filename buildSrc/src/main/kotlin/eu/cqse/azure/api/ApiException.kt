package eu.cqse.azure.api

internal class ApiException(val errorCode: Int, message: String) : Exception(message) {
    var details: String = ""
}