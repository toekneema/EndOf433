package com.example.endof433;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void one (View view) {
        Intent intent = new Intent(this, RatingActivity.class);
        startActivity(intent);
    }

    public void two (View view) {
        Intent intent = new Intent(this, FindClubsActivity.class);
        startActivity(intent);
    }

    public void three (View view) {
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }

    public void four (View view) {
        Intent intent = new Intent(this, NotificationActivity.class);
        startActivity(intent);
    }

}
