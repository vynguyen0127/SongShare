package com.example.songshare;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.songshare.fragments.ComposeFragment;
import com.example.songshare.fragments.FeedFragment;
import com.example.songshare.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("MainActivity", "Main opened!");
        setContentView(R.layout.activity_main);
        ParseObject.registerSubclass(Post.class);

        final FragmentManager fragmentManager = getSupportFragmentManager();

        final Fragment fragment1 = new FeedFragment();
        final Fragment fragment2 = new ComposeFragment();
        final Fragment fragment3 = new ProfileFragment();
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottom_navigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Toast.makeText(MainActivity.this, "Home!",Toast.LENGTH_SHORT).show();
                        // do something here
                        fragment = fragment1;
                        break;
                    case R.id.action_profile:
                        Toast.makeText(MainActivity.this, "Profile!",Toast.LENGTH_SHORT).show();
                        // do something here
                        fragment = fragment2;
                        break;
                    case R.id.action_search:
                        Toast.makeText(MainActivity.this, "Compose!",Toast.LENGTH_SHORT).show();
                        // do something here
                        fragment = fragment3;
                        break;
                    default:
                        fragment = fragment1;

                }
                fragmentManager.beginTransaction().replace(R.id.flContainer,fragment).commit();
                return true;
            }
        });
        bottomNavigationView.setSelectedItemId(R.id.action_home);

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

}