package com.example.songshare;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.songshare.adapters.PlaylistAdapter;
import com.example.songshare.models.Playlist;
import com.example.songshare.models.Post;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PlaylistAddActivity extends AppCompatActivity {

    private Post post;
    private TextView tvSongTitle;
    private TextView tvArtist;
    private ImageView ivAlbum;
    private TextView tvHint;
    private RecyclerView rvPlaylists;
    private List<Playlist> playlists;
    private PlaylistAdapter adapter;
    
    private final OkHttpClient okHttpClient = new OkHttpClient();
    private String accessToken;
    private String currentUserUri;

    public static final String TAG = "PlaylistAddActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        post = getIntent().getParcelableExtra("Post");
        accessToken = getIntent().getStringExtra("Token");
        currentUserUri = getIntent().getStringExtra("Uri");

        tvSongTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);
        ivAlbum = findViewById(R.id.ivCover);
        rvPlaylists = findViewById(R.id.rvPlaylists);
        tvHint = findViewById(R.id.tvHint);
        
        tvSongTitle.setText(post.getSongTitle());
        tvArtist.setText(post.getArtist());
        Glide.with(PlaylistAddActivity.this)
                .load(post.getAlbumURL())
                .into(ivAlbum);
        
        playlists = new ArrayList<>();
        adapter = new PlaylistAdapter(PlaylistAddActivity.this,playlists);
        
        rvPlaylists.setAdapter(adapter);
        rvPlaylists.setLayoutManager(new LinearLayoutManager(PlaylistAddActivity.this));
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvPlaylists);
        makeRequest();
    }

    private void makeRequest() {
        String url = getUrl();
        Log.i(TAG, "token:" + accessToken);
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        call(request);
        
    }

    private String getUrl(){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/me/playlists").newBuilder();
        urlBuilder.addQueryParameter("limit","50");
        String url = urlBuilder.build().toString();
        return url;
    }

    private void call(Request request){
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
                    JSONArray array  = new JSONArray(json.get("items").toString());

                    for(int i = 0; i < array.length(); i++){
                        JSONObject object = new JSONObject(array.get(i).toString());
                        JSONObject owner = object.getJSONObject("owner");
                        String uri = owner.getString("uri");
                        String collab = object.getString("collaborative");

                        if(Objects.equals(uri, currentUserUri) || Objects.equals(collab, "true")){
                            playlists.add(new Playlist(object));
                        }
                    }

                    notifyAdapter();


                } catch (JSONException e) {
                    Log.i(TAG,e.toString());
                }

            }

        });
    }

    private void notifyAdapter(){
        Log.i(TAG,"Notifying Playlist");
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                //Update UI
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void addSong(String playlistId, String songUri){

        HttpUrl.Builder urlBuilder = HttpUrl.parse(String.format("https://api.spotify.com/v1/playlists/%s/tracks",playlistId)).newBuilder();
        urlBuilder.addQueryParameter("uris",songUri);
        String url = urlBuilder.build().toString();

        Log.i(TAG,"song: " + songUri + ", url: " +  url);

        final RequestBody body = RequestBody.create("", null);
        final Request.Builder formBody = new Request.Builder().url(url).method("POST",body).header("Authorization", "Bearer " + accessToken);
        okHttpClient.newCall(formBody.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i(TAG, "onFailure");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.i(TAG,response.toString());

            }
        });

    }
    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
            Playlist item = playlists.get(viewHolder.getAdapterPosition());
            addSong(item.getPlaylistId(), post.getSongId());
            finish();
        }
    };
}