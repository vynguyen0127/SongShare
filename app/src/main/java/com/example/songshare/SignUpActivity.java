package com.example.songshare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import org.json.JSONArray;

import java.util.Objects;

public class SignUpActivity extends AppCompatActivity {

    EditText etUsername;
    EditText etPassword;
    Button btnSignUp;

    ImageView ivProfile;
    TextView tvUsername;
    Button btnYes;
    Button btnNo;

    public static final String TAG = "SignUpActivity";
    String uri;
    String id;
    String profile_url;
    String display_name;
    String access_token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        uri = getIntent().getStringExtra("uri");
        display_name = getIntent().getStringExtra("display_name");
        profile_url = getIntent().getStringExtra("profile_url");
        id = getIntent().getStringExtra("id");
        access_token = getIntent().getStringExtra("token");

        Log.i(TAG,"uri: " + uri);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);
                showPopUpWindow(v);
            }
        });

        etPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG,"onRestart");
        Intent i = new Intent(SignUpActivity.this, SplashActivity.class);
        startActivity(i);
    }

    private void showPopUpWindow(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                SignUpActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_verify, null);

        ivProfile = popupView.findViewById(R.id.ivProfile);
        tvUsername = popupView.findViewById(R.id.tvUsername);

        Glide.with(getApplicationContext())
                .load((Objects.equals(profile_url,"")) ? R.drawable.ic_baseline_account_circle_24 : profile_url)
                .circleCrop()
                .into(ivProfile);

        tvUsername.setText(display_name);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setAnimationStyle(R.style.popup_window_animation_alt);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        btnYes = popupView.findViewById(R.id.btnYes);
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
                popupWindow.dismiss();
                Log.i(TAG, "go to main activity");
                Intent i = new Intent(SignUpActivity.this, MainActivity.class);
                i.putExtra("uri",uri);
                i.putExtra("token",access_token);
                startActivity(i);
                finish();
            }
        });

        btnNo = popupView.findViewById(R.id.btnNo);
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    private void signUpUser() {
        ParseUser user = new ParseUser();
        user.setUsername(etUsername.getText().toString());
        user.setPassword(etPassword.getText().toString());
        user.put("liked_posts",new JSONArray());
        user.put("spotify_uri",uri);
        user.put("spotify_id",id);
        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Intent i = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                } else{
                    Log.e(TAG,"error signing up");
                }
            }
        });
    }
}