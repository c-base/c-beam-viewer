package org.c_base.c_beam_viewer;

import android.graphics.drawable.Drawable;

public class NavigationDrawerItem {
    private final String name;
    private final Drawable image;


    public NavigationDrawerItem(String name, Drawable image) {
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public Drawable getImage() {
        return image;
    }
}
