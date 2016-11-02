package org.training.max.sunshine;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.training.max.sunshine.util.BuildUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment for displaying forecast list.
 */
public class ForecastFragment extends Fragment {

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
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Tue 6/24 - Foggy - 21/8",
                "Wed 6/25 - Cloudy - 22/17",
                "Thurs 6/26 - Rainy - 18/11",
                "Fri 6/27 - Foggy - 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
                "Sun 6/29 - Sunny - 20/7"
        };
        List<String> weekData = new ArrayList<>(Arrays.asList(data));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                weekData);


        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(arrayAdapter);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            FetchWeatherTask asyncTask = new FetchWeatherTask();
            asyncTask.execute("94043");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Async task for collecting of forecast from host
     */
    public class FetchWeatherTask extends AsyncTask<String, Void, Void> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected Void doInBackground(String... locations) {
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
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();

                Log.v(LOG_TAG, "JSON: " + forecastJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error: ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting to parse it.
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
            return null;
        }

    }

}
