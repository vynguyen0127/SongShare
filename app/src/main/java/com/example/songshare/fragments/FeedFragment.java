package com.example.songshare.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.songshare.MainActivity;
import com.example.songshare.R;
import com.example.songshare.adapters.PostsAdapter;
import com.example.songshare.models.Post;
import com.example.songshare.models.Song;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;


public class FeedFragment extends Fragment {


    public static final String TAG = "FeedFragment";

    private RecyclerView rvPosts;
    private PostsAdapter adapter;
    private List<Post> allPosts;
    private String accessToken;
    private ProgressBar progress;
    private SwipeRefreshLayout swipeContainer;
    FragmentManager fragmentManager;
    ItemTouchHelper.SimpleCallback itemTouchHelperCallback;

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
    public void onViewCreated(@NonNull View view, @Nullable  Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvPosts = view.findViewById(R.id.rvPosts);

        allPosts = new ArrayList<>();

        adapter = new PostsAdapter(getContext(), allPosts,MainActivity.postMode.FEED);

        rvPosts.setAdapter(adapter);
        rvPosts.setLayoutManager(new LinearLayoutManager(getContext()));
        progress = (ProgressBar) view.findViewById(R.id.progress);
        queryPosts();

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);

        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

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

        fragmentManager = getActivity().getSupportFragmentManager();


        itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull @NotNull RecyclerView recyclerView, @NonNull @NotNull RecyclerView.ViewHolder viewHolder, @NonNull @NotNull RecyclerView.ViewHolder target) {
                Log.i(TAG,"onMove");
                return false;
            }


            @Override
            public void onSwiped(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int direction) {
                Log.i(TAG,"onSwipe");
                Post post = allPosts.get(viewHolder.getAdapterPosition());
                Song song = new Song(post.getAlbumURL(),post.getArtist(),post.getSongTitle(),post.getSongUri(),"");
                if(direction == ItemTouchHelper.RIGHT) {
                    accessToken = fetchToken();

                    rvPosts.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());

                    goToFragment(post, song, new PlaylistAddFragment());

                }
                else {
                    Toast.makeText(getContext(), "Swipe left!", Toast.LENGTH_SHORT).show();
                    rvPosts.getAdapter().notifyItemChanged(viewHolder.getAdapterPosition());

                    goToFragment(post, song, new PostDetailFragment());

                }

            }


        };
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(rvPosts);
    }


    private void queryPosts() {
        progress.setVisibility(ProgressBar.VISIBLE);
        Log.i(TAG,"getting posts");
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
    private String fetchToken(){
        return ((MainActivity)this.getActivity()).getAccessToken();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void goToFragment(Post post, Song song,Fragment fragment){
        Fragment destFragment =  fragment;
        String backStateName = destFragment.getClass().getName();

        boolean fragmentPopped = fragmentManager.popBackStackImmediate (backStateName, 0);

        if (!fragmentPopped){ //fragment not in back stack, create it.
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.replace(R.id.flContainer, destFragment);
            ft.addToBackStack(backStateName);
            ft.commit();
        }

        Bundle bundle = new Bundle();

        if(destFragment instanceof PostDetailFragment){
            bundle.putParcelable("post",post);
        }
        else if(destFragment instanceof PlaylistAddFragment){
            bundle.putParcelable("song", Parcels.wrap(song));
        }
        bundle.putString("token",accessToken);
        destFragment.setArguments(bundle);

        fragmentManager.beginTransaction().replace(R.id.flContainer,destFragment).commit();
    }


}