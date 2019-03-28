package keonheelee.github.io.simplegithubapp.ui.repo

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import keonheelee.github.io.simplegithubapp.api.GithubApi

class RepositoryViewModelFactory(val api: GithubApi): ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return RepositoryViewModel(api) as T
    }
}