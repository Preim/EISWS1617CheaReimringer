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
    TextView nameTv, age_genderTv, residenceTv, radtypTv, speedTv, distanceTv;
    SharedPreferences pref;
    String token,grav;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);
        TextView nameTv = (TextView) findViewById(R.id.nameTV);
        TextView age_genderTv = (TextView) findViewById(R.id.alter_geschlechtTv);
        TextView residenceTv = (TextView) findViewById(R.id.residenceET);
        TextView radtypTv = (TextView) findViewById(R.id.radtypTv);
        TextView speedTv = (TextView) findViewById(R.id.speedTv);
        TextView distanceTv = (TextView) findViewById(R.id.distanceTv);

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
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);



            //mResult.setText(result);


            if (progressDialog != null) {
                progressDialog.dismiss();
            }

        }

        private String getProfile(String urlPath) throws IOException {
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
                    String age = jsonObj.getString("bdate");
                    String gender = jsonObj.getString("geschlecht");
                    String age_gender =  age + ", " + gender;
                    String name = jsonObj.getString("name");
                    String residence = jsonObj.getString("wohnort");
                    String radtyp = jsonObj.getString("radtyp");
                    String speed = jsonObj.getString("speed");
                    String distance = jsonObj.getString("distance");
                    nameTv.setText(name);
                    age_genderTv.setText(age_gender);
                    residenceTv.setText(residence);
                    radtypTv.setText(radtyp);
                    speedTv.setText(speed);
                    distanceTv.setText(distance);


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
