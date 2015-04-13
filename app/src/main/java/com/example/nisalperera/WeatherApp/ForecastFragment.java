package com.example.nisalperera.WeatherApp;

/**
 * Created by nisalperera on 15-04-09.
 */

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A Forecast fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // callback for fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecast_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle action bar items here
        int id = item.getItemId();
        // for debuging purposes click refresh button to update data
        if (id == R.id.action_refresh) {
            updateWeather();
            //rerurn true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather(){

        // we read the preferences here , if preferences is not set we fall back to the default
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Log.v(LOG_TAG, "pref_location_key " + getString(R.string.pref_location_key));
        Log.v(LOG_TAG, "pref_location_default " + getString(R.string.pref_location_default));

        //these two are used as params[0] and params[1] when crating the url i fetchweather task
        String location = prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        String units = prefs.getString(getString(R.string.pref_units_key),getString(R.string.pref_units_metric));

        Log.v(LOG_TAG, "got new location at " + location);

        FetchweatherTask fetchTask = new FetchweatherTask();
        fetchTask.execute(location,units);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /*
        String[] weatherdata = {"Monday sunny 18", "Tuesday rainy 10", "Wednesday sunny 12", "Thursday windy 13", "Monday sunny 18", "Tuesday rainy 10", "Wednesday sunny 12", "Thursday windy 13", "Monday sunny 18", "Tuesday rainy 10", "Wednesday sunny 12", "Thursday windy 13", "Monday sunny 18", "Tuesday rainy 10", "Wednesday sunny 12", "Thursday windy 13"};

        List<String> weekforecast = new ArrayList<String>(Arrays.asList(weatherdata));
        */
        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        listView.setAdapter(mForecastAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // make a quick toast with weather data of the clicked item
                Toast.makeText(getActivity(), mForecastAdapter.getItem(position), Toast.LENGTH_SHORT).show();

                // call DetailActivity to show weatherdata on a full screen
                Intent DetailActivityIntent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, mForecastAdapter.getItem(position));
                startActivity(DetailActivityIntent);
            }
        });


        return rootView;
    }


    public class FetchweatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchweatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(FORMAT_PARAM, "json")
                        .appendQueryParameter(UNITS_PARAM, params[1])
                        .appendQueryParameter(DAYS_PARAM, "15") //for next 20 days
                        .build();

                Log.v(LOG_TAG, "built Uri = " + builtUri.toString());
                URL url = new URL(builtUri.toString());

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

                Log.v(LOG_TAG, "Forecast JSON String" + forecastJsonStr);

                // here we call the WeatherDataParser
                WeatherDataParser weatherParser = new WeatherDataParser();

                try {
                    //String [] weatherdata = weatherParser.getWeatherDataFromJson(forecastJsonStr, 7 );
                    //Log.v(LOG_TAG, "@@@" + weatherdata[0] );
                    return weatherParser.getWeatherDataFromJson(forecastJsonStr, 15); //number of days is 20 for now
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }


            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mForecastAdapter.clear();
                mForecastAdapter.addAll(result);
            }
        }
    }
}
