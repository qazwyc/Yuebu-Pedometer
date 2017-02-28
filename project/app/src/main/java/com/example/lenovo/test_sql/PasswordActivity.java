package com.example.lenovo.test_sql;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class PasswordActivity extends AppCompatActivity {
    EditText name, password, confirm, age, height, weight;
    RadioGroup sex;
    Button regist;
    String sexType;
    String phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        init();
        Intent intent = getIntent();
        phone = intent.getStringExtra("phone");
        regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sex.getCheckedRadioButtonId() == R.id.boy){
                    sexType = "男";
                }else{
                    sexType = "女";
                }
                if(TextUtils.isEmpty(name.getText())){
                    Toast.makeText(PasswordActivity.this,"用户名不能为空",Toast.LENGTH_SHORT).show();
                }
                else if(MainActivity.myDB.getUserbyName(name.getText().toString(), new String[]{"name"}).getCount() != 0){
                    Toast.makeText(PasswordActivity.this,"用户名已被占用",Toast.LENGTH_SHORT).show();
                }
                else if(name.getText().toString().length() > 20) {
                    Toast.makeText(PasswordActivity.this, "用户名不得多于20个字符", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(password.getText())){
                    Toast.makeText(PasswordActivity.this,"密码不能为空",Toast.LENGTH_SHORT).show();
                }
                else if(password.getText().toString().length() < 4 || password.getText().toString().length() > 16){
                    Toast.makeText(PasswordActivity.this, "密码不得少于4个字符，多于16个字符", Toast.LENGTH_SHORT).show();
                }
                else if(!TextUtils.equals(password.getText(), confirm.getText())){
                    Toast.makeText(PasswordActivity.this, "两次密码输入不一致", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(age.getText())){
                    Toast.makeText(PasswordActivity.this, "年龄不能为空", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(height.getText())){
                    Toast.makeText(PasswordActivity.this, "身高不能为空", Toast.LENGTH_SHORT).show();
                }
                else if(TextUtils.isEmpty(height.getText())){
                    Toast.makeText(PasswordActivity.this, "体重不能为空", Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.v("MyDebug",name.getText().toString()+ " "+password.getText().toString()+" "+phone+" "+sexType + " "+
                            Integer.parseInt(age.getText().toString())+" "+Integer.parseInt(height.getText().toString())+" "+
                            Integer.parseInt(weight.getText().toString())+" ");
                    MainActivity.myDB.insert2User(name.getText().toString(),password.getText().toString(),phone,
                            sexType,Integer.parseInt(age.getText().toString()),Integer.parseInt(height.getText().toString()),
                            Integer.parseInt(weight.getText().toString()),5000);
                    Toast.makeText(PasswordActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    //储存用户名
                    SharedPreferences saveUser = getSharedPreferences("User", MODE_PRIVATE);
                    SharedPreferences.Editor editor = saveUser.edit();
                    editor.putString("username",name.getText().toString());
                    editor.apply();

                    startActivity(new Intent(PasswordActivity.this,StepCountActivity.class));
                    finish();
                }
            }
        });
    }
    public void init(){
        name = (EditText) findViewById(R.id.name);
        password = (EditText) findViewById(R.id.password);
        confirm = (EditText) findViewById(R.id.confirm);
        age = (EditText) findViewById(R.id.age);
        height = (EditText) findViewById(R.id.height);
        weight = (EditText) findViewById(R.id.weight);
        sex = (RadioGroup) findViewById(R.id.sex);
        regist = (Button) findViewById(R.id.submit);
    }
}
