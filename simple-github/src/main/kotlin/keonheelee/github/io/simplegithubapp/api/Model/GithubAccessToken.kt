package keonheelee.github.io.simplegithubapp.api.Model

import com.google.gson.annotations.SerializedName

class GithubAccessToken(
        @field:SerializedName("access_token") val accessToken: String,
        val scope: String,
        @field:SerializedName("token_type") val tokenType: String)
