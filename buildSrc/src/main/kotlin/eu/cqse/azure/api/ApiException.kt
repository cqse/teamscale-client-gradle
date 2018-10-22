package eu.cqse.azure.api

class ApiException(val errorCode: Int, message: String) : Exception(message) {
    var details: String = ""
}