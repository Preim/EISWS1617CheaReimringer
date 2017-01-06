package eis.bikefriends;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.app.TimePickerDialog;
import android.widget.TimePicker;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class eventErstellenActivity extends AppCompatActivity implements View.OnClickListener{
    Button eventErst, eventAbbr, timebtn, datebtn;
    EditText inputTitle, inputDescription, inputStart, inputDestination;
    TextView inputDate, inputTime;
    TimePickerDialog timePickerDialog;
    DatePickerDialog datePickerDialog;
    Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_erstellen);

        eventErst = (Button) findViewById(R.id.erstellenB);
        eventErst.setOnClickListener(this);
        eventAbbr = (Button) findViewById(R.id.abbrechenB);
        eventAbbr.setOnClickListener(this);
        timebtn = (Button) findViewById(R.id.timebtn);
        timebtn.setOnClickListener(this);
        datebtn = (Button) findViewById(R.id.datebtn);
        datebtn.setOnClickListener(this);

        inputTitle = (EditText) findViewById(R.id.titleET);
        inputDescription = (EditText) findViewById(R.id.descriptionET);
        inputDate = (TextView) findViewById((R.id.dateTV));
        inputTime = (TextView) findViewById(R.id.timeTV);
        inputStart = (EditText) findViewById(R.id.startET);
        inputDestination = (EditText) findViewById(R.id.destinationET);

        assert eventErst != null;
        //eventErst.setOnClickListener(); // entfernt, siehe in onClick

        assert eventAbbr != null;
        //eventAbbr.setOnClickListener(..); //entfernt, siehe in onClick
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.abbrechenB:
                Intent eventsIntent = new Intent(eventErstellenActivity.this, EventsActivity.class);
                eventErstellenActivity.this.startActivity(eventsIntent);
                break;
            case R.id.erstellenB:
                String ipAdresse = GlobalClass.getInstance().getIpAddresse();
                new PostDataTask().execute(ipAdresse + "/events");
                break;
            case R.id.datebtn:
                datePickerDialog = new DatePickerDialog(eventErstellenActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar dateCalendar = Calendar.getInstance();
                        dateCalendar.set(Calendar.YEAR, year);
                        dateCalendar.set(Calendar.MONTH, monthOfYear);
                        dateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String dateString = DateUtils.formatDateTime(eventErstellenActivity.this, dateCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
                        Date date = new Date(dateCalendar.getTimeInMillis());
                        java.text.DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                        dateString = dateFormat.format(date);

                        inputDate.setText(dateString);

                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

                datePickerDialog.show();
                break;
            case R.id.timebtn: {
                timePickerDialog = new TimePickerDialog(eventErstellenActivity.this, new TimePickerDialog.OnTimeSetListener(){
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute){
                        Calendar timeCalendar = Calendar.getInstance();
                        timeCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        timeCalendar.set(Calendar.MINUTE, minute);
                        String timestring = DateUtils.formatDateTime(eventErstellenActivity.this, timeCalendar.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME);
                        inputTime.setText(timestring);
                    }
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(eventErstellenActivity.this));
                timePickerDialog.show();
                break;
            }
            default:
                break;
        }

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

/*            inputTitle = (EditText) findViewById(R.id.titelET);
            inputBeschreibung = (EditText) findViewById(R.id.beschreibungET);
            //final EditText inputDate = (EditText) findViewById(R.id.datumET);
            inputDate = (TextView) findViewById((R.id.dateTV));
            inputTime = (EditText) findViewById(R.id.timeET);
            inputDestination = (EditText) findViewById(R.id.zielET);*/
            int inputID = 0;

            try {
                //data erstellen
                JSONObject dataToSend = new JSONObject();
                //TODO get user id
                // dataToSend.put("user_id", inputID);
                dataToSend.put("title", inputTitle.getText().toString().trim());
                dataToSend.put("start", inputStart.getText().toString().trim());
                dataToSend.put("destination", inputDestination.getText().toString().trim() );
                dataToSend.put("time", inputTime.getText().toString().trim());
                dataToSend.put("date", inputDate.getText().toString().trim());
                dataToSend.put("description", inputDescription.getText().toString().trim());






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
