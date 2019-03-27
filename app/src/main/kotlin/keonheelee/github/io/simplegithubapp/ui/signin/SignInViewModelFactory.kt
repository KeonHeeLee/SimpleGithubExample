package keonheelee.github.io.simplegithubapp.ui.signin

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import keonheelee.github.io.simplegithubapp.api.AuthApi
import keonheelee.github.io.simplegithubapp.data.AuthTokenProvider

class SignInViewModelFactory(
        val api: AuthApi,
        val authTokenProvider: AuthTokenProvider)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SignInViewModel(api, authTokenProvider) as T
    }

}