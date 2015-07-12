package com.klgleb.githubclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;


public class LoginActivity extends AppCompatActivity {

    private EditText mPassTxt;
    private EditText mLoginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mLoginText = (EditText) findViewById(R.id.loginTxt);
        mPassTxt = (EditText) findViewById(R.id.passwTxt);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }


    public void onClick(View view) {
        Intent intent = new Intent();

        intent.putExtra(MainActivity.LOGIN_KEY, mLoginText.getText().toString());
        intent.putExtra(MainActivity.PASS_KEY, mPassTxt.getText().toString());


        setResult(RESULT_OK, intent);

        finish();
    }
}
