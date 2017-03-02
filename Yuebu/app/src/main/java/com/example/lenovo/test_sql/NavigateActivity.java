package com.example.lenovo.test_sql;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchOption;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class NavigateActivity extends AppCompatActivity {
    MapView mMapView;
    ToggleButton mToggleButton;
    ImageButton search;
    Button start;
    Chronometer timer;
    TextView distance_view;
    EditText search_keywords;
    BaiduMap mBaiduMap;
    LatLng MyLatLng = new LatLng(0,0);
    public LocationClient mLocationClient = null;
    public BDLocationListener myListener = new MyLocationListener();
    SensorManager msensorManager;
    Sensor mMagneticSensor,mAccelerometerSensor;
    float mCurrentRotation = 0f;

    // 构造折线点坐标
    List<LatLng> points = new ArrayList<LatLng>();
    float distance;
    PoiSearch mPoiSearch;
    List<PoiInfo> list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_navigate);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMapView = (MapView) findViewById(R.id.baidumapView);
        mBaiduMap = mMapView.getMap();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        
        //设置定位及其图标
        Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.pointer),100,100,true);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
        MyLocationConfiguration config = new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL,true,bitmapDescriptor);
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setMyLocationConfigeration(config);
        
        //定位按钮
        mToggleButton = (ToggleButton) findViewById(R.id.button_center);
        
        mLocationClient = new LocationClient(getApplicationContext());     //声明LocationClient类
        mLocationClient.registerLocationListener( myListener );    //注册监听函数
        initLocation();
        mLocationClient.start();

        //磁、加速度传感器
        msensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagneticSensor = msensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mAccelerometerSensor = msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mMapView.getMap().setOnMapTouchListener(new BaiduMap.OnMapTouchListener() {
            @Override
            public void onTouch(MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_MOVE:
                        mToggleButton.setChecked(false);
                        break;
                    default:
                        break;
                }
            }
        });
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //创建InfoWindow展示的view
                TextView mark_detail = new TextView(getApplicationContext());
                Bundle bundle = marker.getExtraInfo();
                mark_detail.setText(marker.getTitle()+"\n地址："+bundle.getString("address")+"\n电话："+bundle.getString("phoneNum"));
                mark_detail.setTextColor(Color.BLACK);
                mark_detail.setBackgroundColor(Color.BLUE);
                //定义用于显示该InfoWindow的坐标点
                LatLng pt = marker.getPosition();
                //创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
                InfoWindow mInfoWindow = new InfoWindow(mark_detail, pt, -100);
                //显示InfoWindow
                mBaiduMap.showInfoWindow(mInfoWindow);
                return false;
            }
        });

        search = (ImageButton) findViewById(R.id.search);
        search_keywords = (EditText) findViewById(R.id.search_input);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                POISearch();
            }
        });

        // 获得计时器对象
        timer = (Chronometer)this.findViewById(R.id.chronometer1);
        distance_view = (TextView) findViewById(R.id.distance);
        start = (Button) findViewById(R.id.start_walk);
        start.setTag(0); //暂停状态
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if((int)start.getTag() == 0){
                    start.setTag(1);
                    start.setText("结束");
                    distance = 0;
                    distance_view.setText("距离： 0.00 公里");
                    // 将计时器清零
                    timer.setBase(SystemClock.elapsedRealtime());
                    //开始计时
                    timer.start();
                }else{
                    start.setTag(0);
                    timer.stop();
                    start.setText("开始");
                }
            }
        });
    }
    private void POISearch(){
        mPoiSearch = PoiSearch.newInstance();
        OnGetPoiSearchResultListener poiListener = new OnGetPoiSearchResultListener(){
            public void onGetPoiResult(PoiResult result){
                List<PoiInfo> list = result.getAllPoi();
                if(list == null){
                    Toast.makeText(getApplicationContext(),"你搜索的结果为空",Toast.LENGTH_SHORT).show();
                    return;
                }
                //获取POI检索结果
                mBaiduMap.clear();
                for(int i=0;i<list.size();i++){
                    LatLng point = list.get(i).location;
                    String icon_name="icon_mark"+(char)(97+i);
                    //构建Marker图标
                    BitmapDescriptor bitmap = BitmapDescriptorFactory
                            .fromResource(getDrawableResource(icon_name));
                    Bundle bundle = new Bundle();
                    bundle.putString("address",list.get(i).address);
                    bundle.putString("phoneNum",list.get(i).phoneNum);
                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmap).title(list.get(i).name).extraInfo(bundle);
                    //在地图上添加Marker，并显示
                    mBaiduMap.addOverlay(option);
                }
                mToggleButton.setChecked(false);
                MapStatus mapStatus = new MapStatus.Builder().target(list.get(0).location).build();
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
                mBaiduMap.setMapStatus(mapStatusUpdate);
            }
            public void onGetPoiDetailResult(PoiDetailResult result){
                //获取Place详情页检索结果
            }
            public void onGetPoiIndoorResult(PoiIndoorResult result){};
        };
        mPoiSearch.setOnGetPoiSearchResultListener(poiListener);
        if(!TextUtils.isEmpty(search_keywords.getText())){
            mPoiSearch.searchNearby((new PoiNearbySearchOption()
                    .location(MyLatLng).radius(10000)
                    .keyword(search_keywords.getText().toString()).pageNum(10)));
        }
    }

    public int  getDrawableResource(String imageName){
        Log.v("MyDebug",imageName);
        Class drawable = R.drawable.class;
        try {
            Field field = drawable.getField(imageName);
            int resId = field.getInt(imageName);
            return resId;
        } catch (NoSuchFieldException e) {//如果没有在"drawable"下找到imageName,将会返回0
            return 0;
        } catch (IllegalAccessException e) {
            return 0;
        }

    }

    private void initLocation(){
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span=1000;
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(false);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            MyLatLng = new LatLng(location.getLatitude(),location.getLongitude());
            if((int)start.getTag()==1){
                if(points.size()>0){
                    distance = distance + (float)Math.round(DistanceUtil.getDistance(MyLatLng,points.get(points.size()-1))/1000*100)/100;
                    distance_view.setText("距离： "+ distance +"公里");
                }
                points.add(MyLatLng);
                if(points.size()>=2){
                    OverlayOptions ooPolyline = new PolylineOptions().width(10).color(Color.BLACK).points(points);
                    //添加在地图中
                    mBaiduMap.addOverlay(ooPolyline);
                }
            }

            if(mToggleButton.isChecked()){
                makeUseOfNewLocation();
            }
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
        }
    }
    private void makeUseOfNewLocation(){
        MyLocationData.Builder builder = new MyLocationData.Builder()
                .direction(mCurrentRotation)
                .latitude(MyLatLng.latitude)
                .longitude(MyLatLng.longitude);
        mBaiduMap.setMyLocationData(builder.build());

        MapStatus mapStatus = new MapStatus.Builder().target(MyLatLng).build();
        MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mapStatus);
        mBaiduMap.setMapStatus(mapStatusUpdate);
    }
    private SensorEventListener mSensorEventListener = new SensorEventListener() {
        float[] accValues = null;
        float[] magValues = null;
        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    // do something about values of accelerometer
                    accValues = event.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    // do something about values of magnetic field
                    magValues = event.values.clone();
                    break;
                default:
                    break;
            }
            float[] R = new float[9];
            float[] values = new float[3];
            if(accValues != null && magValues != null){
                SensorManager.getRotationMatrix(R, null, accValues, magValues);
                SensorManager.getOrientation(R, values);
                mCurrentRotation = (float) Math.round(Math.toDegrees(values[0])*100)/100;
                if(MyLatLng!=null){
                    MyLocationData.Builder builder = new MyLocationData.Builder()
                            .direction(mCurrentRotation)
                            .latitude(MyLatLng.latitude)
                            .longitude(MyLatLng.longitude);
                    mBaiduMap.setMyLocationData(builder.build());
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    };
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
        mLocationClient.stop();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        msensorManager.registerListener(mSensorEventListener, mMagneticSensor, SensorManager.SENSOR_DELAY_GAME);
        msensorManager.registerListener(mSensorEventListener,mAccelerometerSensor,SensorManager.SENSOR_DELAY_GAME);
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        msensorManager.unregisterListener(mSensorEventListener);
    }
}
