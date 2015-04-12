package com.example.nisalperera.WeatherApp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this , SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment {

        private final String LOG_TAG = DetailFragment.class.getSimpleName();

        public DetailFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            Log.v(LOG_TAG, "String got passed ");

            // The detail Activity called via intent.  Inspect the intent for forecast data.
            Intent intent = getActivity().getIntent();
            View rootView = inflater.inflate(R.layout.fragment_detail_activity, container, false);
            Log.v(LOG_TAG, "String got passed ");
            if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
                String forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
                Log.v(LOG_TAG, "String got passed " + forecastStr);
                ((TextView)(rootView.findViewById(R.id.detail_text))).setText(forecastStr);
            }


            return rootView;
        }
    }
}
