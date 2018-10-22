package eu.cqse.azure

import com.google.gson.Gson
import eu.cqse.azure.api.AzureFileShareService
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

open class AzureDownloadTask : DefaultTask() {
    private val msVersion = "2018-03-28"
    private val azure = Gson().fromJson<AzureConfig>(
            FileReader(File(project.rootDir, "azure.conf")),
            AzureConfig::class.java)
    private val azureService = Retrofit.Builder()
            .baseUrl("${azure.scheme}://${azure.account}.${azure.url}/${azure.container}/")
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
            .create(AzureFileShareService::class.java)


    @TaskAction
    fun listZipFiles() {
        val date = generateUtcTimeString()
        val stringToSign = AzureSigningString(
                verb = "GET",
                account = azure.account,
                container = azure.container,
                xMsVersion = msVersion)
                .apply {
                    headers["x-ms-date"] = date
                    path = azure.zipPath
                    queryParameters += linkedMapOf("comp" to "list", "restype" to "directory")
                }.toString()
        val call = azureService.listZips(azure.zipPath, date, generateAuthHeader(stringToSign))

        val response = call.execute()

        if (response.isSuccessful) {
            println(response.body())
        } else {
            println("$stringToSign\n")
            println(response.errorBody()?.bytes()?.toString(Charsets.UTF_8))
        }

    }

    fun generateAuthHeader(stringToSign: String): String {
        val base64Encoder = Base64.getEncoder()
        val base64Decoder = Base64.getDecoder()
        val mac = Mac.getInstance("HmacSHA256")

        val keySpec = SecretKeySpec(base64Decoder.decode(azure.key), "HmacSHA256")
        mac.init(keySpec)

        val bytesToSign = stringToSign.toByteArray(Charsets.UTF_8)

        val signatureHash = base64Encoder.encode(mac.doFinal(bytesToSign)).toString(Charsets.UTF_8)
        return "SharedKey ${azure.account}:$signatureHash"
    }

    fun generateUtcTimeString(): String {
        val requestTime = Date()
        val dateFormatString = "EEE, dd MMM YYYY HH:mm:ss"
        val dateFormat = SimpleDateFormat(dateFormatString, Locale.UK)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return "${dateFormat.format(requestTime)} GMT"
    }

}