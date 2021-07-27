package com.example.songshare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
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

public class SearchFragment extends Fragment {

    public static final String TAG = "SearchFragment";
    private RecyclerView rvResults;
    private List<Song> songs;
    private SongAdapter adapter;
    private ProgressBar progress;
    FragmentManager fragmentManager;

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private String accessToken;
    static String lastQuery;

    public SearchFragment() {
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
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.i(TAG,"onResume");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG,"onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);
        if(lastQuery != null){
            Log.i(TAG,"doing previous search");
            makeRequest(lastQuery,true);
        }
        rvResults = view.findViewById(R.id.rvResults);

        songs = new ArrayList<>();
        adapter = new SongAdapter(getContext(),songs, MainActivity.songMode.SEARCH, SearchFragment.this);
        adapter.setToken(accessToken);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(gridLayoutManager);

        progress = (ProgressBar) view.findViewById(R.id.progress);
        fragmentManager = getActivity().getSupportFragmentManager();
    }


    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_search, menu);
        final MenuItem searchItem = menu.findItem(R.id.app_bar_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Fetch the data remotely
//                makeRequest(query,true);
                lastQuery = query;
                // Reset SearchView
                searchView.clearFocus();
                searchView.setQuery("", false);
                searchView.setIconified(true);
                searchItem.collapseActionView();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Update UI
                        progress.setVisibility(ProgressBar.GONE);
                    }
                });

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(s != null) {
                    Log.i(TAG, "text change, new request");
                    makeRequest(s, true);
                    lastQuery = s;
                }else{
                    songs.clear();
                    notifyAdapter();
                }
                return true;
            }
        });

    }

    public void setToken(String token){
        accessToken = token;
        Log.i(TAG, "token: " + accessToken);
    }

    private void makeRequest (String query, Boolean search) {

        progress.setVisibility(ProgressBar.VISIBLE);

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
        urlBuilder.addQueryParameter("limit","50");
        urlBuilder.addQueryParameter("type", "track");
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
                    JSONArray array;

                    JSONObject json2 = new JSONObject(json.getString("tracks"));
                    array = new JSONArray(json2.getString("items"));

                    if(!songs.isEmpty()){
                        songs.clear();
                    }
                    songs.addAll(Song.fromJsonArray(array));
                    notifyAdapter();

                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Update UI
                            progress.setVisibility(ProgressBar.GONE);
                        }
                    });


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

    public void goToFragment(Song song, Fragment fragment){
        Fragment destFragment = fragment;
        String backStateName = destFragment.getClass().getName();

        boolean fragmentPopped = fragmentManager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.flContainer, destFragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }

        Bundle bundle = new Bundle();
        bundle.putParcelable("song", Parcels.wrap(song));
        bundle.putString("token",accessToken);
        destFragment.setArguments(bundle);

        fragmentManager.beginTransaction().replace(R.id.flContainer,destFragment).commit();
    }


}