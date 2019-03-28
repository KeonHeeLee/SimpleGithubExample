package keonheelee.github.io.simplegithubapp.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class AuthInterceptor(private val token: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain)
    // with() 함수와 run() 함수로 추가 변수 선언을 제거
            : Response = with(chain){
        val newRequest = request().newBuilder().run{
            addHeader("Authorization", "token " + token)
            build()
        }
        proceed(newRequest)
    }
}