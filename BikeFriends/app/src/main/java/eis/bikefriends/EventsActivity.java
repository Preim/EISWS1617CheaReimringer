package eis.bikefriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
    ArrayList<HashMap<String, String>> resultsList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultsList = new ArrayList<>();
        final Button bSpeed = (Button) findViewById(R.id.speedB);

        mResult = (TextView) findViewById(R.id.tv_result);
        new GetVeranstaltungTask().execute("http://192.168.0.104:3000/events");

        assert bSpeed != null;
        bSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent matchingIntent = new Intent(EventsActivity.this, GpsActivity.class);
                EventsActivity.this.startActivity(matchingIntent);

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


            //set data response to textView
            mResult.setText(result);

            //cancel progress dialog
            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        private String getVeranstaltung(String urlPath) throws IOException {
            StringBuilder result = new StringBuilder();
            BufferedReader bufferedReader = null;

            try {
                //Initialize and config request, then connect to server
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(10000 /* milliseconds */);
                urlConnection.setRequestMethod("GET");
                urlConnection.setRequestProperty("Content-Type", "application/json");// set header
                urlConnection.connect();

                //Read data response from server
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

                        String id = r.getString("id");
                        String username = r.getString("username");
                        String averageSpeed = r.getString("averageSpeed");
                        String averageDistance = r.getString("averageSessionDistance");
                        //String bikeSports = r.getJSONObject("bikesports").toString();

                        HashMap<String, String> user = new HashMap<>();

                        user.put("id", id);
                        user.put("username", username);
                        user.put("averageSpeed", averageSpeed);
                        //user.put("bikeSports", bikeSports);
                        user.put("averageDistance", averageDistance);

                        resultsList.add(user);
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
