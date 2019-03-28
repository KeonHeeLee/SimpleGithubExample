package keonheelee.github.io.simplegithubapp.di.ui

import dagger.Module
import dagger.Provides
import keonheelee.github.io.simplegithubapp.api.GithubApi
import keonheelee.github.io.simplegithubapp.data.SearchHistoryDao
import keonheelee.github.io.simplegithubapp.ui.search.SearchActivity
import keonheelee.github.io.simplegithubapp.ui.search.SearchAdapter
import keonheelee.github.io.simplegithubapp.ui.search.SearchViewModelFactory

@Module
class SearchModule {

    // SearchAdapter 객체를 제공
    @Provides
    fun provideAdapter(activity: SearchActivity): SearchAdapter
            = SearchAdapter().apply { setItemClickListener(activity) }

    // SearchViewModelFactory 객체를 제공
    fun provideViewModelFactory(
            githubApi: GithubApi, searchHistoryDao: SearchHistoryDao)
            : SearchViewModelFactory
            = SearchViewModelFactory(githubApi, searchHistoryDao)
}