package eis.bikefriends;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

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
    public final static String eventID = "eis.bikefriends.EventsActivity_eventID";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;



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

        assert createEventAB != null;
        createEventAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent vErstellenIntent = new Intent(EventsActivity.this, eventErstellenActivity.class);
                EventsActivity.this.startActivity(vErstellenIntent);
            }
        });


        //Toolbar
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        //Toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Veranstaltungen");
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    //Toolbar back
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }



    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Events Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    class GetVeranstaltungTask extends AsyncTask<String, Void, String> {

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
            //TODO Korrekte Darstellung der Listenelemente
            ListAdapter adapter = new SimpleAdapter(
                    EventsActivity.this, resultsList,
                    R.layout.list_item, new String[]{"title", "start", "time", "date"},
                    new int[]{R.id.eventTitle, R.id.eventStart, R.id.eventTime, R.id.eventDate});

            resultsLV.setAdapter(adapter);

            //TODO Korrekte EventID weiterleiten
            resultsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent itemInten = new Intent(EventsActivity.this, EventDetailsActivity.class);
                    itemInten.putExtra(eventID, String.valueOf(id));
                    startActivity(itemInten);
                }
            });

            /*resultsLV.setOnClickListener(onListClick);
            AdapterView.OnItemClickListener onListClick = new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent itemIntent = new Intent(EventsActivity.this, EventDetailsActivity.class);
                    itemIntent.putExtra(eventID, String.valueOf(id));
                    startActivity(itemIntent);
                }
            };*/

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
                        //String time = r.getString("time");

                        HashMap<String, String> event = new HashMap<>();

                        event.put("id", id);
                        event.put("title", title);
                        event.put("start", start);
                        event.put("destination", destination);
                        event.put("date", date);
                       // event.put("time", time);

                        resultsList.add(event);
                    }
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
