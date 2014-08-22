package com.smusing.variations.variations;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.TextView;

public class MenuAbout extends Activity{

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_about);

        //gets the text view and displays the about
        TextView tView=(TextView)findViewById(R.id.about);
        Linkify.addLinks(tView, Linkify.ALL);
        String about=(
                "This app was made in Android Studio by smusings."
                 +"\n "
                + "\n All locations and information on them were acquired through the Factual Java Driver."
                + "\n The map and markers were made using Google Maps V2. "
                +"\n"
                +"\n If you find any errors, or want to provide feedback feel free to email me at: sleeplessmusings@gmail.com"
        );
        tView.setText(about);
    }
}
