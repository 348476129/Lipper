package com.fallllllll.lipperwithkotlin.data.network.interceptor

import com.fallllllll.lipperwithkotlin.core.constants.CLIENT_ACCESS_TOKEN
import com.fallllllll.lipperwithkotlin.data.local.user.UserManager
import com.fallllllll.lipperwithkotlin.utils.LogUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

/**
 * Created by fall on 2017/6/2/002.
 * GitHub :  https://github.com/348476129/LipperWithKotlin
 */
class LipperInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        LogUtils.i(original.url().url().toString())
        val requestBuilder: Request.Builder = original.newBuilder()
        if (UserManager.instance.isLogin()) {
            requestBuilder.header("Authorization", "Bearer " + UserManager.instance.access_token)
        } else {
            if (original.headers().get("Authorization").isNullOrEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + CLIENT_ACCESS_TOKEN)
            }

        }
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}