package kr.nectarine.android.fruitygcm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.concurrent.TimeUnit;

import kr.nectarine.android.fruitygcm.interfaces.FruityGcmListener;
import kr.nectarine.android.fruitygcm.rxjava.RetryWithDelay;
import kr.nectarine.android.fruitygcm.storage.GcmSharedPreference;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by nectarine on 2014. 5. 12..
 */

public class FruityGcmClient {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static int REGISTER_RETRY = 32;
    private final static int REGISTER_BACK_OFF_TIME = 5;
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
            fruityGcmListener.onDeliverRegistrationId(regId, false);
        }
    }

    public static void registerInBackground(final Context context, final String senderId, final FruityGcmListener fruityGcmListener) {

        final SharedPreferences prefs = GcmSharedPreference.get(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(GcmSharedPreference.RECEIVER_REGISTRATION_ID, "");
        editor.apply();

        Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        String regId;
                        String receiverRegId;
                        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
                        try {
                            regId = gcm.register(senderId);
                            receiverRegId = GcmSharedPreference.get(context).getString(GcmSharedPreference.RECEIVER_REGISTRATION_ID, "");
                            if (regId != null && !regId.isEmpty()) {
                                subscriber.onNext(regId);
                                subscriber.onCompleted();
                            } else if (!TextUtils.isEmpty(receiverRegId)) {
                                // register failed, but receiver got registration
                                subscriber.onNext(receiverRegId);
                                subscriber.onCompleted();
                            } else {
                                subscriber.onError(new RuntimeException("registration failed"));
                            }
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RetryWithDelay(REGISTER_RETRY, REGISTER_BACK_OFF_TIME, TimeUnit.SECONDS))
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String regId) {
                        String currentRegId = GcmSharedPreference.get(context).getString(GcmSharedPreference.REGISTRATION_ID, "");
                        storeRegistrationData(context, regId, senderId);
                        fruityGcmListener.onDeliverRegistrationId(regId, !currentRegId.equals(regId));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        fruityGcmListener.onRegisterFailed();
                    }
                });
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
        if (TextUtils.isEmpty(registrationId) || TextUtils.isEmpty(senderId)) {
            return "";
        }
        return registrationId;
    }

}
