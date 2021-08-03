package com.example.songshare.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
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

public class SearchFragment extends Fragment {


    public static final String TAG = "SearchFragment";
    private RecyclerView rvResults;
    static private List<Song> songs;
    private SongAdapter adapter;
    private ProgressBar progress;
    FragmentManager fragmentManager;
    String dateLowerParam;
    String dateUpperParam;
    String popLowerParam;
    String popUpperParam;
    String lastQuery;

    private final OkHttpClient okHttpClient = new OkHttpClient();
    private String accessToken;
    private Button btnApplyFilters;
    Button btnSortPopular;
    Button btnSortDate;
    Button btnClear;
    ImageButton ibClose;
    EditText etDateLower;
    EditText etDateUpper;
    EditText etPopLower;
    EditText etPopUpper;
    LinearLayout layoutFilter;
    CardView cvFilter;
    TextView tvFail;

    ToggleButton swClean;

    Boolean cleanSearch;
    MainActivity.sortMode sortMode;
    Boolean dateSearch;
    Boolean filterVisible;
    Boolean popSearch;

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

        cvFilter = view.findViewById(R.id.cvFilter);
        btnApplyFilters = view.findViewById(R.id.btnRangeSearch);
        etDateLower = view.findViewById(R.id.etDateLower);
        etDateUpper = view.findViewById(R.id.etDateUpper);
        etPopLower = view.findViewById(R.id.etPopLower);
        etPopUpper = view.findViewById(R.id.etPopUpper);
        swClean = view.findViewById(R.id.swClean);
        btnClear = view.findViewById(R.id.btnClear);
        btnSortPopular = view.findViewById(R.id.btnSortPopular);
        btnSortDate = view.findViewById(R.id.btnSortDate);
        ibClose = view.findViewById(R.id.ibClose);
        tvFail = view.findViewById(R.id.tvFail);
        tvFail.setVisibility(View.GONE);

        ibClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adjustFilterView();
            }
        });


        if(songs == null) {
            songs = new ArrayList<>();
            cleanSearch = false;
            dateSearch = false;
            filterVisible = false;
            popSearch = false;
            sortMode = MainActivity.sortMode.NONE;
        }
        else{
            if(dateSearch){
                etDateLower.setText(dateLowerParam);
                etDateUpper.setText(dateUpperParam);
            }
            if(popSearch){
                etPopLower.setText(popLowerParam);
                etPopUpper.setText(popUpperParam);
            }
            if(filterVisible){
                layoutFilter.setVisibility(View.VISIBLE);
            }
            if(cleanSearch){
                swClean.setChecked(true);
            }
        }

        adapter = new SongAdapter(getContext(),songs, MainActivity.songMode.SEARCH, SearchFragment.this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(),2);
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(gridLayoutManager);

        progress = (ProgressBar) view.findViewById(R.id.progress);
        fragmentManager = getActivity().getSupportFragmentManager();

        btnSortPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"sorting by popularity");
                List<Song> temp = sortSongs(songs,MainActivity.sortMode.POPULARITY);
                sortMode = MainActivity.sortMode.POPULARITY;
                songs.clear();
                songs.addAll(temp);
                notifyAdapter();
                filterVisible = false;
                cvFilter.setVisibility(View.GONE);
            }
        });

        btnSortDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"sorting by date");
                List<Song> temp = sortSongs(songs, MainActivity.sortMode.RELEASE);
                sortMode = MainActivity.sortMode.RELEASE;
                songs.clear();
                songs.addAll(temp);
                notifyAdapter();
                filterVisible = false;
                cvFilter.setVisibility(View.GONE);
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
                }else{
                    dateSearch = false;
                }

                // TODO: check user input
                // if no lower param, set to 0
                // if no upper param, set to 100
                if(!etPopLower.getText().toString().isEmpty() && !etPopUpper.getText().toString().isEmpty()){
                    popLowerParam = etPopLower.getText().toString();
                    popUpperParam = etPopUpper.getText().toString();
                    popSearch = true;
                }else{
                    popSearch = false;
                }

                if(swClean.isChecked()){
                    cleanSearch = true;
                }else{
                    cleanSearch = false;
                }

                cvFilter.setVisibility(View.GONE);
                filterVisible = false;


                makeRequest(lastQuery);


                hideKeyboard();
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFilters(true);
            }
        });

    }

    private void clearFilters(boolean b) {
        cleanSearch = false;
        swClean.setChecked(false);

        etDateUpper.setText("");
        etDateLower.setText("");

        etPopUpper.setText("");
        etPopLower.setText("");
        dateSearch = false;
        popSearch = false;

        sortMode = MainActivity.sortMode.NONE;
        if(b){
            adjustFilterView();
            makeRequest(lastQuery);
            hideKeyboard();
        }
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
                lastQuery = query;
                // Reset SearchView
                searchView.clearFocus();
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
                    lastQuery = s;
                }
                return false;
            }
        });

        final MenuItem filterItem = menu.findItem(R.id.filter);
        filterItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                adjustFilterView();
                return false;
            }
        });

        final MenuItem clearItem = menu.findItem(R.id.clear);
        clearItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                songs.clear();
                clearFilters(false);
                searchView.clearFocus();
                searchView.setQuery("", false);
                searchView.setIconified(true);
                searchItem.collapseActionView();
                notifyAdapter();
                return false;
            }
        });
    }

    private void adjustFilterView() {
        if(filterVisible){
            Log.i(TAG,"not filters");
            TransitionManager.beginDelayedTransition(cvFilter,
                    new AutoTransition());
            cvFilter.setVisibility(View.GONE);
            rvResults.smoothScrollToPosition(0);
        }
        else{
            Log.i(TAG,"showing filters");
            TransitionManager.beginDelayedTransition(cvFilter,
                    new AutoTransition());
            cvFilter.setVisibility(View.VISIBLE);

        }
        filterVisible = !filterVisible;
    }

    public void setToken(String token){
        accessToken = token;
        Log.i(TAG, "token: " + accessToken);
    }

    private void makeRequest (String query) {

        tvFail.setVisibility(View.GONE);
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
;
        List<Song> temp = new ArrayList<>();
        temp.addAll(songs);

        if(dateSearch){
            temp = dateRangeSearch(temp);
        }

        if(popSearch){
            temp = popularitySearch(temp);
        }

        if(cleanSearch){
            temp = explicitSearch(temp);
        }

        if(temp.isEmpty()){
            tvFail.setVisibility(View.VISIBLE);
        }


        songs.clear();
        songs.addAll(temp);
        notifyAdapter();
        printSongs(songs);
    }

    private List<Song> popularitySearch(List<Song> s) {
        Log.i(TAG,"Popularity Search");

        // make dummy Song objects to hold popularity
        Song song_lb = new Song();
        song_lb.setPopularity(Integer.parseInt(popLowerParam));

        Song song_ub = new Song();
        song_ub.setPopularity(Integer.parseInt(popUpperParam));

        return rangeSearch(s, song_lb,song_ub, MainActivity.sortMode.POPULARITY);

    }

    private List<Song> dateRangeSearch(List<Song> s) {
        Log.i(TAG,"Date Search");

        // make dummy Song objects to hold dates
        Song song_lb = new Song();
        Date d1 = new Date(dateLowerParam);
        song_lb.setDate(d1);

        Song song_ub = new Song();
        Date d2 = new Date(dateUpperParam);
        song_ub.setDate(d2);

        return rangeSearch(s, song_lb,song_ub, MainActivity.sortMode.RELEASE);
    }

    private List<Song> rangeSearch(List<Song> s, Song song_lb, Song song_ub, MainActivity.sortMode mode){
        // sort the songs & create copy
        List<Song> temp = sortSongs(s,mode);

        SongComparator comparator = new SongComparator(mode);

        // search for each bound
        int index_lower = lowerBound(temp,song_lb,comparator);
        int index_upper = upperBound(temp,song_ub,comparator);

        Log.i(TAG, "lower: " + index_lower + ", upper: " + index_upper);

        List<Song> set = new ArrayList<>();
        if(index_lower != index_upper) {
            set = temp.subList(index_lower, index_upper);
        }
        return set;
    }

    private List<Song> explicitSearch(List<Song> so){
        Log.i(TAG,"Explicit Search");
        List<Song> set = new ArrayList<>();

        for (Song s : so) {
            if (!s.getExplicit()) {
                set.add(s);
            }
        }

        printSongs(set);
        return set;
    }

    // returns an index 1 past the smallest value greater than or equal to the target
    private int upperBound(List<Song> s, Song target, Comparator comparator) {

        int mid;
        int n = s.size() - 1;

        int low = 0;
        int high = n;

        while(low < high){
            mid = low + (high - low) / 2;

            // if target >= songs[mid]
            if(comparator.compare(target,s.get(mid)) >= 0){
                low = mid + 1;
            }
            else{
                high = mid;
            }
        }

        // song[low] <= target
        while (low < n && (comparator.compare(s.get(low), target) <= 0)) {
            low++;
        }
        if(low == n){
            low = (comparator.compare(s.get(low),target) <= 0) ? low + 1 : low;
        }

        return low;
    }

    // returns an index to the greatest value less than or equal to the target
    private int lowerBound(List<Song> s, Song target, Comparator comparator) {
        int mid;
        int n = s.size() - 1;

        int low  = 0;
        int high = n;

        while(low < high){
            mid = low + (high - low) / 2;

            // if target <= songs[mid]
            if(comparator.compare(target,s.get(mid)) <= 0){
                high = mid;
            }
            else{
                low = mid + 1;
            }
        }


        if(low < n && (comparator.compare(s.get(low),target) < 0)){
            low++;
        }

        if(low == n){
            low = (comparator.compare(s.get(low),target) <= 0) ? low + 1 : low;
        }


        return low;
    }

    private List<Song> sortSongs(List<Song> s, MainActivity.sortMode mode) {
        SongComparator comparator = new SongComparator(mode);
        List<Song> temp = new ArrayList<>();
        temp.addAll(s);
        Collections.sort(temp, comparator);
        Log.i(TAG, "new order: ");
        printSongs(temp);
        return temp;
    }

    public void printSongs(List<Song> list){
        int i = 0;
        for(Song song: list){
            Log.i(TAG,
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

    private void hideKeyboard(){
        // make keyboard disappear
        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

}