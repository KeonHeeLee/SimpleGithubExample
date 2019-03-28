package keonheelee.github.io.simplegithubapp.di.ui

import dagger.Module
import dagger.Provides
import keonheelee.github.io.simplegithubapp.api.GithubApi
import keonheelee.github.io.simplegithubapp.ui.repo.RepositoryViewModelFactory

@Module
class RepositoryModule {

    // RepositoryViewModelFactory 객체를 제공
    @Provides
    fun provideViewModelFactory(githubApi: GithubApi)
            : RepositoryViewModelFactory
            = RepositoryViewModelFactory(githubApi)
}