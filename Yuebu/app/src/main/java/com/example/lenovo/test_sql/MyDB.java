package com.example.lenovo.test_sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lenovo on 2016/11/16/016.
 */
public class MyDB extends SQLiteOpenHelper{
    private static String DB_NAME="Pedometer";
    private static final String TABLE_NAME="user";
    private static final int DB_VERSION=1;
    public MyDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase){
        String CREATE_TABLE="CREATE TABLE if not exists "+TABLE_NAME+
                " (_id INTEGER PRIMARY KEY,name Text,password,phone Text,sex Text,age INTEGER,height INTEGER,weight INTEGER,daysteps INTEGER)";
        sqLiteDatabase.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int j){
    }
    //插入
    public void insert2User(String name, String password, String phone, String sex, int age, int height, int weight, int steps){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("password", password);
        contentValues.put("phone", phone);
        contentValues.put("sex", sex);
        contentValues.put("age", age);
        contentValues.put("height", height);
        contentValues.put("weight",weight);
        contentValues.put("daysteps",steps);
        db.insert(TABLE_NAME, null, contentValues);
        db.close();
    }
    //更新对应姓名记录
    public void updateUser(String name, String password, String phone, String sex, int age, int height, int weight, int steps){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("password", password);
        contentValues.put("phone", phone);
        contentValues.put("sex", sex);
        contentValues.put("age", age);
        contentValues.put("height", height);
        contentValues.put("weight",weight);
        contentValues.put("daysteps",steps);
        db.update(TABLE_NAME, contentValues, "name=?", new String[]{name});
        db.close();
    }
    //返回对应姓名记录
    public Cursor getUserbyName(String name, String[] select){
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME, select, "name=?",new String[]{name}, null, null, null, null);
    }
    //返回对应手机号记录
    public Cursor getUserbyPhone(String phone, String[] select){
        SQLiteDatabase db = getReadableDatabase();
        return db.query(TABLE_NAME, select, "phone=?",new String[]{phone}, null, null, null, null);
    }
}
