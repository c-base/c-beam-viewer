package org.c_base.c_beam_viewer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
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

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity {
    private static final String LOG_TAG = "MainActivity";
    private static final String ACTION_OPEN_URL = "open_url";
    private static final String EXTRA_URL = "url";

    private WebView webView;
    private Settings settings;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private String[] mDrawerItems;
    private TypedArray mDrawerImages;


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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
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
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setBackgroundColor(Color.argb(120, 0, 0, 0));

        mDrawerItems = getResources().getStringArray(R.array.drawer_items_array);
        mDrawerImages = getResources().obtainTypedArray(R.array.drawer_images_array);

        ArrayList<Ring> mRings = new ArrayList<Ring>();
        for (int i = 0; i < mDrawerItems.length; i++) {
            mRings.add(new Ring(mDrawerItems[i], mDrawerImages.getDrawable(i)));
        }

        mDrawerList.setAdapter(new RingAdapter(this, R.layout.drawer_list_item,
                R.id.drawer_list_item_textview, mRings));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

    }

    private void selectItem(int position) {

        setTitle(mDrawerItems[position]);

        switch (position) {
            case 0: // CLAMP
                //startActivity(ClampActivity.class);
                openUrl(getApplicationContext(), "http://logbuch.c-base.org/");
                break;
            case 1: // CARBON
                openUrl(getApplicationContext(), "https://wiki.cbrp3.c-base.org/dokuwiki/");
                break;
            case 2: // CIENCE
                openUrl(getApplicationContext(), "https://c-beam.cbrp3.c-base.org/weather");
                break;
            case 3: // CREACTIV
                openUrl(getApplicationContext(), "http://c-beam.cbrp3.c-base.org/bvg");
                break;
            case 4: // CULTURE
                openUrl(getApplicationContext(), "https://c-portal.c-base.org");
                break;
            case 5: // COM
                openUrl(getApplicationContext(), "https://cbag3.c-base.org");
                break;
            case 6: // CORE
                openUrl(getApplicationContext(), "https://c-beam.cbrp3.c-base.org/reddit");
                break;
            case 7: // Settings
                startSettingsActivity();
        }
    }

    protected class RingAdapter extends ArrayAdapter {
        private static final String TAG = "UserAdapter";
        private ArrayList<Ring> items;
        private Context context;

        @SuppressWarnings("unchecked")
        public RingAdapter(Context context, int itemLayout, int textViewResourceId, ArrayList<Ring> items) {
            super(context, itemLayout, textViewResourceId, items);
            this.context = context;
            this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View listview = super.getView(position, convertView, parent);

            TextView textView = (TextView) listview.findViewById(R.id.drawer_list_item_textview);
            Ring r = items.get(position);

            textView.setText(r.getName());

            ImageView b = (ImageView) listview.findViewById(R.id.drawer_ring_imageView);

            b.setImageDrawable(r.getImage());
            if (r.getImage() == null) {
                b.setVisibility(View.GONE);
            }
            return listview;
        }

    }

    protected class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            selectItem(position);
        }
    }


}
