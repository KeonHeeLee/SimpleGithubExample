package keonheelee.github.io.simplegithubapp.ui.api.Model

import com.google.gson.annotations.SerializedName

class RepoSearchResponse(
        @field:SerializedName("total_count") val totalCount: Int,
        // GithubRepo 형태의 리스트 표현
        val items: List<GithubRepo>)
