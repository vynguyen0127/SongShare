package com.example.songshare;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.view.ViewCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.ErrorCallback;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerContext;
import com.spotify.protocol.types.PlayerState;

import org.jetbrains.annotations.NotNull;

public class CustomSnackbar extends BaseTransientBottomBar<CustomSnackbar> {
    public static final String TAG = "CustomSnackBar";

    private static SpotifyAppRemote mSpotifyAppRemote;

    static AppCompatSeekBar  mSeekBar;
    static TrackProgressBar mTrackProgressBar;
    static ImageButton mPlayPauseButton;
    static TextView tvPlayerSong;
    static TextView tvPlayerArtist;

    Subscription<PlayerState> mPlayerStateSubscription;
    Subscription<PlayerContext> mPlayerContextSubscription;

    private final ErrorCallback mErrorCallback = this::logError;

    private Subscription.EventCallback<PlayerState> mPlayerStateEventCallback;
    static View content;

    protected CustomSnackbar(@NonNull @NotNull ViewGroup parent, @NonNull @NotNull View content, @NonNull @NotNull com.google.android.material.snackbar.ContentViewCallback contentViewCallback) {
        super(parent, content, contentViewCallback);
    }
    public static CustomSnackbar make(ViewGroup parent, @Duration int duration,SpotifyAppRemote remote){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        content = inflater.inflate(R.layout.custom_snackbar,parent,false);
        mSpotifyAppRemote = remote;
        // create snackbar with custom view
        ContentViewCallback callback= new ContentViewCallback(content);
        CustomSnackbar customSnackbar = new CustomSnackbar(parent, content, callback);
        tvPlayerSong = content.findViewById(R.id.tvPlayerSong);
        tvPlayerArtist = content.findViewById(R.id.tvPlayerArtist);
        // Remove black background padding on left and right
        customSnackbar.getView().setPadding(0, 0, 0, 0);

        // set snackbar duration
        customSnackbar.setDuration(duration);
        return customSnackbar;
    }

    public CustomSnackbar setAction() {

        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return this;
    }

    public CustomSnackbar setPlay() {
        ImageButton actionView = (ImageButton) getView().findViewById(R.id.play);
        actionView.setVisibility(View.VISIBLE);
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSpotifyAppRemote
                        .getPlayerApi()
                        .getPlayerState()
                        .setResultCallback(
                                playerState -> {
                                    if (playerState.isPaused) {
                                        mSpotifyAppRemote
                                                .getPlayerApi()
                                                .resume();
                                    } else {
                                        mSpotifyAppRemote
                                                .getPlayerApi()
                                                .pause();
                                    }
                                });
            }
        });
        return this;
    }

    public void setPlayer(){
        mSeekBar = getView().findViewById(R.id.seek_to);
        mSeekBar.setEnabled(false);
        mSeekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mSeekBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mPlayPauseButton = getView().findViewById(R.id.play);

        mTrackProgressBar = new TrackProgressBar(mSeekBar);

        mPlayerStateEventCallback =
                new Subscription.EventCallback<PlayerState>() {
                    @Override
                    public void onEvent(PlayerState playerState) {

                        // Update progressbar
                        if (playerState.playbackSpeed > 0) {
                            mTrackProgressBar.unpause();
                        } else {
                            mTrackProgressBar.pause();
                        }

                        // Invalidate play / pause
                        if (playerState.isPaused) {
                            mPlayPauseButton.setImageResource(R.drawable.btn_play);
                        } else {
                            mPlayPauseButton.setImageResource(R.drawable.btn_pause);
                        }

                        if (playerState.track != null) {
                            // Invalidate seekbar length and position
                            mSeekBar.setMax((int) playerState.track.duration);
                            mTrackProgressBar.setDuration(playerState.track.duration);
                            mTrackProgressBar.update(playerState.playbackPosition);
                            tvPlayerArtist.setText(playerState.track.artist.name);
                            tvPlayerSong.setText(playerState.track.name);
                        }

                        mSeekBar.setEnabled(true);
                    }
                };

        onSubscribedToPlayerStateButtonClicked(null);
        onSubscribedToPlayerContextButtonClicked(null);
    }


    private static class ContentViewCallback implements
            BaseTransientBottomBar.ContentViewCallback {

        // view inflated from custom layout
        private View content;

        public ContentViewCallback(View content) {
            this.content = content;
        }

        @Override
        public void animateContentIn(int delay, int duration) {
            // add custom *in animations for your views
            // e.g. original snackbar uses alpha animation, from 0 to 1
            ViewCompat.setScaleY(content, 0f);

            ViewCompat.animate(content)
                    .scaleY(1f).setDuration(duration)
                    .setStartDelay(delay);
        }

        @Override
        public void animateContentOut(int delay, int duration) {
            // add custom *out animations for your views
            // e.g. original snackbar uses alpha animation, from 1 to 0
            ViewCompat.setScaleY(content, 1f);
            ViewCompat.animate(content)
                    .scaleY(0f)
                    .setDuration(duration)
                    .setStartDelay(delay);
        }
    }

    public void onSubscribedToPlayerContextButtonClicked(View view) {
        if (mPlayerContextSubscription != null && !mPlayerContextSubscription.isCanceled()) {
            mPlayerContextSubscription.cancel();
            mPlayerContextSubscription = null;
        }


        mPlayerContextSubscription =
                (Subscription<PlayerContext>)
                        mSpotifyAppRemote
                                .getPlayerApi()
                                .subscribeToPlayerContext()
                                .setErrorCallback(
                                        throwable -> {
                                            logError(throwable);
                                        });
    }

    public void onSubscribedToPlayerStateButtonClicked(View view) {

        if (mPlayerStateSubscription != null && !mPlayerStateSubscription.isCanceled()) {
            mPlayerStateSubscription.cancel();
            mPlayerStateSubscription = null;
        }

        mPlayerStateSubscription =
                (Subscription<PlayerState>)
                        mSpotifyAppRemote
                                .getPlayerApi()
                                .subscribeToPlayerState()
                                .setEventCallback(mPlayerStateEventCallback)
                                .setErrorCallback(
                                        throwable -> {
                                            logError(throwable);
                                        });
    }

    private void logError(Throwable throwable) {
        Toast.makeText(getContext(), R.string.err_generic_toast, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "", throwable);
    }

    private void logMessage(String msg) {
        logMessage(msg, Toast.LENGTH_SHORT);
    }

    private void logMessage(String msg, int duration) {
        Toast.makeText(getContext(), msg, duration).show();
        Log.d(TAG, msg);
    }
    private class TrackProgressBar {

        private static final int LOOP_DURATION = 500;
        private final SeekBar mSeekBar;
        private final Handler mHandler;

        private final SeekBar.OnSeekBarChangeListener mSeekBarChangeListener =
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mSpotifyAppRemote
                                .getPlayerApi()
                                .seekTo(seekBar.getProgress())
                                .setErrorCallback(mErrorCallback);
                    }
                };

        private final Runnable mSeekRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        int progress = mSeekBar.getProgress();
                        mSeekBar.setProgress(progress + LOOP_DURATION);
                        mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
                    }
                };

        private TrackProgressBar(SeekBar seekBar) {
            mSeekBar = seekBar;
            mSeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);
            mHandler = new Handler();
        }

        private void setDuration(long duration) {
            mSeekBar.setMax((int) duration);
        }

        private void update(long progress) {
            mSeekBar.setProgress((int) progress);
        }

        private void pause() {
            mHandler.removeCallbacks(mSeekRunnable);
        }

        private void unpause() {
            mHandler.removeCallbacks(mSeekRunnable);
            mHandler.postDelayed(mSeekRunnable, LOOP_DURATION);
        }
    }
}

