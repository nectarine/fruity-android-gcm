package kr.nectarine.android.fruitygcm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import kr.nectarine.android.fruitygcm.FruityGcmClient;
import kr.nectarine.android.fruitygcm.R;
import kr.nectarine.android.fruitygcm.interfaces.FruityGcmListener;
import kr.nectarine.android.fruitygcm.storage.GcmSharedPreference;

public class GcmNeedsUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        final SharedPreferences prefs = GcmSharedPreference.get(context);
        String senderId = prefs.getString(GcmSharedPreference.SENDER_ID, "");

        if (resultCode == ConnectionResult.SUCCESS && !senderId.isEmpty()) {
            // only works when play service is available and sendId is stored
            FruityGcmClient.registerInBackground(context, senderId, new FruityGcmListener() {

                @Override
                public void onPlayServiceNotAvailable(boolean didPlayHandle) {

                }

                @Override
                public void onDeliverRegistrationId(String regId) {
                    Intent i = new Intent(context.getString(R.string.fruity_action_token_needs_update));
                    i.putExtra(FruityGcmClient.REGISTRATION_ID, regId);
                    context.sendBroadcast(i);
                }

                @Override
                public void onRegisterFailed() {
                    Intent i = new Intent(context.getString(R.string.fruity_action_token_needs_update));
                    i.putExtra(FruityGcmClient.REGISTRATION_ID, "");
                    context.sendBroadcast(i);
                }
            });

        }
    }
}
