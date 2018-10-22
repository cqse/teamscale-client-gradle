package eu.cqse.azure.api

import eu.cqse.azure.api.model.EnumerationResults
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface AzureFileShareService {

    @Headers("x-ms-version: 2018-03-28")
    @GET("{path}?comp=list&restype=directory")
    fun listZips(
            @Path("path") path: String,
            @Header("x-ms-date") date: String,
            @Header("authorization") authString: String
    ): Call<EnumerationResults>


}