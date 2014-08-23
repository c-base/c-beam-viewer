package org.c_base.c_beam_viewer.ui.activity;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.c_base.c_beam_viewer.R;
import org.c_base.c_beam_viewer.ui.NavigationDrawerItem;

public abstract class DrawerActivity extends ActionBarActivity {
    private DrawerLayout drawerLayout;
    private ListView drawerList;


    private void startSettingsActivity() {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
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
                MainActivity.openUrl(this, "http://logbuch.c-base.org/");
                break;
            case 1: // coredump
                MainActivity.openUrl(this, "https://wiki.cbrp3.c-base.org/dokuwiki/");
                break;
            case 2: // weather
                MainActivity.openUrl(this, "http://c-beam.cbrp3.c-base.org/weather");
                break;
            case 3: // bvg
                MainActivity.openUrl(this, "http://c-beam.cbrp3.c-base.org/bvg");
                break;
            case 4: // c-portal
                MainActivity.openUrl(this, "https://c-portal.c-base.org");
                break;
            case 5: // artefacts
                MainActivity.openUrl(this, "https://cbag3.c-base.org");
                break;
            case 6: // c-reddit
                MainActivity.openUrl(this, "https://c-beam.cbrp3.c-base.org/reddit");
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
