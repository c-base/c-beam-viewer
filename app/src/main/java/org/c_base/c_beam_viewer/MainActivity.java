package org.c_base.c_beam_viewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.SslError;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.c_base.c_beam_viewer.mqtt.MqttManager;

public class MainActivity extends ActionBarActivity {
    private static final String ACTION_OPEN_URL = "open_url";
    private static final String EXTRA_URL = "url";

    private WebView webView;
    private String LOG_TAG = "MainActivity";

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

        useFullScreenMode();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.web_view);
        configureWebView();

        startMqttConnection(this);
        openUrlFromIntent(getIntent());
    }

    private void useFullScreenMode() {
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void configureWebView() {
        WebViewClient client = new CbeamViewerWebViewClient();
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.setWebViewClient(client);
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        openUrlFromIntent(intent);
    }

    private void openUrlFromIntent(Intent intent) {
        String url = intent.getStringExtra(EXTRA_URL);
        Log.d(LOG_TAG, "URL: " + url);
        if (url == null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            url = sharedPref.getString(SettingsActivity.KEY_PREF_DEFAULT_URL, "http://c-beam.cbrp3.c-base.org/c-beam-viewer");
        }
        webView.loadUrl(url);
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
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class CbeamViewerWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            return false;
        }

        @Override
        public void onReceivedSslError(final WebView view, final SslErrorHandler handler, final SslError error) {
            handler.proceed();
        }
    }
}
