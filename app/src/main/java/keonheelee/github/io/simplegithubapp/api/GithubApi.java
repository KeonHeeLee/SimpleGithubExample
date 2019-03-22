package keonheelee.github.io.simplegithubapp.api;

import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo;
import keonheelee.github.io.simplegithubapp.api.Model.RepoSearchResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GithubApi {

    @GET("search/repositories")
    Call<RepoSearchResponse> searchRepository(@Query("q") String query);

    @GET("repos/{owner}/{name}")
    Call<GithubRepo> getRepository(@Path("owner") String ownerLogin,
                                   @Path("name") String repoName);
}
