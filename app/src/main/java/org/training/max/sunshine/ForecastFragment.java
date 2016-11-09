package org.training.max.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.training.max.sunshine.util.BuildUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Fragment for displaying forecast list.
 */
public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastArrayAdapter;

    /**
     * Default constructor
     */
    public ForecastFragment() {
        // This constructor is intentionally empty. Nothing special is needed here.
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable handling menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mForecastArrayAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastArrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String forecast = mForecastArrayAdapter.getItem(position);

                Intent detailActivityIntent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(detailActivityIntent);
            }

        });

        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPref.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        FetchWeatherTask weatherTask = new FetchWeatherTask();
        weatherTask.execute(location);
    }

    /**
     * Async task for collecting of forecast from host
     */
    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        /**
         * Prepare the weather high/lows for presentation.
         *
         * @param high     high double value
         * @param low      low double value
         * @param unitType format for temperature (metric or imperial)
         * @return representable string with high/low temperatures
         */
        private String formatHighLows(double high, double low, String unitType) {
            String suffix = "C";

            // Converting metric temperature to imperial format
            if (getString(R.string.pref_units_imperial).equals(unitType)) {
                // temperature in Fahrenheit = (temperature in Celsius * 1.8) + 32
                high = (high * 1.8) + 32;
                low = (low * 1.8) + 32;
                suffix = "F";
            } else if (!getString(R.string.pref_units_metric).equals(unitType)) {
                Log.d(LOG_TAG, "Unit type not found: " + unitType);
            }

            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow + " " + suffix;
            return highLowStr;
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
        private String[] getWeatherDataFromJson(String forecastJsonStr) throws JSONException {
            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            final Calendar calendar = new GregorianCalendar();
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final String unitType = sharedPref.getString(getString(R.string.pref_units_key),
                    getString(R.string.pref_units_metric));

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

                String highAndLow = formatHighLows(high, low, unitType);

                // For now, using the format "Day, description, hi/low"
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... locations) {
            if (locations == null || locations.length == 0) {
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

                return getWeatherDataFromJson(forecastJsonStr);
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

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null && result.length > 0) {
                mForecastArrayAdapter.clear();
                mForecastArrayAdapter.addAll(result);
            }
        }
    }

}
