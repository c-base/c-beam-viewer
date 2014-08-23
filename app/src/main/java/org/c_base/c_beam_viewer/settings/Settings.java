package org.c_base.c_beam_viewer.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.c_base.c_beam_viewer.ui.activity.SettingsActivity;

public class Settings {
    private static final String DEFAULT_MQTT_URI = "tcp://c-beam.cbrp3.c-base.org:1883";
    private static final String DEFAULT_USERNAME = "";
    private static final String DEFAULT_PASSWORD = "";
    public static final String DEFAULT_START_PAGE = "http://c-beam.cbrp3.c-base.org/c-beam-viewer";

    private final SharedPreferences preferences;

    public Settings(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getMqttUri() {
        return preferences.getString(SettingsActivity.KEY_PREF_MQTT_URI, DEFAULT_MQTT_URI);
    }

    public String getUserName() {
        return preferences.getString(SettingsActivity.KEY_PREF_MQTT_USER, DEFAULT_USERNAME);
    }

    public String getPassword() {
        return preferences.getString(SettingsActivity.KEY_PREF_MQTT_PASSWORD, DEFAULT_PASSWORD);
    }

    public boolean getUseTls() {
        return preferences.getBoolean(SettingsActivity.KEY_PREF_MQTT_TLS, false);
    }

    public String getStartPage() {
        return preferences.getString(SettingsActivity.KEY_PREF_DEFAULT_URL, DEFAULT_START_PAGE);
    }
}
