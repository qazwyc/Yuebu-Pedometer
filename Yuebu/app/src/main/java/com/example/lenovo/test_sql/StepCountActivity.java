package com.example.lenovo.test_sql;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Image;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.lenovo.test_sql.StepCount.UI.RoundProgressView;
import com.example.lenovo.test_sql.StepCount.config.Constant;
import com.example.lenovo.test_sql.StepCount.service.StepService;

public class StepCountActivity extends AppCompatActivity  implements Handler.Callback, View.OnClickListener{
    //循环取当前时刻的步数中间的间隔时间
    long TIME_INTERVAL = 500;
    private TextView steps,dayGoal;
    ImageButton UserMain;
    Button navigate;
    TextView distance, kcal;
    private RoundProgressView roundProgressView;
    private Messenger messenger;
    private Messenger mGetReplyMessenger = new Messenger(new Handler(this));
    private Handler delayHandler;
    private int weight, daysteps;

    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            try {
                messenger = new Messenger(service);
                Message msg = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                msg.replyTo = mGetReplyMessenger;
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.MSG_FROM_SERVER:
                // 更新界面上的步数
                steps.setText(msg.getData().getInt("step") + "");
                //Log.i("MyDebug","msg.step: "+msg.getData().getInt("step") + "");
                roundProgressView.setMax(daysteps);
                dayGoal.setText("每日目标"+daysteps+"步");
                roundProgressView.setProgress(msg.getData().getInt("step"));
                double distances = msg.getData().getInt("step") * 0.5 / 1000;
                double kcals = distances * weight * 1.036; //体重（kg）×距离（公里）×1.036
                distance.setText("距离："+ Math.round(distances*100)/100.0 +"公里");
                kcal.setText("消耗卡路里："+ Math.round(kcals*100)/100.0 +"kcal");
                delayHandler.sendEmptyMessageDelayed(Constant.REQUEST_SERVER, TIME_INTERVAL);
                break;
            case Constant.REQUEST_SERVER:
                try {
                    Message msg1 = Message.obtain(null, Constant.MSG_FROM_CLIENT);
                    msg1.replyTo = mGetReplyMessenger;
                    messenger.send(msg1);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        init();
    }

    public void onClick(View v){
        switch(v.getId()){
            case R.id.UserMain:
                Intent intent = new Intent(StepCountActivity.this, UserActivity.class);
                startActivity(intent);
                break;
            case R.id.navigate:
                startActivity(new Intent(StepCountActivity.this, NavigateActivity.class));
                break;
            default: break;
        }
    }

    private void init() {
        steps = (TextView) findViewById(R.id.steps);
        roundProgressView = (RoundProgressView) findViewById(R.id.stepProgress);
        dayGoal = (TextView) findViewById(R.id.dayGoal);
        distance = (TextView) findViewById(R.id.distance);
        kcal = (TextView) findViewById(R.id.kcal);
        UserMain = (ImageButton) findViewById(R.id.UserMain);
        UserMain.setOnClickListener(this);
        navigate = (Button) findViewById(R.id.navigate);
        navigate.setOnClickListener(this);
        delayHandler = new Handler(this);
        //读取用户信息，包括当日步数、每日目标等
        SharedPreferences saveUser = getSharedPreferences("User",MODE_PRIVATE);
        String username = saveUser.getString("username",null);
        MainActivity.myDB = new MyDB(getApplicationContext());
        Cursor cursor = MainActivity.myDB.getUserbyName(username, new String[]{"weight","daysteps"});
        cursor.moveToFirst();
        weight = cursor.getInt(cursor.getColumnIndex("weight"));
        daysteps = cursor.getInt(cursor.getColumnIndex("daysteps"));
    }
    @Override
    protected void onStart() {
        super.onStart();
        setupService();
    }

    private void setupService() {
        Intent intent = new Intent(this, StepService.class);
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause(){
        //请求更新步数
        delayHandler.sendEmptyMessage(Constant.REQUEST_SERVER);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
    }
}
