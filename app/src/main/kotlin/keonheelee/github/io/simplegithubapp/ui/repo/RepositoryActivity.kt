package keonheelee.github.io.simplegithubapp.ui.repo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

import com.bumptech.glide.Glide

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.ui.api.GithubApi
import keonheelee.github.io.simplegithubapp.ui.api.Model.GithubRepo
import keonheelee.github.io.simplegithubapp.ui.api.Model.provideGithubApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import kotlinx.android.synthetic.main.activity_repository.*;

class RepositoryActivity : AppCompatActivity() {

    companion object {
        const val KEY_USER_LOGIN = "user_login"
        const val KEY_REPO_NAME = "repo_name"
    }

    internal val api: GithubApi by lazy { provideGithubApi(this) }
    internal var repoCall: Call<GithubRepo>? = null

    // REST API 응답에 포함된 날짜 및 시간 표시 형식
    internal val dateFormatInResponse = SimpleDateFormat(
            "yyyy-MM-dd'HH:mm:ssX", Locale.getDefault())

    // 화면에서 사용자에게 보여줄 날짜 및 시간 표시 형식
    internal val dateFormatToShow = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        // 액티비티 호출 시 전달받은 사용자 이름과 저장소 이름을 추출
        val login = intent.getStringExtra(KEY_USER_LOGIN)
                ?: throw IllegalArgumentException("No login info exists in extras")
        val repo = intent.getStringExtra(KEY_REPO_NAME)
                ?: throw IllegalArgumentException("No repo info exists in extras")

        showRepositoryInfo(login, repo)
    }

    private fun showRepositoryInfo(login: String, repoName: String) {
        showProgress()

        repoCall = api.getRepository(login, repoName)
        repoCall!!.enqueue(object : Callback<GithubRepo> {
            override fun onResponse(call: Call<GithubRepo>, response: Response<GithubRepo>) {
                hideProgress(true)

                val repo = response.body()
                if (response.isSuccessful && repo != null) {
                    // 저장소 소유자의 프로필 사진을 표시합니다.
                    Glide.with(this@RepositoryActivity)
                            .load(repo.owner.avartarUrl)
                            .into(ivActivityRepositoryProfile)

                    // 저장소 정보를 표시
                    tvActivityRepositoryName.text = repo.fullName
                    tvActivityRepositoryStars.text = resources
                            .getQuantityString(R.plurals.star, repo.stars, repo.stars)
                    if (null == repo.description) {
                        tvActivityRepositoryDescription.setText(R.string.no_description_provided)
                    } else {
                        tvActivityRepositoryDescription.text = repo.description
                    }
                    if (null == repo.language) {
                        tvActivityRepositoryLanguage.setText(R.string.no_language_specified)
                    } else {
                        tvActivityRepositoryLanguage.text = repo.language
                    }

                    try {
                        val lastUpdate = dateFormatInResponse.parse(repo.updatedAt)
                        tvActivityRepositoryLastUpdate.text = dateFormatToShow.format(lastUpdate)
                    } catch (e: ParseException) {
                        tvActivityRepositoryLastUpdate.text = getString(R.string.unknown)
                    }

                } else {
                    showError("Not successful: " + response.message())
                }
            }

            override fun onFailure(call: Call<GithubRepo>, t: Throwable) {
                hideProgress(false)
                showError(t.message)
            }
        })
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

    override fun onStop(){
        super.onStop()
        repoCall?.run { cancel() }
    }
}
