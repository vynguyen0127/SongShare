package com.example.songshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.songshare.Post.Post;
import com.example.songshare.fragments.ComposeFragment;
import com.example.songshare.fragments.FeedFragment;
import com.example.songshare.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient;
import com.spotify.sdk.android.auth.AuthorizationRequest;
import com.spotify.sdk.android.auth.AuthorizationResponse;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    private static final String CLIENT_ID = "1cb8dc3da6564e51af249a98d3d0eba1";
    private static final String REDIRECT_URI = "http://localhost:8888/";
    private static final int REQUEST_CODE = 1337;
    private String accessToken;
    static SpotifyAppRemote remote;

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
        Fragment composeFragment = new ComposeFragment();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_home:
//                        Toast.makeText(MainActivity.this, "Home!",Toast.LENGTH_SHORT).show();
                        // do something here
                        fragment = feedFragment;
                        break;
                    case R.id.action_profile:
//                        Toast.makeText(MainActivity.this, "Profile!",Toast.LENGTH_SHORT).show();
                        // do something here
                        fragment = profileFragment;
                        break;
                    case R.id.action_search:
//                        Toast.makeText(MainActivity.this, "Compose!",Toast.LENGTH_SHORT).show();
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

        authorizeAccount();

    }


    public void authorizeAccount(){
        // Log in to Spotify Account to receive authorization
        AuthorizationRequest.Builder builder = new AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN,REDIRECT_URI);
        builder.setScopes(new String[]{"streaming"}); // need to add additional scopes to modify user's playlists
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


}