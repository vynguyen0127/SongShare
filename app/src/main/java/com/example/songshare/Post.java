package com.example.songshare;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class Post extends ParseObject {

    private static final String KEY_CAPTION = "caption";
    private static final String KEY_USER = "user";
    private static final String KEY_SONGID = "song_id";

    public String getCaption(){
        return getString(KEY_CAPTION);
    }

    public ParseUser getUser(){
        return getParseUser(KEY_USER);
    }

    public String getSongID(){
        return getString(KEY_SONGID);
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
}
