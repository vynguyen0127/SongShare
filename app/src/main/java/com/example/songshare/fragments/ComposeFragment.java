package com.example.songshare.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.Song;
import com.example.songshare.SongAdapter;

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

public class ComposeFragment extends Fragment {

    public static final String TAG = "ComposeFragment";
    EditText etSearch;
    ImageButton ibSearch;
    RecyclerView rvResults;
    List<Song> songs;
    SongAdapter adapter;

    private final OkHttpClient mOkHttpClient = new OkHttpClient();
    private String mAccessToken;

    public ComposeFragment() {
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
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        etSearch = view.findViewById(R.id.etSearch);
        ibSearch = view.findViewById(R.id.ibSearch);
        rvResults = view.findViewById(R.id.rvResults);

        songs = new ArrayList<>();
        adapter = new SongAdapter(getContext(),songs);

        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));

        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Search clicked!");
                makeRequest(etSearch.getText().toString());
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.disconnect();
    }

    public void makeRequest (String query) {
        mAccessToken = ((MainActivity)this.getActivity()).getAccessToken();

        if (mAccessToken == null) {
            Log.e(TAG,"no token");
            return;
        }

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/search").newBuilder();
        urlBuilder.addQueryParameter("q", query);
        urlBuilder.addQueryParameter("type", "track");
        String url = urlBuilder.build().toString();

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + mAccessToken)
                .build();

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
                    JSONObject json2 = new JSONObject(json.getString("tracks"));

                    JSONArray array = new JSONArray(json2.getString("items"));

                    songs.addAll(Song.fromJsonArray(array));
//                    adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.i(TAG,e.toString());
                }

            }

        });

    }


}