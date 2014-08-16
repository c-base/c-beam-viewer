package org.c_base.c_beam_viewer;

import android.app.Application;
import android.content.Context;

import org.c_base.c_beam_viewer.mqtt.MqttManager;

public class CbeamViewerApplication extends Application {

    private MqttManager connection;

    public static CbeamViewerApplication getInstance(Context context) {
        return (CbeamViewerApplication) context.getApplicationContext();
    }

    public MqttManager getMqttManager() {
        if (connection == null) {
            connection = new MqttManager(this);
        }

        return connection;
    }
}
