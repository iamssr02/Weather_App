package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.weatherapp.dataClasses.ModelClass
import com.example.weatherapp.dataClasses.Utilities.ApiUtilities
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var date: TextView
    private lateinit var temp: TextView
    private lateinit var minTemp: TextView
    private lateinit var maxTemp: TextView
    private lateinit var feelsLike: TextView
    private lateinit var type: TextView
    private lateinit var windspeed: TextView
    private lateinit var humidity: TextView
    private lateinit var pressure: TextView
    private lateinit var city: TextView
    private lateinit var sunrise: TextView
    private lateinit var sunset: TextView
    private lateinit var bgImage: ImageView
    private lateinit var citySearch: EditText
    val API: String = "1472fe708df6d7d4cf2c5ff997a78262"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        date=findViewById(R.id.date)
        temp=findViewById(R.id.temp)
        minTemp=findViewById(R.id.min_temp)
        maxTemp=findViewById(R.id.max_temp)
        feelsLike=findViewById(R.id.feelsLike)
        type=findViewById(R.id.type)
        windspeed = findViewById(R.id.wind)
        humidity = findViewById(R.id.humidity)
        pressure = findViewById(R.id.pressure)
        city = findViewById(R.id.city)
        sunrise = findViewById(R.id.sunrise)
        sunset = findViewById(R.id.sunset)
        citySearch = findViewById(R.id.search)
        bgImage = findViewById(R.id.bg_image)

        citySearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getCityWeather(citySearch.text.toString())
                val view = this.currentFocus
                if (view != null) {
                    val imm: InputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                    citySearch.clearFocus()
                }
                true
            } else false
        }
        val getShared = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        //get data when offline
        date.text = getShared.getString("date","")
        maxTemp.text = getShared.getString("maxTemp","")
        minTemp.text = getShared.getString("minTemp","")
        temp.text = getShared.getString("temp","")
        feelsLike.text = getShared.getString("feelsLike","")
        type.text = getShared.getString("type","")
        humidity.text = getShared.getString("humidity","")
        windspeed.text = getShared.getString("windSpeed","")
        pressure.text = getShared.getString("pressure","")
        city.text = getShared.getString("city","")
        sunrise.text = getShared.getString("sunrise","")
        sunset.text = getShared.getString("sunset","")
        findViewById<TextView>(R.id.singaporeTemp).text =getShared.getString("singaporeTemp","")
        findViewById<TextView>(R.id.sydneyTemp).text =getShared.getString("sydneyTemp","")
        findViewById<TextView>(R.id.mumbaiTemp).text =getShared.getString("mumbaiTemp","")
        findViewById<TextView>(R.id.delhiTemp).text =getShared.getString("delhiTemp","")
        findViewById<TextView>(R.id.newYorkTemp).text =getShared.getString("newYorkTemp","")
        findViewById<TextView>(R.id.melbourneTemp).text =getShared.getString("melbourneTemp","")
        updateUI(getShared.getString("type",""))
        getCurrentLocation()
    }

    private fun updateUI(type: String?) {
        if(type == "Thunderstorm") {
            bgImage.setImageResource(R.drawable.thunder_bg)
        }
        else if (type == "Drizzle") {
            bgImage.setImageResource(R.drawable.drizzle_bg)
        }
        else if (type == "Rain") {
            bgImage.setImageResource(R.drawable.rain_bg)
        }
        else if (type == "Snow"){
            bgImage.setImageResource(R.drawable.snow_bg)
        }
        else if (type == "Clear") {
            bgImage.setImageResource(R.drawable.clear_bg)
        }
        else if (type == "Clouds") {
            bgImage.setImageResource(R.drawable.cloudy_bg)
        }
        else if (type == "Haze" || type == "Smoke") {
            bgImage.setImageResource(R.drawable.haze_bg)
        }
        else {
            bgImage.setImageResource(R.drawable.weather_bg)
            bgImage.animation = AnimationUtils.loadAnimation(this,R.anim.bg_image_animation)
        }
    }

    private fun getCityWeather(cityName: String) {
        ApiUtilities.getAPIInterface()?.getCityWeatherData(cityName, API, "metric")?.enqueue(
            object: Callback<ModelClass>{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if(response.isSuccessful){
                        setDataOnViews(response.body())
                    }
                    else
                        Toast.makeText(applicationContext, "Weather for this city doesn't exist",Toast.LENGTH_SHORT).show()
                }
                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(applicationContext, "Not a valid city name",Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getMumbaiWeather() {
        ApiUtilities.getAPIInterface()?.getCityWeatherData("Mumbai", API, "metric")?.enqueue(
            object: Callback<ModelClass>{
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if(response.isSuccessful){
                        val pref = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                        pref.putString("mumbaiTemp","${response.body()!!.main.temp.roundToInt()}°C")
                        pref.apply()
                        val getShared = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                        findViewById<TextView>(R.id.mumbaiTemp).text =getShared.getString("mumbaiTemp","")
                    }
                }
                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(applicationContext, "Not a valid city name",Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getNewYorkWeather() {
        ApiUtilities.getAPIInterface()?.getCityWeatherData("New York", API, "metric")?.enqueue(
            object: Callback<ModelClass>{
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if(response.isSuccessful){
                        val pref = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                        pref.putString("newYorkTemp","${response.body()!!.main.temp.roundToInt()}°C")
                        pref.apply()
                        val getShared = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                        findViewById<TextView>(R.id.newYorkTemp).text =getShared.getString("newYorkTemp","")
                    }
                }
                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(applicationContext, "Not a valid city name",Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getDelhiWeather() {
        ApiUtilities.getAPIInterface()?.getCityWeatherData("Delhi", API, "metric")?.enqueue(
            object: Callback<ModelClass>{
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if(response.isSuccessful){
                        val pref = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                        pref.putString("delhiTemp","${response.body()!!.main.temp.roundToInt()}°C")
                        pref.apply()
                        val getShared = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                        findViewById<TextView>(R.id.delhiTemp).text =getShared.getString("delhiTemp","")
                    }
                }
                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(applicationContext, "Not a valid city name",Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getSydneyWeather() {
        ApiUtilities.getAPIInterface()?.getCityWeatherData("Sydney", API, "metric")?.enqueue(
            object: Callback<ModelClass>{
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if(response.isSuccessful){
                        val pref = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                        pref.putString("sydneyTemp","${response.body()!!.main.temp.roundToInt()}°C")
                        pref.apply()
                        val getShared = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                        findViewById<TextView>(R.id.sydneyTemp).text =getShared.getString("sydneyTemp","")
                    }
                }
                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(applicationContext, "Not a valid city name",Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getSingaporeWeather() {
        ApiUtilities.getAPIInterface()?.getCityWeatherData("Singapore", API, "metric")?.enqueue(
            object: Callback<ModelClass>{
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if(response.isSuccessful){
                        val pref = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                        pref.putString("singaporeTemp","${response.body()!!.main.temp.roundToInt()}°C")
                        pref.apply()
                        val getShared = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                        findViewById<TextView>(R.id.singaporeTemp).text =getShared.getString("singaporeTemp","")
                    }
                }
                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(applicationContext, "Not a valid city name",Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun getMelbourneWeather() {
        ApiUtilities.getAPIInterface()?.getCityWeatherData("Melbourne", API, "metric")?.enqueue(
            object: Callback<ModelClass>{
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if(response.isSuccessful){
                        val pref = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
                        pref.putString("melbourneTemp","${response.body()!!.main.temp.roundToInt()}°C")
                        pref.apply()
                        val getShared = applicationContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
                        findViewById<TextView>(R.id.melbourneTemp).text =getShared.getString("melbourneTemp","")
                    }
                }
                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(applicationContext, "Not a valid city name",Toast.LENGTH_SHORT).show()
                }
            })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeStampToLocalTime(timeStamp: Long): String{
        val localTime = timeStamp.let {
            Instant.ofEpochSecond(it).atZone(ZoneId.systemDefault()).toLocalTime()
        }
        return localTime.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDataOnViews(body: ModelClass?) {

        getMumbaiWeather()
        getDelhiWeather()
        getMelbourneWeather()
        getSingaporeWeather()
        getSydneyWeather()
        getNewYorkWeather()

        val pref = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm")
        val currentDate = sdf.format(Date())
        pref.putString("date","Last updated on: $currentDate")
        pref.putString("maxTemp","${body!!.main.temp_max.roundToInt()}°C/")
        pref.putString("minTemp","${body!!.main.temp_min.roundToInt()}°C")
        pref.putString("temp","${body!!.main.temp.roundToInt()}°C")
        pref.putString("feelsLike","Feels like ${body!!.main.feels_like.roundToInt()}°C")
        pref.putString("type", body!!.weather[0].main)
        pref.putString("humidity","${body.main.humidity.toString()}%")
        pref.putString("windSpeed","${body.wind.speed.toString()} km/hr")
        pref.putString("pressure","${body.main.pressure.toString()} mBar")
        pref.putString("city",body.name.toString())
        pref.putString("sunrise",timeStampToLocalTime(body.sys.sunrise.toLong()))
        pref.putString("sunset",timeStampToLocalTime(body.sys.sunset.toLong()))
        pref.apply()

        val getShared = this.getSharedPreferences("PREFS", Context.MODE_PRIVATE)

        date.text = getShared.getString("date","")
        maxTemp.text = getShared.getString("maxTemp","")
        minTemp.text = getShared.getString("minTemp","")
        temp.text = getShared.getString("temp","")
        feelsLike.text = getShared.getString("feelsLike","")
        type.text = getShared.getString("type","")
        humidity.text = getShared.getString("humidity","")
        windspeed.text = getShared.getString("windSpeed","")
        pressure.text = getShared.getString("pressure","")
        city.text = getShared.getString("city","")
        sunrise.text = getShared.getString("sunrise","")
        sunset.text = getShared.getString("sunset","")
        updateUI(getShared.getString("type",""))
        bgImage.animation = AnimationUtils.loadAnimation(this,R.anim.bg_image_animation)
    }

    private fun fetchCurrentLocationWeather(latitude: String, longitude: String){
        ApiUtilities.getAPIInterface()?.getCurrentWeatherData(latitude,longitude,API,"metric","en")?.enqueue(
            object: Callback<ModelClass>{
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if(response.isSuccessful){
                        setDataOnViews(response.body())
                    }
                }

                override fun onFailure(call: Call<ModelClass>, t: Throwable) {
                    Toast.makeText(applicationContext, "Error occurred while fetching weather",Toast.LENGTH_SHORT).show()
                }

            })
    }

    private fun getCurrentLocation() {
        if (checkSelfPermission()){
            if (isLocationEnabled()){
                fusedLocationProviderClient.lastLocation.addOnCompleteListener(this){task ->
                    val location = task.result
                    if(location == null){
                        Toast.makeText(this, "Error while receiving location",Toast.LENGTH_SHORT).show()
                    }
                    else{
                        fetchCurrentLocationWeather(location.latitude.toString(),location.longitude.toString())
                    }
                }
            }
            else{
                //settings
                Toast.makeText(this, "Please turn on location",Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
        else{
            //request permission here
            requestPermission()
        }
    }

    private fun requestPermission() {
        Log.d("tag", "requestPermission: ")
        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION), 100)
    }

    private fun checkSelfPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)
                ==PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,android.Manifest.permission.ACCESS_COARSE_LOCATION)
                ==PackageManager.PERMISSION_GRANTED)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ||locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getCurrentLocation()
            }
            else{
                Toast.makeText(this, "Please allow all permissions",Toast.LENGTH_SHORT).show()
            }
        }
    }
}