package keonheelee.github.io.simplegithubapp

import io.reactivex.disposables.Disposable

operator fun AutoClearedDisposable.plusAssign(disposable: Disposable){
    this.add(disposable)
}