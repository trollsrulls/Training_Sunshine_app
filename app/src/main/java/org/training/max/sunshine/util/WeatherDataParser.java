package org.training.max.sunshine.util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for parsing weather data from server
 */
public final class WeatherDataParser {

    private static final String LIST_NAME = "list";
    private static final String TEMPERATURE_NAME = "temp";
    private static final String WEATHER_NAME = "weather";
    private static final String MAIN_WEATHER_NAME = "main";
    private static final String MAX_TEMPERATURE_NAME = "max";
    private static final String MIN_TEMPERATURE_NAME = "min";

    private WeatherDataParser() {
        // This constructor is intentionally empty. It is utility class which shouldn't be instantiated.
    }

    /**
     * Retrieve the maximum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     *
     * @param weatherJsonStr JSON string with weather data from server
     * @param dayIndex       index of day (0 - index of first day)
     * @return max temperature for day
     * @throws JSONException
     */
    public static double getMaxTemperatureForDay(String weatherJsonStr, int dayIndex) throws JSONException {
        return new JSONObject(weatherJsonStr)
                .getJSONArray(LIST_NAME)
                .getJSONObject(dayIndex)
                .getJSONObject(TEMPERATURE_NAME)
                .getDouble(MAX_TEMPERATURE_NAME);
    }

    /**
     * Retrieve the minimum temperature for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     *
     * @param weatherJsonStr JSON string with weather data from server
     * @param dayIndex       index of day (0 - index of first day)
     * @return min temperature for day
     * @throws JSONException
     */
    public static double getMinTemperatureForDay(String weatherJsonStr, int dayIndex) throws JSONException {
        return new JSONObject(weatherJsonStr)
                .getJSONArray(LIST_NAME)
                .getJSONObject(dayIndex)
                .getJSONObject(TEMPERATURE_NAME)
                .getDouble(MIN_TEMPERATURE_NAME);
    }

    /**
     * Retrieve the main weather for the day indicated by dayIndex
     * (Note: 0-indexed, so 0 would refer to the first day).
     *
     * @param weatherJsonStr JSON string with weather data from server
     * @param dayIndex       index of day (0 - index of first day)
     * @return main whether for the day
     * @throws JSONException
     */
    public static String getMainWeatherForDay(String weatherJsonStr, int dayIndex) throws JSONException {
        return new JSONObject(weatherJsonStr)
                .getJSONArray(LIST_NAME)
                .getJSONObject(dayIndex)
                .getJSONObject(WEATHER_NAME)
                .getString(MAIN_WEATHER_NAME);
    }

}
