package eu.cqse.azure

import com.google.gson.Gson
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileReader
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

open class AzureDownloadTask : DefaultTask() {

    private val DATE_FORMAT = "EEE, dd MMM YYYY HH:mm:ss"
    private val azure = Gson().fromJson<AzureConfig>(
            FileReader(File(project.rootDir, "azure.conf")),
            AzureConfig::class.java)

    @TaskAction
    fun download() {
        val https = OkHttpClient.Builder()
                .build()

        val url = HttpUrl.Builder()
                .scheme(azure.scheme)
                .host("${azure.account}.${azure.url}")
                .addPathSegment(azure.container)
                .setQueryParameter("comp", "list")
                .setQueryParameter("restype", "directory")
                .build()

        val requestTime = Date()
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.UK)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val requestTimeString = "${dateFormat.format(requestTime)} GMT"

        println(requestTimeString)

        val msVersion = "2018-03-28"

        val request = Request.Builder()
                .url(url)
                .header("x-ms-version", msVersion)
                .header("x-ms-date", requestTimeString)
                .header("authorization", "SharedKey ${azure.account}:${generateSignature(requestTimeString, msVersion)}")
                .get()
                .build()

        val response = https.newCall(request).execute()

        println(response.code())
        println(response.body()?.bytes()?.toString(Charsets.UTF_8))
    }

    fun generateSignature(dateString: String, msVersion: String): String {
        val stringToSign = AzureSigningString().apply {
            verb = "GET"
            account = azure.account
            container = azure.container
            headers += linkedMapOf("x-ms-date" to dateString, "x-ms-version" to msVersion)
            queryParameters += linkedMapOf("comp" to "list", "restype" to "directory")
        }.toString()

        println(stringToSign)

        val bytesToSign = stringToSign.toByteArray(Charsets.UTF_8)

        val base64Encoder = Base64.getEncoder()
        val base64Decoder = Base64.getDecoder()
        val mac = Mac.getInstance("HmacSHA256")
        val keySpec = SecretKeySpec(base64Decoder.decode(azure.key), "HmacSHA256")
        mac.init(keySpec)

        val encodedHash = base64Encoder.encode(mac.doFinal(bytesToSign)).toString(Charsets.UTF_8)
        println(encodedHash)

        return encodedHash
    }
}