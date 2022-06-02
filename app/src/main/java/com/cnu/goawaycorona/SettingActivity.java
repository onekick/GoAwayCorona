package com.cnu.goawaycorona;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SettingActivity extends Activity {
    private static final String PREFERENCE_NAME = "MyPreferecne";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀바 제거
        setContentView(R.layout.activity_setting);

        ImageView imageViewHome = findViewById(R.id.imageViewHome);
        imageViewHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        ImageView imageViewBack = findViewById(R.id.imageViewBack);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        LinearLayout linearLayoutReset = findViewById(R.id.linearLayoutReset);
        linearLayoutReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SettingActivity", "linearLayoutReset clicked.");
                clearPreference();
                Toast.makeText(getBaseContext(),"데이터가 초기화 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

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