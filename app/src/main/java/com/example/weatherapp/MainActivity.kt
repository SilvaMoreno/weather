package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import kotlin.math.round

class MainActivity : AppCompatActivity() {
    private val apiKey = "1c8ecc9c0780f9af34634e0c9b54a1fd";
    private lateinit var weatherService: WeatherService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherService = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)

        GlobalScope.launch(Dispatchers.IO) {
            try {

            val weatherData = weatherService.getWeather("Viana do Castelo", apiKey)
            withContext(Dispatchers.Main) {
                updateUi(weatherData)
            }
            } catch (ex: Exception) {
                Log.d("exception","ola mundi")
                Log.d("exception", ex.message  ?: "untreated error")
               // Toast.makeText(this@MainActivity, ex.message  ?: "untreated error", Toast.LENGTH_SHORT)
            }

        }
    }

    private fun updateUi(weatherData: WeatherData) {
        findViewById<TextView>(R.id.textViewCity).text= weatherData.name
        findViewById<TextView>(R.id.textViewWeather).text= weatherData.weather[0].main
        findViewById<TextView>(R.id.textViewTemperature).text= "${round(weatherData.main.temp).toInt()} ºC"
        val iconUrl = "https://openweathermap.org/img/w/${weatherData.weather[0].icon}.png"
        Glide.with(this)
            .load(iconUrl)
            .into(findViewById<ImageView>(R.id.imageViewWeatherIcon))

        // Fórmula de conversão: 1 m/s = 3.6 km/h
        findViewById<TextView>(R.id.textViewHumidity).text= "${weatherData.main.humidity.toInt()}%"

        findViewById<TextView>(R.id.textViewWindSpeed).text= "${convertWindDirectionToText(weatherData.wind.deg)} ${round(weatherData.wind.speed * 3.6).toInt()} km/h"
    }

    private fun convertWindDirectionToText(windDegrees: Double): String {
        val directions = arrayOf("N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE", "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW", "N")

        // Garante que os graus estejam dentro do intervalo [0, 360)
        val normalizedDegrees = (windDegrees % 360 + 360) % 360

        // Divide a circunferência em 16 setores
        val index = ((normalizedDegrees + 11.25) / 22.5).toInt()

        return directions[index]
    }

}

data class WeatherData (
    val name: String,
    val main: Main,
    val weather: List<Weather>,
    var wind: Wind
)

data class Main(
    val temp: Double,
    val humidity: Double
)

data class Weather(
    val main: String,
    val icon: String
)

data class Wind (
    val speed: Double,
    val deg: Double
)