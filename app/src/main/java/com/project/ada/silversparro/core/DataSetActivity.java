package com.project.ada.silversparro.core;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.project.ada.silversparro.R;

/**
 * Created by ankitmaheshwari on 8/28/17.
 */

public class DataSetActivity extends AppCompatActivity {

    private static final String TAG = "DataSetActivity";

    EditText input;

    TextInputLayout inputLayout;

    LinearLayout dummyFocus;

    Button continueButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_dataset);

        input = (EditText) findViewById(R.id.et_dataset);
        continueButton = (Button) findViewById(R.id.bt_continue);
        inputLayout = (TextInputLayout) findViewById(R.id.input_data_set);
        dummyFocus = (LinearLayout) findViewById(R.id.dummy_focus);
        dummyFocus.requestFocus();
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick continue");
                submitInput();
            }
        });
    }

    private boolean submitInput(){
        String inputText = input.getText().toString();
        if (TextUtils.isEmpty(inputText)){
            MainApplication.showToast(R.string.please_enter_valid_dataset);
            return false;
        }else {
            Persistence.setDataSetName(inputText);
            Persistence.setImgUrlUnderProgress(null);
            startMainActivity();
            return true;
        }
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
