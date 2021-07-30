package com.example.songshare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.songshare.MainActivity;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class PlaylistAddFragment extends Fragment {

    private Song song;
    private TextView tvSongTitle;
    private TextView tvArtist;
    private ImageView ivAlbum;
    private Button btnCreate;
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
        btnCreate = view.findViewById(R.id.btnCreate);
        rvPlaylists = view.findViewById(R.id.rvPlaylists);

        tvSongTitle.setText(song.getSongTitle());
        tvArtist.setText(song.getArtist());
        Glide.with(getContext())
                .load(song.getAlbumUrl())
                .into(ivAlbum);

        playlists = new ArrayList<>();
        adapter = new PlaylistAdapter(getContext(),playlists,PlaylistAddFragment.this);

        rvPlaylists.setAdapter(adapter);
        rvPlaylists.setLayoutManager(new LinearLayoutManager(getContext()));

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopUpWindow(v);
            }
        });
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

    public void addSong(String playlistId, boolean b){
        String songUri = song.getSongUri();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(String.format("https://api.spotify.com/v1/playlists/%s/tracks",playlistId)).newBuilder();
        urlBuilder.addQueryParameter("uris",songUri);
        String url = urlBuilder.build().toString();

        Log.i(TAG,"song: " + songUri + ", url: " +  url);

        final RequestBody body = RequestBody.create("", null);
        final Request.Builder request = new Request.Builder()
                .url(url)
                .method("POST",body)
                .header("Authorization", "Bearer " + accessToken);

        okHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i(TAG, "onFailure");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                Log.i(TAG,response.toString());

                // TODO: Toast saying song added to playlist


            }
        });

        if(b) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStackImmediate();
        }
    }

    private void createPlaylist(String name, String description, String playlistPublic){

        String id = ((MainActivity) getActivity()).getUserId();
        HttpUrl.Builder urlBuilder = HttpUrl.parse(String.format("https://api.spotify.com/v1/users/%s/playlists",id)).newBuilder();
        String url = urlBuilder.build().toString();

        Log.i(TAG,"id: " + id + ", url: " +  url);
        String json = String.format("{\"name\":\"%s\",\"description\":\"%s\",\"public\":\"%s\"}",name,description,playlistPublic);

        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        final Request.Builder request = new Request.Builder()
                .url(url)
                .method("POST",body)
                .header("Authorization", "Bearer " + accessToken);


        okHttpClient.newCall(request.build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i(TAG, "onFailure");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                String data = responseBody.string();
                try {
                    JSONObject json = new JSONObject(data);
                    String playlistId = json.getString("id");
                    addSong(playlistId,false);
                }catch (JSONException e){
                    e.printStackTrace();
                }

            }
        });
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();
    }

    private void showPopUpWindow(View view) {

        EditText etPlaylist;
        EditText etPlaylistDesc;
        Switch switchPublic;
        Button btnAdd;

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                ((MainActivity)this.getActivity()).getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_make_playlist, null);

        etPlaylist = popupView.findViewById(R.id.etPlaylist);
        etPlaylistDesc = popupView.findViewById(R.id.etPlaylistDesc);
        switchPublic = popupView.findViewById(R.id.switchPublic);
        btnAdd = popupView.findViewById(R.id.btnAdd);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setAnimationStyle(R.style.popup_window_animation_phone);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = etPlaylist.getText().toString();
                
                String desc = etPlaylistDesc.getText().toString();
                String playlistPublic = switchPublic.isChecked() ? "true" : "false";

                createPlaylist(name,desc, playlistPublic);
                popupWindow.dismiss();
            }
        });


    }

}