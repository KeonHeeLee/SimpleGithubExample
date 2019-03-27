package keonheelee.github.io.simplegithubapp.ui.signin

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.v7.app.AppCompatActivity
import android.view.View
import io.reactivex.android.schedulers.AndroidSchedulers
import keonheelee.github.io.simplegithubapp.rx.AutoClearedDisposable

import keonheelee.github.io.simplegithubapp.BuildConfig
import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.api.AuthApi
import keonheelee.github.io.simplegithubapp.api.provideAuthApi
import keonheelee.github.io.simplegithubapp.data.AuthTokenProvider
import keonheelee.github.io.simplegithubapp.extensions.plusAssign
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

    // 액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가
    internal val viewDisposables
            = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    // SignInViewModel을 생성할 때 필요한 뷰모델 팩토리 클래스의 인스턴스를 생성
    internal val viewModelFactory by lazy {
        SignInViewModelFactory(provideAuthApi(), AuthTokenProvider(this))
    }

    // 뷰모델 인스턴스는 onCreate()애서 받으므로, lateinit으로 선언
    lateinit var viewModel: SignInViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // SignInViewModel의 인스턴스를 받음
        viewModel = ViewModelProviders.of(
                this, viewModelFactory)[SignInViewModel::class.java]

        // Lifecycle.addObserver() 함수를 사용하여
        // AutoClearedDisposable 객체를 옵서버로 등록
        lifecycle += disposables

        // viewDisposables에서 이 액티비티의 생명주기 이벤트를 받도록 함
        lifecycle += viewDisposables

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

        // 액세스 토큰 이벤트를 구독
        viewDisposables += viewModel.accessToken
                // 액세스 토큰이 없는 경우는 무시
                .filter{ !it.isEmpty }
                .observeOn(AndroidSchedulers.mainThread())
                // 액세스 토큰이 있는 것을 확인했다면 메인 화면으로 이동
                .subscribe{ launchMainActivity() }

        // 에러 메세지 이벤트를 구독
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { message-> showError(message) }

        viewDisposables += viewModel.isLoading
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isLoading ->
                    // 작업 진행 여부 이벤트에 따라 프로그레스바의 표시 상태를 변경
                    if(isLoading) showProgress()
                    else hideProgress()
                }

        // 기기에 저장되어 있는 액세스 토큰을 불러옴
        disposables += viewModel.loadAccessToken()
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
       disposables += viewModel.requestAccessToken(
               BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code)
    }


    private fun showProgress() {
        btnActivitySignInStart.visibility = View.GONE
        pbActivitySignIn.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        btnActivitySignInStart.visibility = View.VISIBLE
        pbActivitySignIn.visibility = View.GONE
    }

    private fun showError(message: String) {
        longToast(message)
    }

    private fun launchMainActivity() {
        startActivity(intentFor<MainActivity>().clearTask().newTask())
    }
}
