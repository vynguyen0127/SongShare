package com.example.songshare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.PopupWindow;
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
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class SearchFragment extends Fragment {


    public static final String TAG = "SearchFragment";
    private RecyclerView rvResults;
    private List<Song> songs;
    private SongAdapter adapter;
    private ProgressBar progress;
    FragmentManager fragmentManager;
    String dateLowerParam;
    String dateUpperParam;


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

        final MenuItem filterItem = menu.findItem(R.id.filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                showPopUpWindow(getView());
                return false;
            }
        });
    }

    public void setToken(String token){
        accessToken = token;
        Log.i(TAG, "token: " + accessToken);
    }

    private void makeRequest (String query, Boolean search) {

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

    private void showPopUpWindow(View view) {
        Button btnSortPopular;
        Button btnSortDate;
        Button btnRangeSearch;
        EditText etDateLower;
        EditText etDateUpper;
        CheckBox chkExplicit;
        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                ((MainActivity)this.getActivity()).getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_filter, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setAnimationStyle(R.style.popup_window_animation_phone);
        btnSortPopular = popupView.findViewById(R.id.btnSortPopular);
        btnSortDate = popupView.findViewById(R.id.btnSortDate);
        btnRangeSearch = popupView.findViewById(R.id.btnRangeSearch);
        etDateLower = popupView.findViewById(R.id.etDateLower);
        etDateUpper = popupView.findViewById(R.id.etDateLower);
        chkExplicit = popupView.findViewById(R.id.chkExplicit);

        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        btnSortPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"sorting by popularity");
                sortSongs(MainActivity.sortMode.POPULARITY_REVERSE);
                notifyAdapter();
                popupWindow.dismiss();
            }
        });

        btnSortDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"sorting by date");
                sortSongs(MainActivity.sortMode.RELEASE);
                notifyAdapter();
                popupWindow.dismiss();
            }
        });

        btnRangeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: check user input
                // if no lower date entered, input all 0
                // if no upper date entered, put some future date

                dateRangeSearch(etDateLower.getText().toString(),etDateUpper.getText().toString());
                popupWindow.dismiss();
            }
        });


    }

    private void dateRangeSearch(String dateLower, String dateUpper) {
        // make dummy Song objects to hold dates
        Song song_lb = new Song();
        Date d1 = new Date(dateLower);
        song_lb.setDate(d1);

        Song song_ub = new Song();
        Date d2 = new Date(dateUpper);
        song_ub.setDate(d2);

        // sort the songs from oldest to most recent
        sortSongs(MainActivity.sortMode.RELEASE);

        SongComparator comparator = new SongComparator(MainActivity.sortMode.RELEASE);

        // search for each date
        int index_lower = lowerBound(songs,song_lb,comparator);
        int index_upper = upperBound(songs,song_ub,comparator);

        Log.i(TAG, "lower: " + index_lower + ", upper: " + index_upper);

        List<Song> temp = new ArrayList<>();

        // populate list with new results
        for(int i =  index_lower; i < index_upper; i++){
            Song song = songs.get(i);
            temp.add(song);
        }

        songs.clear();
        for(Song s: temp){
            songs.add(s);
        }

        notifyAdapter();

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
        List<Song> temp = songs;
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
                    ", Album: " + song.getAlbumUrl() +
                    ", Artist: " + song.getArtist() +
                    ", Song: " + song.getSongTitle() +
                            ", URI: " + song.getSongUri());
            i++;
        }
    }

}