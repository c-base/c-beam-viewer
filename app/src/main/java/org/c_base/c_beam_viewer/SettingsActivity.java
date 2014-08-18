package org.c_base.c_beam_viewer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity {
    public static final String KEY_PREF_MQTT_USER = "pref_key_mqtt_user";
    public static final String KEY_PREF_MQTT_PASSWORD = "pref_key_mqtt_password";
    public static final String KEY_PREF_MQTT_URI = "pref_key_mqtt_uri";
    public static final String KEY_PREF_MQTT_TLS = "pref_key_mqtt_tls";
    public static final String KEY_PREF_DEFAULT_URL = "pref_key_default_url";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}