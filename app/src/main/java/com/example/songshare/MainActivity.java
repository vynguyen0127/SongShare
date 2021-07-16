package com.example.songshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.songshare.fragments.FeedFragment;
import com.example.songshare.fragments.ProfileFragment;
import com.example.songshare.fragments.SearchFragment;
import com.example.songshare.models.Post;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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

    private static final String[] SCOPES = {"streaming", "playlist-read-private" ,"playlist-modify-public" ,"playlist-modify-private","user-read-private","user-library-read"
                        ,"user-library-read","playlist-read-collaborative"};
    private OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity", "Main opened!");
        setContentView(R.layout.activity_main);

        ParseObject.registerSubclass(Post.class);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        Fragment feedFragment = new FeedFragment();
        Fragment profileFragment = new ProfileFragment();
        Fragment composeFragment = new SearchFragment();

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
                        fragment = composeFragment;
                        break;
                    default:
                        fragment = feedFragment;

                }
                fragmentManager.beginTransaction().replace(R.id.flContainer,fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(getResources().getColor(R.color.cream));

        authorizeAccount();

    }


    public void authorizeAccount(){
        // Log in to Spotify Account to receive authorization
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN,REDIRECT_URI);
        builder.setScopes(SCOPES); // need to add additional scopes to modify user's playlists
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
            ParseUser.logOut();
            ParseUser currentUser = ParseUser.getCurrentUser();
            Intent i = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public String getAccessToken(){
        return accessToken;
    }

    public String getCurrentUserUri(){return currentUserUri;}


}