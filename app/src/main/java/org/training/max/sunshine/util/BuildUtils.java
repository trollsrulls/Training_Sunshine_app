package org.training.max.sunshine.util;

import android.net.Uri;

import org.apache.commons.lang3.StringUtils;
import org.training.max.sunshine.BuildConfig;

/**
 * Utility class with methods for building some data.
 */
public final class BuildUtils {

    private static final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    private static final String QUERY_PARAM = "q";
    private static final String FORMAT_PARAM = "mode";
    private static final String UNITS_PARAM = "units";
    private static final String DAYS_PARAM = "cnt";
    private static final String APPID_PARAM = "APPID";

    /**
     * We can't instantiate the utility class.
     */
    private BuildUtils() {
        // This constructor is intentionally empty. It is utility class which shouldn't be instantiated.
    }

    /**
     * Construct the URL for the OpenWeatherMap query
     * Something like that:
     * <p>
     * http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=7659e2c75dd75ab082547cf8aafd3a23
     * <p>
     * Possible parameters are available at OWM's forecast API page, at http://openweathermap.org/API#forecast
     *
     * @param location Specified location
     * @return URL for requesting forecast. If location is 'null', then will return 'null'
     */
    public static String buildOpenWeatherMapUrlString(String location) {
        if (location != null && !StringUtils.isEmpty(location.trim())) {
            // We use json format for our app
            final String format = "json";
            // We always get weather forecast in metric units
            final String units = "metric";
            // TODO: Maybe should do this configurable
            // Count of forecast days
            final int numDays = 14;

            return Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, location.trim())
                    .appendQueryParameter(FORMAT_PARAM, format)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_MAP_API_KEY)
                    .build().toString();
        }
        return null;
    }

}