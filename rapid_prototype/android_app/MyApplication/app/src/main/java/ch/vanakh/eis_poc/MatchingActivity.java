package ch.vanakh.eis_poc;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;

public class MatchingActivity extends AppCompatActivity {

    private TextView mResult;
    private ListView lv;

    ArrayList<HashMap<String, String>> resultsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);
        resultsList = new ArrayList<>();

        lv = (ListView) findViewById(R.id.list);

        final Button bSubmit = (Button) findViewById(R.id.b_submit);
        final EditText input_id = (EditText) findViewById(R.id.input_id);
        final EditText input_username = (EditText) findViewById(R.id.input_username);
        final EditText input_averageSpeed = (EditText) findViewById(R.id.input_averageSpeed);
        assert bSubmit != null;
        //new PostDataTask().execute("http://192.168.0.101:3000/profiles");
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostDataTask().execute("http://localhost:3000/profiles");
                //mResult = (TextView) findViewById(R.id.tv_result);

            }
        });
    }

    class PostDataTask extends AsyncTask<String, Void, String> {



        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(MatchingActivity.this);
            progressDialog.setMessage("Inserting data...");
            progressDialog.show();
        }


        @Override
        protected String doInBackground(String... params){
            try{
                return postData(params[0]);
            } catch (IOException ex) {
                return "Networkerror!";
            } catch (JSONException ex) {
                return "Data Invalid";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //mResult.setText(result);


            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            ListAdapter adapter = new SimpleAdapter(
                    MatchingActivity.this, resultsList,
                    R.layout.list_item, new String[]{"id", "username", "averageSpeed", "averageDistance"},
                    new int[]{R.id.id, R.id.username, R.id.averageSpeed, R.id.averageDistance});

            lv.setAdapter(adapter);

        }


        private String postData(String urlPath) throws IOException, JSONException {
            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;
            final EditText input_id = (EditText) findViewById(R.id.input_id);
            final EditText input_username = (EditText) findViewById(R.id.input_username);
            final EditText input_averageSpeed = (EditText) findViewById(R.id.input_averageSpeed);

            try{
                ArrayList<String> list = new ArrayList<String>();
                list.add("BMX");
                list.add("Einrad");
                //data zum server senden
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("id", input_id.getText().toString().trim() );
                dataToSend.put("username", input_username.getText().toString().trim());
                dataToSend.put("bikesports", new JSONArray(list) );
                dataToSend.put("averageSpeed", Integer.parseInt(input_averageSpeed.getText().toString()));
                dataToSend.put("averageSessionDistance", 20);

                //init und config request, connect zum server
                URL url = new URL(urlPath);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000 /*millisekunden*/);
                urlConnection.setConnectTimeout(10000 /*msec*/);
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.connect();

                //write data
                OutputStream outputStream = urlConnection.getOutputStream();
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                bufferedWriter.write(dataToSend.toString());
                bufferedWriter.flush();

                //read response
                InputStream inputStream = urlConnection.getInputStream();
                bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));
                String line;
                while ((line = bufferedReader.readLine()) != null){
                    result.append(line).append("\n");
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

                        HashMap<String, String> events = new HashMap<>();

                        events.put("id", id);
                        events.put("username", username);
                        events.put("averageSpeed", averageSpeed);
                        events.put("bikeSports", bikeSports);
                        events.put("averageDistance", averageDistance);


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




                //Log.d("jsondebug", result.toString());
            } finally {
                if (bufferedReader != null){
                    bufferedReader.close();
                }
                if (bufferedWriter != null){
                    bufferedWriter.close();
                }
            }
            //System.out.printf(result.toString());

            return result.toString();
        }
    }


}
