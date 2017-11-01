package com.lewis.clean;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class CleanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonClicked(View view) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this, CleanService.class));
        this.startService(intent);
    }
}
