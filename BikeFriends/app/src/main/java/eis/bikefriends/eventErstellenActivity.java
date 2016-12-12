package eis.bikefriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class eventErstellenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_erstellen);

        final Button eventErst = (Button) findViewById(R.id.eventErstB);
        final Button eventAbbr = (Button) findViewById(R.id.abbrechenB);

        assert eventErst != null;
        eventErst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostDataTask().execute("http://192.168.0.104:3000/events");
            }
        });

        assert eventAbbr != null;
        eventAbbr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent eventsIntent = new Intent(eventErstellenActivity.this, EventsActivity.class);
                eventErstellenActivity.this.startActivity(eventsIntent);
            }
        });
    }

    class PostDataTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(eventErstellenActivity.this);
            progressDialog.setMessage("Inserting data...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                return postData(params[0]);
            } catch (IOException ex) {
                return "Network error!";
            } catch (JSONException ex) {
                return "Data Invalid!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        private String postData(String urlPath) throws IOException, JSONException {

            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;

            final EditText inputTitle = (EditText) findViewById(R.id.titelET);
            final EditText inputBeschreibung = (EditText) findViewById(R.id.beschreibungET);
            final EditText inputDatum = (EditText) findViewById(R.id.datumET);
            final EditText inputTime = (EditText) findViewById(R.id.timeET);
            final EditText inputZiel = (EditText) findViewById(R.id.zielET);
            int inputID = 0;

            try {
                //data erstellen
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("id", inputID);
                dataToSend.put("event", inputTitle.getText().toString().trim());
                dataToSend.put("ort", inputZiel.getText().toString().trim() );
                dataToSend.put("zeit", inputTime.getText().toString().trim());
                dataToSend.put("datum", inputDatum.getText().toString());
                dataToSend.put("beschreibung", inputBeschreibung.getText().toString().trim());






                //connect zum server
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(10000 /* milliseconds */);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true); //enable output (body data)
                urlConnection.setRequestProperty("Content-Type", "application/json");// set header
                urlConnection.connect();

                //Write data
                OutputStream outputStream = urlConnection.getOutputStream();
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(dataToSend.toString());
                bufferedWriter.flush();

                //Read data
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result.append(line).append("\n");
                    Log.d("json", line.toString());
                }
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }

            }

            Log.d("json", result.toString());
            return result.toString();
        }
    }
}
