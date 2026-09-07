package com.example.data.api

import android.content.Context
import com.example.data.tunnel.DirectHttpsTransport
import com.example.data.tunnel.TunnelClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class OliApiClient(
    private val context: Context,
    private val tunnelClient: TunnelClient
) {
    private var currentBaseUrl: String = "http://127.0.0.1:8080/"
    private var currentService: OliApiService? = null

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        tunnelClient.configureOkHttpClient(builder).build()
    }

    fun setBaseUrl(url: String) {
        val normalized = if (url.endsWith("/")) url else "$url/"
        if (currentBaseUrl != normalized || currentService == null) {
            currentBaseUrl = normalized
            val retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
            currentService = retrofit.create(OliApiService::class.java)
        }
    }

    fun getService(): OliApiService {
        if (currentService == null) {
            setBaseUrl(currentBaseUrl)
        }
        return currentService!!
    }

    fun getBaseUrl(): String = currentBaseUrl
}
