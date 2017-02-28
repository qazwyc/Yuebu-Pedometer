package com.example.lenovo.test_sql.Chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.lenovo.test_sql.R;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;


public class ChatActivity extends AppCompatActivity implements OnClickListener {
    Button mBtnSend;// 发送btn
    Button mBtnMore;// 更多功能btn
    Button health_food, illness_search;
    LinearLayout more_func;
    private Button mBtnBack;// 返回btn
    private EditText mEditTextContent;
    private ListView mListView;
    private List<ChatMsgEntity> mDataArrays = new ArrayList<ChatMsgEntity>();// 消息对象数组
    private ChatMsgViewAdapter mAdapter;// 消息视图的Adapter
    private String user_name = null;

    private static final String TuLingurl = "http://www.tuling123.com/openapi/api";
    final String TuLingAPIkey = "24968d82152647808a46ae7dae57f930";

    private static final String HealthKnowUrl = "http://apis.baidu.com/tngou/lore/news";
    final String HealthKnowKey = "0416c4e34fb3de07e8b794af22f3fc4b";

    private static final String HealthFoodUrl = "http://apis.baidu.com/tngou/food/name";
    private static final String IllnessUrl = "http://apis.baidu.com/tngou/disease/name";
    //健康知识分类1-12："id":11,"name":"减肥瘦身";"id":7,"name":"私密生活";"id":5,"name":"女性保养";"id":4,"name":"男性健康";
    // "id":6,"name":"孕婴手册";"id":13,"name":"夫妻情感";"id":8,"name":"育儿宝典";"id":3,"name":"健康饮食";
    // "id":12,"name":"医疗护理";"id":1,"name":"老人健康";"id":2,"name":"孩子健康";"id":10,"name":"四季养生";
    // "id":9,"name":"心里健康"

    HttpURLConnection connection = null;
    //message 类型
    private static final int TuLingContent = 1;
    private static final int HealthKonwContent = 2;
    private static final int HealthFoodContent = 3;
    private static final int IllnessContent = 4;
    private static final int NO_INTERNET = 5;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initView();// 初始化view

        sayWelcome("您好，小悦为您服务！");
        int classify = new Random().nextInt(12)+1;
        int id = new Random().nextInt(20000);
        String httpArg = "?classify="+classify+"&id="+id+"&rows=1";
        sendRequestWithHttpConnection(HealthKnowUrl,httpArg,"GET",HealthKnowKey);

        SharedPreferences saveUser = getSharedPreferences("User",MODE_PRIVATE);
        user_name = saveUser.getString("username",null);
    }

    /**
     * 初始化view
     */
    public void initView() {
        mListView = (ListView) findViewById(R.id.listview);
        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener(this);
        mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

        mBtnMore = (Button) findViewById(R.id.more_fun);
        mBtnMore.setOnClickListener(this);
        more_func = (LinearLayout) findViewById(R.id.more_function);
        health_food = (Button) findViewById(R.id.health_food);
        health_food.setOnClickListener(this);
        illness_search = (Button) findViewById(R.id.illness_search);
        illness_search.setOnClickListener(this);
    }
    /**
     * 发送欢迎信息
     */
    private void sayWelcome(String welcome) {
        ChatMsgEntity entity = new ChatMsgEntity();
        entity.setDate(getDate());
        entity.setName("小悦");
        entity.setMsgType(true);// 收到的消息
        entity.setMessage(Html.fromHtml(welcome));
        mDataArrays.add(entity);
        mAdapter = new ChatMsgViewAdapter(this, mDataArrays);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mAdapter.getCount() - 1);
    }
    /**
     * 回复
     */
    private void reply(String welcome) {
        Log.i("Health",welcome);
        ChatMsgEntity entity = new ChatMsgEntity();
        entity.setDate(getDate());
        entity.setName("小悦");
        entity.setMsgType(true);// 收到的消息
        entity.setMessage(Html.fromHtml(welcome));
        mDataArrays.add(entity);
        mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变
        mListView.setSelection(mAdapter.getCount() - 1);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:// 发送按钮点击事件
                send();
                break;
            case R.id.more_fun:
                if(more_func.getVisibility() == View.GONE)
                    more_func.setVisibility(View.VISIBLE);
                else
                    more_func.setVisibility(View.GONE);
                break;
            case R.id.health_food:
                if(!TextUtils.isEmpty(mEditTextContent.getText())){
                    try{
                        String name= URLDecoder.decode(mEditTextContent.getText().toString(),"UTF-8");
                        String httpArg = "?name="+name;
                        sendRequestWithHttpConnection(HealthFoodUrl,httpArg,"GET",HealthKnowKey);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.illness_search:
                if(!TextUtils.isEmpty(mEditTextContent.getText())){
                    try{
                        String name= URLDecoder.decode(mEditTextContent.getText().toString(),"UTF-8");
                        String httpArg = "?name="+name;
                        sendRequestWithHttpConnection(IllnessUrl,httpArg,"GET",HealthKnowKey);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_back:// 返回按钮点击事件
                finish();// 结束,实际开发中，可以返回主界面
                break;
        }
    }
    /**
     * 发送消息
     */
    private void send() {
        String contString = mEditTextContent.getText().toString();
        if (contString.length() > 0) {
            ChatMsgEntity entity = new ChatMsgEntity();
            entity.setName(user_name);
            entity.setDate(getDate());
            entity.setMessage(Html.fromHtml(contString));
            entity.setMsgType(false);

            mDataArrays.add(entity);
            mAdapter.notifyDataSetChanged();// 通知ListView，数据已发生改变

            mEditTextContent.setText("");// 清空编辑框数据

            mListView.setSelection(mListView.getCount() - 1);// 发送一条消息时，ListView显示选择最后一项

            String httpArg = "key=" + TuLingAPIkey + "&info=" + contString + "&userid=" + user_name;
            sendRequestWithHttpConnection(TuLingurl,httpArg,"POST",TuLingAPIkey);
        }
    }

    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case TuLingContent:
                    try{
                        JSONTokener jsonParser = new JSONTokener(message.obj.toString());
                        JSONObject reply = (JSONObject) jsonParser.nextValue();
                        String code = reply.getString("code");
                        Log.i("MyDebug","code: "+code);
                        if(code.equals("40001")){
                            Toast.makeText(ChatActivity.this,"未获得key权限",Toast.LENGTH_SHORT).show();
                        }else if(code.equals("40002")){
                            Toast.makeText(ChatActivity.this,"请求内容 info 为空",Toast.LENGTH_SHORT).show();
                        }else if(code.equals("40004")){
                            Toast.makeText(ChatActivity.this,"您今天说的太多了,小悦要休息啦",Toast.LENGTH_SHORT).show();
                        }else if(code.equals("40007")){
                            Toast.makeText(ChatActivity.this,"格式异常",Toast.LENGTH_SHORT).show();
                        }else{
                            StringBuilder text = new StringBuilder();
                            text.append(reply.getString("text"));
                            if(code.equals("200000")){
                                text.append("\n").append(reply.getString("url"));
                            }else if(code.equals("302000")){   //新闻
                                JSONArray list = reply.getJSONArray("list");
                                for(int i = 0;i < list.length();i++){
                                    JSONObject temp = list.getJSONObject(i);
                                    text.append("\n").append(temp.getString("article")).append(" ").append(temp.getString("detailurl"));
                                }
                            }else if(code.equals("308000")){   //菜谱
                                JSONArray list = reply.getJSONArray("list");
                                for(int i = 0;i < list.length();i++){
                                    JSONObject temp = list.getJSONObject(i);
                                    text.append("\n").append(temp.getString("name")).append(": ").append(temp.getString("info")).
                                            append(temp.getString("detailurl"));
                                }
                            }
                            Log.i("MyDebug","Text: "+text.toString());

                            reply(text.toString());
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
                case HealthKonwContent:
                    try{
                        JSONTokener jsonParser = new JSONTokener(message.obj.toString());
                        JSONObject reply = (JSONObject) jsonParser.nextValue();
                        if(reply.getBoolean("status")){
                            JSONArray result = reply.getJSONArray("list");
                            JSONObject temp = result.getJSONObject(0);
                            reply("标题: "+ temp.getString("title")+"\n\n"+temp.getString("description"));
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case HealthFoodContent:
                    try{
                        JSONTokener jsonParser = new JSONTokener(message.obj.toString());
                        JSONObject reply = (JSONObject) jsonParser.nextValue();
                        if(reply.getBoolean("status")){
                            String temp = reply.getString("summary")+"详情"+reply.getString("url");
                            reply(temp);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case IllnessContent:
                    try{
                        JSONTokener jsonParser = new JSONTokener(message.obj.toString());
                        JSONObject reply = (JSONObject) jsonParser.nextValue();
                        if(reply.getBoolean("status")){
                            String temp = reply.getString("drugtext")+"详情"+reply.getString("url");
                            reply(temp);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                case NO_INTERNET:
                    Toast.makeText(ChatActivity.this, "当前没有可用网络", Toast.LENGTH_SHORT).show();
                    break;
                default:  break;
            }
        }
    };

    //判断网络是否可用
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) return true;
            }
        }
        return false;
    }

    private void sendRequestWithHttpConnection(final String httpUrl,final String httpArg, final String method, final String key) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(isNetworkAvailable(ChatActivity.this)){
                    try {
                        String httpurl = null;
                        if(method.equals("GET")){
                            httpurl = httpUrl + httpArg;
                        }else{
                            httpurl = httpUrl;
                        }
                        Log.i("MyDebug",httpurl);
                        connection = (HttpURLConnection) ((new URL(httpurl).openConnection()));
                        connection.setRequestMethod(method);
                        connection.setReadTimeout(8000);
                        connection.setConnectTimeout(8000);
                        // 填入apikey到HTTP header
                        if(method.equals("GET")){
                            connection.setRequestProperty("apikey",  key);
                        }
                        if(method.equals("POST")){
                            connection.setDoOutput(true);
                            connection.getOutputStream().write(httpArg.getBytes("UTF-8"));
                        }
                        connection.connect();

                        InputStream inputStream = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        Log.i("MyDebug","response: "+response);

                        Message message = new Message();
                        if(httpUrl.equals(HealthKnowUrl)){
                            message.what = HealthKonwContent;
                        }else if(httpUrl.equals(HealthFoodUrl)){
                            message.what = HealthFoodContent;
                        }else if(httpUrl.equals(IllnessUrl)){
                            message.what = IllnessContent;
                        }
                        else{
                            message.what = TuLingContent;
                        }
                        message.obj =  response.toString();
                        handler.sendMessage(message);

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (connection != null) {
                            connection.disconnect();
                        }
                    }
                }else {
                    Message message = new Message();
                    message.what = NO_INTERNET;
                    handler.sendMessage(message);
                }
            }
        }).start();
    }
    /**
     * 发送消息时，获取当前事件
     *
     * @return 当前时间
     */
    private String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return format.format(new Date());
    }
}
