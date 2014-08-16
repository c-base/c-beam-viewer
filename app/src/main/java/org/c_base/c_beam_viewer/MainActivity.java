package org.c_base.c_beam_viewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.c_base.c_beam_viewer.mqtt.MqttManager;

public class MainActivity extends ActionBarActivity {
    private static final String ACTION_OPEN_URL = "open_url";
    private static final String EXTRA_URL = "url";

    private TextView textView;

    public static void openUrl(Context context, String url) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(ACTION_OPEN_URL);
        intent.putExtra(EXTRA_URL, url);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.textView);

        startMqttConnection(this);
        openUrlFromIntent(getIntent());
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        openUrlFromIntent(intent);
    }

    private void openUrlFromIntent(Intent intent) {
        String url = intent.getStringExtra(EXTRA_URL);
        textView.setText(url);
    }

    private void startMqttConnection(Context context) {
        CbeamViewerApplication app = CbeamViewerApplication.getInstance(context);
        MqttManager connection = app.getMqttManager();
        connection.startConnection();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
