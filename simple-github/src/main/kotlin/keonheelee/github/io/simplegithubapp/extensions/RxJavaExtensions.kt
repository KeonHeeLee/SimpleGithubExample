package keonheelee.github.io.simplegithubapp.extensions

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import keonheelee.github.io.simplegithubapp.rx.AutoClearedDisposable

operator fun AutoClearedDisposable.plusAssign(disposable: Disposable){
    this.add(disposable)
}

fun runOnIoScheduler(func: () -> Unit): Disposable
        = Completable.fromCallable(func)
        .subscribeOn(Schedulers.io())
        .subscribe()