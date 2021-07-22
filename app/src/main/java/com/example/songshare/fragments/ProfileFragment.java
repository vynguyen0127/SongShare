package com.example.songshare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.adapters.PostsAdapter;
import com.example.songshare.models.Post;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;


public class ProfileFragment extends Fragment {

    private String accessToken;
    private RecyclerView rvTopSongs;
    private TextView tvTitle2;
    private TextView tvUsername;
    private ImageView ivProfile;
    private List<Post> myPosts;
    private PostsAdapter adapter;
    private ProgressBar progress;
    private OkHttpClient okHttpClient = new OkHttpClient();

    public static final String TAG = "ProfileFragment";

    public ProfileFragment() {
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
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG,"profile view created");
        rvTopSongs = view.findViewById(R.id.rvTopSongs);
        tvTitle2 = view.findViewById(R.id.tvTitle2);
        tvUsername = view.findViewById(R.id.tvUsername);
        progress = (ProgressBar) view.findViewById(R.id.progress);
        myPosts = new ArrayList<>();

        adapter = new PostsAdapter(getContext(),myPosts, MainActivity.postMode.FEED);
        rvTopSongs.setAdapter(adapter);

        rvTopSongs.setLayoutManager(new LinearLayoutManager(getContext()));

        tvUsername.setText(ParseUser.getCurrentUser().getUsername());
        tvTitle2.setText("Posts:");

        queryPosts();
    }

    public void setToken(String token){
        accessToken = token;
        Log.i(TAG, "token: " + accessToken);
    }

    private void queryPosts() {
        ParseUser current = ParseUser.getCurrentUser();
        progress.setVisibility(ProgressBar.VISIBLE);
        Log.i(TAG,"getting posts");
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
        query.whereEqualTo("user",current);
        query.addDescendingOrder("createdAt");
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "Issue getting posts: " + e.toString());
                    return;
                }

                for (Post post : posts) {
                    Log.i(TAG, "Post: " + post.getCaption() + ", User: " + post.getUser().getUsername() + ", Song: " + post.getSongTitle());
                }
                myPosts.addAll(posts);
                adapter.notifyDataSetChanged();
                Handler mainHandler = new Handler(Looper.getMainLooper());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //Update UI
                        progress.setVisibility(ProgressBar.GONE);
                    }
                });
            }
        });
    }

}