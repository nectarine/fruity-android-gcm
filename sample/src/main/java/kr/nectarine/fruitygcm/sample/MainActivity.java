package kr.nectarine.fruitygcm.sample;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import kr.nectarine.android.fruitygcm.FruityGcmClient;
import kr.nectarine.android.fruitygcm.interfaces.FruityGcmListener;


public class MainActivity extends Activity {

    TextView tvId;
    private static final String SENDER_ID = "365545157648";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvId = (TextView) findViewById(R.id.tv_id);

        FruityGcmClient.start(this, SENDER_ID, new FruityGcmListener() {
            @Override
            public void onPlayServiceNotAvailable(boolean didPlayHandle) {

            }

            @Override
            public void onDeliverRegistrationId(final String regId, boolean isNew) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvId.setText(regId);
                    }
                });
            }

            @Override
            public void onRegisterFailed() {

            }
        });


    }


}
