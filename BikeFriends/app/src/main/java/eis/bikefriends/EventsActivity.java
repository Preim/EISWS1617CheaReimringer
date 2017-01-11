package eis.bikefriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.HashMap;

public class EventsActivity extends AppCompatActivity {
    private TextView mResult;
    private ListView resultsLV;
    ArrayList<HashMap<String, String>> resultsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        resultsList = new ArrayList<>();
        resultsLV = (ListView) findViewById(R.id.resultsLV);
        //final Button bSpeed = (Button) findViewById(R.id.speedB);
        FloatingActionButton createEventAB = (FloatingActionButton) findViewById(R.id.createEventAB);

        //mResult = (TextView) findViewById(R.id.tv_result);
        String ipAdresse = GlobalClass.getInstance().getIpAddresse();

        new GetVeranstaltungTask().execute(ipAdresse + "/events");

        createEventAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vErstellenIntent = new Intent(EventsActivity.this, eventErstellenActivity.class);
                EventsActivity.this.startActivity(vErstellenIntent);
            }
        });
    }

    class GetVeranstaltungTask extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progressDialog = new ProgressDialog(EventsActivity.this);
            progressDialog.setMessage("Loading Events...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return getVeranstaltung(params[0]);
            }   catch (IOException ex)  {
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
            //TODO Korrekte Darstellung der Listenelemente
            //TODO Verlinkung zu der Detailseite
            ListAdapter adapter = new SimpleAdapter(
                    EventsActivity.this, resultsList,
                    R.layout.list_item, new String[]{"title", "start", "time", "date"},
                    new int[]{R.id.eventTitle, R.id.eventStart, R.id.eventTime, R.id.eventDate});

            resultsLV.setAdapter(adapter);

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

                    JSONArray results = jsonObj.getJSONArray("results");

                    for (int i = 0; i < results.length(); i++) {
                        JSONObject r = results.getJSONObject(i);

                        String id = r.getString("_id");
                        String title = r.getString("title");
                        String start = r.getString("start");
                        String destination = r.getString("destination");
                        String date = r.getString("date");
                        //String zeit = r.getJSONObject("zeit").toString();
                        String time = r.getString("time");

                        HashMap<String, String> event = new HashMap<>();

                        event.put("id", id);
                        event.put("title", title);
                        event.put("start", start);
                        event.put("destination", destination);
                        event.put("date", date);
                        event.put("time", time);

                        resultsList.add(event);
                    }
                }catch (final JSONException e) {
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
