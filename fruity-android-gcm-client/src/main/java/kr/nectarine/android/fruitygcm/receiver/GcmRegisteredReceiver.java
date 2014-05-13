package kr.nectarine.android.fruitygcm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import kr.nectarine.android.fruitygcm.FruityGcmClient;
import kr.nectarine.android.fruitygcm.R;
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

        if (!FruityGcmClient.IN_PROCESS) {
            Intent i = new Intent(context.getString(R.string.fruity_action_token_needs_update));
            i.putExtra(FruityGcmClient.REGISTRATION_ID, registrationId);
            context.sendBroadcast(i);
        }
    }
}
