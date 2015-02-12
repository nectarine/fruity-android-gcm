package kr.nectarine.android.fruitygcm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import kr.nectarine.android.fruitygcm.storage.GcmSharedPreference;

public class GcmRegisteredReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String registrationId = intent.getStringExtra("registration_id");

        if (registrationId == null) {
            return;
        }

        final SharedPreferences prefs = GcmSharedPreference.get(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(GcmSharedPreference.RECEIVER_REGISTRATION_ID, registrationId);
        editor.apply();
    }
}
