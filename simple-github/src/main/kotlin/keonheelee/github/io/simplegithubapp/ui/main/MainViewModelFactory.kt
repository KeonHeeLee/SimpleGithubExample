package keonheelee.github.io.simplegithubapp.ui.main

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import keonheelee.github.io.simplegithubapp.data.SearchHistoryDao

class MainViewModelFactory(val searchHistoryDao: SearchHistoryDao)
    : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainViewModel(searchHistoryDao) as T
    }
}