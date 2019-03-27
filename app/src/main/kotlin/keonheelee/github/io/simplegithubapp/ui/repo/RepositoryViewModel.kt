package keonheelee.github.io.simplegithubapp.ui.repo

import android.arch.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import keonheelee.github.io.simplegithubapp.api.GithubApi
import keonheelee.github.io.simplegithubapp.api.Model.GithubRepo
import keonheelee.github.io.simplegithubapp.util.SupportOptional
import keonheelee.github.io.simplegithubapp.util.optionalOf

class RepositoryViewModel(val api:GithubApi) : ViewModel() {

    // 저장소 젖ㅇ보를 전달할 서브젝트
    val repository: BehaviorSubject<SupportOptional<GithubRepo>>
            = BehaviorSubject.create()

    // 에러 메세지를 전달할 서브젝트
    val message: BehaviorSubject<String> = BehaviorSubject.create()

    // 저장소 정보를 보여주는 레이아웃의 표시 여부를 전달할 서브젝트
    // 초깃값으로 false를 지정
    val isContentVisible: BehaviorSubject<Boolean>
            = BehaviorSubject.createDefault(false)

    // 작업 진행 상태를 전달할 서브젝트
    val isLoding: BehaviorSubject<Boolean> = BehaviorSubject.create()

    // API를 사용하여 저장소 정보릉 요청
    fun requestRepositoryInfo(login: String, repoName: String): Disposable{
        val repoObservable = if (!repository.hasValue()) {
            // repository 서브젝트에 저장된 값이 없는 경우에만
            // API를 통해 저장소를 요청
            api.getRepository(login, repoName)
        } else {
            // repository 서브젝트에 저장소 정보가 있는 경우
            // 추가로 저장소 정보를 요청하지 않아도 됨
            // 따라서 더 이상 작업을 진행하지 않도록 Observable.empty()를 반환
            Observable.empty()
        }

        return repoObservable
                // 저장소 정보를 받기 시작하면 작업 진행 상태를 true로 변경
                .doOnSubscribe { isLoding.onNext(true) }
                // 작업이 완료되면 작업 진행 상태를 false로 변경
                .doOnTerminate { isLoding.onNext(false) }
                .subscribeOn(Schedulers.io())
                .subscribe ({ repo ->
                    // repository 서브젝트에 저장소 정보를 전달
                    repository.onNext(optionalOf(repo))

                    // 저장소 정보를 보여주는 뷰를 화면에 보여주기 위해
                    // isContentVisible 서브젝트에 이벤트를 전달
                    isContentVisible.onNext(true)
                }) {
                    // 에러가 발생하면 message 서브젝트에 에러 메세지를 전달
                    message.onNext(it.message ?: "Unexpected error")
                }
    }
}