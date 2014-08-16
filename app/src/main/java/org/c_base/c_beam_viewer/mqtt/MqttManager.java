package org.c_base.c_beam_viewer.mqtt;

import android.content.Context;
import android.util.Log;

import java.util.UUID;

import org.c_base.c_beam_viewer.MainActivity;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttManager implements MqttCallback, IMqttActionListener {
    private static final String LOG_TAG = "MqttManager";
    private static final int QOS = 1;
    public static final String CHANNEL = "c-beam-viewer";

    private final Context context;
    private MqttAndroidClient client;

    public MqttManager(Context context) {
        this.context = context;
    }

    public void startConnection() {
        if (client != null) {
            return;
        }

        client = createMqttClient();
        client.setCallback(this);
        MqttConnectOptions options = createMqttConnectOptions();
        try {
            client.connect(options, null, this);
        } catch (MqttException e) {
            Log.e(LOG_TAG, "Error while connecting to server", e);
        }
    }

    private MqttAndroidClient createMqttClient() {
        String serverUri = "tcp://c-beam.cbrp3.c-base.org:1883";
        String clientId = "c-beam-viewer-" + UUID.randomUUID();
        return new MqttAndroidClient(context, serverUri, clientId);
    }

    private MqttConnectOptions createMqttConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
//        options.setUserName("username");
//        options.setPassword("password".toCharArray());
        options.setCleanSession(true);
        return options;
    }

    private void subscribe() {
        String topic = getTopic("test");
        try {
            client.subscribe(topic, QOS);
        } catch (MqttException e) {
            Log.e(LOG_TAG, "Failed to subscribe", e);
        }
    }

    @Override
    public void connectionLost(final Throwable throwable) {
        Log.w(LOG_TAG, "Connection lost", throwable);
    }

    @Override
    public void messageArrived(final String topic, final MqttMessage mqttMessage) throws Exception {
        Log.d(LOG_TAG, "Message arrived: " + topic);
        String url = new String(mqttMessage.getPayload(), "UTF-8");
        openUrl(url);
    }

    private void openUrl(final String url) {
        MainActivity.openUrl(context, url);
    }

    @Override
    public void deliveryComplete(final IMqttDeliveryToken deliveryToken) {
        Log.d(LOG_TAG, "Delivery complete");
    }

    @Override
    public void onSuccess(final IMqttToken token) {
        subscribe();
    }

    @Override
    public void onFailure(final IMqttToken token, final Throwable throwable) {
        Log.e(LOG_TAG, "Connection failed");
    }

    private String getTopic(String subTopic) {
        return CHANNEL + "/" + subTopic;
    }
}
