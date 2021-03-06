package com.example.songshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    public static final String TAG = "LoginActivity";
    EditText etUsername;
    EditText etPassword;
    Button btnLogin;
    Button btnSignUp;
    String uri;
    String display_name;
    String profile_url;
    String id;
    String access_token;
    ImageView ivLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if(ParseUser.getCurrentUser() != null){
            Log.i(TAG,"Logging in previous user");
            goMainActivity();
        }

        uri = getIntent().getStringExtra("uri");
        display_name = getIntent().getStringExtra("display_name");
        profile_url = getIntent().getStringExtra("profile_url");
        id = getIntent().getStringExtra("id");
        access_token = getIntent().getStringExtra("token");
        Log.i(TAG,"uri: " + uri);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignUp = findViewById(R.id.btnSignUp);
        ivLogo = findViewById(R.id.ivLogo);

        Glide.with(LoginActivity.this)
                .load(R.drawable.logo)
                .into(ivLogo);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        View view = this.getWindow().getDecorView();
        view.setBackgroundColor(getResources().getColor(R.color.light_blue));
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"onClick Login");
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                loginUser(username,password);
                // do account check
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                Log.i(TAG,"sending uri to signup: " + uri);
                i.putExtra("uri",uri);
                i.putExtra("display_name",display_name);
                i.putExtra("profile_url",profile_url);
                i.putExtra("id",id);
                i.putExtra("token",access_token);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG,"onRestart");
        Intent i = new Intent(LoginActivity.this, SplashActivity.class);
        startActivity(i);
    }

    private void loginUser(String username, String password) {
        Log.i(TAG,"Attempting to log in user: " + username);
        // navigate to main activity if user has signed in properly
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e != null){
                    Log.e(TAG, "Issue with login", e);
                    Toast.makeText(LoginActivity.this,"Error logging in: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(Objects.equals(uri, ParseUser.getCurrentUser().get("spotify_uri").toString())) {
                    goMainActivity();
                    Toast.makeText(LoginActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(LoginActivity.this, "Uri: " + uri + " ,Spotify: " + ParseUser.getCurrentUser().get("spotify_uri"), Toast.LENGTH_SHORT).show();
                    ParseUser.logOut();
                }
            }
        });
    }

    private void goMainActivity(){
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("token",access_token);
        i.putExtra("uri",uri);
        startActivity(i);
        finish();
    }
}