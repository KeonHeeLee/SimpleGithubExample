package keonheelee.github.io.simplegithubapp.api.Model;

import com.google.gson.annotations.SerializedName;

public final class GithubOwner {

    public final String login;

    @SerializedName("avartar_url")
    public final String avartarUrl;

    public GithubOwner(String login, String avartarUrl){
        this.login = login;
        this.avartarUrl = avartarUrl;
    }
}
