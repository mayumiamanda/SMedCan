package com.example.smedcan.Interfaces;

import com.example.smedcan.CallActivity;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

public class JavascriptInterface {
    @NotNull
    private final CallActivity callActivity;

    public JavascriptInterface(@NotNull CallActivity callActivity) {
//        super();
        this.callActivity = callActivity;
    }

    @android.webkit.JavascriptInterface
    public final void onPeerConnected() {
        this.callActivity.onPeerConnected();
    }

    @NotNull
    public final CallActivity getCallActivity() {
        return this.callActivity;
    }


}
