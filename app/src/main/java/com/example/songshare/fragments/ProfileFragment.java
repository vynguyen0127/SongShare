package com.example.songshare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.songshare.R;
import com.example.songshare.adapters.SongAdapter;
import com.example.songshare.models.Song;
import com.parse.ParseUser;

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


public class ProfileFragment extends Fragment {

    private String accessToken;
    private RecyclerView rvTopSongs;
    private TextView tvTitle2;
    private TextView tvUsername;
    private ImageView ivProfile;
    private List<Song> topSongs;
    private SongAdapter adapter;

    private OkHttpClient okHttpClient = new OkHttpClient();

    public static final String TAG = "ProfileFragment";

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG,"profile view created");
        rvTopSongs = view.findViewById(R.id.rvTopSongs);
        tvTitle2 = view.findViewById(R.id.tvTitle2);
        tvUsername = view.findViewById(R.id.tvUsername);

        topSongs = new ArrayList<>();
        adapter = new SongAdapter(getContext(),topSongs,true);

        rvTopSongs.setAdapter(adapter);
        rvTopSongs.setLayoutManager(new GridLayoutManager(getContext(),2));

        tvUsername.setText(ParseUser.getCurrentUser().getUsername());
        tvTitle2.setText("My top songs:");
        makeRequest();
    }

    public void setToken(String token){
        accessToken = token;
        Log.i(TAG, "token: " + accessToken);
    }

    public void makeRequest () {

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
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/me/top/tracks").newBuilder();
        urlBuilder.addQueryParameter("limit","10");
        urlBuilder.addQueryParameter("time_range","short_term");
//        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/me/tracks").newBuilder();
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
                    JSONArray array = new JSONArray(json.getString("items"));

                    if(!topSongs.isEmpty()){
                        topSongs.clear();
                    }
                    topSongs.addAll(Song.fromJsonArray(array));
                    notifyAdapter();


                } catch (JSONException e) {
                    Log.i(TAG,e.toString());
                }
            }

        });
    }

    private void notifyAdapter(){
        Log.i(TAG,"Notifying TopSongsAdapter");
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                //Update UI
                adapter.notifyDataSetChanged();
                rvTopSongs.smoothScrollToPosition(0);
            }
        });
    }
}