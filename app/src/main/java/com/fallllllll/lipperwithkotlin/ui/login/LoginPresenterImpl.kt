package com.fallllllll.lipperwithkotlin.ui.login

import android.net.Uri
import com.fallllllll.lipperwithkotlin.R
import com.fallllllll.lipperwithkotlin.core.expandFunction.commonChange
import com.fallllllll.lipperwithkotlin.core.expandFunction.isTokenOutOfDate
import com.fallllllll.lipperwithkotlin.core.presenter.BasePresenter
import com.fallllllll.lipperwithkotlin.core.rxjava.RxBus
import com.fallllllll.lipperwithkotlin.data.databean.eventBean.LoginEvent
import com.fallllllll.lipperwithkotlin.data.databean.eventBean.WebLoginBackEvent
import com.fallllllll.lipperwithkotlin.data.local.user.LipperUser
import com.fallllllll.lipperwithkotlin.data.local.user.UserManager
import com.fallllllll.lipperwithkotlin.data.local.user.UserToken
import com.fallllllll.lipperwithkotlin.data.network.model.DribbbleModel
import com.fallllllll.lipperwithkotlin.data.network.model.OauthModel
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Created by fall on 2017/6/13/013.
 * GitHub :  https://github.com/348476129/LipperWithKotlin
 */
class LoginPresenterImpl(private val dribbbleModel: DribbbleModel,
                         private val oauthModel: OauthModel,
                         private val loginView: LoginContract.LoginView) : BasePresenter(), LoginContract.LoginPresenter {
    private lateinit var token: UserToken
    override fun goShotsActivity() {
        loginView.loginSuccessful()
    }

    override fun getUserData(code: String) {
        loginView.beforeLogin()
        loginView.showTopDialog(loginView.getString(R.string.under_login))

        val disposable = oauthModel.getToken(code)
                .flatMap {
                    token = it
                    dribbbleModel.getUserInfo(it.access_token ?: "")
                }
                .delay(2, TimeUnit.SECONDS)
                .commonChange()
                .subscribe({ next(it, token) }, { error(it) })
        compositeDisposable.add(disposable)
    }

    override fun onPresenterCreate() {

        subscribeWebLoginEvent()

        if (UserManager.instance.isLogin()) {
            compositeDisposable.add(updateUserData())
        }
    }

    override fun onLoginClick() {
        if (UserManager.instance.isLogin()) {
            compositeDisposable.add(updateUserData())
        } else {
            loginView.goWebActivity()
        }
    }

    private fun updateUserData(): Disposable {
        loginView.beforeLogin()
        loginView.showTopDialog(loginView.getString(R.string.under_login))
        return  dribbbleModel.getUserInfo(UserManager.instance.access_token)
                .delay(2, TimeUnit.SECONDS)
                .commonChange()
                .subscribe({ next(it) }, { error(it) })
    }


    private fun next(lipperUser: LipperUser, token: UserToken? = null) {
        if (token != null) {
            UserManager.instance.updateToken(token)
        }
        UserManager.instance.updateUser(lipperUser)
        loginView.hideAllTopDialog()
        loginView.loginSuccessful()
        RxBus.get().post(LoginEvent(true))
    }

    private fun error(throwable: Throwable) {
        loginView.hideAllTopDialog()
        loginView.loginFinish()
        if (throwable.isTokenOutOfDate()) {
            loginView.showErrorDialog(loginView.getString(R.string.login_expire))
            UserManager.instance.logOut()
        } else {
            loginView.showErrorDialog(loginView.getString(R.string.login_failed))
        }
    }

    private fun subscribeWebLoginEvent() {
        compositeDisposable.add(RxBus.get().toFlowable<WebLoginBackEvent>()
                .subscribe({
                    getUserData(Uri.parse(it.url).getQueryParameter("code"))
                }, {
                    subscribeWebLoginEvent()
                }))
    }
}