package com.example.lenovo.test_sql.StepCount.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/***
 * 开机完成广播
 * Created by lenovo on 2016/12/6/006.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, StepService.class);
        context.startService(i);
    }
}
