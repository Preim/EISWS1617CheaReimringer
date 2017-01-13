package eis.bikefriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class EventDetailsActivity extends AppCompatActivity {

    String eventID;
    private TextView eventIDTv, event_titleTv, event_startTv, event_destTv, event_timeTv, event_dateTv, event_descriptTv;
    String title, start, destination, time, date, description;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        //Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Veranstaltungens Name");
        }

        String ipAdresse = GlobalClass.getInstance().getIpAddresse();
        eventID = getIntent().getStringExtra(EventsActivity.eventID);
        new GetVeranstaltungTask().execute(ipAdresse + "/events/" + eventID);
    }

    //Toolbar back
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    //TODO Datenabrufen

    class GetVeranstaltungTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progressDialog = new ProgressDialog(EventDetailsActivity.this);
            progressDialog.setMessage("Loading Events...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return getVeranstaltung(params[0]);
            } catch (IOException ex) {
                return "Network error!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);


            //mResult.setText(result);


            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            event_titleTv = (TextView) findViewById(R.id.event_titleTv);
            event_startTv = (TextView) findViewById(R.id.event_startTv);
            event_destTv = (TextView) findViewById(R.id.event_destTv);
            event_timeTv = (TextView) findViewById(R.id.event_timeTv);
            event_dateTv = (TextView) findViewById(R.id.event_dateTv);
            event_descriptTv = (TextView) findViewById(R.id.event_descriptTv);
            eventIDTv = (TextView) findViewById(R.id.eventIDTv);

            eventIDTv.setText(eventID);
            event_titleTv.setText(title);
            event_startTv.setText(start);
            event_destTv.setText(destination);
            event_timeTv.setText(time);
            event_dateTv.setText(date);
            event_descriptTv.setText(description);

        }

        private String getVeranstaltung(String urlPath) throws IOException {
            StringBuilder result = new StringBuilder();
            BufferedReader bufferedReader = null;

            try {
                //connect zum server
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(10000 /* milliseconds */);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");// set header
                urlConnection.connect();

                //Read data response
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                    Log.d("json", line);
                }

                try {


                    JSONObject jsonObj = new JSONObject(result.toString());

                    title = jsonObj.getString("title");
                    start = jsonObj.getString("start");
                    destination = jsonObj.getString("destination");
                    date = jsonObj.getString("date");
                    time = jsonObj.getString("time");
                    description = jsonObj.getString("description");


                } catch (final JSONException e) {
                    Log.e("parsingError", "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
            Log.d("json", result.toString());


            return result.toString();
        }
    }


}
