package com.example.songshare.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.songshare.MainActivity;
import com.example.songshare.Post.Post;
import com.example.songshare.Post.PostsAdapter;
import com.example.songshare.PostDetailActivity;
import com.example.songshare.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class FeedFragment extends Fragment {


    public static final String TAG = "FeedFragment";
    RecyclerView rvPosts;
    PostsAdapter adapter;
    List<Post> allPosts;
    String mAccessToken;

    private SwipeRefreshLayout swipeContainer;
    static SpotifyAppRemote remote;

    public FeedFragment() {
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
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.connectRemote();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPosts = view.findViewById(R.id.rvPosts);

        allPosts = new ArrayList<>();

        adapter = new PostsAdapter(getContext(), allPosts);

        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        queryPosts();

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                allPosts.clear();
                queryPosts();
                swipeContainer.setRefreshing(false);
            }
        });

        swipeContainer.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mAccessToken = ((MainActivity)this.getActivity()).getAccessToken();
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvPosts);

    }

    private void queryPosts() {
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        query.include(Post.KEY_USER);
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
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        adapter.disconnectRemote();
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
            if(direction == ItemTouchHelper.RIGHT) {

                Post post = allPosts.get(viewHolder.getAdapterPosition());
                Toast.makeText(getContext(), "Swiped right on " + post.getSongTitle() + "!" , Toast.LENGTH_SHORT).show();
                rvPosts.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());
                Intent i = new Intent(getContext(), PostDetailActivity.class);
                i.putExtra("Post",post);
                i.putExtra("Token",mAccessToken);

                startActivity(i);
            }
            else {
                Toast.makeText(getContext(), "Swipe left!", Toast.LENGTH_SHORT).show();
            }

            // add song to Spotify Playlist
        }
    };


}