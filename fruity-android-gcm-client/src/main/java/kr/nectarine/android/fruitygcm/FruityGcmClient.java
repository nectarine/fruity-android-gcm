package kr.nectarine.android.fruitygcm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import kr.nectarine.android.fruitygcm.interfaces.FruityGcmListener;
import kr.nectarine.android.fruitygcm.storage.GcmSharedPreference;

/**
 * Created by nectarine on 2014. 5. 12..
 */

public class FruityGcmClient {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int REGISTER_RETRY = 32;
    private final static int REGISTER_BACK_OFF_TIME = 2000;
    public static final String REGISTRATION_ID = "registration_id";

    public static void start(Activity activity, String senderId, FruityGcmListener fruityGcmListener) {
        start(activity, senderId, true, fruityGcmListener);
    }

    public static void start(final Activity activity, final String senderId, final boolean shouldPlayHandleError, final FruityGcmListener fruityGcmListener) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (shouldPlayHandleError && GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
                fruityGcmListener.onPlayServiceNotAvailable(true);
            } else {

                fruityGcmListener.onPlayServiceNotAvailable(false);
            }
        }

        register(activity, senderId, fruityGcmListener);
    }

    private static void register(final Context context, final String senderId, final FruityGcmListener fruityGcmListener) {
        String regId = getRegistrationId(context);

        if (regId.isEmpty()) {
            // start real register in background
            registerInBackground(context, senderId, fruityGcmListener);
        } else {
            fruityGcmListener.onDeliverRegistrationId(regId);
        }
    }

    public static void registerInBackground(final Context context, final String senderId, final FruityGcmListener fruityGcmListener) {

        new AsyncTask<Void, Void, String>() {

            @Override
            protected String doInBackground(Void... params) {

                // clear receiver registration
                final SharedPreferences prefs = GcmSharedPreference.get(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString(GcmSharedPreference.RECEIVER_REGISTRATION_ID, "");
                editor.apply();

                String regId = "";
                String receiverRegId;
                for (int i = 0; i < REGISTER_RETRY; i++) {

                    try {
                        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                        regId = gcm.register(senderId);

                        if (regId != null && !regId.isEmpty()) {
                            return regId;

                        } else if (!(receiverRegId = GcmSharedPreference.get(context).getString(GcmSharedPreference.RECEIVER_REGISTRATION_ID, "")).isEmpty()) {
                            // register failed, but receiver got registration
                            return receiverRegId;
                        }

                        regId = "";

                    } catch (IOException ex) {
                        Log.d("tag", "FruityGcmClient > doInBackground : " + ex.getMessage());
                        // handle exception case
                        // don't handle in this scope to handle regId empty case at the same time
                    }

                    try {
                        // retry after 3 secs
                        Log.d("tag", "FruityGcmClient > doInBackground : retry " + i);
                        Thread.sleep(REGISTER_BACK_OFF_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
                return regId;
            }

            @Override
            protected void onPostExecute(String regId) {
                storeRegistrationData(context, regId, senderId);
                if (!regId.isEmpty()) {
                    fruityGcmListener.onDeliverRegistrationId(regId);
                } else {
                    fruityGcmListener.onRegisterFailed();
                }
            }
        }.execute(null, null, null);
    }

    private static void storeRegistrationData(final Context context, final String regId, final String senderId) {
        final SharedPreferences prefs = GcmSharedPreference.get(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(GcmSharedPreference.REGISTRATION_ID, regId);
        editor.putString(GcmSharedPreference.SENDER_ID, senderId);
        editor.apply();
    }


    private static String getRegistrationId(Context context) {
        final SharedPreferences prefs = GcmSharedPreference.get(context);
        String registrationId = prefs.getString(GcmSharedPreference.REGISTRATION_ID, "");
        String senderId = prefs.getString(GcmSharedPreference.SENDER_ID, "");

        // when sender_id is empty, try to re-register, because SharedPref lost sender_id
        if (registrationId.isEmpty() || senderId.isEmpty()) {
            return "";
        }
        return registrationId;
    }

}
