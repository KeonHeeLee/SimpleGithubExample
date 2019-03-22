package keonheelee.github.io.simplegithubapp.data;

import android.content.Context;
import android.preference.PreferenceManager;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class AuthTokenProvider {

    private static final String KEY_AUTH_TOKEN = "auth_token";

    private Context context;

    public AuthTokenProvider(@NotNull Context context){
        this.context = context;
    }

    // SharedPreferences에 엑세스 토큰을 저장
    public void updateToken(@NotNull String token){
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putString(KEY_AUTH_TOKEN, token)
                .apply();
    }

    // SharedPreferences에 저장되어 있는 엑세스 토큰을 반환
    // 저장되어 있는 액세스 토큰이 없는 경우 null 반환
    @Nullable
    public String getToken(){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(KEY_AUTH_TOKEN, null);
    }
}
