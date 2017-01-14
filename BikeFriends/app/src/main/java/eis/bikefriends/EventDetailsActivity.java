package eis.bikefriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
    private TextView eventIDTv, event_titleTv, event_startTv, event_destTv, event_timeTv, event_dateTv, event_descriptTv, event_organiser;
    String title, start, destination, time, date, description, organiser;
    SharedPreferences pref;
    Button teilnehmenbtn;



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
        event_titleTv = (TextView) findViewById(R.id.event_titleTv);
        event_startTv = (TextView) findViewById(R.id.event_startTv);
        event_destTv = (TextView) findViewById(R.id.event_destTv);
        event_timeTv = (TextView) findViewById(R.id.event_timeTv);
        event_dateTv = (TextView) findViewById(R.id.event_dateTv);
        event_descriptTv = (TextView) findViewById(R.id.event_descriptTv);
        eventIDTv = (TextView) findViewById(R.id.eventIDTv);
        event_organiser = (TextView) findViewById(R.id.event_organizerTV);

        pref = getSharedPreferences("AppPref", MODE_PRIVATE);

        teilnehmenbtn = (Button) findViewById(R.id.teilnehmenbtn);

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


    class GetVeranstaltungTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progressDialog = new ProgressDialog(EventDetailsActivity.this);
            progressDialog.setMessage("Lade Veranstaltung...");
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


            //Prüfen, ob der Betrachter der Veranstalter ist. Wenn wahr, teilnehmenbtn verbergen


            if (progressDialog != null) {
                progressDialog.dismiss();
            }


            eventIDTv.setText("Event ID: " + eventID);
            event_titleTv.setText(title);
            event_startTv.setText("Start: " + start);
            event_destTv.setText("Ziel: " + destination);
            event_timeTv.setText("Zeit: " + time);
            event_dateTv.setText("Datum: " + date);
            event_descriptTv.setText("Beschreibung: " + description);
            event_organiser.setText("Veranstalter" + organiser);

            String pref_organiser = pref.getString("userID", null);
            if (organiser.equals(pref_organiser)){
                teilnehmenbtn.setVisibility(View.GONE);
            }

            if(getSupportActionBar()!=null){
                getSupportActionBar().setTitle(title);
            }

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
                    JSONObject event = jsonObj.getJSONObject("results");

                    title = event.getString("title");
                    start = event.getString("start");
                    destination = event.getString("destination");
                    date = event.getString("date");
                    time = event.getString("time");
                    description = event.getString("description");
                    organiser = event.getString("organiser");


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
