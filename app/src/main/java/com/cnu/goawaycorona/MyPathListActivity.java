package com.cnu.goawaycorona;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.opencsv.CSVReader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class MyPathListActivity extends Activity {

    private PathListView listView;
    private PathAdapter adapter;
    private int pageNumber = 1;
    private int indexNumber = 0;
    private TextView textViewPageDateTime;
    private Dialog dialog;
    private EditText edit_addr;
    private static final String PREFERENCE_NAME = "MyPreferecne";


    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;
    private TextView textViewAccuracy;
    private TextView textViewDateTimeAfter;
    private TextView textViewAddressAfter;
    private TextView textViewStatusAfter;
    private MyApp app;
    private boolean isModifiedAutomatically = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀바 제거
        setContentView(R.layout.activity_my_path_list);

        app = (MyApp)getApplicationContext();
        app.setStartTime();

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

        textViewPageDateTime = findViewById(R.id.textViewPageDateTime);

        Intent intent = getIntent();
        pageNumber = intent.getIntExtra("pageNumber",1);

        listView = findViewById(R.id.pathListView);
        adapter = new PathAdapter(this);
        listView.setAdapter(adapter);

        reloadListView();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                Intent intent = new Intent(MyPathListActivity.this, EditMyPathActivity.class);
//                intent.putExtra("pageNumber", pageNumber);
//                intent.putExtra("itemNumber", i);
//                startActivityForResult(intent,1);

                // 수정필요 아이템이 아니면 스킵한다.
                ItemData itemData = (ItemData) adapter.getItem(i);
                if(itemData.isStatus()) return;

                String value = loadPreference(pageNumber+","+i);
                if(value!=null && itemData.getAns().equals(value)) return;

                indexNumber = i;
                dialog = new Dialog(MyPathListActivity.this);
                dialog.setContentView(R.layout.dialog_edit_my_path);


                // UI 요소 연결
                edit_addr = dialog.findViewById(R.id.edit_addr);

                // 터치 안되게 막기
                edit_addr.setFocusable(false);

                // 주소입력창 클릭
                edit_addr.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("주소설정페이지", "주소입력창 클릭");
                        int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                        if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {

                            Log.i("주소설정페이지", "주소입력창 클릭");
                            Intent i = new Intent(getApplicationContext(), AddressApiActivity.class);
                            // 화면전환 애니메이션 없애기
                            overridePendingTransition(0, 0);
                            // 주소결과
                            startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);

                        }else {
                            Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // 다이얼로그에 기존 주소 정보 표시하기
                TextView textViewDateTime = dialog.findViewById(R.id.textViewDateTime);
                TextView textViewAddress = dialog.findViewById(R.id.textViewAddress);
                TextView textViewStatus = dialog.findViewById(R.id.textViewStatus);

                textViewDateTime.setText(itemData.getTime());
                textViewAddress.setText(itemData.getAns());

                if(itemData.isStatus()) {
                    textViewStatus.setText("정상");
                    textViewDateTime.setBackgroundColor(Color.argb(255,255,255,255));
                    textViewAddress.setBackgroundColor(Color.argb(255,255,255,255));
                    textViewStatus.setBackgroundColor(Color.argb(255,0,196,0));
                }
                else {
                    // 수정한 주소가 있으면 그걸로 표시한다.
                    String address = itemData.getAddress();
                    if(address==null) {
                        textViewStatus.setText("수정필요");
                        textViewDateTime.setBackgroundColor(Color.argb(255,255,196,196));
                        textViewAddress.setBackgroundColor(Color.argb(255,255,196,196));
                        textViewStatus.setBackgroundColor(Color.argb(255,196,0,0));
                        textViewAddress.setText("없음");
                    }
                    else {
                        // 만약 주소가 맞으면 올바르게 표시한다.
                        if(address.equals(itemData.getAns())) {
                            textViewStatus.setText("정상");
                            textViewDateTime.setBackgroundColor(Color.argb(255,255,255,255));
                            textViewAddress.setBackgroundColor(Color.argb(255,255,255,255));
                            textViewStatus.setBackgroundColor(Color.argb(255,0,196,0));
                            textViewAddress.setText(itemData.getAns());
                        }
                        else {
                            // 주소가 다르면 주소는 표시하되 비정상으로 표시한다.
                            textViewStatus.setText("수정필요");
                            textViewDateTime.setBackgroundColor(Color.argb(255,255,196,196));
                            textViewAddress.setBackgroundColor(Color.argb(255,255,196,196));
                            textViewStatus.setBackgroundColor(Color.argb(255,196,0,0));
                            textViewAddress.setText(address);
                        }


                    }
                }

                // 다이얼로그에 수정하게 될 정보 표시하기
                textViewDateTimeAfter = dialog.findViewById(R.id.textViewDateTimeAfter);
                textViewAddressAfter = dialog.findViewById(R.id.textViewAddressAfter);
                textViewStatusAfter = dialog.findViewById(R.id.textViewStatusAfter);

                updateDialog(itemData);

                dialog.show();

                // 자동수정 버튼
                Button buttonAutoEdit = dialog.findViewById(R.id.buttonAutoEdit);
                buttonAutoEdit.setOnClickListener(new View.OnClickListener() { // 클릭리스터 생성
                    @Override // 부모 메소드 재정의
                    public void onClick(View v) { // 클릭 이벤트 처리
                        String address = itemData.getAns();
                        edit_addr.setText(address);
                        updateDialog(itemData);
                        isModifiedAutomatically = true;
                    }
                });

                // 수정완료 버튼
                Button buttonDone = dialog.findViewById(R.id.buttonDone);
                buttonDone.setOnClickListener(new View.OnClickListener() { // 클릭리스터 생성
                    @Override // 부모 메소드 재정의
                    public void onClick(View v) { // 클릭 이벤트 처리
                        String address = edit_addr.getText().toString();
                        if(address==null || address.length()==0) {
                            Toast.makeText(MyPathListActivity.this,"주소를 입력해주세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        dialog.dismiss();
                        savePreference(pageNumber+","+indexNumber, address);

                        // 변경된 위치 값을 저장한다.
                        app.setModification(itemData.getCase_num(),itemData.sequence_num, isModifiedAutomatically, address);

                        isModifiedAutomatically=false;

                        reloadListView();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.setStopTime(pageNumber);
    }

    private void updateDialog(ItemData itemData) {
        textViewDateTimeAfter.setText(itemData.getTime());

        String address = edit_addr.getText().toString();

        // 수정한 주소가 있으면 그걸로 표시한다.
        if(address==null) {
            textViewStatusAfter.setText("수정필요");
            textViewDateTimeAfter.setBackgroundColor(Color.argb(255,255,196,196));
            textViewAddressAfter.setBackgroundColor(Color.argb(255,255,196,196));
            textViewStatusAfter.setBackgroundColor(Color.argb(255,196,0,0));
            textViewAddressAfter.setText("없음");
        }
        else {
            // 만약 주소가 맞으면 올바르게 표시한다.
            if(address.equals(itemData.getAns())) {
                textViewStatusAfter.setText("정상");
                textViewDateTimeAfter.setBackgroundColor(Color.argb(255,255,255,255));
                textViewAddressAfter.setBackgroundColor(Color.argb(255,255,255,255));
                textViewStatusAfter.setBackgroundColor(Color.argb(255,0,196,0));
                textViewAddressAfter.setText(itemData.getAns());
            }
            else {
                // 주소가 다르면 주소는 표시하되 비정상으로 표시한다.
                textViewStatusAfter.setText("수정필요");
                textViewDateTimeAfter.setBackgroundColor(Color.argb(255,255,196,196));
                textViewAddressAfter.setBackgroundColor(Color.argb(255,255,196,196));
                textViewStatusAfter.setBackgroundColor(Color.argb(255,196,0,0));
                textViewAddressAfter.setText(address);
            }
        }
    }

    void reloadListView() {
        // load items
        InputStreamReader is = new InputStreamReader(getResources().openRawResource(R.raw.data));
        BufferedReader reader = new BufferedReader(is);
        CSVReader read = new CSVReader(reader);
        String[] record = null;
        adapter.removeAllItems();
        try {
            int sequence_num = 0;
            int case_num = 1;
            while ((record = read.readNext()) != null){
                // skip first field information
                if( record[0].contains("case")) continue;
                ItemData item = new ItemData(record);

                // 각 케이스 내에서 순서를 sequence_num에 저장시켜준다.
                if( case_num == item.getCase_num()) {
                    item.sequence_num = sequence_num;
                    sequence_num++;
                }
                else {
                    case_num = item.getCase_num();
                    sequence_num = 0;
                }

                // add items
                if( item.getCase_num()==pageNumber) {
                    adapter.addItem(item);
                    textViewPageDateTime.setText(item.getTime());
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        // preference의 저장 값을 읽고 adapter가 가진 정보를 업데이트 시켜 준다.

        double totalError = 0;
        // 현재 페이지의 모든 인덱스에 대해 저장된 주소를 가져온다.
        for(int i=0; i<adapter.getCount(); i++) {
            // 저장된게 있으면 업데이트 시키고 상태를 바꾼다.
            ItemData itemData = (ItemData) adapter.getItem(i);
            double error = itemData.getBasic_error();
            if(!itemData.isStatus()) {
                String key = pageNumber+","+i;
                String address = loadPreference(key);
                if( address!=null) {
                    adapter.setAddress(i,address);
                    error = 0;
                }
            }
            totalError+=error;
        }

        adapter.notifyDataSetChanged();

        // 상단 정확도 갱신하기
        // 평균에러 = 에러총합/갯수
        double std_err = totalError/adapter.getCount();
        // 정확도 계산법 = 100% - 평균 에러
        double accuracy = 100.0 - std_err;
        textViewAccuracy.setText(String.format("%.2f", accuracy)+"%");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("test", "onActivityResult");

        switch (requestCode) {
            case SEARCH_ADDRESS_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String data = intent.getExtras().getString("data");
                    if (data != null) {
                        Log.i("test", "data:" + data);
                        edit_addr.setText(data);
                        isModifiedAutomatically = false;
                        ItemData itemData =(ItemData) adapter.getItem(indexNumber);
                        updateDialog(itemData);
                    }
                }
                break;
        }
    }

    private void clearPreference() {
        SharedPreferences preferences =
                getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }
    private String loadPreference(String key) {
        SharedPreferences preferences =
                getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        return preferences.getString(key, null);
    }

    private void savePreference(String key, String value) {
        SharedPreferences preferences =
                getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor= preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }


}