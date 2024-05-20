package ru.netology.nework.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import ru.netology.nework.BuildConfig
import ru.netology.nework.BuildConfig.API_KEY
import ru.netology.nework.BuildConfig.BASE_URL
import ru.netology.nework.auth.AppAuth
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module

class ApiModule {

    @Provides
    @Singleton
    fun provideLogging(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttp(
        logging: HttpLoggingInterceptor,
        appAuth: AppAuth
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            appAuth.authState.value.token?.let { token ->
                val newRequest = chain.request().newBuilder()
                    //.addHeader("Authorization", token)
                    .addHeader("Api-Key", API_KEY)
                    .addHeader("Authorization", token)
                    .build()
                return@addInterceptor chain.proceed(newRequest)
            }
            val newRequest = chain.request().newBuilder()

                //.addHeader("Authorization", token)
                .addHeader("Api-Key", API_KEY)
                .build()
            return@addInterceptor chain.proceed(newRequest)
        }
        .connectTimeout(55L, TimeUnit.SECONDS)
        .readTimeout(55L, TimeUnit.SECONDS)
        .writeTimeout(55L, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(
        okhttp: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl(BASE_URL)
        .client(okhttp)
        .build()

    @Provides
    @Singleton
    fun provideApiService(
        retrofit: Retrofit
    ): ApiService = retrofit.create()
}

