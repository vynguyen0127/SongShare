package com.example.songshare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.Song.Song;
import com.example.songshare.Song.SongAdapter;

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
    private String accessToken;

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
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.connectRemote();
    }

    @Override
    public void onStop() {
        super.onStop();
        etSearch.setText("");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.disconnectRemote();
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
        rvResults.setLayoutManager(new GridLayoutManager(getContext(),2));
//        rvResults.setLayoutManager(new LinearLayoutManager(getContext()));

        ibSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Search clicked!");
                makeRequest();
            }
        });

        etSearch.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    makeRequest();

                    return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {

        inflater.inflate(R.menu.menu_main,menu);

        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

//        if(item.getItemId() == R.id.logOut){
//            ParseUser.logOut();
//            ParseUser currentUser = ParseUser.getCurrentUser();
//            Intent i = new Intent(getContext(), LoginActivity.class);
//            startActivity(i);
//            finish();
//        }
        return super.onOptionsItemSelected(item);
    }
    private void makeRequest () {
        // make keyboard disappear
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

        String query = etSearch.getText().toString();

        accessToken = ((MainActivity)this.getActivity()).getAccessToken();

        if (accessToken == null) {
            Log.e(TAG,"no token");
            return;
        }

        String url = getUrl(query);

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        call(request);

    }
    private String getUrl(String query){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/search").newBuilder();
        urlBuilder.addQueryParameter("q", query);
        urlBuilder.addQueryParameter("type", "track,artists");
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
                    JSONObject json2 = new JSONObject(json.getString("tracks"));

                    JSONArray array = new JSONArray(json2.getString("items"));

                    if(!songs.isEmpty()){
                        songs.clear();
                    }
                    songs.addAll(Song.fromJsonArray(array));
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
            }
        });
    }





}