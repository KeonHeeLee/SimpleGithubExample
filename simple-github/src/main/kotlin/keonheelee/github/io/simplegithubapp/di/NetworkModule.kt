package keonheelee.github.io.simplegithubapp.di

import dagger.Module
import dagger.Provides
import keonheelee.github.io.simplegithubapp.api.AuthInterceptor
import keonheelee.github.io.simplegithubapp.data.AuthTokenProvider
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.lang.IllegalStateException
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule {
    // "unauthorized"라는 이름으로 구분할 수 있는 OkHttpClient 객체를 제공
    // 여기에서 제공하는 OkHttpClient 객체는 요청에 인증 토큰을 추가하지 않음
    @Provides
    @Named("unauthorized")
    @Singleton
    fun provideUnauthorizedOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor) : OkHttpClient
            = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

    // "authorized"라는 이름으로 구분할 수 있는 OkHttpClient 객체를 제공
    // 여기에서 제공하는 OkHttpClient 객체는 요청에 인증 토큰을 추가
    @Provides
    @Named("authorized")
    @Singleton
    fun provideAuthorizedOkHttpClient(
            loggingInterceptor: HttpLoggingInterceptor,
            authInterceptor: AuthInterceptor) : OkHttpClient
            = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

    // HttpLoggingInterceptor 객체를 제공
    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor
            = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // AuthInterceptor 객체를 제공
    @Provides
    @Singleton
    fun provideAuthInterceptor(provider: AuthTokenProvider): AuthInterceptor{
        val token = provider.token
                ?: throw IllegalStateException("authToken cannot be null")
        return AuthInterceptor(token)
    }
}