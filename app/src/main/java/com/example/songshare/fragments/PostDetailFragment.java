package com.example.songshare.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.songshare.R;
import com.example.songshare.models.Song;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

public class PostDetailFragment extends Fragment {
    private Song song;
    private TextView tvSongTitle;
    private TextView tvArtist;
    private ImageView ivAlbum;

    public static final String TAG = "PostDetailFragment";
    private static final String CLIENT_ID = "1cb8dc3da6564e51af249a98d3d0eba1";
    private static final String REDIRECT_URI = "http://localhost:8888/";

    public PostDetailFragment() {
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
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();

        song = Parcels.unwrap(bundle.getParcelable("song"));

        tvSongTitle = view.findViewById(R.id.tvTitle);
        tvArtist = view.findViewById(R.id.tvArtist);
        ivAlbum = view.findViewById(R.id.ivCover);

        tvSongTitle.setText(song.getSongTitle());
        tvArtist.setText(song.getArtistName());
        Glide.with(getContext())
                .load(song.getAlbumUrl())
                .into(ivAlbum);

    }

}
