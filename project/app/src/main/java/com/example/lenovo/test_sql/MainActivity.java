package com.example.lenovo.test_sql;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import cn.smssdk.gui.RegisterPage;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener{
    Button login, register;
    public static MyDB myDB;
    EditText username,password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myDB = new MyDB(getApplicationContext());
        init();
    }

    public void init(){
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);

        login = (Button) findViewById(R.id.loginButton);
        register = (Button) findViewById(R.id.registerButton);
        login.setOnClickListener(this);
        register.setOnClickListener(this);
    }
    @Override
    public void onClick(View v){
        Log.v("MyDebug",""+v.getId());
        switch(v.getId()){
            case R.id.loginButton:
                Cursor cursor = myDB.getUserbyName(username.getText().toString(), new String[]{"name","password"});
                Cursor cursorPhone = myDB.getUserbyPhone(username.getText().toString(), new String[]{"name","password"});
                String error = "";
                if(cursor.getCount() == 0 && cursorPhone.getCount() == 0){
                    error = "账号不存在";
                }
                if(cursor.getCount() != 0 && cursor.moveToFirst()){
                    if(password.getText().toString().equals(cursor.getString(cursor.getColumnIndex("password")))){
                        Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                        //储存用户名
                        SharedPreferences saveUser = getSharedPreferences("User", MODE_PRIVATE);
                        SharedPreferences.Editor editor = saveUser.edit();
                        editor.putString("username",username.getText().toString());
                        editor.apply();

                        Intent intent = new Intent(MainActivity.this,StepCountActivity.class);
                        startActivity(intent);
                    } else{
                        error = "密码错误";
                    }
                }
                if(cursorPhone.getCount()!=0 && cursorPhone.moveToFirst()){
                    if(password.getText().toString().equals(cursorPhone.getString(cursorPhone.getColumnIndex("password")))){
                        Toast.makeText(MainActivity.this,"登录成功",Toast.LENGTH_SHORT).show();
                        //储存用户名
                        SharedPreferences saveUser = getSharedPreferences("User", MODE_PRIVATE);
                        SharedPreferences.Editor editor = saveUser.edit();
                        editor.putString("username",cursorPhone.getString(cursorPhone.getColumnIndex("name")));
                        editor.apply();

                        Intent intent = new Intent(MainActivity.this,StepCountActivity.class);
                        startActivity(intent);
                    }else{
                        error = "密码错误";
                    }
                }
                Toast.makeText(MainActivity.this,error,Toast.LENGTH_SHORT).show();
                break;
            case R.id.registerButton:
                //SMSSDK.initSDK(this, "f3fc6baa9ac4", "7f3dedcb36d92deebcb373af921d635a");
                SMSSDK.initSDK(this, "1995457e5870a", "c8d1d355f7b955b4477f7383e828ca18");
                //打开注册页面
                RegisterPage registerPage = new RegisterPage();
                registerPage.setRegisterCallback(new EventHandler() {
                    public void afterEvent(int event, int result, Object data) {
                        // 解析注册结果
                        if (result == SMSSDK.RESULT_COMPLETE) {
                            @SuppressWarnings("unchecked")
                            HashMap<String,Object> phoneMap = (HashMap<String, Object>) data;
                            Log.v("result",result+"");
                            String phone = (String) phoneMap.get("phone");

                            //判断手机号是否已被占用
                            Cursor cursorPhone = myDB.getUserbyPhone(phone, new String[]{"phone"});
                            if(cursorPhone.getCount()!=0){
                                Toast.makeText(MainActivity.this,"手机号已注册,请直接登陆",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Intent intent = new Intent(MainActivity.this,PasswordActivity.class);
                                intent.putExtra("phone",phone);
                                startActivity(intent);
                            }
                        }
                    }
                });
                registerPage.show(getApplicationContext());
                break;
            default:
                Log.v("Mydebug","id not found");
                break;
        }
    }
}
