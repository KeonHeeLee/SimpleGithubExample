package keonheelee.github.io.simplegithubapp.ui.signin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import keonheelee.github.io.simplegithubapp.BuildConfig;
import keonheelee.github.io.simplegithubapp.R;
import keonheelee.github.io.simplegithubapp.api.AuthApi;
import keonheelee.github.io.simplegithubapp.api.Model.GithubAccessToken;
import keonheelee.github.io.simplegithubapp.api.Model.GithubApiProvider;
import keonheelee.github.io.simplegithubapp.data.AuthTokenProvider;
import keonheelee.github.io.simplegithubapp.ui.main.MainActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    Button btnStart;
    ProgressBar progress;
    AuthApi api;
    AuthTokenProvider authTokenProvider;
    Call<GithubAccessToken> accessTokenCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        btnStart = findViewById(R.id.btnActivityMainSearch);
        progress = findViewById(R.id.pbActivitySignIn);

        btnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                // 사용자 인증을 처리하는 URL을 구성
                // 형식: https://github.com/login/oauth/
                //       authorize?client_id={어플리케이션의 Client ID}
                Uri authUri = new Uri.Builder().scheme("https")
                        .authority("github.com")
                        .appendPath("login")
                        .appendPath("oauth")
                        .appendPath("authorize")
                        .appendQueryParameter("client_id",
                                BuildConfig.GITHUB_CLIENT_ID)
                        .build();

                // 크롬 커스텀 탭으로 웹 페이지 표시
                CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
                intent.launchUrl(SignInActivity.this, authUri);
            }
        });

        api = GithubApiProvider.provideAuthApi();
        authTokenProvider = new AuthTokenProvider(this);

        // 저장된 액세스 토큰이 있다면 메인 액티비티로 이동
        if(authTokenProvider.getToken() != null)
            launchMainActivity();
    }

    @Override
    protected void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        showProgress();

        // 사용자 인증 완료 후 리다이렉션된 주소 가져옴
        Uri uri = intent.getData();
        if(uri == null)
            throw new IllegalArgumentException("No data exists");

        String code = uri.getQueryParameter("code");
        if(code == null)
            throw new IllegalStateException("No code exists");

        getAccessToken(code);
    }

    private void getAccessToken(@NonNull String code){
        showProgress();

        // 액세스 토큰을 요청하는 REST API
        accessTokenCall = api.getAccessToken(
                BuildConfig.GITHUB_CLIENT_ID, BuildConfig.GITHUB_CLIENT_SECRET, code);

        // 비동기 방식으로 액세스 토큰을 요청
        accessTokenCall.enqueue(new Callback<GithubAccessToken>() {
            @Override
            public void onResponse(Call<GithubAccessToken> call,
                                   Response<GithubAccessToken> response) {
                hideProgress();

                GithubAccessToken token = response.body();
                if(response.isSuccessful() && token != null){
                    // 발급받은 액세스 토큰을 저장
                    authTokenProvider.updateToken(token.accessToken);

                    // 메인 액티비티로 이동
                    launchMainActivity();
                } else {
                    showError(new IllegalStateException(
                            "Not successful:" + response.message()));
                }
            }

            @Override
            public void onFailure(Call<GithubAccessToken> call, Throwable t) {
                hideProgress();
                showError(t);
            }
        });
    }


    private void showProgress() {
        btnStart.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        btnStart.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
    }

    private void showError(Throwable throwable) {
        Toast.makeText(this, throwable.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void launchMainActivity() {
        startActivity(new Intent(
                SignInActivity.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}
