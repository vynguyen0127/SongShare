package com.example.songshare;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {

    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("GlIvEC4ni1IWfPTxbb0O1Fzk5dwygtLPEytq5MSs")
                .clientKey("J67SvVhbCrWwdENFqAbT3HiGuXXrpnjQvYJNmZWa")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }

}
