package com.klgleb.githubclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


public class LoginActivity extends Activity {

    private EditText mPassTxt;
    private EditText mLoginText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        mLoginText = (EditText) findViewById(R.id.loginTxt);
        mPassTxt = (EditText) findViewById(R.id.passwTxt);

        if (getActionBar() != null) {
            getActionBar().hide();
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
