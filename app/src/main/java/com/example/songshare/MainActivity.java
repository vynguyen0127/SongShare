package com.example.songshare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.songshare.fragments.FeedFragment;
import com.example.songshare.fragments.ProfileFragment;
import com.example.songshare.fragments.RecommendFragment;
import com.example.songshare.fragments.SearchFragment;
import com.example.songshare.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.SpotifyConnectionTerminatedException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.jetbrains.annotations.NotNull;
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

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final String CLIENT_ID = "1cb8dc3da6564e51af249a98d3d0eba1";
    private static final String REDIRECT_URI = "http://localhost:8888/";
    private static final int REQUEST_CODE = 1337;
    private String accessToken;
    private String currentUserUri;
    View mView;

    public enum songMode {
        SEED,
        RECOMMEND,
        SEARCH,
        PROFILE
    }

    public enum postMode{
        PROFILE,
        FEED
    }

    Fragment feedFragment;
    Fragment profileFragment;
    Fragment searchFragment;
    Fragment recommendFragment;
    public static BottomNavigationView bottomNavigationView;
    private static final String[] SCOPES = {"streaming", "playlist-read-private" ,"playlist-modify-public" ,"playlist-modify-private","user-read-private"
                        ,"user-library-read","playlist-read-collaborative","user-top-read"};

    private OkHttpClient okHttpClient = new OkHttpClient();
    public SpotifyAppRemote remote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity", "Main opened!");
        setContentView(R.layout.activity_main);

        ParseObject.registerSubclass(Post.class);

        currentUserUri = getIntent().getStringExtra("uri");
        accessToken = getIntent().getStringExtra("token");
        Log.i(TAG,"TOKEN: " + accessToken);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        feedFragment = new FeedFragment();
        profileFragment = new ProfileFragment();
        searchFragment = new SearchFragment();
        recommendFragment = new RecommendFragment();
        mView = this.getWindow().getDecorView();
        mView.setBackgroundColor(getResources().getColor(R.color.cream));
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        // do something here
                        fragment = feedFragment;
                        break;
                    case R.id.action_profile:
                        // do something here
                        fragment = profileFragment;
                        break;
                    case R.id.action_search:
                        // do something here
                        fragment = searchFragment;
                        break;
                    case R.id.action_recommend:
                        fragment = recommendFragment;
                        break;
                    default:
                        fragment = feedFragment;

                }
                fragmentManager.beginTransaction().replace(R.id.flContainer,fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);
        ((SearchFragment) searchFragment).setToken(accessToken);
        ((ProfileFragment) profileFragment).setToken(accessToken);
        ((RecommendFragment) recommendFragment).setToken(accessToken);

        connectRemote();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG,"onRestart");
        Intent i = new Intent(MainActivity.this, SplashActivity.class);
        startActivity(i);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
        authorizeAccount();
    }

    public void authorizeAccount(){
        // Log in to Spotify Account to receive authorization
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN,REDIRECT_URI);
        builder.setScopes(SCOPES);
        AuthorizationRequest request = builder.build();
        AuthorizationClient.openLoginActivity(this,REQUEST_CODE,request);
    }

    // Result of LoginActivity
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

                    ((SearchFragment) searchFragment).setToken(response.getAccessToken());
                    ((ProfileFragment) profileFragment).setToken(response.getAccessToken());
                    ((RecommendFragment) recommendFragment).setToken(response.getAccessToken());

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
                    currentUserUri = json.getString("uri");

                    String temp = (String) ParseUser.getCurrentUser().get("spotify_uri");
                    String product = json.getString("product");
                    Log.i(TAG, "Spotify: " + currentUserUri + ", App: " + temp);
                    if(!Objects.equals(currentUserUri,ParseUser.getCurrentUser().get("spotify_uri"))){
                        Log.i(TAG,"URIs do not match!");
                        Looper.prepare();
                        Toast.makeText(MainActivity.this,"ERROR: current Spotify user does not match this user. Please try again.",Toast.LENGTH_LONG).show();
                        logOutUser();
                    }
                    if(!Objects.equals(product,"premium")){
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //Update UI
                                showPopUpWindow(mView);
                            }
                        });
                    }

                } catch (JSONException e) {
                    Log.i(TAG,e.toString());
                }
            }

        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.logOut){
            logOutUser();
        }
        else if(item.getItemId() == R.id.player){
            showPlayer();
        }
        return super.onOptionsItemSelected(item);
    }

    public String getAccessToken(){
        return accessToken;
    }

    public String getCurrentUserUri(){
        getUserUri();
        return currentUserUri;
    }

    public void logOutUser(){
        ParseUser.logOut();
        Intent i = new Intent(MainActivity.this,SplashActivity.class);
        startActivity(i);
        finish();
    }

    private void showPopUpWindow(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                MainActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_warning, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setAnimationStyle(R.style.popup_window_animation_alt);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }

    public void connectRemote(){
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(MainActivity.this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        remote = spotifyAppRemote;
                        Log.d(TAG, "Connected! Yay!");

                        // Now you can start interacting with App Remote

                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e(TAG, throwable.getMessage(), throwable);
                        // Something went wrong when attempting to connect! Handle errors here

                        // Occurs when Spotify app not downloaded
                        if(throwable instanceof CouldNotFindSpotifyApp){
                            Toast.makeText(MainActivity.this,"WARNING: Spotify App not downloaded, please download the app and try again.",Toast.LENGTH_LONG).show();
                        }
                        // Occurs when User is not logged in to Spotify App
                        else if(throwable instanceof NotLoggedInException){
                            Toast.makeText(MainActivity.this,"WARNING: User is not logged into Spotify App.",Toast.LENGTH_LONG).show();
                        }
                        // Occurs when User does not give app permission to access Spotify Account
                        else if(throwable instanceof UserNotAuthorizedException){
                            Toast.makeText(MainActivity.this,"WARNING: Spotify User has not authorized permission.",Toast.LENGTH_LONG).show();
                        }
                        // Occurs when app cannot connect to Spotify app
                        else if(throwable instanceof SpotifyConnectionTerminatedException){
                            Toast.makeText(MainActivity.this,"WARNING: Connection to Spotify app has been terminated. Please restart the app and try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void showPlayer(){
        CustomSnackbar customSnackbar = CustomSnackbar.make(findViewById(R.id.activity_main),CustomSnackbar.LENGTH_INDEFINITE,remote);
        customSnackbar.setPlayer();
        customSnackbar.setAction();
        customSnackbar.show();
    }
}