package eis.bikefriends;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class EventDetailsActivity extends AppCompatActivity {

    String eventID;
    private TextView eventIDTv;


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

        eventID = getIntent().getStringExtra(EventsActivity.eventID);

        eventIDTv = (TextView) findViewById(R.id.eventIDTv);

        eventIDTv.setText("Event ID:" + eventID);
    }

    //Toolbar back
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    //TODO Datenabrufen
}
