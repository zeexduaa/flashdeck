package com.zeexduaa.flashdeck;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/** Entry point. For now it only shows the app name; decks come next. */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.app_name);
    }
}
