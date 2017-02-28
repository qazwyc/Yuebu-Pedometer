package com.example.lenovo.test_sql.Chat;

import android.text.Spanned;

/**
 * Created by lenovo on 2016/12/14/014.
 */
public class ChatMsgEntity {
    private String name;//消息来自
    private String date;//消息日期
    private Spanned message;//消息内容
    private boolean isComMeg = true;// 是否为收到的消息

    public ChatMsgEntity(String name, String date, Spanned text, boolean isComMsg) {
        super();
        this.name = name;
        this.date = date;
        this.message = text;
        this.isComMeg = isComMsg;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Spanned getMessage() {
        return message;
    }

    public void setMessage(Spanned message) {
        this.message = message;
    }

    public boolean getMsgType() {
        return isComMeg;
    }

    public void setMsgType(boolean isComMsg) {
        isComMeg = isComMsg;
    }

    public ChatMsgEntity() {
    }

}

