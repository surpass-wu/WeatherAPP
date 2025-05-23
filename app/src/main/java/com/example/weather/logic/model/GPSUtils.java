package com.example.weather.logic.model;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.util.List;

import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.weather.ui.weather.WeatherActivity;

// 获取设备地理位置（经纬度）并转换为省份信息
public class GPSUtils {

    private static GPSUtils instance;
    private LocationManager locationManager;
    public static final int LOCATION_CODE = 1000;
    public static final int OPEN_GPS_CODE = 1001;
    Location location = null;

    public  String province = "";

    public static GPSUtils getInstance() {
        if (instance == null) {
            instance = new GPSUtils();
        }
        return instance;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public String getProvince() {
        Log.i("GPS: ", "getProvince");

        locationManager = (LocationManager) WeatherActivity.instance.getSystemService(Context.LOCATION_SERVICE);        // 默认Android GPS定位实例


        // 是否已经授权
        if (WeatherActivity.instance.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED)
        {
            //判断GPS是否开启，没有开启，则开启
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                //跳转到手机打开GPS页面
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //设置完成后返回原来的界面
                WeatherActivity.instance.startActivityForResult(intent,OPEN_GPS_CODE);
            }

            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);      // GPS芯片定位 需要开启GPS
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);      // 利用网络定位 需要开启GPS
            location = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);      // 其他应用使用定位更新了定位信息 需要开启GPS
        }

        String p = "";
        if(location != null)
        {
            Log.i("GPS: ", "获取位置信息成功");
            Log.i("GPS: ","经度：" + location.getLatitude());
            Log.i("GPS: ","纬度：" + location.getLongitude());

            // 获取地址信息
            p = getAddress(location.getLatitude(),location.getLongitude());
            Log.i("GPS: ","location：" + p);
        }
        else
        {
            Log.i("GPS: ", "获取位置信息失败，请检查是够开启GPS,是否授权");
            Toast.makeText(WeatherActivity.instance, "无法获取位置信息，请检查是否授权", Toast.LENGTH_SHORT).show();
            // 请求位置权限
            ActivityCompat.requestPermissions(
                    WeatherActivity.instance,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_CODE
            );
        }
        return p;
    }

    /*
     * 根据经度纬度 获取国家，省份
     * */
    public String getAddress(double latitude, double longitude) {
        String cityName = "";
        List<Address> addList = null;
        Geocoder ge = new Geocoder(WeatherActivity.instance);
        try {
            addList = ge.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addList != null && addList.size() > 0) {
            for (int i = 0; i < addList.size(); i++) {
                Address ad = addList.get(i);
                cityName += ad.getCountryName() + " " + ad.getLocality();
            }
        }
        return cityName;
    }

    public double getlat(){
        return location.getLatitude();
    }

    public double getlng(){
        return location.getLongitude();
    }
}