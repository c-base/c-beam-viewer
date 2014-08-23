package org.c_base.c_beam_viewer;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.c_base.c_beam_viewer.mqtt.MqttManager;
import org.c_base.c_beam_viewer.settings.Settings;

public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final String ACTION_OPEN_URL = "open_url";
    private static final String EXTRA_URL = "url";

    private Settings settings;
    private WebView webView;
    private DrawerLayout drawerLayout;
    private ListView drawerList;


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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startSettingsActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startSettingsActivity() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
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

    protected void setupNavigationDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        String[] drawerItems = getResources().getStringArray(R.array.drawer_items_array);
        TypedArray drawerImages = getResources().obtainTypedArray(R.array.drawer_images_array);

        ArrayList<NavigationDrawerItem> navigationDrawerItems = new ArrayList<NavigationDrawerItem>();
        for (int i = 0; i < drawerItems.length; i++) {
            String drawerItem = drawerItems[i];
            Drawable drawerImage = drawerImages.getDrawable(i);
            NavigationDrawerItem navigationDrawerItem = new NavigationDrawerItem(drawerItem, drawerImage);
            navigationDrawerItems.add(navigationDrawerItem);
        }

        NavigationDrawerAdapter adapter = new NavigationDrawerAdapter(this, navigationDrawerItems);
        drawerList.setAdapter(adapter);

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onNavigationDrawerItemSelected(position);
            }
        });
    }

    private void onNavigationDrawerItemSelected(int position) {
        drawerLayout.closeDrawer(drawerList);

        switch (position) {
            case 0: // logbuch
                openUrl(this, "http://logbuch.c-base.org/");
                break;
            case 1: // coredump
                openUrl(this, "https://wiki.cbrp3.c-base.org/dokuwiki/");
                break;
            case 2: // weather
                openUrl(this, "http://c-beam.cbrp3.c-base.org/weather");
                break;
            case 3: // bvg
                openUrl(this, "http://c-beam.cbrp3.c-base.org/bvg");
                break;
            case 4: // c-portal
                openUrl(this, "https://c-portal.c-base.org");
                break;
            case 5: // artefacts
                openUrl(this, "https://cbag3.c-base.org");
                break;
            case 6: // c-reddit
                openUrl(this, "https://c-beam.cbrp3.c-base.org/reddit");
                break;
            case 7: // Settings
                startSettingsActivity();
                break;
        }

    }

    protected class NavigationDrawerAdapter extends ArrayAdapter<NavigationDrawerItem> {
        public NavigationDrawerAdapter(Context context, ArrayList<NavigationDrawerItem> items) {
            super(context, R.layout.drawer_list_item, R.id.drawer_list_item_textview, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view = super.getView(position, convertView, parent);

            NavigationDrawerItem item = getItem(position);

            TextView textView = (TextView) view.findViewById(R.id.drawer_list_item_textview);
            textView.setText(item.getName());

            ImageView imageView = (ImageView) view.findViewById(R.id.drawer_ring_imageView);
            if (item.getImage() == null) {
                imageView.setVisibility(View.GONE);
            } else {
                imageView.setImageDrawable(item.getImage());
            }

            return view;
        }
    }
}
