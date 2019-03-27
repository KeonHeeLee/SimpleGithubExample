package keonheelee.github.io.simplegithubapp.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo

@Database(entities = arrayOf(GithubRepo::class), version = 1)
abstract class SimpleGithubDatabase : RoomDatabase() {
    // 데이터베이스와 연결할 데이터 접근 객체를 정의
    abstract fun searchHistoryDao(): SearchHistoryDao
}