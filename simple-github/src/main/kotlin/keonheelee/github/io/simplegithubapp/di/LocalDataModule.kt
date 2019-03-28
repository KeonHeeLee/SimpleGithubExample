package keonheelee.github.io.simplegithubapp.di

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import keonheelee.github.io.simplegithubapp.data.AuthTokenProvider
import keonheelee.github.io.simplegithubapp.data.SearchHistoryDao
import keonheelee.github.io.simplegithubapp.data.SimpleGithubDatabase
import javax.inject.Named
import javax.inject.Singleton

@Module
class LocalDataModule {

    // 인증 토큰을 관리하는 객체인 AuthTokenProvider를 제공
    // AuthTokenProvider는 SharePreferences를 기반으로 인증 토큰을 관리
    // "appContext"라는 이름으로 구분되는 Context 객체를 필요로 함
    @Provides
    @Singleton
    fun provideAuthTokenProvider(@Named("appContext") context: Context)
            : AuthTokenProvider
            = AuthTokenProvider(context)

    // 저장소 조회 기록을 관리하는 객체인 SearchHistoryDao를 제공
    @Provides
    @Singleton
    fun provideSearchHistoryDao(db: SimpleGithubDatabase)
            : SearchHistoryDao
            = db.searchHistoryDao()

    // 데이터베이스를 관리하는 객체인 SimpleGithubDatabase를 제공
    // "appContext"라는 이름으로 구분되는 Context 객체를 필요로 함
    @Provides
    @Singleton
    fun provideDatabase(@Named("appContext") context: Context)
            : SimpleGithubDatabase
            = Room.databaseBuilder(context,
            SimpleGithubDatabase::class.java, "simple_github.db")
            .build()
}