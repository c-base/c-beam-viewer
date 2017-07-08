package org.c_base.c_beam_viewer.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.c_base.c_beam_viewer.CbeamViewerApplication;
import org.c_base.c_beam_viewer.R;
import org.c_base.c_beam_viewer.mqtt.MqttManager;
import org.c_base.c_beam_viewer.settings.Settings;

public class MainActivity extends DrawerActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final String ACTION_OPEN_URL = "open_url";
    private static final String EXTRA_URL = "url";

    private Settings settings;
    private WebView webView;


    public static void openUrl(Context context, String url) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setAction(ACTION_OPEN_URL);
        intent.putExtra(EXTRA_URL, url);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        useFullScreenMode();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        final View rootView = getWindow().getDecorView();

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT > 10) {
                    rootView.setSystemUiVisibility(View.STATUS_BAR_HIDDEN); // aka View.SYSTEM_UI_FLAG_LOW_PROFILE
                }
                handler.postDelayed(this, 1000);
            }
        });


        setContentView(R.layout.activity_main);

        setupNavigationDrawer();

        webView = (WebView) findViewById(R.id.web_view);
        configureWebView();

        settings = new Settings(this);

        startMqttConnection(this);
        openUrlFromIntent(getIntent());
    }

    private void useFullScreenMode() {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
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
            url = settings.getStartPage();
        }
        webView.loadUrl(url);
    }

    private void startMqttConnection(Context context) {
        CbeamViewerApplication app = CbeamViewerApplication.getInstance(context);
        MqttManager connection = app.getMqttManager();
        connection.startConnection();
    }

    public static class CbeamViewerWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
            return false;
        }

        @Override
        public void onReceivedSslError(final WebView view, @NonNull final SslErrorHandler handler, final SslError error) {
            handler.proceed();
        }
    }
}
