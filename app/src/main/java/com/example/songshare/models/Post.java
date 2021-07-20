package com.example.songshare.models;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.Date;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_CAPTION = "caption";
    public static final String KEY_USER = "user";
    public static final String KEY_SONGID = "song_id";
    public static final String KEY_ALBUM_URL = "album_url";
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_SONG_TITLE = "song_title";
    public static final String KEY_USERS_LIKED = "users_liked";

    public Post(){}

    public String getCaption(){
        return getString(KEY_CAPTION);
    }

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }

    public String getSongUri(){
        return getString(KEY_SONGID);
    }

    public String getAlbumURL(){
        return getString(KEY_ALBUM_URL);
    }

    public String getArtist(){
        return getString(KEY_ARTIST);
    }

    public String getSongTitle(){
        return getString(KEY_SONG_TITLE);
    }

    public JSONArray getUsersLiked(){ return getJSONArray(KEY_USERS_LIKED); }

    public void setCaption(String caption){
        put(KEY_CAPTION, caption);
    }

    public void setUser(ParseUser user){
        put(KEY_USER,user);
    }

    public void setSongId(String songID){
        put(KEY_SONGID,songID);
    }

    public void setAlbumUrl(String albumUrl){ put(KEY_ALBUM_URL, albumUrl); }

    public void setArtist(String artist){ put(KEY_ARTIST, artist); }

    public void setSongTitle(String songTitle){ put(KEY_SONG_TITLE,songTitle ); }

    public void setKeyUsersLiked(JSONArray usersLiked){put(KEY_USERS_LIKED, usersLiked);}

    public static String calculateTimeAgo(Date createdAt) {

        int SECOND_MILLIS = 1000;
        int MINUTE_MILLIS = 60 * SECOND_MILLIS;
        int HOUR_MILLIS = 60 * MINUTE_MILLIS;
        int DAY_MILLIS = 24 * HOUR_MILLIS;

        try {
            createdAt.getTime();
            long time = createdAt.getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " minutes ago";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " hours ago";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " days ago";
            }
        } catch (Exception e) {
            Log.i("Error:", "getRelativeTimeAgo failed", e);
            e.printStackTrace();
        }

        return "";
    }
}
