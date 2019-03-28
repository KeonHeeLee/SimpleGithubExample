package keonheelee.github.io.simplegithubapp

import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import keonheelee.github.io.simplegithubapp.di.DaggerAppComponent

class SimpleGithubApp: DaggerApplication() {

    // DaggerAppComponent의 인스턴스를 반환
    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.builder().application(this).build()
    }
}