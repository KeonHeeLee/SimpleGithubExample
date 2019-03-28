package keonheelee.github.io.simplegithubapp.di

import android.app.Application
import android.content.Context
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule {
    // 어플리케이션 컨텍스트를 제공
    // 다른 컨텍스트와의 혼동을 방지하기 위해 "appContext"라는 이름으로 구분
    @Provides
    @Named("appContext")
    @Singleton
    fun provideContext(application: Application): Context
            = application.applicationContext
}