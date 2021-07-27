package com.example.songshare.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.models.Post;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;

public class PostDetailFragment extends Fragment {

    private Post post;
    private TextView tvSongTitle;
    private TextView tvArtist;
    private ImageView ivAlbum;

    public static final String TAG = "PostDetailFragment";


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
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_post_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();

        post = bundle.getParcelable("post");

        tvSongTitle = view.findViewById(R.id.tvTitle);
        tvArtist = view.findViewById(R.id.tvArtist);
        ivAlbum = view.findViewById(R.id.ivCover);

        tvSongTitle.setText(post.getSongTitle());
        tvArtist.setText(post.getArtist());
        Glide.with(getContext())
                .load(post.getAlbumURL())
                .into(ivAlbum);

    }

    @Override
    public void onCreateOptionsMenu(@NonNull @NotNull Menu menu, @NonNull @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_post_detail, menu);
        final MenuItem deleteItem = menu.findItem(R.id.delete);
        deleteItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                deletePost();
                // return to feed fragment
                navigateHome();
                return true;
            }
        });
    }


    private void deletePost(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Post");
        String postObjectId = post.getObjectId();
        // Retrieve the object by id
        query.getInBackground(postObjectId, (object, e) -> {
            if (e == null) {
                //Object was fetched
                //Deletes the fetched ParseObject from the database
                object.deleteInBackground(e2 -> {
                    if(e2==null){
                        Log.i(TAG,"Delete successful");
                    }else{
                        //Something went wrong while deleting the Object
                        Log.i(TAG,"Error: "+e2.getMessage());
                    }
                });
            }else{
                //Something went wrong
                Log.i(TAG,e.getMessage());
            }
        });

    }

    private void navigateHome(){
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
