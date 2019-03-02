package com.lewis.clear.rxbus

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

object RxBus {
    private val publisher = PublishSubject.create<Any>()

    fun post(event: Any) {
        publisher.onNext(event)
    }

    fun <T> register(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
}