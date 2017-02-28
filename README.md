

（参考博客 http://www.jianshu.com/p/5d57f7fd84fa）
##计步功能
###文件结构
![这里写图片描述](http://img.blog.csdn.net/20170228214352074?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcWF6d3lj/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
###思路框图
![这里写图片描述](http://img.blog.csdn.net/20170228213936544?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcWF6d3lj/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
###代码
（项目源代码
+ StepCountActivity

在onCreate方法中初始化Handler，onStart方法中开启服务，以备退到后台，再到前台，会触发onStart方法，以此来开启service。
```
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        init();
    }
    
    private void init() {
	    ...
	    //
        delayHandler = new Handler(this);
        ...
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
```
以bind形式开启service，故有ServiceConnection接收回调。

```
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
```
接收从服务端回调的步数

```
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case Constant.MSG_FROM_SERVER:
                ...
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
```

+ StepService

有一个Handler，负责与StepCountActivity进行通讯

```
    private static class MessenerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.MSG_FROM_CLIENT:
                    try {
                        Messenger messenger = msg.replyTo;
                        Message replyMsg = Message.obtain(null, Constant.MSG_FROM_SERVER);
                        Bundle bundle = new Bundle();
                        bundle.putInt("step", StepDcretor.CURRENT_STEP);
                        save();
                        replyMsg.setData(bundle);
                        messenger.send(replyMsg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
```
onCreate方法注册关屏、开屏等广播。开启一个线程，执行计步逻辑。同时开启一个计时器，30s往数据库中写入一次数据
```
    @Override
    public void onCreate() {
        super.onCreate();
        initBroadcastReceiver();
        new Thread(new Runnable() {
            public void run() {
                startStepDetector();
            }
        }).start();
        startTimeCount();
    }
```
在注册的广播中，会根据用户是在前台还是后台，对存储时间也是有改变的
```
    private void initBroadcastReceiver() {
        final IntentFilter filter = new IntentFilter();
        // 屏幕灭屏广播
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        //日期修改
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        //关机广播
        filter.addAction(Intent.ACTION_SHUTDOWN);
        // 屏幕亮屏广播
        filter.addAction(Intent.ACTION_SCREEN_ON);
        // 屏幕解锁广播
        filter.addAction(Intent.ACTION_USER_PRESENT);
        // 当长按电源键弹出“关机”对话或者锁屏时系统会发出这个广播
        // example：有时候会用到系统对话框，权限可能很高，会覆盖在锁屏界面或者“关机”对话框之上，
        // 所以监听这个广播，当收到时就隐藏自己的对话，如点击pad右下角部分弹出的对话框
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        mBatInfoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                String action = intent.getAction();

                if (Intent.ACTION_SCREEN_ON.equals(action)) {
                    Log.v(TAG, "screen on");
                } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                    Log.v(TAG, "screen off");
                    //改为60秒一存储
                    duration = 60000;
                } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                    Log.v(TAG, "screen unlock");
                    save();
                    //改为30秒一存储
                    duration = 30000;
                } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
                    Log.v(TAG, " receive Intent.ACTION_CLOSE_SYSTEM_DIALOGS");
                    //保存一次
                    save();
                } else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
                    Log.v(TAG, " receive ACTION_SHUTDOWN");
                    save();
                } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
                    Log.v(TAG, " receive ACTION_TIME_CHANGED");
                    initTodayData();
                    clearStepData();
                }
            }
        };
        registerReceiver(mBatInfoReceiver, filter);
    }
```
在onStartComand中，从数据库中初始化今日步数，并更新通知栏
```
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initTodayData();
        updateNotification("今日步数：" + StepDcretor.CURRENT_STEP + " 步");
        return START_STICKY;
    }
```
同时开启Google内置计步器和加速度传感器

```
    private void startStepDetector() {
        if (sensorManager != null && stepDetector != null) {
            sensorManager.unregisterListener(stepDetector);
            sensorManager = null;
            stepDetector = null;
        }
        sensorManager = (SensorManager) this
                .getSystemService(SENSOR_SERVICE);
        getLock(this);

        addBasePedoListener();
        addCountStepListener();
    }
```

+ StepDcretor

实现了SensorEventListener接口,
```
public void onSensorChanged(SensorEvent event) {
    Sensor sensor = event.sensor; 
   synchronized (this) {
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {  
          calc_step(event);       
         }   
    }
}
```
calc_step方法算出加速度传感器的x、y、z三轴的平均数值（为了平衡在某一个方向数值过大造成的数据误差），接着交给DetectorNewStep方法处理。

```
    synchronized private void calc_step(SensorEvent event) {
        average = (float) Math.sqrt(Math.pow(event.values[0], 2)
                + Math.pow(event.values[1], 2) + Math.pow(event.values[2], 2));
        detectorNewStep(average);
    }
```
针对波峰和波谷，进行检测

```
    /*
     * 检测步子，并开始计步
	 * 1.传入sersor中的数据
	 * 2.如果检测到了波峰，并且符合时间差以及阈值的条件，则判定为1步
	 * 3.符合时间差条件，波峰波谷差值大于initialValue，则将该差值纳入阈值的计算中
	 * */
    public void detectorNewStep(float values) {
        if (gravityOld == 0) {
            gravityOld = values;
        } else {
            if (DetectorPeak(values, gravityOld)) {
                timeOfLastPeak = timeOfThisPeak;
                timeOfNow = System.currentTimeMillis();

                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= ThreadValue) && (timeOfNow - timeOfLastPeak) <= 2000) {
                    timeOfThisPeak = timeOfNow;
                    //更新界面的处理，不涉及到算法
                    preStep();
                }
                if (timeOfNow - timeOfLastPeak >= 200
                        && (peakOfWave - valleyOfWave >= initialValue)) {
                    timeOfThisPeak = timeOfNow;
                    ThreadValue = Peak_Valley_Thread(peakOfWave - valleyOfWave);
                }
            }
        }
        gravityOld = values;
    }

```
通过变量CountTimeState，将计步分为了三种模式，CountTimeState=0时代表还未开启计步器。CountTimeState=1时代表预处理模式，若TEMP\_STEP步数如果在规定的时间内一直在增加，那么TEMP\_STEP值有效，反之，无效舍弃，目的是为了过滤掉一些手机晃动带来的影响。CountTimeState=2时代表正常计步模式

```
    private void preStep() {
        if (CountTimeState == 0) {
            // 开启计时器
            time = new TimeCount(duration, 700);
            time.start();
            CountTimeState = 1;
            Log.v(TAG, "开启计时器");
        } else if (CountTimeState == 1) {
            TEMP_STEP++;
            Log.v(TAG, "计步中 TEMP_STEP:" + TEMP_STEP);
        } else if (CountTimeState == 2) {
            CURRENT_STEP++;
            if (onSensorChangeListener != null) {
                onSensorChangeListener.onChange();
            }
        }
    }
```
检测波峰

```
    /*
     * 检测波峰
     * 以下四个条件判断为波峰：
     * 1.目前点为下降的趋势：isDirectionUp为false
     * 2.之前的点为上升的趋势：lastStatus为true
     * 3.到波峰为止，持续上升大于等于2次
     * 4.波峰值大于1.2g,小于2g
     * 记录波谷值
     * 1.观察波形图，可以发现在出现步子的地方，波谷的下一个就是波峰，有比较明显的特征以及差值
     * 2.所以要记录每次的波谷值，为了和下次的波峰做对比
     * */
    public boolean DetectorPeak(float newValue, float oldValue) {
        lastStatus = isDirectionUp;
        if (newValue >= oldValue) {
            isDirectionUp = true;
            continueUpCount++;
        } else {
            continueUpFormerCount = continueUpCount;
            continueUpCount = 0;
            isDirectionUp = false;
        }

        //Log.v(TAG, "oldValue:" + oldValue);
        if (!isDirectionUp && lastStatus
                && (continueUpFormerCount >= 2 && (oldValue >= minValue && oldValue < maxValue))) {
            peakOfWave = oldValue;
            return true;
        } else if (!lastStatus && isDirectionUp) {
            valleyOfWave = oldValue;
            return false;
        } else {
            return false;
        }
    }

```
动态生成阈值，阈值是为了跟波峰与波谷的差值进行比较，进而判断是否为1步

```
    /*
     * 阈值的计算
     * 1.通过波峰波谷的差值计算阈值
     * 2.记录4个值，存入tempValue[]数组中
     * 3.在将数组传入函数averageValue中计算阈值
     * */
    public float Peak_Valley_Thread(float value) {
        float tempThread = ThreadValue;
        if (tempCount < valueNum) {
            tempValue[tempCount] = value;
            tempCount++;
        } else {
            tempThread = averageValue(tempValue, valueNum);
            for (int i = 1; i < valueNum; i++) {
                tempValue[i - 1] = tempValue[i];
            }
            tempValue[valueNum - 1] = value;
        }
        return tempThread;

    }
```
将阈值进行梯度化，取4组数值，进行梯度化，梯度化的数值是大量测试试出来的

```
    /*
     * 梯度化阈值
     * 1.计算数组的均值
     * 2.通过均值将阈值梯度化在一个范围里
     * */
    public float averageValue(float value[], int n) {
        float ave = 0;
        for (int i = 0; i < n; i++) {
            ave += value[i];
        }
        ave = ave / valueNum;
        if (ave >= 8) {
            Log.v(TAG, "超过8");
            ave = (float) 4.3;
        } else if (ave >= 7 && ave < 8) {
            Log.v(TAG, "7-8");
            ave = (float) 3.3;
        } else if (ave >= 4 && ave < 7) {
            Log.v(TAG, "4-7");
            ave = (float) 2.3;
        } else if (ave >= 3 && ave < 4) {
            Log.v(TAG, "3-4");
            ave = (float) 2.0;
        } else {
            Log.v(TAG, "else");
            ave = (float) 1.7;
        }
        return ave;
    }
```





