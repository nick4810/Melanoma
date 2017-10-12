package com.example.nick.melanoma;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);


    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.goToLogin:
                goToLogin();
                break;
        }
    }

    public void goToLogin() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);

    }

}
