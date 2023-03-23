package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.weatherapp.dataClasses.ModelClass
import com.example.weatherapp.dataClasses.Utilities.ApiUtilities
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.text.SimpleDateFormat
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
    val API: String = "1472fe708df6d7d4cf2c5ff997a78262"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()
        date=findViewById(R.id.date)
        temp=findViewById(R.id.temp)
        minTemp=findViewById(R.id.min_temp)
        maxTemp=findViewById(R.id.max_temp)
        feelsLike=findViewById(R.id.feelsLike)
        type=findViewById(R.id.type)
        windspeed = findViewById(R.id.wind)
        humidity = findViewById(R.id.humidity)
        pressure = findViewById(R.id.pressure)


    }

    private fun setDataOnViews(body: ModelClass?) {
        val sdf = SimpleDateFormat("dd/mm/yy hh:mm")
        val currentDate = sdf.format(Date())
        date.text = "Last updated on: $currentDate"
        maxTemp.text="${body!!.main.temp_max}°C/"
        minTemp.text="${body!!.main.temp_min}°C"
        temp.text = "${body!!.main.temp}°C"
        feelsLike.text = "Real feel: ${body!!.main.feels_like}°C"
        type.text = "${body!!.weather[0].main}"
        humidity.text = body.main.humidity.toString()
        windspeed.text = body.wind.speed.toString()
        pressure.text = body.main.pressure.toString()
    }

    private fun fetchCurrentLocationWeather(latitude: String, longitude: String){
        ApiUtilities.getAPIInterface()?.getCurrentWeatherData(latitude,longitude,API)?.enqueue(
            object: Callback<ModelClass>{
                override fun onResponse(call: Call<ModelClass>, response: Response<ModelClass>) {
                    if(response.isSuccessful){
                        setDataOnViews(response.body())
                    }
                }

                override fun onFailure(call: Call<ModelClass>, t: Throwable) {

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