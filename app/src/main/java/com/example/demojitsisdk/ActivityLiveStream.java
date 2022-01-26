package com.example.demojitsisdk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.WindowManager;

import androidx.annotation.Nullable;


import org.jitsi.meet.sdk.JitsiMeetActivity;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.jitsi.meet.sdk.BuildConfig;
import org.jitsi.meet.sdk.JitsiMeet;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.jitsi.meet.sdk.JitsiMeetView;




public class ActivityLiveStream extends JitsiMeetActivity {

    /**
     * The request code identifying requests for the permission to draw on top
     * of other apps. The value must be 16-bit and is arbitrarily chosen here.
     */
    private static final int OVERLAY_PERMISSION_REQUEST_CODE
            = (int) (Math.random() * Short.MAX_VALUE);

    // JitsiMeetActivity overrides
    //


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
//        finish();
    }

    @Override
    protected boolean extraInitialize() {
//        Log.d(this.getClass().getSimpleName(), "LIBRE_BUILD="+ BuildConfig.LIBRE_BUILD);

        // Setup Crashlytics and Firebase Dynamic Links
        // Here we are using reflection since it may have been disabled at compile time.
        try {
            Class<?> cls = Class.forName("org.jitsi.meet.GoogleServicesHelper");
            Method m = cls.getMethod("initialize", JitsiMeetActivity.class);
            m.invoke(null, this);
        } catch (Exception e) {
            // Ignore any error, the module is not compiled when LIBRE_BUILD is enabled.
        }

        // In Debug builds React needs permission to write over other apps in
        // order to display the warning and error overlays.
        if (BuildConfig.DEBUG) {
            if (canRequestOverlayPermission() && !Settings.canDrawOverlays(this)) {
                Intent intent
                        = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));

                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);

                return true;
            }
        }

        return false;
    }

    @Override
    protected void initialize() {
        super.initialize();

        String token = "", user_auth = "false";


        String roomName = getIntent().getStringExtra("roomName");
        String serverUrl = getIntent().getStringExtra("serverUrl");
        String userType = getIntent().getStringExtra("userType");

//        serverUrl = "https://tcalive.ingeniumedu.com/";

        boolean audioStatus = false;
        boolean videoStatus = false;
        if (userType.equalsIgnoreCase("Student")){
//            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            audioStatus = true;
            videoStatus = true;
            user_auth = getIntent().getStringExtra("user_auth");

            if (user_auth.equalsIgnoreCase("true")){
                token = getIntent().getStringExtra("token");
            }
        }else if (userType.equalsIgnoreCase("Teacher")){
            audioStatus = false;
            videoStatus = false;
            user_auth = getIntent().getStringExtra("user_auth");
            if (user_auth.equalsIgnoreCase("true")){
                token = getIntent().getStringExtra("token");
            }

        }

        // Set default options
        JitsiMeetConferenceOptions defaultOptions
                = new JitsiMeetConferenceOptions.Builder()
//                .setWelcomePageEnabled(false)
                .setServerURL(buildURL(serverUrl))
                .setFeatureFlag("call-integration.enabled", false)
                .setFeatureFlag("pip.enabled", false)
                .build();
        JitsiMeet.setDefaultConferenceOptions(defaultOptions);

        JitsiMeetConferenceOptions options = null;
        try {
            JitsiMeetUserInfo userInfo = new JitsiMeetUserInfo();
            userInfo.setDisplayName("Mohit" + " " + "Patel");


            if (user_auth.equalsIgnoreCase("true")){
                options = new JitsiMeetConferenceOptions.Builder()
                        .setServerURL(new URL(serverUrl))
                        .setRoom(roomName)
                        .setAudioMuted(audioStatus)
//                        .setWelcomePageEnabled(false)
                        .setVideoMuted(videoStatus)
                        .setSubject("Live Stream")
                        .setUserInfo(userInfo)
                        .setToken(token)
                        .build();
            }else {
                options = new JitsiMeetConferenceOptions.Builder()
                        .setServerURL(new URL(serverUrl))
                        .setRoom(roomName)
                        .setAudioMuted(audioStatus)
//                        .setWelcomePageEnabled(false)
                        .setVideoMuted(videoStatus)
                        .setSubject("Live Stream")
                        .setUserInfo(userInfo)
                        .build();
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        JitsiMeetView jitsiMeetView = getJitsiView();
//        jitsiMeetView.setVisibility(View.GONE);
//        jitsiMeetView.showContextMenu();
//        jitsiMeetView.findViewById(R.id.);
        jitsiMeetView.setClickable(false);


        join(options);
    }


//    @Override
//    public void onConferenceTerminated(Map<String, Object> data) {
//        Log.d(TAG, "Conference terminated: " + data);
////        super.onBackPressed();
//        finish();
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE
                && canRequestOverlayPermission()) {
            if (Settings.canDrawOverlays(this)) {
                initialize();

                return;
            }

            throw new RuntimeException("Overlay permission is required when running in Debug mode.");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (BuildConfig.DEBUG && keyCode == KeyEvent.KEYCODE_MENU) {
            JitsiMeet.showDevOptions();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    private @Nullable
    URL buildURL(String urlStr) {
        try {
            return new URL(urlStr);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private boolean canRequestOverlayPermission() {
        return
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M;
    }

//    @Override
//    protected JitsiMeetView getJitsiView() {
//
//        return super.getJitsiView();
//    }

}
