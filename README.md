DEPRECATED
==========

Last Google I/O 2015, GCM client is fully redesigned.
It covers whole boilerplate things that fruity-android-gcm cares.

Try to use it!

https://developers.google.com/cloud-messaging/android/client

fruity-android-gcm
==================

GCM Client which helps you to manage registration Id

It is not easy to correctly handle a regstration_id for GCM.

This library implements all 4 cases which noted in [this post](http://blog.pushbullet.com/2014/02/12/keeping-google-cloud-messaging-for-android-working-reliably-techincal-post/
)

### Download via jCenter

```groovy
compile 'com.github.nectarine:fruity-android-gcm-client:1.0.3'
```


### How to use

I tried to minimized what user should do, still you have to implement four things.

`Android Manifest`, `GcmMessageReceiver`, `FruityRegistrationIdUpdateReceiver`, and call `FruityGcmClient`

#### 1. Add permissions and BroadcastReceivers to the AndroidManifest.xml

```xml
<permission
    android:name="your_package_name.permission.C2D_MESSAGE"
    android:protectionLevel="signature" />

<uses-permission android:name="your_package_name.permission.C2D_MESSAGE" />

<receiver android:name="your_package_name.GcmRegistrationUpdateReceiver" >
    <intent-filter>
        <action android:name="FRUITY_TOKEN_NEEDS_UPDATE" />
    </intent-filter>
</receiver>
<receiver
    android:name="your_package_name.GcmMessageReceiver"
    android:permission="com.google.android.c2dm.permission.SEND" >
    <intent-filter>
        <action android:name="com.google.android.c2dm.intent.RECEIVE" />
        <category android:name="your_package_name" />
    </intent-filter>
</receiver>
```

Note that you must use action name `FRUITY_TOKEN_NEEDS_UPDATE` in the intent-filter to get a registration_id renewal event

#### 2. Implements GcmRegistrationUpdateReceiver

If you extends FruityRegistrationIdUpdateReceiver, it will deliver the registration_id renewal event

```java
public class GcmRegistrationUpdateReceiver extends FruityRegistrationIdUpdateReceiver {

    @Override
    public void onRegistrationIdRenewed(String regId) {
        Log.d("tag", "GcmRegistrationUpdateReceiver > onRegistrationIdRenewed : " + regId);
        // send new regId to the Server
        
    }

    @Override
    public void onRegistrationIdRenewFailed() {
        Log.d("tag", "GcmRegistrationUpdateReceiver > onRegistrationIdRenewFailed : failed");
        //needs extra backoff like retry later
    }
}
```

GcmRegistrationUpdateReceiver is called when action `android.intent.action.MY_PACKAGE_REPLACED` and `android.intent.action.BOOT_COMPLETED` fired. 

#### 3. Implements GcmMessageReceiver

This is same as usual

```java
public class GcmMessageReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        for (String s : extras.keySet()) {
            Log.d("tag", "GcmMessageReceiver > onReceive : " + s + " = " + extras.get(s));
        }
    }
}
```

#### 4. Call FruityGcmClient!

Recommended just call after splash screen, login process, or somewhat during startup.

```java
public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        final TextView tv = (TextView)findViewById(R.id.tv);
        
        FruityGcmClient.start(this, "your_sender_id", new FruityGcmListener() {
            @Override
            public void onPlayServiceNotAvailable(boolean didPlayHandleError) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("play not available");
                    }
                });
            }

            @Override
            public void onDeliverRegistrationId(final String regId, boolean isNew) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("tag", "MainActivity > run : " + regId);
                        tv.setText(regId);
                    }
                });
            }

            @Override
            public void onRegisterFailed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv.setText("reg failed");
                    }
                });
            }
        });
    }

}
```

### MUST CHECK BEFORE USE

This library already contains following permissions.

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```
