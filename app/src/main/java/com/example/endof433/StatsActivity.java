package com.example.endof433;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
    }

    public void shareButtonOnClick(View v){
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        String shareText = null;
        String shareSubject = "";
        EditText et_gamesA = findViewById(R.id.gamesA);
        String gamesA = "";
        EditText et_gamesB = findViewById(R.id.gamesB);
        String gamesB = "";
        EditText et_pointsWonA = findViewById(R.id.pointsWonA);
        String pointsWonA = "";
        EditText et_pointsWonB = findViewById(R.id.pointsWonB);
        String pointsWonB = "";
        EditText et_winnersA = findViewById(R.id.winnersA);
        String winnersA = "";
        EditText et_winnersB = findViewById(R.id.winnersB);
        String winnersB = "";
        EditText et_unforcedErrorsA = findViewById(R.id.unforcedErrorsA);
        String unforcedErrorsA = "";
        EditText et_unforcedErrorsB = findViewById(R.id.unforcedErrorsB);
        String unforcedErrorsB = "";
        EditText et_matchLocation = findViewById(R.id.matchLocation);
        String matchLocation = "";
        EditText et_matchDate = findViewById(R.id.matchDate);
        String matchDate = "";
        try{
            gamesA = et_gamesA.getText().toString();
            gamesB = et_gamesB.getText().toString();
            pointsWonA = et_pointsWonA.getText().toString();
            pointsWonB = et_pointsWonB.getText().toString();
            winnersA = et_winnersA.getText().toString();
            winnersB = et_winnersB.getText().toString();
            unforcedErrorsA = et_unforcedErrorsA.getText().toString();
            unforcedErrorsB = et_unforcedErrorsB.getText().toString();
            matchLocation = et_matchLocation.getText().toString();
            matchDate = et_matchDate.getText().toString();

            StringBuilder sb = new StringBuilder();
            sb.append("Match Location: "+matchLocation+"\n");
            sb.append("Match Date: "+matchDate+"\n");
            sb.append("Opponent A Games Won: "+gamesA+"\n");
            sb.append("Opponent B Games Won: "+gamesB+"\n");
            sb.append("Opponent A Points Won: "+pointsWonA+"\n");
            sb.append("Opponent B Points Won: "+pointsWonB+"\n");
            sb.append("Opponent A Winners: "+winnersA+"\n");
            sb.append("Opponent B Winners: "+winnersB+"\n");
            sb.append("Opponent A Unforced Errors: "+unforcedErrorsA+"\n");
            sb.append("Opponent B Unforced Errors: "+unforcedErrorsB+"\n");
            shareText = sb.toString();

            StringBuilder sb2 = new StringBuilder();
            sb2.append(matchLocation+"\n");
            sb2.append(matchDate+"\n");
            shareSubject = sb2.toString();
        } catch(Exception e){
            Toast.makeText(getApplicationContext(),"Please fill out all input values",Toast.LENGTH_SHORT).show();
        }

        if(shareText != null){
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, shareSubject);
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
            startActivity(Intent.createChooser(shareIntent, "share via"));
        }
    }
    public void goBack(View v) {
        onBackPressed();
    }
}
