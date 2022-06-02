package com.cnu.goawaycorona;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final String PREFERENCE_NAME = "MyPreferecne";
    LinearLayout linearLayoutSelectDiseases;
    LinearLayout linearLayoutChooseInformation;
    LinearLayout linearLayoutCheckContact;
    LinearLayout linearLayoutMyPath;
    LinearLayout linearLayoutAreaPathAccuracy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀바 제거
        setContentView(R.layout.activity_main);

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

        linearLayoutSelectDiseases = findViewById(R.id.linearLayoutSelectDiseases);
        linearLayoutChooseInformation = findViewById(R.id.linearLayoutChooseInformation);
        linearLayoutCheckContact = findViewById(R.id.linearLayoutCheckContent);
        linearLayoutMyPath = findViewById(R.id.linearLayoutMyPath);
        linearLayoutAreaPathAccuracy = findViewById(R.id.linearLayoutAreaPathAccuracy);

        linearLayoutSelectDiseases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SelectDiseasesActivity.class);
                startActivity(intent);
            }
        });
        linearLayoutChooseInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ChooseInformationActivity.class);
                startActivity(intent);
            }
        });
        linearLayoutCheckContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, CheckContactActivity.class);
                startActivity(intent);
            }
        });
        linearLayoutMyPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MyPathActivity.class);
                startActivity(intent);
            }
        });
        linearLayoutAreaPathAccuracy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AreaPathActivity.class);
                startActivity(intent);
            }
        });

        ImageView imageViewSetting = findViewById(R.id.imageViewSetting);
        imageViewSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 위치 정확도 업데이트
        TextView textViewAccuracy = findViewById(R.id.textViewAccuracy);
        double accuracy = getAccuracy(1);
        textViewAccuracy.setText(String.format("%.2f", accuracy)+"%");
    }

    private double getAccuracy(int pageNumber) {

        ArrayList<ItemData> arrayList = new ArrayList<>();        // load items
        InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.data));
        BufferedReader reader = new BufferedReader(is);
        CSVReader read = new CSVReader(reader);
        String[] record = null;
        try {
            while ((record = read.readNext()) != null){
                // skip first field information
                if( record[0].contains("case")) continue;
                ItemData item = new ItemData(record);
                // add items
                if( item.getCase_num()==pageNumber) {
                    arrayList.add(item);
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }


        double totalError = 0;
        // 현재 페이지의 모든 인덱스에 대해 저장된 주소를 가져온다.
        for(int i=0; i<arrayList.size(); i++) {
            // 저장된게 있으면 업데이트 시키고 상태를 바꾼다.
            ItemData itemData = (ItemData) arrayList.get(i);
            double error = itemData.getBasic_error();
            if(!itemData.isStatus()) {
                String key = pageNumber+","+i;
                String address = loadPreference(key);
                if( address!=null) {
                    error = 0;
                }
            }
            totalError+=error;
        }

        // 상단 정확도 갱신하기
        // 평균에러 = 에러총합/갯수
        double std_err = totalError/arrayList.size();
        // 정확도 계산법 = 100% - 평균 에러
        double accuracy = 100.0 - std_err;

        return accuracy;
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