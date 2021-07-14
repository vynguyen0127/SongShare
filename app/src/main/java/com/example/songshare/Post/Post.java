package com.example.songshare.Post;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Post")
public class Post extends ParseObject {

    public static final String KEY_CAPTION = "caption";
    public static final String KEY_USER = "user";
    public static final String KEY_SONGID = "song_id";
    public static final String KEY_ALBUM_URL = "album_url";
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_SONG_TITLE = "song_title";

    public Post(){}

    public String getCaption(){
        return getString(KEY_CAPTION);
    }

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }

    public String getSongID(){
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

    public void setCaption(String caption){
        put(KEY_CAPTION, caption);
    }

    public void setUser(ParseUser user){
        put(KEY_USER,user);
    }

    public void setSongID(String songID){
        put(KEY_SONGID,songID);
    }

    public void setAlbumUrl(String albumUrl){ put(KEY_ALBUM_URL, albumUrl); }

    public void setArtist(String artist){ put(KEY_ARTIST, artist); }

    public void setSongTitle(String songTitle){ put(KEY_SONG_TITLE,songTitle ); }
}
