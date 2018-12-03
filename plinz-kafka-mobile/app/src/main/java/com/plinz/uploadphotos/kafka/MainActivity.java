package com.plinz.uploadphotos.kafka;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Properties;

import io.reactivex.CompletableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;

public class MainActivity extends AppCompatActivity {

    private final String TOPIC="upload-photos";
    Button sendButton;
    EditText editText;
    TextView textView;
    private StompClient mStompClient;
    private final String BASE_WS_URL="ws://10.0.2.2:8080/photos";
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendButton =(Button) findViewById(R.id.send);
        editText =(EditText) findViewById(R.id.send_message);
        textView =(TextView) findViewById(R.id.received_message);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEchoViaStomp(editText.getText().toString());
            }
        });
        connectStomp();
    }

    public void connectStomp() {
        mStompClient = Stomp.over(Stomp.ConnectionProvider.JWS, BASE_WS_URL);

        mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lifecycleEvent -> {
                    switch (lifecycleEvent.getType()) {
                        case OPENED:
                            Log.d(TAG, "Stomp connection opened");
                            break;
                        case ERROR:
                            Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                            Log.d(TAG, "Stomp connection error");
                            break;
                        case CLOSED:
                            connectStomp();
                            Log.d(TAG, "Stomp connection closed");
                    }
                });
        Log.d(TAG, "Chida Registering ");
        // Receive greetings
        mStompClient.topic("/topic/upload-photos")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(topicMessage -> {
                    Log.d(TAG, "Chida Received 111 " + topicMessage.getPayload());
                    textView.setText(topicMessage.getPayload());
                    Log.d(TAG, "Chida Received " + topicMessage.getPayload());
                });

        mStompClient.connect();
    }

    public void sendEchoViaStomp(String message) {
        mStompClient.send("/app/send-photos", message)
                .compose(applySchedulers())
                .subscribe(() -> {
                    Log.d(TAG, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send STOMP echo", throwable);
                    Log.d(TAG, throwable.getMessage());
                });
    }

    protected CompletableTransformer applySchedulers() {
        return upstream -> upstream
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectStomp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectStomp();
    }

    public void disconnectStomp() {
        mStompClient.disconnect();
    }

}
