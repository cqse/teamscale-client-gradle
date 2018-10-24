package eu.cqse.azure.api

import eu.cqse.azure.api.model.EnumerationResults
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

 internal class AzureApi(
        private val account: String,
        container: String,
        private val key: String,
        scheme: String,
        url: String
) {

    private val azureService = Retrofit.Builder()
            .baseUrl("$scheme://$account.$url/$container/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(AzureFileShareService::class.java)
    private val signatureString = AzureSigningString(
            account = account,
            container = container)

    fun getFile(path: String): ByteArray {
        val dateString = generateUtcTimeString()
        val stringToSign = signatureString.copy().apply {
            headers["x-ms-date"] = dateString
            this.path = path
        }.toString()

        val response = azureService.getFile(path, dateString, generateAuthHeader(stringToSign)).execute()
        handleError(response)

        return response.body()?.bytes()!!
    }

    fun list(path: String): EnumerationResults {
        val date = generateUtcTimeString()
        val stringToSign = signatureString.copy().apply {
            headers["x-ms-date"] = date
            this.path = path
            queryParameters += linkedMapOf("comp" to "list", "restype" to "directory")
        }.toString()

        val response = azureService.list(path, date, generateAuthHeader(stringToSign)).execute()
        handleError(response)

        return response.body()!!
    }

    fun deleteFile(path: String) {
        val date = generateUtcTimeString()
        val stringToSign = signatureString.copy().apply {
            verb = "DELETE"
            this.path = path
            headers["x-ms-date"] = date
        }.toString()

        val response = azureService.deleteFile(path, date, generateAuthHeader(stringToSign)).execute()
        handleError(response)
    }

    private fun generateAuthHeader(stringToSign: String): String {
        val base64Encoder = Base64.getEncoder()
        val base64Decoder = Base64.getDecoder()
        val mac = Mac.getInstance("HmacSHA256")

        val keySpec = SecretKeySpec(base64Decoder.decode(key), "HmacSHA256")
        mac.init(keySpec)

        val bytesToSign = stringToSign.toByteArray(Charsets.UTF_8)

        val signatureHash = base64Encoder.encode(mac.doFinal(bytesToSign)).toString(Charsets.UTF_8)
        return "SharedKey $account:$signatureHash"
    }

    private fun generateUtcTimeString(): String {
        val requestTime = Date()
        val dateFormatString = "EEE, dd MMM YYYY HH:mm:ss"
        val dateFormat = SimpleDateFormat(dateFormatString, Locale.UK)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return "${dateFormat.format(requestTime)} GMT"
    }

    private fun handleError(response: Response<*>) {
        if (!response.isSuccessful) {
            throw ApiException(response.code(), response.message()).apply { details = response.errorBody()?.string() ?: "" }
        }
    }
}