package eu.cqse.azure.api

import eu.cqse.azure.api.model.EnumerationResults
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

internal interface AzureFileShareService {
    companion object {
        const val X_MS_VERSION = "2018-03-28"
    }

    @Headers("x-ms-version: $X_MS_VERSION")
    @GET("{path}?comp=list&restype=directory")
    fun list(@Path("path") path: String,
             @Header("x-ms-date") date: String,
             @Header("authorization") authString: String
    ): Call<EnumerationResults>

    @Headers("x-ms-version: $X_MS_VERSION")
    @GET("{path}")
    fun getFile(@Path("path") path: String,
                @Header("x-ms-date") date: String,
                @Header("authorization") authString: String
    ): Call<ResponseBody>

    @Headers("x-ms-version: $X_MS_VERSION")
    @DELETE("{path}")
    fun deleteFile(@Path("path") path: String,
                @Header("x-ms-date") date: String,
                @Header("authorization") authString: String
    ): Call<ResponseBody>
}