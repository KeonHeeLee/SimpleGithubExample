package keonheelee.github.io.simplegithubapp.ui.data

import android.content.Context
import android.preference.PreferenceManager

class AuthTokenProvider(private val context: Context) {

    // SharedPreferences에 저장되어 있는 엑세스 토큰을 반환
    // 저장되어 있는 액세스 토큰이 없는 경우 null 반환
    val token: String?
        get() = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_AUTH_TOKEN, null)

    // SharedPreferences에 엑세스 토큰을 저장
    fun updateToken(token: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_AUTH_TOKEN, token)
                .apply()
    }

    companion object {

        private val KEY_AUTH_TOKEN = "auth_token"
    }
}
