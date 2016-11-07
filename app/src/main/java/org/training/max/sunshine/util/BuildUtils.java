package org.training.max.sunshine.util;

import android.net.Uri;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.training.max.sunshine.BuildConfig;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Utility class with methods for building some data.
 */
public final class BuildUtils {

    private static final String LOG_TAG = BuildUtils.class.getSimpleName();

    private static final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
    private static final String QUERY_PARAM = "q";
    private static final String FORMAT_PARAM = "mode";
    private static final String UNITS_PARAM = "units";
    private static final String DAYS_PARAM = "cnt";
    private static final String APPID_PARAM = "APPID";

    private static final String OWM_LIST = "list";
    private static final String OWM_WEATHER = "weather";
    private static final String OWM_TEMPERATURE = "temp";
    private static final String OWM_MAX = "max";
    private static final String OWM_MIN = "min";
    private static final String OWM_DESCRIPTION = "main";

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
            // Temp declaration.
            final String format = "json";
            final String units = "metric";
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

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     *
     * @param forecastJsonStr JSON string from server
     * @return weather data like string array
     * @throws JSONException
     */
    public static String[] buildWeatherDataFromJson(String forecastJsonStr) throws JSONException {
        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        final Calendar calendar = new GregorianCalendar();
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());

        String[] resultStrs = new String[weatherArray.length()];
        for (int i = 0; i < weatherArray.length(); i++) {
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            String day = simpleDateFormat.format(calendar.getTime());
            calendar.add(GregorianCalendar.DATE, 1);

            // Whether it is array which should contain minimum one element. So we don't have check for length here.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            String description = weatherObject.getString(OWM_DESCRIPTION);

            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            String highAndLow = formatHighLows(high, low);

            // For now, using the format "Day, description, hi/low"
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }
        return resultStrs;
    }

    /**
     * Prepare the weather high/lows for presentation.
     *
     * @param high high double value
     * @param low  low double value
     * @return representable string with high/low temperatures
     */
    private static String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        return roundedHigh + "/" + roundedLow;
    }

}