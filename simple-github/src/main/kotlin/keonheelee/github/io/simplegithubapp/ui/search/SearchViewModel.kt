package keonheelee.github.io.simplegithubapp.ui.search

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import keonheelee.github.io.simplegithubapp.api.GithubApi
import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo
import keonheelee.github.io.simplegithubapp.data.SearchHistoryDao
import keonheelee.github.io.simplegithubapp.extensions.runOnIoScheduler
import keonheelee.github.io.simplegithubapp.util.SupportOptional
import keonheelee.github.io.simplegithubapp.util.emptyOptional
import keonheelee.github.io.simplegithubapp.util.optionalOf
import java.lang.IllegalStateException

class SearchViewModel (
        val api: GithubApi,
        val searchHistoryDao: SearchHistoryDao)
    : ViewModel() {

    // 검색 결과를 전달할 서브젝트. 초깃값으로 빈 값을 지정
    val searchResult: BehaviorSubject<SupportOptional<List<GithubRepo>>>
            = BehaviorSubject.createDefault(emptyOptional())

    // 마지막 검색어를 전달할 서브젝트. 초깃값으로 빈 값을 지정
    val lastSearchKeyword: BehaviorSubject<SupportOptional<String>>
            = BehaviorSubject.createDefault(emptyOptional())

    // 화면에 표시할 메세지를 전달할 서브젝트
    val message: BehaviorSubject<SupportOptional<String>> = BehaviorSubject.create()

    // 작업 진행 상태를 전달할 서브젝트, 초깃값으로 false
    val isLoading: BehaviorSubject<Boolean>
            = BehaviorSubject.createDefault(false)

    // 검색 결과를 요청
    fun searchRepository(query: String): Disposable
            = api.searchRepository(query)
            // 검색어를 lastSearchKeyword 서브젝트에 전달
            .doOnNext { lastSearchKeyword.onNext(optionalOf(query)) }
            .flatMap {
                if(it.totalCount == 0) {
                    Observable.error(IllegalStateException("No search result"))
                } else {
                    Observable.just(it.items)
                }
            }
            // 검색을 시작하기 전에, 현재 화면에 표시되고 있던 검색 결과 및 메세지를 모두 제거
            // 작업 진행 상태를 true로 변경
            .doOnSubscribe {
                searchResult.onNext(emptyOptional())
                message.onNext(emptyOptional())
                isLoading.onNext(true)
            }
            // 작업이 종료되면 작업 진행 상태를 false로 변경
            .doOnTerminate { isLoading.onNext(false) }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ items ->
                // 검색 결과를 searchResult 서브젝트에 전달
                searchResult.onNext(optionalOf(items))
            }){
                // 에러가 발생한 경우 message 서브젝트를 통해 에러 메세지를 전달
                message.onNext(optionalOf(it.message ?: "Unexpected error"))
            }

    // 데이터베이스에 저장소 정보를 추가
    fun addToSearchHistory(repository: GithubRepo): Disposable
            = runOnIoScheduler { searchHistoryDao.add(repository) }
}