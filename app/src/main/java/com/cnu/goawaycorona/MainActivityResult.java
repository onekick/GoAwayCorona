package com.cnu.goawaycorona;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivityResult extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_result);

        EditText textView = findViewById(R.id.textViewResult);
        MyApp app = (MyApp)getApplicationContext();
        String result = app.getResult();
        textView.setText(result);
    }
}