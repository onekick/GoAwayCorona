package com.cnu.goawaycorona;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class EditMyPathActivity extends Activity {

    // 초기변수설정
    EditText edit_addr;
    // 주소 요청코드 상수 requestCode
    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;
    int pageNumber = 1;
    int itemNumber = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_my_path);

        Intent intent = getIntent();
        pageNumber = intent.getIntExtra("pageNumber", pageNumber);
        itemNumber = intent.getIntExtra("itemNumber", itemNumber);
        Log.i("EditMyPathActivity", "pageNumber="+pageNumber);
        Log.i("EditMyPathActivity", "itemNumber="+itemNumber);

        // UI 요소 연결
        edit_addr = findViewById(R.id.edit_addr);

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
                    }
                }
                break;
        }
    }


}
