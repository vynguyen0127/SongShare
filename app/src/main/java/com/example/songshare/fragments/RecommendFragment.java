package com.example.songshare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.adapters.ArtistAdapter;
import com.example.songshare.adapters.SongAdapter;
import com.example.songshare.models.Artist;
import com.example.songshare.models.Song;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackListener;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class RecommendFragment extends Fragment {

    CardStackView csArtists;
    CardStackView csSongs;
    Button btnRecommend;

    List<Song> songs;
    List<Artist> artists;

    ArtistAdapter artistAdapter;
    SongAdapter songAdapter;

    ArrayList<String> seed_artists;
    ArrayList<String> seed_songs;
    ArrayList<String> seed_genres;

    HashMap<String,String> seen_artists;

    FragmentManager fragmentManager;
    String accessToken;
    int numArtists;
    int numSongs;
    private final OkHttpClient okHttpClient = new OkHttpClient();

    public static final String TAG = "RecommendFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recommend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fragmentManager = getActivity().getSupportFragmentManager();

        seed_artists = new ArrayList<>();
        seed_genres = new ArrayList<>();
        seed_songs = new ArrayList<>();

        songs = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(), songs, MainActivity.songMode.SEED);
        songAdapter.setToken(accessToken);

        artists = new ArrayList<>();
        artistAdapter = new ArtistAdapter(getContext(), artists);
        seen_artists = new HashMap<>();

        addSongs();
        addArtists();

        csSongs = view.findViewById(R.id.csSongs);
        csArtists = view.findViewById(R.id.csArtists);

        setArtistStack();
        setSongStack();

        showPopUpWindow(view);

        numArtists = 5;
        numSongs = 5;

        btnRecommend = view.findViewById(R.id.btnRecommend);
        btnRecommend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = fetchUserUri();
                Toast.makeText(getContext(),"Fetching new songs!",Toast.LENGTH_SHORT).show();
                goToResultFragment();
            }
        });

    }

    private void setSongStack() {
        CardStackLayoutManager songManager = new CardStackLayoutManager(getContext(),new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {

            }

            @Override
            public void onCardSwiped(Direction direction) {
                Log.i(TAG, "onSwipe!");
                if(direction == Direction.Right){
                    if(numSongs == 0){
                        Toast.makeText(getContext(),"You have reached the max number of songs",Toast.LENGTH_SHORT).show();
                        csSongs.rewind();
                    }
                    else {
                        String songId = songs.get(0).getSongId();
                        seed_songs.add(songId);
                        numSongs--;
                        Toast.makeText(getContext(), String.valueOf(numSongs) + String.format(" %s remaining.", (numSongs == 1 ? "song" : "songs")), Toast.LENGTH_SHORT).show();
                    }
                }
                songs.remove(0);
                csSongs.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onCardRewound() {

            }

            @Override
            public void onCardCanceled() {

            }

            @Override
            public void onCardAppeared(View view, int position) {

            }

            @Override
            public void onCardDisappeared(View view, int position) {

            }
        });

        songManager.setStackFrom(StackFrom.None);
        songManager.setVisibleCount(3);
        songManager.setTranslationInterval(8.0f);
        songManager.setScaleInterval(0.95f);
        songManager.setSwipeThreshold(0.3f);
        songManager.setMaxDegree(20.0f);
        songManager.setDirections(Direction.HORIZONTAL);
        songManager.setCanScrollHorizontal(true);
        songManager.setCanScrollVertical(true);
        songManager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);


        csSongs.setLayoutManager(songManager);
        csSongs.setAdapter(songAdapter);
    }

    private void setArtistStack() {
        CardStackLayoutManager artistManager = new CardStackLayoutManager(getContext(),new CardStackListener() {
            @Override
            public void onCardDragging(Direction direction, float ratio) {

            }

            @Override
            public void onCardSwiped(Direction direction) {
                if(direction == Direction.Right){
                    if(numArtists == 0){
                        Toast.makeText(getContext(),"You have reached the max number of artists",Toast.LENGTH_SHORT).show();
                    }
                    else if(numArtists > 0) {
                        // add artist id to seed_songs array
                        String id = artists.get(0).getArtistId();
                        seed_artists.add(id);
                        numArtists--;
                        Toast.makeText(getContext(), String.valueOf(numArtists) + String.format(" %s remaining.", (numArtists == 1 ? "artist" : "artists")), Toast.LENGTH_SHORT).show();

                        // use artist id to get related artists
                        fetchRelatedArtists(id);

                        // get artist info
                        fetchArtistGenres(id);
                        artists.remove(0);
                        csArtists.getAdapter().notifyDataSetChanged();
                    }

                }
                else{
                    artists.remove(0);
                    csArtists.getAdapter().notifyDataSetChanged();
                }


            }

            @Override
            public void onCardRewound() {

            }

            @Override
            public void onCardCanceled() {

            }

            @Override
            public void onCardAppeared(View view, int position) {

            }

            @Override
            public void onCardDisappeared(View view, int position) {

            }
        });
        artistManager.setStackFrom(StackFrom.None);
        artistManager.setVisibleCount(3);
        artistManager.setTranslationInterval(8.0f);
        artistManager.setScaleInterval(0.95f);
        artistManager.setSwipeThreshold(0.3f);
        artistManager.setMaxDegree(20.0f);
        artistManager.setDirections(Direction.HORIZONTAL);
        artistManager.setCanScrollHorizontal(true);
        artistManager.setCanScrollVertical(true);
        artistManager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual);

        csArtists.setLayoutManager(artistManager);
        csArtists.setAdapter(artistAdapter);
    }

    private void goToResultFragment() {
        Fragment resultFragment = new ResultFragment();
        String backStateName = resultFragment.getClass().getName();

        boolean fragmentPopped = fragmentManager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.flContainer, resultFragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }
        String uri = fetchUserUri();
        Bundle bundle = new Bundle();
        bundle.putStringArrayList("artists",seed_artists);
        bundle.putStringArrayList("songs",seed_songs);
        bundle.putStringArrayList("genres",seed_genres);
        bundle.putString("token",accessToken);
        bundle.putString("uri",uri);
        resultFragment.setArguments(bundle);

        fragmentManager.beginTransaction().replace(R.id.flContainer,resultFragment).commit();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "Pausing RecommendFragment");
        Log.i(TAG,"artists: " + seed_artists.toString());
        Log.i(TAG,"songs: " + seed_songs.toString());
    }

    private void showPopUpWindow(View view) {

        // inflate the layout of the popup window
        LayoutInflater inflater = (LayoutInflater)
                ((MainActivity)this.getActivity()).getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_window, null);

        // create the popup window
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // lets taps outside the popup also dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setAnimationStyle(R.style.popup_window_animation_phone);
        // show the popup window
        // which view you pass in doesn't matter, it is only used for the window tolken
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        // dismiss the popup window when touched
        popupView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                popupWindow.dismiss();
                return true;
            }
        });
    }


    private void fetchArtistGenres(String id) {
        makeRequest(id,false);
    }

    private void fetchRelatedArtists(String id) {
        makeRequest(id,true);
    }

    private String fetchUserUri(){ return ((MainActivity)this.getActivity()).getCurrentUserUri(); }

    private void makeRequest(String id, boolean b) {
        String url = String.format("https://api.spotify.com/v1/artists/%s",id);

        if(b){
            url += "/related-artists";
        }

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.i(TAG,"onFailure");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                try {

                    String responseData = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseData);

                    if(b){
                        // add artists related artist to array
                        JSONArray jsonArtists = new JSONArray(jsonObject.get("artists").toString());
                        for(int i = 0; i < 5; i++){
                            Artist artist = new Artist(jsonArtists.getJSONObject(i));
                            if(!seen_artists.containsKey(artist.getArtistId())) {
                                seen_artists.put(artist.getArtistId(),"");
                                artists.add(artist);
                            }
                        }
                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                //Update UI
                                artistAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                    else{
                        // fetch artist's genres
                        JSONArray genres = new JSONArray(jsonObject.get("genres").toString());

                        seed_genres.add(genres.get(0).toString());

                    }

                } catch (JSONException e) {
                    Log.i(TAG,e.toString());
                }
            }

        });
    }

    public void setToken(String token){
        accessToken = token;
        Log.i(TAG, "token: " + accessToken);
    }

    private void addSongs(){
        songs.add(new Song("https://i.scdn.co/image/ab67616d00001e02ba5db46f4b838ef6027e6f96","Ed Sheeran","Perfect","spotify:track:0tgVpDi06FyKpA1z0VMD4v","0tgVpDi06FyKpA1z0VMD4v"));
        songs.add(new Song("https://i.scdn.co/image/ab67616d00001e021c76e29153f29cc1e1b2b434","Logic","Perfect","spotify:track:5jbDih9bLGmI8ycUKkN5XA","5jbDih9bLGmI8ycUKkN5XA"));
        songs.add(new Song("https://i.scdn.co/image/ab67616d00001e02241e4fe75732c9c4b49b94c3","One Direction","Perfect","spotify:track:3NLnwwAQbbFKcEcV8hDItk","3NLnwwAQbbFKcEcV8hDItk"));
        songs.add(new Song("https://i.scdn.co/image/ab67616d00001e0293432e914046a003229378da","5 Seconds of Summer","She Looks So Perfect", "spotify:track:1gugDOSMREb34Xo0c1PlxM","1gugDOSMREb34Xo0c1PlxM"));

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                //Update UI
                songAdapter.notifyDataSetChanged();
            }
        });
    }

    private void addArtists(){
        artists.add(new Artist("Harry Styles","6KImCVD70vtIoJWnq6nGn3","https://i.scdn.co/image/ab67616100005174d9f70439ec8893ef495e1b7e"));
        artists.add(new Artist("Olivia Rodrigo","1McMsnEElThX1knmY4oliG","https://i.scdn.co/image/ab6761610000e5eb8885ead433869bbbe56dd2da"));
        artists.add(new Artist("5 Seconds of Summer","5Rl15oVamLq7FbSb0NNBNy","https://i.scdn.co/image/ab6761610000f178ffe8513647c422e6d93ed94a"));
        artists.add(new Artist("Billie Eilish","6qqNVTkY8uBg9cP3Jd7DAH","https://i.scdn.co/image/ab67616100005174d8b9980db67272cb4d2c3daf"));

        seen_artists.put("6KImCVD70vtIoJWnq6nGn3","");
        seen_artists.put("1McMsnEElThX1knmY4oliG","");
        seen_artists.put("5Rl15oVamLq7FbSb0NNBNy","");
        seen_artists.put("6qqNVTkY8uBg9cP3Jd7DAH","");

        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                //Update UI
                artistAdapter.notifyDataSetChanged();
            }
        });
    }

}