package ch.vanakh.eis_poc;

import android.app.ProgressDialog;
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

public class MatchingActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matching);

        final Button bSubmit = (Button) findViewById(R.id.b_submit);
        final EditText input_id = (EditText) findViewById(R.id.input_id);
        final EditText input_username = (EditText) findViewById(R.id.input_username);
        final EditText input_averageSpeed = (EditText) findViewById(R.id.input_averageSpeed);
        assert bSubmit != null;
        //new PostDataTask().execute("http://192.168.0.101:3000/profiles");
        bSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PostDataTask().execute("http://192.168.0.101:3000/profiles");

            }
        });
    }

    class PostDataTask extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;


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


        private String postData(String urlPath) throws IOException, JSONException {
            StringBuilder result = new StringBuilder();
            BufferedWriter bufferedWriter = null;
            BufferedReader bufferedReader = null;
            final EditText input_id = (EditText) findViewById(R.id.input_id);
            final EditText input_username = (EditText) findViewById(R.id.input_username);
            final EditText input_averageSpeed = (EditText) findViewById(R.id.input_averageSpeed);


            try{
                ArrayList<String> list = new ArrayList<String>();
                list.add("Rennrad");
                list.add("Mountainbike");
                //Create data to send to server
                JSONObject dataToSend = new JSONObject();
                dataToSend.put("id", input_id.getText().toString().trim() );
                dataToSend.put("username", input_username.getText().toString().trim());
                dataToSend.put("bikesports", new JSONArray(list) );
                dataToSend.put("averageSpeed", Integer.parseInt(input_averageSpeed.getText().toString()));
                dataToSend.put("averageSessionDistance", 20);

                /*dataToSend.put("id", "7553" );
                dataToSend.put("username", "Test123");
                dataToSend.put("bikesports", new JSONArray(list) );
                dataToSend.put("averageSpeed", 34);
                dataToSend.put("averageSessionDistance", 20);*/

                //init and config request, then connect to server
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
            } finally {
                if (bufferedReader != null){
                    bufferedReader.close();
                }
                if (bufferedWriter != null){
                    bufferedWriter.close();
                }
            }
            System.out.printf(result.toString());
            //Log.d(result.toString());
            return result.toString();
        }
    }


}
