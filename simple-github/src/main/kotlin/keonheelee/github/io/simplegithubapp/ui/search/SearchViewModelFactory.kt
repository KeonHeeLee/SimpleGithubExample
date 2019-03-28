package keonheelee.github.io.simplegithubapp.ui.search

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import keonheelee.github.io.simplegithubapp.api.GithubApi
import keonheelee.github.io.simplegithubapp.data.SearchHistoryDao

class SearchViewModelFactory(
        val api: GithubApi,
        val searchHistoryDao: SearchHistoryDao)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SearchViewModel(api, searchHistoryDao) as T
    }
}