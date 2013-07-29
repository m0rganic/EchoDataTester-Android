package com.kinve.castironecho;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.json.GenericJson;
import com.kinvey.android.Client;
import com.kinvey.android.callback.KinveyPingCallback;
import com.kinvey.android.callback.KinveyUserCallback;
import com.kinvey.java.User;
import com.kinvey.java.core.KinveyClientCallback;

import java.io.IOException;


public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Client kinveyClient;

    private TextView responseText;
    private TextView echoMsgSend;
    private Button echoButton;
    private Button pingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        kinveyClient = new Client.Builder("your app key", "your app secret", this).build();

        bindViews();
        kinveyClient.user().login(new KinveyUserCallback() {
            @Override
            public void onFailure(Throwable error) {
                Log.e(TAG, "Login Failure", error);
                appendLog("Failed to log in \n" + error);
            }

            @Override
            public void onSuccess(User result) {
                Log.i(TAG, "Logged in successfully as " + result.getId());
                appendLog("Logged in successfully");
            }
        });
    }

    private void appendLog(String msg) {
        String current = (String) responseText.getText();
        String newText = current.concat("\n" + msg);
        responseText.setText(newText);
    }

    private void bindViews() {
        responseText = (TextView) this.findViewById(R.id.echoMsgResponse);
        echoMsgSend = (TextView) this.findViewById(R.id.echoMsgSend);
        echoButton = (Button) this.findViewById(R.id.echoButton);
        pingButton = (Button) this.findViewById(R.id.pingButton);

        pingButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                kinveyClient.ping(new KinveyPingCallback() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        Toast.makeText(MainActivity.this, "success", Toast.LENGTH_SHORT).show();
                        appendLog("ping: success");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e(TAG, "Ping Failure", throwable);
                        Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_LONG).show();
                        appendLog("ping: failed");
                    }
                });
            }
        });

        echoButton.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View view) {
                GenericJson msgJson = new GenericJson();
                msgJson.put("msg", MainActivity.this.echoMsgSend.getText().toString());
                kinveyClient.appData("echo", GenericJson.class).save(msgJson, new KinveyClientCallback<GenericJson>() {

                    @Override
                    public void onSuccess(GenericJson genericJson) {
                        try {
                            appendLog("echo: " + genericJson.toPrettyString());
                        } catch (IOException e) {
                            Log.e(TAG, "failed to print result", e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        appendLog("echo: failed " + throwable.getMessage());

                    }
                });

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
