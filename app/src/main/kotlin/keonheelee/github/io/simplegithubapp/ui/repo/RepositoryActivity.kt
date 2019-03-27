package keonheelee.github.io.simplegithubapp.ui.repo

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import keonheelee.github.io.simplegithubapp.rx.AutoClearedDisposable

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.api.GithubApi
import keonheelee.github.io.simplegithubapp.api.provideGithubApi
import keonheelee.github.io.simplegithubapp.extensions.plusAssign

import kotlinx.android.synthetic.main.activity_repository.*;

class RepositoryActivity : AppCompatActivity() {

    companion object {
        const val KEY_USER_LOGIN = "user_login"
        const val KEY_REPO_NAME = "repo_name"
    }

    internal val api: GithubApi by lazy { provideGithubApi(this) }
    // internal var repoCall: Call<GithubRepo>? = null
    internal val disposables = AutoClearedDisposable(this)

    // 액티비티가 완전히 종료되기 전까지 이벤트를 계속 받기 위해 추가
    internal val viewDisposables
            = AutoClearedDisposable(lifecycleOwner = this, alwaysClearOnStop = false)

    // RepositoryViewModel을 생성하기 위해 필요한 뷰모델 팩토리 클래스의 인스턴스를 생성
    internal val viewModelFactory by lazy {
        RepositoryViewModelFactory(provideGithubApi(this))
    }

    // 뷰모델의 인스턴스는 onCreate()에서 받으므로, lateinit으로 선언
    lateinit var viewModel: RepositoryViewModel

    // REST API 응답에 포함된 날짜 및 시간 표시 형식
    internal val dateFormatInResponse = SimpleDateFormat(
            "yyyy-MM-dd'HH:mm:ssX", Locale.getDefault())

    // 화면에서 사용자에게 보여줄 날짜 및 시간 표시 형식
    internal val dateFormatToShow = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        // RepositoryViewModel의 인스턴스를 받음
        viewModel = ViewModelProviders.of(
                this, viewModelFactory)[RepositoryViewModel::class.java]

        lifecycle += disposables


        // viewDisposable에서 이 액티비티의 생명주기 이벤트를 받도록 함
        lifecycle += viewDisposables

        // 저장소 정보 이벤트를 구독
        viewDisposables += viewModel.repository
                // 유효한 저장소 이벤트만 받도록 함
                .filter { !it.isEmpty }
                .map { it.value }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { repository ->
                    Glide.with(this@RepositoryActivity)
                            .load(repository.owner.avartarUrl)
                            .into(ivActivityRepositoryProfile)

                    tvActivityRepositoryName.text = repository.fullName

                    tvActivityRepositoryStars.text = resources
                            .getQuantityString(R.plurals.star,
                                    repository.stars, repository.stars)
                    if (repository.description == null)
                        tvActivityRepositoryDescription.setText(R.string.no_description_provided)
                    else
                        tvActivityRepositoryDescription.text = repository.description

                    if (repository.language == null)
                        tvActivityRepositoryLanguage.setText(R.string.no_language_specified)
                    else
                        tvActivityRepositoryLanguage.text = repository.language

                    try {
                        val lastUpdate = dateFormatInResponse
                                .parse(repository.updatedAt)
                        tvActivityRepositoryLastUpdate.text =
                                dateFormatToShow.format(lastUpdate)
                    } catch (e: ParseException) {
                        tvActivityRepositoryLastUpdate.text = getString(R.string.unknown)
                    }
                }

        // 메세지 이벤트를 구독
        viewDisposables += viewModel.message
                .observeOn(AndroidSchedulers.mainThread())
                // 메세지를 이벤트를 받으면 화면에 해당 메세지를
                .subscribe { message -> showError(message) }

        // 저장소 정보를 보여주는 뷰의 표시 유무를 결정하는 이벤트를 구독
        viewDisposables += viewModel.isContentVisible
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe{ visible -> setContentVisibility(visible) }

        // 작업 진행 여부 이벤트를 구독
        viewDisposables += viewModel.isLoding
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { isLoding ->
                    // 작업 진행 여부 이벤트에 따라 프로그레스바의 표시 상태를 변경
                    if(isLoding)
                        showProgress()
                    else
                        hideProgress()
                }

        val login = intent.getStringExtra(KEY_USER_LOGIN) ?: throw IllegalArgumentException(
                "No login info exists in extras")
        val repo = intent.getStringExtra(KEY_REPO_NAME) ?: throw IllegalArgumentException(
                "No repo info exists in extras")

        // 저장소 정보를 요청
        disposables += viewModel.requestRepositoryInfo(login, repo)
    }

    private fun showProgress() {
        pbActivityRepository.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        pbActivityRepository.visibility = View.GONE
    }

    private fun setContentVisibility(show: Boolean) {
        llActivityRepositoryContent.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String?) {
        with(tvActivityRepositoryMessage){
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }
}
