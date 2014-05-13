package kr.nectarine.android.fruitygcm.storage;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by nectarine on 2014. 5. 12..
 */
public class GcmSharedPreference {

    private static final String GCM_PREF_NAME = "fruity_gcm_pref";
    public static final String REGISTRATION_ID = "registration_id";
    public static final String RECEIVER_REGISTRATION_ID = "receiver_registration_id";
    public static final String SENDER_ID = "sender_id";

    public static SharedPreferences get(Context context) {
        return context.getSharedPreferences(GCM_PREF_NAME, Context.MODE_PRIVATE);
    }

}
