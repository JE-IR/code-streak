package com.example.data.remote

import com.example.data.model.Quote
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

interface QuotesApiService {
    @GET("random")
    suspend fun getRandomQuote(): List<Quote>

    @GET("today")
    suspend fun getTodayQuote(): List<Quote>

    companion object {
        private const val BASE_URL = "https://zenquotes.io/api/"

        fun create(): QuotesApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(QuotesApiService::class.java)
        }
    }
}
