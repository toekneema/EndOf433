package com.example.endof433;

import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class RatingActivity extends AppCompatActivity {

    private LinearLayout originalFrame;

    private int initialRating;
    private int[] opponentRatings;
    private int newRating;

    private int changingRating;
    private int netPoints;
    private int bestWin;
    private int worstLoss;

    private boolean[] wonOrLost;
    final static int totalMatches = 7;
    private int numOfMatches = 0;

    private EditText initial;
    private TextView finalTextView;
    private EditText[] editTexts;
    private Spinner[] spinners;
    private Button calculate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        //flag animation code; 3 lines
        final ImageView flag = (ImageView) findViewById(R.id.flaganimation);
        flag.setBackgroundResource(R.drawable.flaganimation);
        ((AnimationDrawable) flag.getBackground()).start();

        initial = (EditText) findViewById(R.id.initialRating);
        finalTextView = (TextView) findViewById(R.id.newnew);

        editTexts = new EditText[totalMatches];

        editTexts[0] = (EditText) findViewById(R.id.opponent1Rating);
        editTexts[1] = (EditText) findViewById(R.id.opponent2Rating);
        editTexts[2] = (EditText) findViewById(R.id.opponent3Rating);
        editTexts[3] = (EditText) findViewById(R.id.opponent4Rating);
        editTexts[4] = (EditText) findViewById(R.id.opponent5Rating);
        editTexts[5] = (EditText) findViewById(R.id.opponent6Rating);
        editTexts[6] = (EditText) findViewById(R.id.opponent7Rating);

        spinners = new Spinner[totalMatches];
        String[] items = new String[]{"Won", "Lost"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_spinner_dropdown_item, items);

        spinners[0] = (Spinner) findViewById(R.id.spinner1);
        spinners[0].setAdapter(adapter);
        spinners[1] = (Spinner) findViewById(R.id.spinner2);
        spinners[1].setAdapter(adapter);
        spinners[2] = (Spinner) findViewById(R.id.spinner3);
        spinners[2].setAdapter(adapter);
        spinners[3] = (Spinner) findViewById(R.id.spinner4);
        spinners[3].setAdapter(adapter);
        spinners[4] = (Spinner) findViewById(R.id.spinner5);
        spinners[4].setAdapter(adapter);
        spinners[5] = (Spinner) findViewById(R.id.spinner6);
        spinners[5].setAdapter(adapter);
        spinners[6] = (Spinner) findViewById(R.id.spinner7);
        spinners[6].setAdapter(adapter);

        calculate = (Button) findViewById(R.id.calculate);

        getStartedMethod();
    }

    private void getStartedMethod() {


        opponentRatings = new int[totalMatches];
        wonOrLost = new boolean[totalMatches];

        for (int i = 0; i < totalMatches; i++) {
            wonOrLost[i] = false;
        }
    }

    public void buttonClick(View v) {

        try {
            initialRating = Integer.parseInt(initial.getText().toString()); //could break if they put in non-number
            Log.v("INITIAL", "" + initialRating);


            for (int i = 0; i < totalMatches; i++) {
                String curr = editTexts[i].getText().toString();
                if (curr.matches("")) {  //come back to this later, cuz it's faulty if i enter a number in opponent4 but not for opponent 3
                    break;
                }

                numOfMatches++;

                opponentRatings[i] = Integer.parseInt(curr);
                String status = spinners[i].getSelectedItem().toString();
                if (status.matches("Won")) {
                    wonOrLost[i] = true;
                } else if (status.matches("Lost")) {
                    wonOrLost[i] = false;
                }
            }

            changingRating = initialRating;

            calculateRating(initialRating); //the big call
            clearAll(); //resets so that I can do use calculate button multiple times

        } catch(Exception e) {
            e.printStackTrace();
            Log.v("ERROR CAUGHT", "no values entered yet");
            Toast t = Toast.makeText(this,"No values entered",Toast.LENGTH_SHORT);
            t.show();
        }
    }

    private void calculateRating(int rating) {

        for (int i = 0; i < numOfMatches; i++) {
            int diff = (rating - opponentRatings[i]);
            changingRating = ratingChart(diff, wonOrLost[i]);
        }

        //find bestWin
        bestWin = 0;
        for (int i = 0; i < numOfMatches; i++) {
            if (wonOrLost[i]) {
                if (opponentRatings[i] > bestWin) {
                    bestWin = opponentRatings[i];
                }
            }
        }

        //find worstLoss
        worstLoss = 3000;
        for (int i = 0; i < numOfMatches; i++) {
            if (!wonOrLost[i]) {
                if (opponentRatings[i] < worstLoss) {
                    worstLoss = opponentRatings[i];
                }
            }
        }

        netPoints = changingRating - rating;

        if (netPoints >= 75) {
            tier2Rerating();
            calculateRating(initialRating);
        } else if (netPoints >= 50) {
            tier1Rerating(netPoints);
            calculateRating(initialRating);
        }

        newRating = changingRating;

        finalTextView.setText("" + newRating);
    }

    private int ratingChart(int diff, boolean win) {

        if (win) {
            if (diff >= 238) {
                changingRating += 0;
            } else if (diff >= 213) {
                changingRating += 1;
            } else if (diff >= 188) {
                changingRating += 1;
            } else if (diff >= 163) {
                changingRating += 2;
            } else if (diff >= 138) {
                changingRating += 2;
            } else if (diff >= 113) {
                changingRating += 3;
            } else if (diff >= 88) {
                changingRating += 4;
            } else if (diff >= 63) {
                changingRating += 5;
            } else if (diff >= 38) {
                changingRating += 6;
            } else if (diff >= 13) {
                changingRating += 7;
            } else if (diff >= 0) {
                changingRating += 8;
            } else if (diff >= -12) {
                changingRating += 8;
            } else if (diff >= -37) {
                changingRating += 10;
            } else if (diff >= -62) {
                changingRating += 13;
            } else if (diff >= -87) {
                changingRating += 16;
            } else if (diff >= -112) {
                changingRating += 20;
            } else if (diff >= -137) {
                changingRating += 25;
            } else if (diff >= -162) {
                changingRating += 30;
            } else if (diff >= -187) {
                changingRating += 35;
            } else if (diff >= -212) {
                changingRating += 40;
            } else if (diff >= -237) {
                changingRating += 45;
            } else {
                changingRating += 50;
            }
        } else {
            if (diff >= 238) {
                changingRating -= 50;
            } else if (diff >= 213) {
                changingRating -= 45;
            } else if (diff >= 188) {
                changingRating -= 40;
            } else if (diff >= 163) {
                changingRating -= 35;
            } else if (diff >= 138) {
                changingRating -= 30;
            } else if (diff >= 113) {
                changingRating -= 25;
            } else if (diff >= 88) {
                changingRating -= 20;
            } else if (diff >= 63) {
                changingRating -= 16;
            } else if (diff >= 38) {
                changingRating -= 13;
            } else if (diff >= 13) {
                changingRating -= 10;
            } else if (diff >= 0) {
                changingRating -= 8;
            } else if (diff >= -12) {
                changingRating -= 8;
            } else if (diff >= -37) {
                changingRating -= 7;
            } else if (diff >= -62) {
                changingRating -= 6;
            } else if (diff >= -87) {
                changingRating -= 5;
            } else if (diff >= -112) {
                changingRating -= 4;
            } else if (diff >= -137) {
                changingRating -= 3;
            } else if (diff >= -162) {
                changingRating -= 2;
            } else if (diff >= -187) {
                changingRating -= 2;
            } else if (diff >= -212) {
                changingRating -= 1;
            } else if (diff >= -237) {
                changingRating -= 1;
            } else {
                changingRating -= 0;
            }
        }
        return changingRating;
    }

    private void tier2Rerating() {
        boolean allWins;
        for (int i = 0; i < totalMatches; i++) {
            if (!wonOrLost[i]) {
                allWins = false;
                break;
            }
        }
        allWins = true;

        if (allWins) { //need to figure out what median implied rating means
            initialRating += 100;
        } else {
            int avg = (bestWin + worstLoss)/2;
            initialRating = (initialRating + avg) / 2;
        }
    }

    private void tier1Rerating(int netPoints) {
        initialRating += netPoints;
    }

    private void clearAll() {
        numOfMatches = 0;
        initialRating = 0;
        newRating = 0;
        bestWin = 0;
        worstLoss = 0;

        getStartedMethod();

    }
    public void goBack(View v) {
        onBackPressed();
    }
}
