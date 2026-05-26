package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val current: CurrentUnits,
    val hourly: HourlyData,
    val daily: DailyData
)

@JsonClass(generateAdapter = true)
data class CurrentUnits(
    val time: String,
    val temperature_2m: Double,
    val relative_humidity_2m: Double?,
    val weather_code: Int,
    val uv_index: Double?
)

@JsonClass(generateAdapter = true)
data class HourlyData(
    val time: List<String>,
    val temperature_2m: List<Double>,
    val weather_code: List<Int>
)

@JsonClass(generateAdapter = true)
data class DailyData(
    val time: List<String>,
    val weather_code: List<Int>,
    val temperature_2m_max: List<Double>,
    val temperature_2m_min: List<Double>
)

// UI friendly transformed Weather state
data class WeatherState(
    val temperature: String,
    val relativeHumidity: String,
    val uvIndex: String,
    val airQualityIndex: String, // Calculated or estimated cleanly since Open-Meteo is free
    val weatherDescription: String,
    val isRainy: Boolean,
    val isCloudy: Boolean,
    val isClear: Boolean,
    val hourlyForecast: List<HourlyForecastItem>,
    val dailyForecast: List<DailyForecastItem>,
    val lastUpdated: Long
)

data class HourlyForecastItem(
    val hour: String, // e.g. "10:00 AM"
    val temp: String,
    val code: Int,
    val isNight: Boolean
)

data class DailyForecastItem(
    val dayName: String, // e.g. "Mon"
    val maxTemp: String,
    val minTemp: String,
    val code: Int
)

object WeatherUtils {
    fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy Condition"
            51, 53, 55 -> "Light Drizzle"
            61, 63, 65 -> "Continuous Rain"
            71, 73, 75 -> "Snow Fall"
            80, 81, 82 -> "Rain Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Fine Sky"
        }
    }

    fun isRainy(code: Int): Boolean {
        return code in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82, 95, 96, 99)
    }

    fun isCloudy(code: Int): Boolean {
        return code in listOf(1, 2, 3, 45, 48)
    }

    fun isClear(code: Int): Boolean {
        return code == 0
    }
}
