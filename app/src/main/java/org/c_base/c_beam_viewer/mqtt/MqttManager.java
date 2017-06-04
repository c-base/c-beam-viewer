package org.c_base.c_beam_viewer.mqtt;


import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import javax.net.ssl.SSLSocketFactory;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;
import org.c_base.c_beam_viewer.R;
import org.c_base.c_beam_viewer.settings.Settings;
import org.c_base.c_beam_viewer.ui.activity.MainActivity;
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
    private static final String CHANNEL = "demodevice";
    private static final String OPEN_URL_TOPIC = "open";
    private static final String CLIENT_ID_PREFIX = "c-beam-viewer-";

    private final Context context;
    private final Settings settings;
    private MqttAndroidClient client;
    private Timer timer;

    public MqttManager(Context context, Settings settings) {
        this.context = context;
        this.settings = settings;
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
        String serverUri = settings.getMqttUri();
        String clientId = CLIENT_ID_PREFIX + UUID.randomUUID();
        return new MqttAndroidClient(context, serverUri, clientId);
    }

    private MqttConnectOptions createMqttConnectOptions() {
        String userName = settings.getUserName();
        String password = settings.getPassword();
        boolean useTLS = settings.getUseTls();
        MqttConnectOptions options = new MqttConnectOptions();
        if (!TextUtils.isEmpty(userName)) {
            options.setUserName(userName);
        }
        if (!TextUtils.isEmpty(password)) {
            options.setPassword(password.toCharArray());
        }
        if (useTLS) {
            try {
                InputStream certificateInputStream = getCaCertFromResources();
                SSLSocketFactory socketFactory = SslUtil.getSocketFactory(certificateInputStream);
                options.setSocketFactory(socketFactory);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        options.setCleanSession(true);
        return options;
    }

    private void subscribe() {
        String topic = getTopic(OPEN_URL_TOPIC);
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

        try {
            Buffer buffer = new Buffer().write(mqttMessage.getPayload());
            String url = JsonReader.of(buffer).nextString();
            openUrl(url);
        } catch (IOException e) {
            e.printStackTrace();

            String url = new String(mqttMessage.getPayload(), "UTF-8");
            openUrl(url);
        }
    }

    private void openUrl(final String url) {
        MainActivity.openUrl(context, url);
        try {
            Buffer buffer = new Buffer();
            JsonWriter.of(buffer).value(url);
            client.publish(CHANNEL + "/opened", buffer.readByteArray(), 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deliveryComplete(final IMqttDeliveryToken deliveryToken) {
        Log.d(LOG_TAG, "Delivery complete");
    }

    @Override
    public void onSuccess(final IMqttToken token) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                publishDiscoveryMessage();
            }
        }, 0, 60 * 1000);

        subscribe();
    }

    private void publishDiscoveryMessage() {
        try {
            byte[] discoveryMessage = loadDiscoveryMessage();
            client.publish("fbp", discoveryMessage, 0, false);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private byte[] loadDiscoveryMessage() {
        try {
            InputStream inputStream = context.getResources().getAssets().open("discovery.json");
            BufferedSource source = Okio.buffer(Okio.source(inputStream));
            try {
                return source.readUtf8().replaceAll("\n", "").getBytes("UTF-8");
            } finally {
                source.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onFailure(final IMqttToken token, final Throwable throwable) {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        showErrorMessage(R.string.connection_to_server_failed);
        Log.e(LOG_TAG, "Connection failed" + throwable.getMessage());
    }

    private void showErrorMessage(int errorMessageResId) {
        String text = context.getString(errorMessageResId);
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    private String getTopic(String subTopic) {
        return CHANNEL + "/" + subTopic;
    }

    private InputStream getCaCertFromResources() {
        return context.getResources().openRawResource(R.raw.cacert);
    }
}
