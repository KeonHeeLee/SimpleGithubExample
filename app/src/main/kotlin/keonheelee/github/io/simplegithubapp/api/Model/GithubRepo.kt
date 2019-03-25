package keonheelee.github.io.simplegithubapp.api.Model

import com.google.gson.annotations.SerializedName
import keonheelee.github.io.simplegithubapp.ui.api.Model.GithubOwner

class GithubRepo(
        val name: String,
        @field:SerializedName("full_name") val fullName: String,
        // GithubOwner 형태의 객체 표현
        val owner: GithubOwner,
        val description: String?,
        val language: String?,
        @field:SerializedName("updated_at") val updatedAt: String,
        @field:SerializedName("stargazers_count") val stars: Int)
