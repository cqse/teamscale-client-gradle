package eu.cqse.azure.okhttp

import com.google.gson.Gson
import eu.cqse.azure.AzureConfig
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList

open class AzureDownloadTaskOkHttp : DefaultTask() {

    private val dateFormat = "EEE, dd MMM YYYY HH:mm:ss"
    private val msVersion = "2018-03-28"
    private val azure = Gson().fromJson<AzureConfig>(
            FileReader(File(project.rootDir, "azure.conf")),
            AzureConfig::class.java)
    private val https = OkHttpClient.Builder()
            .build()
    val urlBuilder = HttpUrl.Builder()
            .scheme(azure.scheme)
            .host("${azure.account}.${azure.url}")
            .addPathSegment(azure.container)
            .addPathSegment(azure.zipPath)

    fun listZipFiles(): List<String> {
        val url = urlBuilder
                .setQueryParameter("comp", "list")
                .setQueryParameter("restype", "directory")
                .build()

        val requestTimeString = generateUtcTimeString()

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

        val request = Request.Builder()
                .url(url)
                .get()
                .header("x-ms-version", msVersion)
                .header("x-ms-date", requestTimeString)
                .header("authorization", generateAuthHeader(stringToSign))
                .build()

        val response = https.newCall(request).execute()

        println(response.code())
        println(response.body()?.bytes()?.toString(Charsets.UTF_8))

        return ArrayList()
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
        val dateFormat = SimpleDateFormat(dateFormat, Locale.UK)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return "${dateFormat.format(requestTime)} GMT"
    }
}