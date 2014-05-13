package kr.nectarine.android.fruitygcm.interfaces;

/**
 * Created by nectarine on 2014. 5. 12..
 */
public abstract interface FruityGcmListener {

    void onPlayServiceNotAvailable(boolean didPlayHandle);

    void onDeliverRegistrationId(String regId);

    void onRegisterFailed();
}
