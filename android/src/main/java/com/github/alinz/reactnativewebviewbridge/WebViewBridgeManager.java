package com.github.alinz.reactnativewebviewbridge;

import android.webkit.WebView;
import android.app.Activity;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import com.reactnativecommunity.webview.RNCWebViewManager;

import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

public class WebViewBridgeManager extends RNCWebViewManager {
    private static final String REACT_CLASS = "RCTWebViewBridge";

    public static final int COMMAND_SEND_TO_BRIDGE = 101;
    public static final int COMMAND_SHOW_KEYBOARD = 102;
    public static final int COMMAND_HIDE_KEYBOARD = 103;

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    public
    @Nullable
    Map<String, Integer> getCommandsMap() {
        Map<String, Integer> commandsMap = super.getCommandsMap();

        commandsMap.put("sendToBridge", COMMAND_SEND_TO_BRIDGE);
        commandsMap.put("showKeyboard", COMMAND_SHOW_KEYBOARD);
        commandsMap.put("hideKeyboard", COMMAND_HIDE_KEYBOARD);

        return commandsMap;
    }

    @Override
    protected WebView createViewInstance(ThemedReactContext reactContext) {
        WebView root = super.createViewInstance(reactContext);
        root.addJavascriptInterface(new JavascriptBridge(root), "WebViewBridge");
        return root;
    }

    @Override
    public void receiveCommand(WebView root, int commandId, @Nullable ReadableArray args) {
        super.receiveCommand(root, commandId, args);
        InputMethodManager imm = (InputMethodManager) root.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        switch (commandId) {
            case COMMAND_SEND_TO_BRIDGE:
                sendToBridge(root, args.getString(0));
                break;
            case COMMAND_SHOW_KEYBOARD:
                root.requestFocus();
                imm.showSoftInput(root, InputMethodManager.SHOW_FORCED);
                break;
            case COMMAND_HIDE_KEYBOARD:
                root.clearFocus();
                imm.hideSoftInputFromWindow(root.getWindowToken(), 0);
                break;
            default:
        }
    }

    private void sendToBridge(WebView root, String message) {
        String script = "WebViewBridge.onMessage('" + message + "');";
        WebViewBridgeManager.evaluateJavascript(root, script);
    }

    static private void evaluateJavascript(WebView root, String javascript) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            root.evaluateJavascript(javascript, null);
        } else {
            root.loadUrl("javascript:" + javascript);
        }
    }

    @ReactProp(name = "allowFileAccessFromFileURLs")
    public void setAllowFileAccessFromFileURLs(WebView root, boolean allows) {
        root.getSettings().setAllowFileAccessFromFileURLs(allows);
    }

    @ReactProp(name = "allowUniversalAccessFromFileURLs")
    public void setAllowUniversalAccessFromFileURLs(WebView root, boolean allows) {
        root.getSettings().setAllowUniversalAccessFromFileURLs(allows);
    }
}
