package com.example.songshare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.songshare.OverlapDecoration;
import com.example.songshare.R;
import com.example.songshare.adapters.ArtistAdapter;
import com.example.songshare.adapters.SongAdapter;
import com.example.songshare.models.Artist;
import com.example.songshare.models.Song;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RecommendFragment extends Fragment {

    RecyclerView rvSongs;
    RecyclerView rvArtists;
    List<Song> songs;
    List<Artist> artists;
    ArtistAdapter artistAdapter;
    SongAdapter songAdapter;

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

        songs = new ArrayList<>();
        songAdapter = new SongAdapter(getContext(),songs,false);

        artists = new ArrayList<>();
        artistAdapter = new ArtistAdapter(getContext(),artists);

        addSongs();
        addArtists();

        rvSongs = view.findViewById(R.id.rvSongs);
        rvSongs.addItemDecoration(new OverlapDecoration());
        rvSongs.setLayoutManager(new LinearLayoutManager(getContext()));
        rvSongs.setAdapter(songAdapter);

        rvArtists = view.findViewById(R.id.rvArtists);
        rvArtists.addItemDecoration(new OverlapDecoration());
        rvArtists.setLayoutManager(new LinearLayoutManager(getContext()));
        rvArtists.setAdapter(artistAdapter);

        new ItemTouchHelper(simpleCallback).attachToRecyclerView(rvSongs);
        new ItemTouchHelper(simpleCallback2).attachToRecyclerView(rvArtists);

    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
            songs.remove(viewHolder.getAdapterPosition());
            rvSongs.getAdapter().notifyDataSetChanged();
            rvSongs.smoothScrollToPosition(0);
        }


    };

    ItemTouchHelper.SimpleCallback simpleCallback2 = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT|ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
            artists.remove(viewHolder.getAdapterPosition());
            rvArtists.getAdapter().notifyDataSetChanged();
            rvArtists.smoothScrollToPosition(0);

        }


    };
    private void addSongs(){
        songs.add(new Song("https://i.scdn.co/image/ab67616d00001e02ba5db46f4b838ef6027e6f96","Ed Sheeran","Perfect","spotify:track:0tgVpDi06FyKpA1z0VMD4v"));
        songs.add(new Song("https://i.scdn.co/image/ab67616d00001e021c76e29153f29cc1e1b2b434","Logic","Perfect","spotify:track:5jbDih9bLGmI8ycUKkN5XA"));
        songs.add(new Song("https://i.scdn.co/image/ab67616d00001e02241e4fe75732c9c4b49b94c3","One Direction","Perfect","spotify:track:3NLnwwAQbbFKcEcV8hDItk"));
        songs.add(new Song("https://i.scdn.co/image/ab67616d00001e0293432e914046a003229378da","5 Seconds of Summer","She Looks So Perfect", "spotify:track:1gugDOSMREb34Xo0c1PlxM"));
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