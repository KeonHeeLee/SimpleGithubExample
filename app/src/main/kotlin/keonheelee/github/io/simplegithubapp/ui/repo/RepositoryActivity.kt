package keonheelee.github.io.simplegithubapp.ui.repo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

import com.bumptech.glide.Glide

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

import keonheelee.github.io.simplegithubapp.R
import keonheelee.github.io.simplegithubapp.ui.api.GithubApi
import keonheelee.github.io.simplegithubapp.ui.api.Model.GithubApiProvider
import keonheelee.github.io.simplegithubapp.ui.api.Model.GithubRepo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepositoryActivity : AppCompatActivity() {
    lateinit internal var llContent: LinearLayout
    lateinit internal var ivProfile: ImageView
    lateinit internal var tvName: TextView
    lateinit internal var tvStars: TextView
    lateinit internal var tvDescription: TextView
    lateinit internal var tvLanguage: TextView
    lateinit internal var tvLastUpdate: TextView
    lateinit internal var pbProgress: ProgressBar
    lateinit internal var tvMessage: TextView
    lateinit internal var api: GithubApi
    lateinit internal var repoCall: Call<GithubRepo>

    // REST API 응답에 포함된 날짜 및 시간 표시 형식
    internal var dateFormatInResponse = SimpleDateFormat(
            "yyyy-MM-dd'HH:mm:ssX", Locale.getDefault())

    // 화면에서 사용자에게 보여줄 날짜 및 시간 표시 형식
    internal var dateFormatToShow = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_repository)

        llContent = findViewById(R.id.llActivityRepositoryContent)
        ivProfile = findViewById(R.id.ivActivityRepositoryProfile)
        tvName = findViewById(R.id.tvActivityRepositoryName)
        tvStars = findViewById(R.id.tvActivityRepositoryStars)
        tvDescription = findViewById(R.id.tvActivityRepositoryDescription)
        tvLanguage = findViewById(R.id.tvActivityRepositoryLanguage)
        tvLastUpdate = findViewById(R.id.tvActivityRepositoryLastUpdate)
        pbProgress = findViewById(R.id.pbActivityRepository)
        tvMessage = findViewById(R.id.tvActivityRepositoryMessage)

        api = GithubApiProvider.provideGithubApi(this)

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
        repoCall.enqueue(object : Callback<GithubRepo> {
            override fun onResponse(call: Call<GithubRepo>, response: Response<GithubRepo>) {
                hideProgress(true)

                val repo = response.body()
                if (response.isSuccessful && repo != null) {
                    // 저장소 소유자의 프로필 사진을 표시합니다.
                    Glide.with(this@RepositoryActivity)
                            .load(repo.owner.avartarUrl)
                            .into(ivProfile)

                    // 저장소 정보를 표시
                    tvName.text = repo.fullName
                    tvStars.text = resources
                            .getQuantityString(R.plurals.star, repo.stars, repo.stars)
                    if (null == repo.description) {
                        tvDescription.setText(R.string.no_description_provided)
                    } else {
                        tvDescription.text = repo.description
                    }
                    if (null == repo.language) {
                        tvLanguage.setText(R.string.no_language_specified)
                    } else {
                        tvLanguage.text = repo.language
                    }

                    try {
                        val lastUpdate = dateFormatInResponse.parse(repo.updatedAt)
                        tvLastUpdate.text = dateFormatToShow.format(lastUpdate)
                    } catch (e: ParseException) {
                        tvLastUpdate.text = getString(R.string.unknown)
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
        llContent.visibility = View.GONE
        pbProgress.visibility = View.VISIBLE
    }

    private fun hideProgress(isSucceed: Boolean) {
        llContent.visibility = if (isSucceed) View.VISIBLE else View.GONE
        pbProgress.visibility = View.GONE
    }

    private fun showError(message: String?) {
        tvMessage.text = message ?: "Unexpected error."
        tvMessage.visibility = View.VISIBLE
    }

    companion object {
        val KEY_USER_LOGIN = "user_login"
        val KEY_REPO_NAME = "repo_name"
    }
}
