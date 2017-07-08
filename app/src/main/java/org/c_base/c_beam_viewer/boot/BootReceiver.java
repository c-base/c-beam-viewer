package org.c_base.c_beam_viewer.boot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import org.c_base.c_beam_viewer.CbeamViewerApplication;
import org.c_base.c_beam_viewer.mqtt.MqttManager;
import org.c_base.c_beam_viewer.ui.activity.MainActivity;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            final Intent newActivityIntent = new Intent(context, MainActivity.class);
            newActivityIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(newActivityIntent);
        } else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            boolean hasConnectivity = !noConnectivity;
            if (hasConnectivity) {
                startMqttConnection(context);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported action: " + action);
        }
    }

    private void startMqttConnection(Context context) {
        CbeamViewerApplication app = CbeamViewerApplication.getInstance(context);
        MqttManager connection = app.getMqttManager();
        connection.startConnection();
    }
}
