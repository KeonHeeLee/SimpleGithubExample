package keonheelee.github.io.simplegithubapp.ui.repo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import keonheelee.github.io.simplegithubapp.AutoClearedDisposable

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.api.GithubApi
import keonheelee.github.io.simplegithubapp.api.Model.provideGithubApi
import keonheelee.github.io.simplegithubapp.plusAssign

import kotlinx.android.synthetic.main.activity_repository.*;

class RepositoryActivity : AppCompatActivity() {

    companion object {
        const val KEY_USER_LOGIN = "user_login"
        const val KEY_REPO_NAME = "repo_name"
    }

    internal val api: GithubApi by lazy { provideGithubApi(this) }
    // internal var repoCall: Call<GithubRepo>? = null
    internal val disposables = AutoClearedDisposable(this)

    // REST API 응답에 포함된 날짜 및 시간 표시 형식
    internal val dateFormatInResponse = SimpleDateFormat(
            "yyyy-MM-dd'HH:mm:ssX", Locale.getDefault())

    // 화면에서 사용자에게 보여줄 날짜 및 시간 표시 형식
    internal val dateFormatToShow = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        lifecycle += disposables

        // 액티비티 호출 시 전달받은 사용자 이름과 저장소 이름을 추출
        val login = intent.getStringExtra(KEY_USER_LOGIN)
                ?: throw IllegalArgumentException("No login info exists in extras")
        val repo = intent.getStringExtra(KEY_REPO_NAME)
                ?: throw IllegalArgumentException("No repo info exists in extras")

        showRepositoryInfo(login, repo)
    }

    private fun showRepositoryInfo(login: String, repoName: String) {

        // REST API를 통해 저장소 정보를 요청
        disposables += api.getRepository(login, repoName)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showProgress() }
                .doOnError { hideProgress(false) }
                .doOnComplete { hideProgress(true) }
                .subscribe({ repo->
                    Glide.with(this@RepositoryActivity)
                            .load(repo.owner.avartarUrl)
                            .into(ivActivityRepositoryProfile)

                    tvActivityRepositoryName.text = repo.fullName
                    tvActivityRepositoryStars.text = resources
                            .getQuantityString(R.plurals.star, repo.stars, repo.stars)
                    if(repo.description == null)
                        tvActivityRepositoryDescription.setText(R.string.no_description_provided)
                    else
                        tvActivityRepositoryDescription.text = repo.description

                    if(repo.language == null)
                        tvActivityRepositoryLanguage.setText(R.string.no_language_specified)
                    else
                        tvActivityRepositoryLanguage.text = repo.description

                    try{
                        val lastUpdate = dateFormatInResponse.parse(repo.updatedAt)
                        tvActivityRepositoryLastUpdate.text =
                                dateFormatToShow.format(lastUpdate)
                    } catch (e: ParseException) {
                        tvActivityRepositoryLastUpdate.text = getString(R.string.unknown)
                    }
                }){
                    showError(it.message)
                }
    }

    private fun showProgress() {
        llActivityRepositoryContent.visibility = View.GONE
        pbActivityRepository.visibility = View.VISIBLE
    }

    private fun hideProgress(isSucceed: Boolean) {
        llActivityRepositoryContent.visibility = if (isSucceed) View.VISIBLE else View.GONE
        pbActivityRepository.visibility = View.GONE
    }

    private fun showError(message: String?) {
        with(tvActivityRepositoryMessage){
            text = message ?: "Unexpected error."
            visibility = View.VISIBLE
        }
    }
}
