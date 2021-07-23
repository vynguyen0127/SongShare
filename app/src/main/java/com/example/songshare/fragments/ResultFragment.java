package com.example.songshare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.adapters.SongAdapter;
import com.example.songshare.models.Song;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ResultFragment extends Fragment {

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
    FragmentManager fragmentManager;

    String artistsString;
    String songsString;
    String genresString;
    String uri;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();
        seed_artists = bundle.getStringArrayList("artists");
        seed_genres = bundle.getStringArrayList("genres");
        seed_songs = bundle.getStringArrayList("songs");
        accessToken = bundle.getString("token");
        uri = bundle.getString("uri");
        fragmentManager = getActivity().getSupportFragmentManager();
        artistsString = arrayToString(seed_artists);
        songsString = arrayToString(seed_songs);
        genresString = arrayToString(seed_genres);

        rvResults = view.findViewById(R.id.rvResults);

        results = new ArrayList<>();
        adapter = new SongAdapter(getContext(),results, MainActivity.songMode.RECOMMEND,ResultFragment.this);
        adapter.setToken(accessToken);
        adapter.setUri(uri);

        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new GridLayoutManager(getContext(),2));

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

    public void goToPostDraftFragment(Song song){
        Fragment postDraftFragment = new PostDraftFragment();
        String backStateName = postDraftFragment.getClass().getName();

        boolean fragmentPopped = fragmentManager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.flContainer, postDraftFragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable("song", Parcels.wrap(song));
        postDraftFragment.setArguments(bundle);

        fragmentManager.beginTransaction().replace(R.id.flContainer,postDraftFragment).commit();
    }

    public void goToPlaylistFragment(Song song){
        Fragment playlistFragment = new PlaylistAddFragment();
        String backStateName = playlistFragment.getClass().getName();

        boolean fragmentPopped = fragmentManager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.flContainer, playlistFragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }

        Bundle bundle = new Bundle();
        bundle.putString("token",accessToken);
        bundle.putParcelable("song", Parcels.wrap(song));
        playlistFragment.setArguments(bundle);

        fragmentManager.beginTransaction().replace(R.id.flContainer,playlistFragment).commit();
    }
}