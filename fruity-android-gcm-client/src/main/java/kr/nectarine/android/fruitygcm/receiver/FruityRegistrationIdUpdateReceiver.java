package kr.nectarine.android.fruitygcm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import kr.nectarine.android.fruitygcm.FruityGcmClient;

public abstract class FruityRegistrationIdUpdateReceiver extends BroadcastReceiver {
    public FruityRegistrationIdUpdateReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String regId = intent.getStringExtra(FruityGcmClient.REGISTRATION_ID);

        if (regId != null && !regId.isEmpty()) {
            onRegistrationIdRenewed(regId);
        } else {
            onRegistrationIdRenewFailed();
        }

    }

    public abstract void onRegistrationIdRenewed(String registrationId);
    public abstract void onRegistrationIdRenewFailed();

}
