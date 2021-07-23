package com.example.songshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseUser;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SplashActivity extends AppCompatActivity {

    public static final String TAG = "SplashActivity";
    private static final String CLIENT_ID = "1cb8dc3da6564e51af249a98d3d0eba1";
    private static final String REDIRECT_URI = "http://localhost:8888/";
    private static final int REQUEST_CODE = 1337;
    private static final String[] SCOPES = {"streaming", "playlist-read-private" ,"playlist-modify-public" ,"playlist-modify-private","user-read-private"
            ,"user-library-read","playlist-read-collaborative","user-top-read"};
    private OkHttpClient okHttpClient;
    String accessToken;
    String uri;
    String id;
    String profile_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        okHttpClient = new OkHttpClient();
        authorizeAccount();
    }

    public void authorizeAccount(){
        // Log in to Spotify Account to receive authorization
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN,REDIRECT_URI);
        builder.setScopes(SCOPES);
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this,REQUEST_CODE,request);
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthorizationResponse response = AuthorizationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    Log.i(TAG,"Token retrieved");
                    // Handle successful response

                    // Need access token to make calls to Spotify Web API
                    accessToken = response.getAccessToken();
                    getUserUri();

                    break;

                // Auth flow returned an error
                case ERROR:
                    Log.i(TAG,"ERROR: " + response.getError());
                    // Handle error response
                    break;

                // Most likely auth flow was cancelled
                default:
                    Log.i(TAG,"DEFAULT");
                    // Handle other cases
            }
        }
    }

    private void getUserUri() {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/me").newBuilder();
        String url = urlBuilder.build().toString();

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i(TAG,"onFailure");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);
                    uri = json.getString("uri");
                    id = json.getString("id");

                    JSONArray images = new JSONArray(json.getJSONArray("images").toString());
                    JSONObject imgObject = new JSONObject(images.get(0).toString());
                    profile_url = imgObject.getString("url");

                    Log.i(TAG,"uri: " + uri);

                } catch (JSONException e) {
                    Log.i(TAG,e.toString());
                    profile_url = "";
                }

                if((ParseUser.getCurrentUser() != null) ){
                    if(Objects.equals(uri, ParseUser.getCurrentUser().get("spotify_uri").toString())) {
                        Log.i(TAG, "go to main activity");
                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                        i.putExtra("uri",uri);
                        i.putExtra("token",accessToken);
                        startActivity(i);
                        finish();
                    }else{
                        ParseUser.logOut();
                        Log.i(TAG,"go to log in page");
                        Intent i = new Intent(SplashActivity.this,LoginActivity.class);
                        startActivity(i);
                        finish();
                    }

                }else{
                    Log.i(TAG,"go to log in page");
                    Intent i = new Intent(SplashActivity.this,LoginActivity.class);
                    startActivity(i);
                    finish();

                }
            }

        });
    }
}