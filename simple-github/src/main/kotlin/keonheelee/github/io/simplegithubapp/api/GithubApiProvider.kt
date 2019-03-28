package keonheelee.github.io.simplegithubapp.api

import android.content.Context
import keonheelee.github.io.simplegithubapp.data.AuthTokenProvider

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

// 액세스 토큰 획득을 위한 객체 생성
fun provideAuthApi(): AuthApi
        = Retrofit.Builder()
        .baseUrl("https://github.com/")
        .client(provideOkHttpClient(provideLoggingInterceptor(), null))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)


// 저장소 정보에 접근하기 위한 객체를 생성
fun provideGithubApi(context: Context): GithubApi
        = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .client(provideOkHttpClient(provideLoggingInterceptor(),
                provideAuthInterceptor(provideAuthTokenProvider(context))))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.createAsync())
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GithubApi::class.java)


// 네트워크 통신에 사용할 클라이언트 객체 생성
private fun provideOkHttpClient(
        interceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor?): OkHttpClient
    = OkHttpClient.Builder()
        .run {
            if (authInterceptor != null) {
                // 매 요청의 헤더에 액세스 토큰 정보 추가
                addInterceptor(authInterceptor)
            }
            // 이 클라이언트를 통해 오고 가는 네트워크 요청/응답을 로그로 표시하도록 함
            addInterceptor(interceptor)
            build()
        }


// 네트워크 요청/응답을 로그에 표시하는 Interceptor 객체를 생성
private fun provideLoggingInterceptor(): HttpLoggingInterceptor
        = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

// 액세스 토큰을 헤더에 추가하는 Interceptor 객체를 생성
private fun provideAuthInterceptor(
        provider: AuthTokenProvider): AuthInterceptor {
    val token = provider.token ?: throw IllegalStateException("authToken cannot be null")

    return AuthInterceptor(token)
}

private fun provideAuthTokenProvider(context: Context): AuthTokenProvider
    = AuthTokenProvider(context.applicationContext)