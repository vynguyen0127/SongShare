package com.example.songshare;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.songshare.adapters.SongAdapter;
import com.example.songshare.models.Song;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
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

public class RecommendActivity extends AppCompatActivity {

    public static final String TAG = "RecommendActivity";
    private String accessToken;
    private ProgressBar progress;
    OkHttpClient okHttpClient = new OkHttpClient();
    SongAdapter adapter;
    List<Song> results;
    RecyclerView rvResults;

    ArrayList<String> seed_artists;
    ArrayList<String> seed_songs;
    ArrayList<String> seed_genres;

    String artistsString;
    String songsString;
    String genresString;
    String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        seed_artists = getIntent().getStringArrayListExtra("artists");
        seed_genres = getIntent().getStringArrayListExtra("genres");
        seed_songs = getIntent().getStringArrayListExtra("songs");
        accessToken = getIntent().getStringExtra("token");
        uri = getIntent().getStringExtra("uri");

        artistsString = arrayToString(seed_artists);
        songsString = arrayToString(seed_songs);
        genresString = arrayToString(seed_genres);

        rvResults = findViewById(R.id.rvResults);

        results = new ArrayList<>();
        adapter = new SongAdapter(RecommendActivity.this,results, MainActivity.songMode.RECOMMEND);
        adapter.setToken(accessToken);
        adapter.setUri(uri);

        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new GridLayoutManager(RecommendActivity.this,2));

        makeRequest();
    }

    private String arrayToString(ArrayList<String> seed) {
        String s = "";
        for(int i = 0; i < seed.size(); i++){
            if(i == 0) {
                s += seed.get(i);
            }
            else {
                s += "," + seed.get(i);
            }
        }

        return s;
    }

    private void makeRequest () {

        if (accessToken == null) {
            Log.e(TAG,"no token");
            return;
        }

        String url = getUrl();

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        call(request);

    }

    private String getUrl(){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/recommendations").newBuilder();
        urlBuilder.addQueryParameter("seed_artists", artistsString);
        urlBuilder.addQueryParameter("seed_songs",songsString);
        urlBuilder.addQueryParameter("seed_genres", genresString);
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
                    JSONObject jsonObject = new JSONObject(responseData.toString());
                    JSONArray tracks = new JSONArray(jsonObject.getString("tracks"));
                    Log.i(TAG,"onResponse: " + tracks.toString());

                    results.addAll(Song.fromJsonArray(tracks));
                    notifyAdapter();

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
                rvResults.smoothScrollToPosition(0);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_post_detail,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.exit){
            Intent i = new Intent(RecommendActivity.this,MainActivity.class);
            startActivity(i);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}