package keonheelee.github.io.simplegithubapp.api.Model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "repositories")
class GithubRepo(
        val name: String,
        @field:SerializedName("full_name")
        @PrimaryKey @ColumnInfo(name="full_name") val fullName: String,
        // GithubOwner 형태의 객체 표현
        @Embedded val owner: GithubOwner,
        val description: String?,
        val language: String?,
        @field:SerializedName("updated_at")
        @ColumnInfo(name="updated_at") val updatedAt: String,
        @field:SerializedName("stargazers_count") val stars: Int)
