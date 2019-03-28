package keonheelee.github.io.simplegithubapp.di.ui

import dagger.Module
import dagger.Provides
import keonheelee.github.io.simplegithubapp.data.SearchHistoryDao
import keonheelee.github.io.simplegithubapp.ui.main.MainActivity
import keonheelee.github.io.simplegithubapp.ui.main.MainViewModelFactory
import keonheelee.github.io.simplegithubapp.ui.search.SearchAdapter

@Module
class MainModule {

    // SearchAdapter 객체를 제공
    @Provides
    fun provideAdapter(activity: MainActivity): SearchAdapter
            = SearchAdapter().apply { setItemClickListener(activity) }

    // MainViewModelFactory
    @Provides
    fun provideViewModelFactory(searchHistoryDao: SearchHistoryDao)
            : MainViewModelFactory
            = MainViewModelFactory(searchHistoryDao)
}