package com.example.songshare.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.models.Post;
import com.example.songshare.models.Song;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.parceler.Parcels;

public class PostDraftFragment extends Fragment {

    public static final String TAG = "PostDraftFragment";
    Song song;

    ImageView ivAlbum;
    TextView tvSongTitle;
    TextView tvArtist;
    Button btnPost;
    EditText etCaption;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post_draft, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle bundle = this.getArguments();
        song = Parcels.unwrap(bundle.getParcelable("song"));

        ivAlbum = view.findViewById(R.id.ivCover);
        tvSongTitle = view.findViewById(R.id.tvTitle);
        tvArtist = view.findViewById(R.id.tvArtist);
        btnPost = view.findViewById(R.id.btnPost);
        etCaption = view.findViewById(R.id.etCaption);

        Glide.with(getContext())
                .load(song.getAlbumUrl())
                .transform(new RoundedCorners(20))
                .into(ivAlbum);

        tvSongTitle.setText(song.getSongTitle());
        tvArtist.setText(song.getArtist());
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePost(ParseUser.getCurrentUser());
            }
        });
    }

    private void savePost(ParseUser currentUser){
        Post post = new Post();
        post.setCaption(etCaption.getText().toString());
        post.setUser(currentUser);
        post.setSongTitle(song.getSongTitle());
        post.setArtist(song.getArtist());
        post.setAlbumUrl(song.getAlbumUrl());
        post.setSongId(song.getSongUri());
        post.setUsersLiked(new JSONArray());
        post.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.e(TAG, "Error while saving",e);
                    Toast.makeText(getContext(), "Error while saving",Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG,"Post save was successful!" );
                etCaption.setText("");
            }
        });
        navigateHome();
    }

    private void navigateHome() {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.popBackStackImmediate();

        Fragment feedFragment = new FeedFragment();
        String backStateName = feedFragment.getClass().getName();
        boolean fragmentPopped = fragmentManager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.flContainer, feedFragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }

        fragmentManager.beginTransaction().replace(R.id.flContainer,feedFragment).commit();
        MainActivity.bottomNavigationView.setSelectedItemId(R.id.action_home);
    }

}