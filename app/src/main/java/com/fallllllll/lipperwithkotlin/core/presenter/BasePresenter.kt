package com.fallllllll.lipperwithkotlin.core.presenter

import io.reactivex.disposables.CompositeDisposable
import kotlin.properties.Delegates

/**
 * Created by fall on 2017/5/27/027.
 * GitHub :  https://github.com/348476129/LipperWithKotlin
 */
abstract class BasePresenter : Contract.Presenter {
    protected lateinit var compositeDisposable: CompositeDisposable

    override fun attach() {
        compositeDisposable = CompositeDisposable()
    }

    override fun detach() {
        compositeDisposable.clear()

    }
}