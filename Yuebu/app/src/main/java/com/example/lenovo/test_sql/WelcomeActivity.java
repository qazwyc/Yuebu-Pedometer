package com.example.lenovo.test_sql;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        new Thread(){
            public void run(){
                try{
                    sleep(1000);

                    SharedPreferences saveUser = getSharedPreferences("User",MODE_PRIVATE);
                    if(saveUser.contains("username")){
                        Intent intent = new Intent(WelcomeActivity.this,StepCountActivity.class);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);
                        startActivity(intent);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
