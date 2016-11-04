package ch.vanakh.eis_poc;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final Button bMatching = (Button) findViewById(R.id.gotomatching);

        assert bMatching != null;

        bMatching.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent matchingIntent = new Intent(MenuActivity.this, MatchingActivity.class);
                MenuActivity.this.startActivity(matchingIntent);
            }
        });
    }
}
