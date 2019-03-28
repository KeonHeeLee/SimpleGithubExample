package keonheelee.github.io.simplegithubapp.api

import io.reactivex.Observable
import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo
import keonheelee.github.io.simplegithubapp.api.Model.RepoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GithubApi {

    @GET("search/repositories")
    fun searchRepository(@Query("q") query: String): Observable<RepoSearchResponse>

    @GET("repos/{owner}/{name}")
    fun getRepository(@Path("owner") ownerLogin: String,
                      @Path("name") repoName: String): Observable<GithubRepo>
}
