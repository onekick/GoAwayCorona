package com.cnu.goawaycorona;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SettingActivity extends Activity {
    private static final String PREFERENCE_NAME = "MyPreferecne";
    private MyApp app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀바 제거
        setContentView(R.layout.activity_setting);

        app = (MyApp) getApplication();


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

                app.reset();

                Toast.makeText(getBaseContext(),"데이터가 초기화 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout linearLayoutSave = findViewById(R.id.linearLayoutSave);
        linearLayoutSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("SettingActivity", "linearLayoutSave clicked.");



                // 만족도 확인창 표시
                Dialog dialog = new Dialog(SettingActivity.this);
                dialog.setContentView(R.layout.dialog_satisfication);

                RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup);
                radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                        app.setSatisfied(checkedId == R.id.radioButtonSatisfied);
                    }
                });

                dialog.show();

                // 다이얼로그 돌아가기 버튼
                Button buttonAutoEdit = dialog.findViewById(R.id.buttonCancel);
                buttonAutoEdit.setOnClickListener(new View.OnClickListener() { // 클릭리스터 생성
                    @Override // 부모 메소드 재정의
                    public void onClick(View v) { // 클릭 이벤트 처리
                        dialog.dismiss();
                    }
                });

                // 다이얼로그 선택완료 버튼
                Button buttonDone = dialog.findViewById(R.id.buttonDone);
                buttonDone.setOnClickListener(new View.OnClickListener() { // 클릭리스터 생성
                    @Override // 부모 메소드 재정의
                    public void onClick(View v) { // 클릭 이벤트 처리
                        String filePath = app.save();
                        dialog.dismiss();
                        Toast.makeText(getBaseContext(),"파일명:"+filePath, Toast.LENGTH_LONG).show();
                    }
                });


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