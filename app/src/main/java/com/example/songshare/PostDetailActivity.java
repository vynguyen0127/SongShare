package com.example.songshare;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.songshare.Playlist.Playlist;
import com.example.songshare.Playlist.PlaylistAdapter;
import com.example.songshare.Post.Post;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostDetailActivity extends AppCompatActivity {

    private Post post;
    private TextView tvSongTitle;
    private TextView tvArtist;
    private ImageView ivAlbum;
    private TextView tvHint;
    private RecyclerView rvPlaylists;
    private List<Playlist> playlists;
    private PlaylistAdapter adapter;
    
    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String accessToken;

    public static final String TAG = "PostDetailActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        post = getIntent().getParcelableExtra("Post");
        accessToken = getIntent().getStringExtra("Token");

        tvSongTitle = findViewById(R.id.tvPlaylistName);
        tvArtist = findViewById(R.id.tvArtist);
        ivAlbum = findViewById(R.id.ivCover);
        rvPlaylists = findViewById(R.id.rvPlaylists);
        tvHint = findViewById(R.id.tvHint);
        
        tvSongTitle.setText(post.getSongTitle());
        tvArtist.setText(post.getArtist());
        Glide.with(PostDetailActivity.this)
                .load(post.getAlbumURL())
                .into(ivAlbum);
        
        playlists = new ArrayList<>();
        adapter = new PlaylistAdapter(PostDetailActivity.this,playlists);
        
        rvPlaylists.setAdapter(adapter);
        rvPlaylists.setLayoutManager(new LinearLayoutManager(PostDetailActivity.this));
        
        makeRequest();
    }

    private void makeRequest() {
        String url = getUrl();

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        call(request);
        
    }

    private String getUrl(){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/me/playlists").newBuilder();

        String url = urlBuilder.build().toString();
        return url;
    }

    private void call(Request request){
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i(TAG,"onFailure");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {
                    String responseData = response.body().string();
                    JSONObject json = new JSONObject(responseData);

                    Log.i(TAG, json.toString());

//                    if(!playlists.isEmpty()){
//                        playlists.clear();
//                    }
//                    playlists.addAll(Playlist.fromJsonArray(array));
//                    notifyAdapter();


                } catch (JSONException e) {
                    Log.i(TAG,e.toString());
                }

            }

        });
    }

    private void notifyAdapter(){
        Log.i(TAG,"Notifying SongAdapter");
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                //Update UI
                adapter.notifyDataSetChanged();
            }
        });
    }
}