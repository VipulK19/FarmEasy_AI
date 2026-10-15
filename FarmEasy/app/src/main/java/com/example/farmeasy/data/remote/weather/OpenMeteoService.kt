package com.example.farmeasy.data.remote.weather

import com.example.farmeasy.domain.model.ForecastItem
import com.example.farmeasy.domain.model.WeatherReport
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable
data class OpenMeteoResponse(
    @SerialName("latitude") val latitude: Double? = null,
    @SerialName("longitude") val longitude: Double? = null,
    @SerialName("current") val current: CurrentWeatherDto? = null,
    @SerialName("daily") val daily: DailyWeatherDto? = null
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("temperature_2m") val temperature: Double? = null,
    @SerialName("relative_humidity_2m") val humidity: Int? = null,
    @SerialName("precipitation") val precipitation: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
    @SerialName("wind_speed_10m") val windSpeed: Double? = null
)

@Serializable
data class DailyWeatherDto(
    @SerialName("time") val time: List<String>? = null,
    @SerialName("weather_code") val weatherCodes: List<Int>? = null,
    @SerialName("temperature_2m_max") val maxTemps: List<Double>? = null,
    @SerialName("temperature_2m_min") val minTemps: List<Double>? = null,
    @SerialName("precipitation_sum") val precipitationSums: List<Double>? = null
)

class OpenMeteoService(private val client: HttpClient) {

    suspend fun fetchWeather(lat: Double, lng: Double): WeatherReport {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lng&current=temperature_2m,relative_humidity_2m,precipitation,weather_code,wind_speed_10m&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum&timezone=auto"
        val response: OpenMeteoResponse = client.get(url).body()

        val temp = response.current?.temperature ?: 28.0
        val hum = response.current?.humidity ?: 55
        val wind = response.current?.windSpeed ?: 12.0
        val precip = response.current?.precipitation ?: 0.0
        val code = response.current?.weatherCode ?: 1
        val conditionDesc = mapWeatherCodeToDescription(code)

        val forecastItems = mutableListOf<ForecastItem>()
        val times = response.daily?.time ?: emptyList()
        val maxs = response.daily?.maxTemps ?: emptyList()
        val mins = response.daily?.minTemps ?: emptyList()
        val pSums = response.daily?.precipitationSums ?: emptyList()
        val wCodes = response.daily?.weatherCodes ?: emptyList()

        val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfOut = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 0 until minOf(times.size, maxs.size, mins.size)) {
            val dateStr = times[i]
            val dayName = try {
                val d = sdfIn.parse(dateStr)
                if (d != null) sdfOut.format(d) else "Day $i"
            } catch (_: Exception) {
                "Day $i"
            }
            forecastItems.add(
                ForecastItem(
                    dayOfWeek = dayName,
                    maxTemp = maxs.getOrElse(i) { temp + 2.0 },
                    minTemp = mins.getOrElse(i) { temp - 4.0 },
                    weatherCondition = mapWeatherCodeToDescription(wCodes.getOrElse(i) { 1 }),
                    precipitationMm = pSums.getOrElse(i) { 0.0 }
                )
            )
        }

        val advisory = if (precip > 5.0) {
            "Rain expected. Postpone foliar spraying and ensure irrigation channels are drained."
        } else if (temp > 35.0) {
            "High temperature. Provide light evening irrigation to reduce evapotranspiration stress."
        } else {
            "Optimal weather for intercultural operations, field scouting, and pesticide application."
        }

        return WeatherReport(
            locationName = "Farm Region (${"%.2f".format(lat)}, ${"%.2f".format(lng)})",
            temperatureCelsius = temp,
            humidityPercent = hum,
            windSpeedKmh = wind,
            conditionDescription = conditionDesc,
            precipitationMm = precip,
            advisoryNotice = advisory,
            forecast = forecastItems.take(7),
            lastUpdated = System.currentTimeMillis(),
            isCached = false
        )
    }

    private fun mapWeatherCodeToDescription(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1, 2, 3 -> "Partly Cloudy"
            45, 48 -> "Foggy"
            51, 53, 55 -> "Light Drizzle"
            61, 63, 65 -> "Rain Showers"
            71, 73, 75 -> "Snow Flurries"
            80, 81, 82 -> "Heavy Rain Showers"
            95, 96, 99 -> "Thunderstorm"
            else -> "Scattered Clouds"
        }
    }
}
