package keonheelee.github.io.simplegithubapp.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import keonheelee.github.io.simplegithubapp.ui.main.MainActivity
import keonheelee.github.io.simplegithubapp.ui.repo.RepositoryActivity
import keonheelee.github.io.simplegithubapp.ui.search.SearchActivity
import keonheelee.github.io.simplegithubapp.ui.signin.SignInActivity

@Module
abstract class ActivityBinder {

    // SignInActivity를 객체 그래프에 추가할 수 있도록 함
    @ContributesAndroidInjector
    abstract fun bindSignInActivity(): SignInActivity

    // MainActivity를 객체 그래프에 추가할 수 있도록 함
    @ContributesAndroidInjector
    abstract fun bindMainActivity(): MainActivity

    // SearchActivity를 객체 그래프에 추가할 수 있도록 함
    @ContributesAndroidInjector
    abstract fun bindSearchActivity(): SearchActivity

    // RepositoryActivity를 객체 그래프에 추가할 수 있도록 함
    @ContributesAndroidInjector
    abstract fun bindRepositoryActivity(): RepositoryActivity
}