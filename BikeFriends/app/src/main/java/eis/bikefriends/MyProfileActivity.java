package eis.bikefriends;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyProfileActivity extends AppCompatActivity {
    TextView nameTv, age_genderTv, radtypTv, speedTv, distanceTv, residenceTV;
    SharedPreferences pref;
    String token,grav;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);
        nameTv = (TextView) findViewById(R.id.nameTV);
        age_genderTv = (TextView) findViewById(R.id.alter_geschlechtTv);
        radtypTv = (TextView) findViewById(R.id.radtypTv);
        speedTv = (TextView) findViewById(R.id.speedTv);
        distanceTv = (TextView) findViewById(R.id.distanceTv);
        residenceTV = (TextView) findViewById(R.id.residenceTV);

        String ipaddress = GlobalClass.getInstance().getIpAddresse();
        //TODO Replace hardcoded UUID with real ID/Token
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);
        token = pref.getString("token", "DEADBEEF");
        //TODO Generate Dummy Profile #DEADBEEF?

        new GetMyProfileTask().execute(ipaddress + "/profiles/" + token );

        //Toolbar
        if(getSupportActionBar()!=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Mein Profil");
        }
    }

    //Toolbar back
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    class GetMyProfileTask extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        JSONObject json_result;


        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            progressDialog = new ProgressDialog(MyProfileActivity.this);
            progressDialog.setMessage("Loading Events...");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return getProfile(params[0]);
            }   catch (IOException ex)  {
                return "Network error!";
            } catch (JSONException e) {
                return "Data Invalid!";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);



            //mResult.setText(result);



            if (progressDialog != null) {
                progressDialog.dismiss();
            }
            if(json_result!=null){
                try {


                    JSONObject jsonObj = new JSONObject(result.toString());
                    //TODO Convert to age
                    String age = jsonObj.getString("birthdate");
                    String gender = jsonObj.getString("gender");
                    String age_gender =  age + ", " + gender;
                    String username = jsonObj.getString("username");
                    String residence = jsonObj.getString("residence");
                    //String radtyp = jsonObj.getString("radtyp");
                    //String speed = jsonObj.getString("speed");
                    //String distance = jsonObj.getString("distance");
                    nameTv.setText(username);
                    age_genderTv.setText(age_gender);
                    residenceTV.setText(residence);
                    //TODO Reale Daten anlegen
                    radtypTv.setText("Rennrad");
                    speedTv.setText("30 km/h");
                    distanceTv.setText("200km");


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
            }
        }

        private String getProfile(String urlPath) throws IOException, JSONException {
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
            } finally {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            }
            json_result = new JSONObject(result.toString());
            Log.d("json", result.toString());


            return result.toString();
        }
    }
}
