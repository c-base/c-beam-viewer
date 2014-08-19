package org.c_base.c_beam_viewer;

import android.app.Application;
import android.content.Context;

import org.c_base.c_beam_viewer.mqtt.MqttManager;
import org.c_base.c_beam_viewer.settings.Settings;

public class CbeamViewerApplication extends Application {

    private MqttManager connection;

    public static CbeamViewerApplication getInstance(Context context) {
        return (CbeamViewerApplication) context.getApplicationContext();
    }

    public MqttManager getMqttManager() {
        if (connection == null) {
            Settings settings = new Settings(this);
            connection = new MqttManager(this, settings);
        }

        return connection;
    }
}
