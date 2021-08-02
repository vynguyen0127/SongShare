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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.example.songshare.SongComparator;
import com.example.songshare.adapters.SongAdapter;
import com.example.songshare.models.Date;
import com.example.songshare.models.Song;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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
    static private List<Song> songs;
    private SongAdapter adapter;
    private ProgressBar progress;
    FragmentManager fragmentManager;
    String dateLowerParam;
    String dateUpperParam;


    private final OkHttpClient okHttpClient = new OkHttpClient();
    private String accessToken;
    static String lastQuery;
    private Button btnApplyFilters;
    Button btnSortPopular;
    Button btnSortDate;
    Button btnClear;
    EditText etDateLower;
    EditText etDateUpper;
    LinearLayout layoutFilter;
    CheckBox chkExplicit;
    CheckBox chkClean;

    enum Explicit{
        EXPLICIT,
        CLEAN,
        NONE
    }

    Explicit expSearch;
    MainActivity.sortMode sortMode;
    Boolean dateSearch;
    Boolean filterVisible;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        rvResults = view.findViewById(R.id.rvResults);

        btnApplyFilters = view.findViewById(R.id.btnRangeSearch);
        etDateLower = view.findViewById(R.id.etDateLower);
        etDateUpper = view.findViewById(R.id.etDateUpper);
        layoutFilter = view.findViewById(R.id.layoutFilter);

        btnClear = view.findViewById(R.id.btnClear);
        btnSortPopular = view.findViewById(R.id.btnSortPopular);
        btnSortDate = view.findViewById(R.id.btnSortDate);
        chkExplicit = view.findViewById(R.id.chkExplicit);
        chkClean = view.findViewById(R.id.chkClean);

        layoutFilter.setVisibility(View.GONE);

        if(songs == null) {
            songs = new ArrayList<>();
            expSearch = Explicit.NONE;
            dateSearch = false;
            filterVisible = false;
            sortMode = MainActivity.sortMode.NONE;
        }
        else{
            if(dateSearch){
                etDateLower.setText(dateLowerParam);
                etDateUpper.setText(dateUpperParam);
            }
            if(filterVisible){
                layoutFilter.setVisibility(View.VISIBLE);
            }
            switch(expSearch){
                case EXPLICIT:
                    chkExplicit.setChecked(true);
                    break;
                case CLEAN:
                    chkClean.setChecked(true);
                    break;
                default:
                    break;
            }
        }

        adapter = new SongAdapter(getContext(),songs, MainActivity.songMode.SEARCH, SearchFragment.this);
        adapter.setToken(accessToken);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(gridLayoutManager);

        progress = (ProgressBar) view.findViewById(R.id.progress);
        fragmentManager = getActivity().getSupportFragmentManager();

        btnSortPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"sorting by popularity");
                List<Song> temp = sortSongs(MainActivity.sortMode.POPULARITY_REVERSE);
                songs.clear();
                songs.addAll(temp);
                notifyAdapter();
                filterVisible = false;
                layoutFilter.setVisibility(View.GONE);
            }
        });

        btnSortDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"sorting by date");
                List<Song> temp = sortSongs(MainActivity.sortMode.RELEASE);
                songs.clear();
                songs.addAll(temp);
                notifyAdapter();
                filterVisible = false;
                layoutFilter.setVisibility(View.GONE);
            }
        });

        btnApplyFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: check user input
                // if no lower date entered, input all 0
                // if no upper date entered, put some future date
                if(!etDateLower.getText().toString().isEmpty() && !etDateUpper.getText().toString().isEmpty()){
                    dateLowerParam = etDateLower.getText().toString();
                    dateUpperParam = etDateUpper.getText().toString();
                    dateSearch = true;
                }

                if(chkExplicit.isChecked() ^ chkClean.isChecked()){
                    expSearch = chkExplicit.isChecked() ? Explicit.EXPLICIT : Explicit.CLEAN;
                }
                else if(chkExplicit.isChecked() && chkClean.isChecked()){
                    expSearch = Explicit.NONE;
                }

                layoutFilter.setVisibility(View.GONE);
                filterVisible = false;

                if(!songs.isEmpty()){
                    applyFilters();
                    switch (sortMode){
                        case POPULARITY_REVERSE:
                            sortSongs(MainActivity.sortMode.POPULARITY_REVERSE);
                    }
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFilters();
            }
        });

    }

    private void clearFilters() {
        chkClean.setChecked(false);
        chkExplicit.setChecked(false);
        expSearch = Explicit.NONE;

        etDateUpper.setText("");
        etDateLower.setText("");
        dateSearch = false;
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG,"saving items");
        outState.putBoolean("dateSearch",dateSearch);
        if(dateSearch){
            outState.putString("dateLower",dateLowerParam);
            outState.putString("dateUpper",dateUpperParam);
        }
        outState.putBoolean("filterView",filterVisible);
        outState.putSerializable("expSearch",expSearch);

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
                Log.i(TAG,"onQueryTextSubmit");
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if(!s.isEmpty()) {
                    Log.i(TAG, "query: " + s);
                    makeRequest(s);
                }
                return false;
            }
        });

        final MenuItem filterItem = menu.findItem(R.id.filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(filterVisible){
                    Log.i(TAG,"not filters");
                    layoutFilter.setVisibility(View.GONE);
                }
                else{
                    Log.i(TAG,"showing filters");
                    layoutFilter.setVisibility(View.VISIBLE);
                }
                filterVisible = !filterVisible;
                return false;
            }
        });
    }

    public void setToken(String token){
        accessToken = token;
        Log.i(TAG, "token: " + accessToken);
    }

    private void makeRequest (String query) {

        progress.setVisibility(ProgressBar.VISIBLE);

        accessToken = ((MainActivity)this.getActivity()).getAccessTokenSpotify();

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

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG,"onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"onDestroy");
    }

    private String getUrl(String query){
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.spotify.com/v1/search").newBuilder();
        urlBuilder.addQueryParameter("q", query);
        urlBuilder.addQueryParameter("limit","50");
        urlBuilder.addQueryParameter("type", "track");
        return urlBuilder.build().toString();
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
//                    notifyAdapter();

                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Update UI
                            progress.setVisibility(ProgressBar.GONE);
                        }
                    });

                    applyFilters();
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

    private void applyFilters() {
        List<HashSet<Song>> filtered = new ArrayList<>();

        if(dateSearch){
            filtered.add(dateRangeSearch(dateLowerParam,dateUpperParam));
        }
        switch (expSearch){
            case NONE:
                break;
            case CLEAN:
                filtered.add(explicitSearch(Explicit.CLEAN));
                break;
            case EXPLICIT:
                filtered.add(explicitSearch(Explicit.EXPLICIT));
                break;
        }

        int size = filtered.size();
        switch (size){
            case 0:
                Log.i(TAG,"no filters");
                break;
            case 1:
                Log.i(TAG,"1 filter");
                songs.clear();
                for(Song song : filtered.get(0)){
                    songs.add(song);
                }
                break;
            default:
                Log.i(TAG,"do set intersection");
                songs.clear();

                // do set intersection between all filtered sets
                HashSet<Song> intersection = new HashSet<>();
                intersection.addAll(filtered.get(0));

                for(int i = 1; i < size; i++){
                    intersection.retainAll(filtered.get(i));
                }

                for(Song song:intersection){
                    songs.add(song);
                }
                break;
        }
        notifyAdapter();
        printSongs(songs);
    }

    private HashSet<Song> dateRangeSearch(String dateLower, String dateUpper) {
        // make dummy Song objects to hold dates
        Song song_lb = new Song();
        Date d1 = new Date(dateLower);
        song_lb.setDate(d1);

        Song song_ub = new Song();
        Date d2 = new Date(dateUpper);
        song_ub.setDate(d2);

        // sort the songs from oldest to most recent & create copy
        List<Song> temp = sortSongs(MainActivity.sortMode.RELEASE);

        SongComparator comparator = new SongComparator(MainActivity.sortMode.RELEASE);

        // search for each date
        int index_lower = lowerBound(temp,song_lb,comparator);
        int index_upper = upperBound(temp,song_ub,comparator);

        Log.i(TAG, "lower: " + index_lower + ", upper: " + index_upper);

        HashSet<Song> set = new HashSet<>();
        // populate list with new results
        for(int i =  index_lower; i < index_upper; i++){
            Song song = temp.get(i);
            set.add(song);
        }

        return set;
    }

    private HashSet<Song> explicitSearch(Explicit mode){
        HashSet<Song> set = new HashSet<>();

        if(mode == Explicit.EXPLICIT) {
            for (Song s : songs) {
                if (s.getExplicit()) {
                    set.add(s);
                }
            }
        }
        else if(mode == Explicit.CLEAN){
            for (Song s : songs) {
                if (!s.getExplicit()) {
                    set.add(s);
                }
            }
        }

        return set;
    }

    // returns an index to the smallest value greater than or equal to the target
    private int upperBound(List<Song> songs, Song target, Comparator comparator) {

        int mid;
        int n = songs.size() - 1;

        int low = 0;
        int high = n;

        while(low < high){
            mid = low + (high - low) / 2;

            // if target >= songs[mid]
            if(comparator.compare(target,songs.get(mid)) >= 0){
                low = mid + 1;
            }
            else{
                high = mid;
            }
        }

        if(low < n && (comparator.compare(songs.get(low),target) <= 0)){
            low++;
        }

        return low;
    }

    // returns an index to the greatest value less than or equal to the target
    private int lowerBound(List<Song> songs, Song target, Comparator comparator) {
        int mid;
        int n = songs.size() - 1;

        int low  = 0;
        int high = n;

        while(low < high){
            mid = low + (high - low) / 2;

            // if target <= songs[mid]
            if(comparator.compare(target,songs.get(mid)) <= 0){
                high = mid;
            }
            else{
                low = mid + 1;
            }
        }
        if(low < n && (comparator.compare(songs.get(low),target) < 0)){
            low++;
        }

        return low;
    }

    private List<Song> sortSongs(MainActivity.sortMode mode) {
        SongComparator comparator = new SongComparator(mode);
        List<Song> temp = new ArrayList<>();
        temp.addAll(songs);
        Collections.sort(temp, comparator);
        Log.i(TAG, "new order: ");
        printSongs(temp);
        return temp;
    }

    public void printSongs(List<Song> list){
        int i = 0;
        for(Song song: list){
            Log.i("Song",
                    i + ". Date: " + song.getReleaseDate() +
                    ", Popularity: " + song.getPopularity() +
                            ", Explicit: " + (song.getExplicit()? " Yes " : " No") +
                    ", Album: " + song.getAlbumUrl() +
                    ", Artist: " + song.getArtist() +
                    ", Song: " + song.getSongTitle() +
                            ", URI: " + song.getSongUri());
            i++;
        }
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

    public void reset(){
        if(songs != null) {
            if(!songs.isEmpty()) {
                songs.clear();
                songs = null;
                clearFilters();
                layoutFilter.setVisibility(View.GONE);
            }
        }
    }

}