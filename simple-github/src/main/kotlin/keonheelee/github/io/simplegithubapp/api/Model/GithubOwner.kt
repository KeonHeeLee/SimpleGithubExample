package keonheelee.github.io.simplegithubapp.api.Model

import com.google.gson.annotations.SerializedName

class GithubOwner(
        val login: String,
        @field:SerializedName("avartar_url") val avartarUrl: String)
