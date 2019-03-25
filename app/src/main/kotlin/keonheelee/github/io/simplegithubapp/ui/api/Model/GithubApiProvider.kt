package keonheelee.github.io.simplegithubapp.ui.api.Model

import android.content.Context

import java.io.IOException

import keonheelee.github.io.simplegithubapp.ui.api.AuthApi
import keonheelee.github.io.simplegithubapp.ui.api.GithubApi
import keonheelee.github.io.simplegithubapp.ui.data.AuthTokenProvider
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GithubApiProvider {

    // 액세스 토큰 획득을 위한 객체 생성
    fun provideAuthApi(): AuthApi {
        return Retrofit.Builder()
                .baseUrl("https://github.com/")
                .client(provideOkHttpClient(provideLoggingInterceptor(), null))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AuthApi::class.java)
    }

    // 저장소 정보에 접근하기 위한 객체를 생성
    fun provideGithubApi(context: Context): GithubApi {
        return Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(provideOkHttpClient(provideLoggingInterceptor(),
                        provideAuthInterceptor(provideAuthTokenProvider(context))))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GithubApi::class.java)
    }

    // 네트워크 통신에 사용할 클라이언트 객체 생성
    private fun provideOkHttpClient(
            interceptor: HttpLoggingInterceptor,
            authInterceptor: AuthInterceptor?): OkHttpClient {
        val b = OkHttpClient.Builder()

        if (authInterceptor != null) {
            // 매 요청의 헤더에 액세스 토큰 정보 추가
            b.addInterceptor(authInterceptor)
        }
        // 이 클라이언트를 통해 오고 가는 네트워크 요청/응답을 로그로 표시하도록 함
        b.addInterceptor(interceptor)
        return b.build()
    }

    // 네트워크 요청/응답을 로그에 표시하는 Interceptor 객체를 생성
    private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return interceptor
    }

    // 액세스 토큰을 헤더에 추가하는 Interceptor 객체를 생성
    private fun provideAuthInterceptor(
            provider: AuthTokenProvider): AuthInterceptor {
        val token = provider.token ?: throw IllegalStateException("authToken cannot be null")

        return AuthInterceptor(token)
    }

    private fun provideAuthTokenProvider(
            context: Context): AuthTokenProvider {
        return AuthTokenProvider(context.applicationContext)
    }

    internal class AuthInterceptor(private val token: String) : Interceptor {

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()

            // 요청의 헤더에 액세스 토큰 정보 추가
            val b = original.newBuilder()
                    .addHeader("Authorization", "token $token")

            val request = b.build()
            return chain.proceed(request)
        }
    }
}
