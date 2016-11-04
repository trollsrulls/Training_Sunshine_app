package org.training.max.sunshine.task;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.training.max.sunshine.util.BuildUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Async task for collecting of forecast from host
 */
public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

    private static final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

    @Override
    protected String[] doInBackground(String... locations) {
        if (locations == null || locations.length == 0) {
            Log.w(LOG_TAG, "Location for request doesn't specified.");
            // Nothing to lookup.
            return null;
        }

        final String stringUrl = BuildUtils.buildOpenWeatherMapUrlString(locations[0]);
        if (stringUrl == null) {
            // If url is 'null', then don't need to process anything.
            return null;
        }

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String forecastJsonStr = null;

        try {
            URL url = new URL(stringUrl);
            Log.v(LOG_TAG, "Built URL: " + url.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer buffer = new StringBuffer();

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }

            forecastJsonStr = buffer.toString();
            Log.v(LOG_TAG, "Forecast JSON string: " + forecastJsonStr);

            return BuildUtils.buildWeatherDataFromJson(forecastJsonStr);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error: ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting to parse it.
            return null;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error during parsing JSON string from server: " + forecastJsonStr, e);
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream: ", e);
                }
            }
        }
    }

}