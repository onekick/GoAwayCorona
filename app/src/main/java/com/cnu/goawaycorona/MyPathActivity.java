package com.cnu.goawaycorona;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.opencsv.CSVReader;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.BreakIterator;
import java.util.ArrayList;

public class MyPathActivity extends Activity {
    private static final String PREFERENCE_NAME = "MyPreferecne";
    private TextView textViewPageDateTime;
    private int pageNumber = 1;

    private PathView pathView;
    private TextView textViewAccuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀바 제거
        setContentView(R.layout.activity_my_path);

        textViewAccuracy = findViewById(R.id.textViewAccuracy);

        ImageView imageViewHome = findViewById(R.id.imageViewHome);
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ImageView imageViewBack = findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        pathView = findViewById(R.id.pathView);
        pathView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyPathActivity.this, MyPathListActivity.class);
                intent.putExtra("pageNumber", pageNumber);
                startActivityForResult(intent,1);
            }
        });


        ImageView imageViewPrev = findViewById(R.id.imageViewPrev);
        ImageView imageViewNext = findViewById(R.id.imageViewNext);
        textViewPageDateTime = findViewById(R.id.textViewPageDateTime);

        imageViewPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( pageNumber>1) pageNumber--;
                reloadData();
            }
        });
        imageViewNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( pageNumber<10) pageNumber++;
                reloadData();
            }
        });

        reloadData();
    }


    void reloadData() {
        // load items
        InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.data));
        BufferedReader reader = new BufferedReader(is);
        CSVReader read = new CSVReader(reader);
        String[] record = null;
        pathView.clear();
        try {
            while ((record = read.readNext()) != null){
                // skip first field information
                if( record[0].contains("case")) continue;
                ItemData item = new ItemData(record);
                // add items
                if( item.getCase_num()==pageNumber) {
                    pathView.add(item);
                    textViewPageDateTime.setText(item.getTime());
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        pathView.invalidate();

        double accuracy = pathView.getAccuracy(this, pageNumber);
        textViewAccuracy.setText(String.format("%.2f", accuracy)+"%");

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        reloadData();
    }
    public void clearPreference() {
        SharedPreferences preferences =
                getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
    public String loadPreference(String key) {
        SharedPreferences preferences =
                getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return preferences.getString(key, null);
    }

    public void savePreference(String key, String value) {
        SharedPreferences preferences =
                getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }
}

