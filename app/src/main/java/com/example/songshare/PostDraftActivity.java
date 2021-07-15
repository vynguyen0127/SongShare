package com.example.songshare;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.songshare.Post.Post;
import com.example.songshare.Song.Song;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

public class PostDraftActivity extends AppCompatActivity {

    public static final String TAG = "PostDraftActivity";
    Song song;

    ImageView ivAlbum;
    TextView tvSongTitle;
    TextView tvArtist;
    Button btnPost;
    EditText etCaption;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_draft);

        song = Parcels.unwrap(getIntent().getParcelableExtra("Song"));

        ivAlbum = findViewById(R.id.ivCover);
        tvSongTitle = findViewById(R.id.tvPlaylistName);
        tvArtist = findViewById(R.id.tvArtist);
        btnPost = findViewById(R.id.btnPost);
        etCaption = findViewById(R.id.etCaption);

        Glide.with(PostDraftActivity.this)
                .load(song.getAlbumUrl())
                .into(ivAlbum);
        tvSongTitle.setText(song.getSongTitle());
        tvArtist.setText(song.getArtistName());
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePost(ParseUser.getCurrentUser());
                InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(etCaption.getWindowToken(), 0);

            }
        });


    }

    private void savePost(ParseUser currentUser){
        Post post = new Post();
        post.setCaption(etCaption.getText().toString());
        post.setUser(currentUser);
        post.setSongTitle(song.getSongTitle());
        post.setArtist(song.getArtistName());
        post.setAlbumUrl(song.getAlbumUrl());
        post.setSongID(song.getSongId());
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.e(TAG, "Error while saving",e);
                    Toast.makeText(PostDraftActivity.this, "Error while saving",Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG,"Post save was successful!" );
                etCaption.setText("");
                Intent i = new Intent(PostDraftActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }


}