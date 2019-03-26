package keonheelee.github.io.simplegithubapp.ui.signin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import keonheelee.github.io.simplegithubapp.AutoClearedDisposable

import keonheelee.github.io.simplegithubapp.BuildConfig
import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.api.AuthApi
import keonheelee.github.io.simplegithubapp.api.Model.provideAuthApi
import keonheelee.github.io.simplegithubapp.data.AuthTokenProvider
import keonheelee.github.io.simplegithubapp.plusAssign
import keonheelee.github.io.simplegithubapp.ui.main.MainActivity

import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.longToast
import org.jetbrains.anko.newTask

class SignInActivity : AppCompatActivity() {

    internal val api: AuthApi by lazy { provideAuthApi() }
    internal val authTokenProvider
            : AuthTokenProvider by lazy { AuthTokenProvider(this) }
    // internal var accessTokenCall: Call<GithubAccessToken>? = null
    // internal val disposables = CompositeDisposable()
    internal val disposables = AutoClearedDisposable(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Lifecycle.addObserver() 함수를 사용하여
        // AutoClearedDisposable 객체를 옵서버로 등록
        lifecycle += disposables

        btnActivitySignInStart.setOnClickListener {
            // 사용자 인증을 처리하는 URL을 구성
            // 형식: https://github.com/login/oauth/
            //       authorize?client_id={어플리케이션의 Client ID}
            val authUri = Uri.Builder().scheme("https")
                    .authority("github.com")
                    .appendPath("login")
                    .appendPath("oauth")
                    .appendPath("authorize")
                    .appendQueryParameter("client_id",
                            BuildConfig.GITHUB_CLIENT_ID)
                    .build()

            // 크롬 커스텀 탭으로 웹 페이지 표시
            val intent = CustomTabsIntent.Builder().build()
            intent.launchUrl(this@SignInActivity, authUri)
        }

        // 저장된 액세스 토큰이 있다면 메인 액티비티로 이동
        if (authTokenProvider.token != null)
            launchMainActivity()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        showProgress()

        // 사용자 인증 완료 후 리다이렉션된 주소 가져옴
        val uri = intent.data ?: throw IllegalArgumentException("No data exists")
        val code = uri.getQueryParameter("code") ?: throw IllegalStateException("No code exists")

        getAccessToken(code)
    }

    private fun getAccessToken(code: String) {
        // 비동기 방식으로 액세스 토큰을 요청
        // 액세스 토큰을 요청하는 REST API
        disposables += api.getAccessToken(
                BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)
                // REST API를 통해 받은 응답에서 액세스 토큰만 추출
                .map { it.accessToken }
                // 이 이후에 수행되는 코드는 모두 메인 스레드에서 실행
                // RxAndroid에서 제공하는 스케줄러인
                // AndroidSchedulers.mainThread()를 사용
                 .observeOn(AndroidSchedulers.mainThread())
                // 구독할 때 수행할 작업을 구현
                 .doOnSubscribe { showProgress() }
                 // 스트림이 종료될 때 수행할 작업을 구현
                 .doOnTerminate { hideProgress() }
                  // 옵서버블을 구독
                 .subscribe({token->
                      // API를 통해 액세스 토큰을 정상적으로 받았을 때 처리할 작업 구현
                      // 작업 중 오류가 발생하면, 이 블록은 호출 안됨

                      // 발급받은 액세스 토큰을 저장
                      authTokenProvider.updateToken(token)
                      // 메인 액티비티로 이동
                      launchMainActivity()
                  }) {
                      // 에러 블록
                      // 네트워크 오류나 데이터 처리 오류 등
                      // 작업이 정상적으로 완료되지 않았을 때 호출
                      showError(it)
                  }
    }


    private fun showProgress() {
        btnActivitySignInStart.visibility = View.GONE
        pbActivitySignIn.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        btnActivitySignInStart.visibility = View.VISIBLE
        pbActivitySignIn.visibility = View.GONE
    }

    private fun showError(throwable: Throwable) {
        longToast(throwable.message ?: "No message available")
    }

    private fun launchMainActivity() {
        startActivity(intentFor<MainActivity>().clearTask().newTask())
    }
}
