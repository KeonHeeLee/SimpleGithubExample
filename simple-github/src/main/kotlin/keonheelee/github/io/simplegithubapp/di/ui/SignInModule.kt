package keonheelee.github.io.simplegithubapp.di.ui

import dagger.Module
import dagger.Provides
import keonheelee.github.io.simplegithubapp.api.AuthApi
import keonheelee.github.io.simplegithubapp.data.AuthTokenProvider
import keonheelee.github.io.simplegithubapp.ui.signin.SignInViewModelFactory

@Module
class SignInModule {
    // SignInViewModelFactory 객체를 제공
    @Provides
    fun provideViewModelFactory(authApi: AuthApi, authTokenProvider: AuthTokenProvider)
            : SignInViewModelFactory
            = SignInViewModelFactory(authApi, authTokenProvider)
}