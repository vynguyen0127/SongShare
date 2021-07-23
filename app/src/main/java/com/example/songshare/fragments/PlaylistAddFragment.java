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
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.songshare.R;
import com.example.songshare.adapters.PlaylistAdapter;
import com.example.songshare.models.Playlist;
import com.example.songshare.models.Song;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

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

public class PlaylistAddFragment extends Fragment {

    private Song song;
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

    public static final String TAG = "PlaylistAddFragment";

    public PlaylistAddFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_playlist_add, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();

        song = Parcels.unwrap(bundle.getParcelable("song"));
        accessToken = bundle.getString("token");
        currentUserUri = (String) ParseUser.getCurrentUser().get("spotify_uri");

        tvSongTitle = view.findViewById(R.id.tvTitle);
        tvArtist = view.findViewById(R.id.tvArtist);
        ivAlbum = view.findViewById(R.id.ivCover);
        rvPlaylists = view.findViewById(R.id.rvPlaylists);
        tvHint = view.findViewById(R.id.tvHint);

        tvSongTitle.setText(song.getSongTitle());
        tvArtist.setText(song.getArtistName());
        Glide.with(getContext())
                .load(song.getAlbumUrl())
                .into(ivAlbum);

        playlists = new ArrayList<>();
        adapter = new PlaylistAdapter(getContext(),playlists,PlaylistAddFragment.this);

        rvPlaylists.setAdapter(adapter);
        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));

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
//        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/me/playlists").newBuilder();
        String temp = (String) ParseUser.getCurrentUser().get("spotify_id");
        HttpUrl.Builder urlBuilder = HttpUrl.parse(String.format("https://api.spotify.com/v1/users/%s/playlists",temp)).newBuilder();
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

                    Log.i(TAG,array.toString());
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

    public void addSong(String playlistId){

        String songUri = song.getSongUri();

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

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();

    }

}