package keonheelee.github.io.simplegithubapp.ui.main

import android.arch.lifecycle.ViewModel
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo
import keonheelee.github.io.simplegithubapp.data.SearchHistoryDao
import keonheelee.github.io.simplegithubapp.extensions.runOnIoScheduler
import keonheelee.github.io.simplegithubapp.util.SupportOptional
import keonheelee.github.io.simplegithubapp.util.emptyOptional
import keonheelee.github.io.simplegithubapp.util.optionalOf

class MainViewModel(val searchHistoryDao: SearchHistoryDao): ViewModel() {

    // 메세지를 전달할 서브젝트
    val message: BehaviorSubject<SupportOptional<String>>
            = BehaviorSubject.create()

    // 데이터베이스에 저장되어 있는 저장소 조회 기록을 Flowable 형태로 제공
    // searchHistory 자체가 값을 갖지 않고, searchHistoryDao를 통해 데이터를 가져오므로
    // 지원 프로퍼티(backing property) 형태로 선언
    val searchHistory: Flowable<SupportOptional<List<GithubRepo>>>
        get() = searchHistoryDao.getHistory()
                // SupportOptional 형태로 데이터를 감싸줌
                .map { optionalOf(it) }
                // 매 이벤트가 발생할 때마다 함수 블록을 호출
                .doOnNext { optional ->
                    if(optional.value.isEmpty())
                        // 표시할 데이터가 없는 경우,
                    else
                    // message 서브젝트를 통해 표시할 메세지를 전달
                        message.onNext(emptyOptional())
                }
                .doOnError{
                    // 에러 메세지를 message 서브젝트를 통해 전달
                    message.onNext(optionalOf(it.message ?: "Unexpected error"))
                }
                // 에러가 발생한 경우 빈 데이터를 반환
                .onErrorReturn{ emptyOptional() }

    // 데이터베이스에 저장된 저장소 조회 기록을 모두 삭제
    fun clearSearchHistory(): Disposable
            = runOnIoScheduler { searchHistoryDao.clearAll() }
}