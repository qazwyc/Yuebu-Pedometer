package com.example.lenovo.test_sql;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.lenovo.test_sql.Chat.ChatActivity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserActivity extends AppCompatActivity implements View.OnClickListener{
    Button logout,chat,info_edit;
    ImageButton StepCountMain;
    ListView info_list;
    String username;
    String[] info_values;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        init();
        fillList();
    }
    private void fillList(){

        SharedPreferences saveUser = getSharedPreferences("User", MODE_PRIVATE);
        username = saveUser.getString("username",null);
        info_edit.setText(username+"的个人主页");
        MainActivity.myDB = new MyDB(getApplicationContext());
        String[] info_types = new String[]{"password","phone","sex","age","height","weight","daysteps"};
        final String[] info_type = new String[]{"新密码","手机","性别","年龄","身高(cm)","体重(Kg)","每日计划步数"};
        Cursor cursor = MainActivity.myDB.getUserbyName(username, info_types);
        cursor.moveToFirst();
        info_values = new String[]{cursor.getString(cursor.getColumnIndex("password")),cursor.getString(cursor.getColumnIndex("phone"))
            ,cursor.getString(cursor.getColumnIndex("sex")),cursor.getInt(cursor.getColumnIndex("age"))+"",
                cursor.getInt(cursor.getColumnIndex("height"))+"",cursor.getInt(cursor.getColumnIndex("weight"))+"",
                cursor.getInt(cursor.getColumnIndex("daysteps"))+""};
        //填充列表数据
        final List<Map<String,Object>> info_items = new ArrayList<>();
        for (int i=1;i <= 6;i++){
            Map<String,Object> temp= new LinkedHashMap<>();
            temp.put("info_type",info_type[i]);
            temp.put("info_value",info_values[i]);
            info_items.add(temp);
        }
        //标准体重
        int height = cursor.getInt(cursor.getColumnIndex("height"));
        int weight = cursor.getInt(cursor.getColumnIndex("weight"));
        int weight_recommend;
        if(cursor.getString(cursor.getColumnIndex("sex")).equals("男")){
            weight_recommend = (int)( (height-100)*0.90 );
        }else{
            weight_recommend = (int)( (height-100)*0.92 );
        }
        double BMI = Math.round( weight/(height*height/10000.0) *100)/100;
        Map<String,Object> temp1= new LinkedHashMap<>();
        temp1.put("info_type","推荐体重");
        temp1.put("info_value",weight_recommend);
        info_items.add(temp1);
        //BMI指数
        Map<String,Object> temp2= new LinkedHashMap<>();
        temp2.put("info_type","BMI");
        temp2.put("info_value",BMI);
        info_items.add(temp2);

        final SimpleAdapter simpleAdapter = new SimpleAdapter(this,info_items,R.layout.info_item,
                new String[] {"info_type","info_value"},new int[] {R.id.type,R.id.value});
        info_list.setAdapter(simpleAdapter);

        //点击显示详情
        final AlertDialog.Builder modify_item = new AlertDialog.Builder(UserActivity.this);
        modify_item.create();
        info_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position >= 6)   return;
                final int tobemodify = position;
                //自定义对话框样式
                LayoutInflater factory = LayoutInflater.from(UserActivity.this);
                View v = factory.inflate(R.layout.modify_info,null);
                modify_item.setView(v);
                //获取控件
                final TextView name_type = (TextView) v.findViewById(R.id.name_type);
                name_type.setText(info_type[tobemodify+1]);
                final EditText name_modify = (EditText) v.findViewById(R.id.name_mdy);
                name_modify.setText(info_items.get(tobemodify).get("info_value").toString());
                modify_item.setTitle("个人信息修改").setPositiveButton("保存修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        info_values[tobemodify+1] = name_modify.getText().toString();
                        MainActivity.myDB.updateUser(username,info_values[0],info_values[1],info_values[2],Integer.parseInt(info_values[3])
                                ,Integer.parseInt(info_values[4]), Integer.parseInt(info_values[5]), Integer.parseInt(info_values[6]));
                        if(tobemodify == 1 || tobemodify == 3 || tobemodify == 4){
                            int weight_recommend;
                            int height = Integer.parseInt(info_values[4]);
                            int weight = Integer.parseInt(info_values[5]);
                            if(info_values[2].equals("男")){
                                weight_recommend = (int)( (height-100)*0.90 );
                            }else{
                                weight_recommend = (int)( (height-100)*0.92 );
                            }
                            double BMI = Math.round( weight/(height*height/10000.0) *100)/100;
                            info_items.get(6).put("info_value",weight_recommend+"");
                            info_items.get(7).put("info_value",BMI+"");
                        }
                        info_items.get(tobemodify).put("info_value",name_modify.getText().toString());
                        simpleAdapter.notifyDataSetChanged();
                    }
                }).setNegativeButton("放弃修改", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
            }
        });
    }
    private void init(){
        info_list = (ListView) findViewById(R.id.info_list);
        info_edit =(Button) findViewById(R.id.info_edit);
        logout = (Button) findViewById(R.id.log_out);
        logout.setOnClickListener(this);
        chat = (Button) findViewById(R.id.chat);
        chat.setOnClickListener(this);
        StepCountMain = (ImageButton) findViewById(R.id.imageButtonStepCountMain);
        StepCountMain.setOnClickListener(this);
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.log_out:
                SharedPreferences saveUser = getSharedPreferences("User", MODE_PRIVATE);
                SharedPreferences.Editor editor = saveUser.edit();
                editor.remove("username");
                editor.apply();

                Intent intent = new Intent(UserActivity.this,MainActivity.class);
                startActivity(intent);
                break;
            case R.id.chat:
                startActivity(new Intent(UserActivity.this,ChatActivity.class));
                break;
            case R.id.imageButtonStepCountMain:
                startActivity(new Intent(UserActivity.this,StepCountActivity.class));
                break;
            default: break;
        }
    }
}
