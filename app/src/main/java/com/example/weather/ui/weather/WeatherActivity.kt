package com.example.weather.ui.weather

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.weather.R
import com.example.weather.customview.RealtimeWeatherView
import com.example.weather.logic.model.GPSUtils
import com.example.weather.logic.model.HourlyForecast
import com.example.weather.logic.model.PlaceManage
import com.example.weather.logic.model.Weather
import com.example.weather.logic.model.getSky
import com.example.weather.ui.MP3View
import com.example.weather.ui.SchActivity
import com.example.weather.ui.placesearch.PlaceSearchActivity
import com.example.weather.ui.weather.placemanage.PlaceManageAdapter
import com.example.weather.ui.weather.placemanage.PlaceManageViewModel
import com.example.weather.ui.weather.weathershow.HourlyAdapter
import com.example.weather.ui.weather.weathershow.WeatherShowViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class WeatherActivity : AppCompatActivity() {

    private lateinit var placeName: TextView

    private lateinit var realtimeWeatherView: RealtimeWeatherView

    private lateinit var nowLayout: RelativeLayout

    private lateinit var forecastLayout: LinearLayout

    private lateinit var coldRiskText: TextView

    private lateinit var dressingText: TextView

    private lateinit var ultravioletText: TextView

    private lateinit var carWashingText: TextView

    private lateinit var weatherLayout: ScrollView

    private lateinit var swipeRefresh: SwipeRefreshLayout

    private lateinit var navBtn: Button

    private lateinit var hourlyRecyclerView: RecyclerView

    private val hourlyForecastList = ArrayList<HourlyForecast>()

    private lateinit var hourlyAdapter: HourlyAdapter

    private lateinit var searchPlaceEntrance: EditText

    private lateinit var addBtn: Button

    private lateinit var posBtn: Button

    private lateinit var schBtn: Button

    private lateinit var musicBtn : Button

    private lateinit var placeManageRecyclerView: RecyclerView

    private lateinit var placeManageAdapter: PlaceManageAdapter

    lateinit var drawerLayout: DrawerLayout

    private lateinit var gpsUtils : GPSUtils

    val weatherViewModel  by lazy { ViewModelProvider(this).get(WeatherShowViewModel::class.java) }

    val placeManageViewModel by lazy {ViewModelProvider(this).get(PlaceManageViewModel::class.java)}

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {

        instance = this
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
        val attributes = window.attributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            window.setAttributes(attributes)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT

        placeName = findViewById(R.id.placeName)
        realtimeWeatherView = findViewById(R.id.realtimeWeather)
        nowLayout = findViewById(R.id.nowLayout)
        forecastLayout = findViewById(R.id.forecastLayout)
        coldRiskText = findViewById(R.id.coldRiskText)
        dressingText = findViewById(R.id.dressingText)
        ultravioletText = findViewById(R.id.ultravioletText)
        carWashingText = findViewById(R.id.carWashingText)
        weatherLayout = findViewById(R.id.weatherLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        navBtn = findViewById(R.id.navBtn)
        drawerLayout = findViewById(R.id.drawerLayout)
        hourlyRecyclerView = findViewById(R.id.hourlyRecyclerView)
        searchPlaceEntrance = findViewById(R.id.searchPlaceEntrance)
        addBtn = findViewById(R.id.addBtn)
        placeManageRecyclerView = findViewById(R.id.placeManageRecyclerView)
        posBtn = findViewById(R.id.pos)
        schBtn = findViewById(R.id.schedule)
        musicBtn = findViewById(R.id.music)

        //启动PlaceSearchActivity
        searchPlaceEntrance.setOnClickListener {
            val intent = Intent(this, PlaceSearchActivity::class.java).apply {
                putExtra("FROM_ACTIVITY","WeatherActivity")
            }
            startActivity(intent)
        }

        posBtn.setOnClickListener {
            getCurrentLocationAndUpdateWeather()
        }

        schBtn.setOnClickListener {
            val intent = Intent(this, SchActivity::class.java)
            startActivity(intent)
        }

        musicBtn.setOnClickListener {
            val intent = Intent(this , MP3View::class.java)
            startActivity(intent)
        }

        //设置24小时预报的RecyclerView
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        hourlyRecyclerView.layoutManager = layoutManager
        hourlyAdapter = HourlyAdapter(hourlyForecastList)
        hourlyRecyclerView.adapter = hourlyAdapter

        //设置地点管理的RecyclerView
        val layoutManager2 = LinearLayoutManager(this)
        placeManageRecyclerView.layoutManager = layoutManager2
        placeManageAdapter = PlaceManageAdapter(this,placeManageViewModel.placeManageList)
        placeManageRecyclerView.adapter = placeManageAdapter

        if (weatherViewModel.locationLng.isEmpty()) {
            weatherViewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (weatherViewModel.locationLat.isEmpty()) {
            weatherViewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (weatherViewModel.placeName.isEmpty()) {
            weatherViewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        if (weatherViewModel.placeAddress.isEmpty()) {
            weatherViewModel.placeAddress = intent.getStringExtra("place_address") ?: ""
        }

        weatherViewModel.weatherLiveData.observe(this) { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            swipeRefresh.isRefreshing = false
        }

        placeManageViewModel.placeManageLiveData.observe(this) { result ->
            val placeManages = result.getOrNull()
            if (placeManages != null) {
                placeManageViewModel.placeManageList.clear()
                placeManageViewModel.placeManageList.addAll(placeManages)
                placeManageAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this,"无法获取地点管理数据", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }

        placeManageViewModel.toastLiveData.observe(this) { it ->
            if (it != "") {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        swipeRefresh.setColorSchemeResources(R.color.royal_blue)
        refreshWeather()                    //刷新天气
        placeManageViewModel.refreshPlaceManage()     //刷新地点管理

        swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        addBtn.setOnClickListener {
            drawerLayout.open()
            val addPlaceManage = PlaceManage(weatherViewModel.placeName,weatherViewModel.locationLng,weatherViewModel.locationLat,
                                weatherViewModel.placeAddress,weatherViewModel.placeRealtimeTem,weatherViewModel.placeSkycon)
            placeManageViewModel.addPlaceManage(addPlaceManage)
        }
        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    companion object {
        lateinit var instance: Activity
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == GPSUtils.LOCATION_CODE) {
            // 检查位置权限是否已被授予
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 位置权限已授予，重新获取位置信息并更新天气
                gpsUtils = GPSUtils.getInstance()
            } else {
                // 位置权限被拒绝，显示一条消息或采取适当的措施
                Toast.makeText(this, "位置权限被拒绝", Toast.LENGTH_SHORT).show()
            }
        }
    }




    override fun onStop() {
        super.onStop()
        placeManageViewModel.clearToast()
    }


    private fun getCurrentLocationAndUpdateWeather() {
        gpsUtils = GPSUtils.getInstance()

        // 使用 GPSUtils 获取当前省份
        val currentProvince = gpsUtils.getProvince()
        Log.i("location" , currentProvince)

        // 显示对话框以确认是否更新位置
        if(currentProvince != ""){
            showLocationUpdateConfirmationDialog(currentProvince)
        }
    }

    private fun showLocationUpdateConfirmationDialog(currentProvince: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("更新位置")
        builder.setMessage("当前位置为 $currentProvince，是否更新天气信息？")
        builder.setPositiveButton("确定") { dialog, which ->
            // 用户点击了确定按钮，更新位置并刷新天气
            weatherViewModel.locationLng = gpsUtils.getlng().toString()
            weatherViewModel.locationLat = gpsUtils.getlat().toString()
            weatherViewModel.placeName = gpsUtils.getProvince().split(" ")[1]
            weatherViewModel.placeAddress = gpsUtils.getProvince()
            weatherViewModel.weatherLiveData.observe(this , Observer { result ->
                val weather = result.getOrNull()
                if(weather != null)
                {
                    showWeatherInfo(weather)
                }
            })
            weatherViewModel.refreshWeather(weatherViewModel.locationLng , weatherViewModel.locationLat)
        }
        builder.setNegativeButton("取消") { dialog, which ->
            // 用户点击了取消按钮，不执行任何操作
        }
        builder.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun showWeatherInfo(weather: Weather) {
        placeName.text = weatherViewModel.placeName
        val realtime = weather.realtime
        val hourly = weather.hourly
        val daily = weather.daily
        // 填充now.xml布局中的数据
        val realtimeTemInt = realtime.temperature.toInt()
        val currentSkyInfo = getSky(realtime.skycon).info
        val currentPM25 = realtime.airQuality.aqi.chn.toInt()
        val currentApparentTemInt = realtime.apparentTemperature.toInt()
        val currentWindDir = realtime.wind.direction
        val currentWindScale = calculateWindScale(realtime.wind.speed.toInt())
        val currentHumidity = (realtime.humidity * 100).toInt()
        realtimeWeatherView.setRealtimeWeather(realtimeTemInt,currentSkyInfo,currentPM25,
            currentApparentTemInt,currentWindDir,currentWindScale,currentHumidity)
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        //将网络请求的当前温度和Skycon保存到ViewModel中
        weatherViewModel.placeRealtimeTem = realtimeTemInt
        weatherViewModel.placeSkycon = currentSkyInfo

        //是否是点击地点管理中的地点更新该地点温度和天气信息
        if (weatherViewModel.isUpdatePlaceManage) {
            val updatePlaceManage = PlaceManage(weatherViewModel.placeName,weatherViewModel.locationLng,
                weatherViewModel.locationLat, weatherViewModel.placeAddress,
                weatherViewModel.placeRealtimeTem,weatherViewModel.placeSkycon)
            placeManageViewModel.updatePlaceManage(updatePlaceManage)
            weatherViewModel.isUpdatePlaceManage = false
        }

        //填充forecast_hourly.xml布局中的数据
        hourlyForecastList.clear()
        val hours = hourly.skycon.size
        for (i in 0 until hours) {
            val temVal = hourly.temperature[i].value
            val skyVal = hourly.skycon[i].value
            val datetime = hourly.skycon[i].datetime
            hourlyForecastList.add(HourlyForecast(temVal, skyVal, datetime))
        }
        hourlyAdapter.notifyDataSetChanged()

        // 填充forecast_daily.xml布局中的数据
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(
                R.layout.forecast_daily_item,
                forecastLayout, false
            )
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
            val dateInfoStr = when (i) {
                0 -> "今天  ${simpleDateFormat.format(skycon.date)}"
                1 -> "明天  ${simpleDateFormat.format(skycon.date)}"
                else -> "${getDayOfWeek(skycon.date)}  ${simpleDateFormat.format(skycon.date)}"
            }
            dateInfo.text = dateInfoStr
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            view.setOnClickListener {
                showDailyWeatherDetailDialog(skycon.date, skycon.value, temperature.min, temperature.max)
            }
            forecastLayout.addView(view)
        }

        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        coldRiskText.text = lifeIndex.coldRisk[0].desc
        dressingText.text = lifeIndex.dressing[0].desc
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        carWashingText.text = lifeIndex.carWashing[0].desc
        weatherLayout.visibility = View.VISIBLE
    }

    private fun showDailyWeatherDetailDialog(date: Date, skyValue: String, minTemperature: Float, maxTemperature: Float) {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = simpleDateFormat.format(date)
        val sky = getSky(skyValue)
        val detailInfo = "日期: $formattedDate\n天气状况: ${sky.info}\n最低温度: $minTemperature ℃\n最高温度: $maxTemperature ℃"

        val builder = AlertDialog.Builder(this)
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_daily_weather_detail, null)

        val detailTextView: TextView = dialogView.findViewById(R.id.detailTextView)
        val iconImageView: ImageView = dialogView.findViewById(R.id.iconImageView)

        detailTextView.text = detailInfo
        iconImageView.setImageResource(sky.icon)

        builder.setView(dialogView)
        builder.setTitle("天气详情")
        builder.setPositiveButton("确定") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }



    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        drawerLayout.closeDrawers()
        weatherViewModel.locationLng = intent?.getStringExtra("location_lng") ?: ""
        weatherViewModel.locationLat = intent?.getStringExtra("location_lat") ?: ""
        weatherViewModel.placeName = intent?.getStringExtra("place_name") ?: ""
        weatherViewModel.placeAddress = intent?.getStringExtra("place_address") ?: ""
        refreshWeather()
    }

    fun refreshWeather() {
        weatherViewModel.refreshWeather(weatherViewModel.locationLng,weatherViewModel.locationLat)
        swipeRefresh.isRefreshing = true
    }

    private fun getDayOfWeek(date: Date): String {
        val sdf = SimpleDateFormat("E", Locale.getDefault())
        return sdf.format(date)
    }
    private fun calculateWindScale(windSpeed: Int): Int {
        return when {
            windSpeed < 1 -> 0 // Calm
            windSpeed < 6 -> 1 // Light air
            windSpeed < 12 -> 2 // Light breeze
            windSpeed < 20 -> 3 // Gentle breeze
            windSpeed < 29 -> 4 // Moderate breeze
            windSpeed < 39 -> 5 // Fresh breeze
            windSpeed < 50 -> 6 // Strong breeze
            windSpeed < 62 -> 7 // Near gale
            windSpeed < 75 -> 8 // Gale
            windSpeed < 89 -> 9 // Strong gale
            windSpeed < 103 -> 10 // Storm
            windSpeed <= 117 -> 11 // Violent storm
            else -> 12 // Hurricane
        }
    }
}