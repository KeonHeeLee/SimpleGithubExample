package keonheelee.github.io.simplegithubapp.api.Model;

import com.google.gson.annotations.SerializedName;

public final class GithubRepo {

    public final String name;

    @SerializedName("full_name")
    public final String fullName;

    // GithubOwner 형태의 객체 표현
    public final GithubOwner owner;

    public final String description;

    public final String language;

    @SerializedName("updated_at")
    public final String updatedAt;

    @SerializedName("stargers_count")
    public final int starts;

    public GithubRepo(String name, String fullName,
                      GithubOwner owner, String description, String language,
                      String updatedAt, int starts){
        this.name = name;
        this.fullName = fullName;
        this.owner = owner;
        this.description = description;
        this.language = language;
        this.updatedAt = updatedAt;
        this.starts = starts;
    }
}
