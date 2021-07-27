package com.example.songshare;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.spotify.protocol.types.Image;
import com.spotify.protocol.types.PlayerState;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class CustomSnackbar extends BaseTransientBottomBar<CustomSnackbar> {
    public static final String TAG = "CustomSnackBar";

    private static SpotifyAppRemote remote;

    static AppCompatSeekBar seekBar;
    static TrackProgressBar trackProgressBar;
    static ImageButton playPauseButton;
    static TextView tvPlayerSong;
    static TextView tvPlayerArtist;
    static ImageView ivAlbumArt;
    static TextView tvPos;
    static TextView tvDuration;

    Subscription<PlayerState> playerStateSubscription;

    private final ErrorCallback errorCallback = this::logError;

    private Subscription.EventCallback<PlayerState> playerStateEventCallback;
    static View content;

    protected CustomSnackbar(@NonNull @NotNull ViewGroup parent, @NonNull @NotNull View content, @NonNull @NotNull com.google.android.material.snackbar.ContentViewCallback contentViewCallback) {
        super(parent, content, contentViewCallback);
    }
    public static CustomSnackbar make(ViewGroup parent, @Duration int duration,SpotifyAppRemote remote){
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        content = inflater.inflate(R.layout.custom_snackbar,parent,false);
        CustomSnackbar.remote = remote;

        // create snackbar with custom view
        ContentViewCallback callback= new ContentViewCallback(content);
        CustomSnackbar customSnackbar = new CustomSnackbar(parent, content, callback);
        tvPlayerSong = content.findViewById(R.id.tvPlayerSong);
        tvPlayerArtist = content.findViewById(R.id.tvPlayerArtist);
        ivAlbumArt = content.findViewById(R.id.ivAlbumArt);
        tvPos = content.findViewById(R.id.tvPos);
        tvDuration = content.findViewById(R.id.tvDuration);


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

        ImageButton actionView = (ImageButton) getView().findViewById(R.id.play);
        actionView.setVisibility(View.VISIBLE);
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remote
                        .getPlayerApi()
                        .getPlayerState()
                        .setResultCallback(
                                playerState -> {
                                    if (playerState.isPaused) {
                                        remote
                                                .getPlayerApi()
                                                .resume();
                                    } else {
                                        remote
                                                .getPlayerApi()
                                                .pause();
                                    }
                                });
            }
        });

        return this;
    }

    public void setPlayer(){
        seekBar = getView().findViewById(R.id.seek_to);
        seekBar.setEnabled(false);
        seekBar.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        seekBar.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        playPauseButton = getView().findViewById(R.id.play);

        trackProgressBar = new TrackProgressBar(seekBar);

        playerStateEventCallback =
                new Subscription.EventCallback<PlayerState>() {
                    @Override
                    public void onEvent(PlayerState playerState) {

                        // Update progressbar
                        if (playerState.playbackSpeed > 0) {
                            trackProgressBar.unpause();
                        } else {
                            trackProgressBar.pause();
                        }

                        // Invalidate play / pause
                        if (playerState.isPaused) {
                            playPauseButton.setImageResource(R.drawable.ic_play);
                        } else {
                            playPauseButton.setImageResource(R.drawable.ic_pause);
                        }

                        if (playerState.track != null) {

                            long minutes = TimeUnit.MILLISECONDS.toMinutes(playerState.track.duration);
                            long seconds = TimeUnit.MILLISECONDS.toSeconds(playerState.track.duration);
                            seconds %= 60;
                            tvDuration.setText("/" + minutes + ":" + seconds);

                            long min2 = TimeUnit.MILLISECONDS.toMinutes(playerState.playbackPosition);
                            long sec2 = TimeUnit.MILLISECONDS.toSeconds(playerState.playbackPosition);
                            sec2 %= 60;
                            tvPos.setText(min2 + ":" + String.format("%02d", sec2));

                            // Invalidate seekbar length and position
                            seekBar.setMax((int) playerState.track.duration);
                            trackProgressBar.setDuration(playerState.track.duration);
                            trackProgressBar.update(playerState.playbackPosition);
                            tvPlayerArtist.setText(playerState.track.artist.name);
                            tvPlayerSong.setText(playerState.track.name);
                            playerState.track.imageUri.toString();

                            remote
                            .getImagesApi()
                                    .getImage(playerState.track.imageUri, Image.Dimension.SMALL)
                                    .setResultCallback(
                                            bitmap -> {
                                                ivAlbumArt.setImageBitmap(bitmap);
                                            });
                        }

                        seekBar.setEnabled(true);
                    }
                };

        onSubscribedToPlayerStateButtonClicked(null);
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

    public void onSubscribedToPlayerStateButtonClicked(View view) {

        if (playerStateSubscription != null && !playerStateSubscription.isCanceled()) {
            playerStateSubscription.cancel();
            playerStateSubscription = null;
        }

        playerStateSubscription =
                (Subscription<PlayerState>)
                        remote
                                .getPlayerApi()
                                .subscribeToPlayerState()
                                .setEventCallback(playerStateEventCallback)
                                .setErrorCallback(
                                        throwable -> {
                                            logError(throwable);
                                        });
    }

    private void logError(Throwable throwable) {
        Toast.makeText(getContext(), R.string.err_generic_toast, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "", throwable);
    }

    private static class TrackProgressBar {

        private static final int LOOP_DURATION = 500;
        private final SeekBar seekBar;
        private final Handler handler;

        private final SeekBar.OnSeekBarChangeListener seekBarChangeListener =
                new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {}

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        remote
                                .getPlayerApi()
                                .seekTo(seekBar.getProgress());
                    }

                };

        private final Runnable runnable =
                new Runnable() {
                    @Override
                    public void run() {
                        int progress = seekBar.getProgress();
                        seekBar.setProgress(progress + LOOP_DURATION);
                        handler.postDelayed(runnable, LOOP_DURATION);
                        long minutes = TimeUnit.MILLISECONDS.toMinutes((long)progress);
                        long seconds = TimeUnit.MILLISECONDS.toSeconds((long)progress) % 60;
                        Log.i(TAG,"PROGRESS: " + minutes + ":" + String.format("%02d", seconds));
                        tvPos.setText(minutes + ":" + String.format("%02d", seconds));
                    }
                };

        private TrackProgressBar(SeekBar seekBar) {
            this.seekBar = seekBar;
            this.seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
            handler = new Handler();
        }

        private void setDuration(long duration) {
            seekBar.setMax((int) duration);
        }

        private void update(long progress) {
            seekBar.setProgress((int) progress);
        }

        private void pause() {
            handler.removeCallbacks(runnable);
        }

        private void unpause() {
            handler.removeCallbacks(runnable);
            handler.postDelayed(runnable, LOOP_DURATION);
        }
    }
}

