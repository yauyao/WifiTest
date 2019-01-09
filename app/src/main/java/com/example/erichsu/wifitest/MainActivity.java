package com.example.erichsu.wifitest;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;

import android.net.wifi.rtt.WifiRttManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private WifiRttManager wifiRttManager;
    private WifiManager wifiManager;
    private String TAG = "WifiTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_RTT)) {
            Object service = this.getApplicationContext().getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
            if (service instanceof WifiRttManager) {
                wifiRttManager = (WifiRttManager) service;
                Log.i(TAG, "Get WifiRttManager Succ.");
            }

            wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            IntentFilter wifiFileter = new IntentFilter();
            wifiFileter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            wifiFileter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            wifiFileter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(new WifiChangeReceiver(), wifiFileter);
        }else{
            Log.i(TAG, "Get WifiRttManager Fail.");
        }
    }

    private void startScanAPs() {
        wifiManager.setWifiEnabled(true);
        wifiManager.startScan();
    }

    class WifiChangeReceiver extends BroadcastReceiver {
        @SuppressLint("MissingPermission")
        @RequiresApi(api = 28)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                List<ScanResult> scanResults = wifiManager.getScanResults();
                Log.i(TAG, "Wifi Scan size:" + scanResults.size());
                for (ScanResult scanResult : scanResults) {
                    Log.i(TAG, scanResult.toString());
                    RangingRequest.Builder builder = new RangingRequest.Builder();
                    builder.addAccessPoint(scanResult);
                    // A ScanResult can be retrieved by e.g. perform a WiFi scan for WiFi's in range -> https://developer.android.com/reference/android/net/wifi/ScanResult.html
                    final RangingRequest request = new RangingRequest.Builder()
                            .addAccessPoint(scanResult)
                            .build();
                    final RangingResultCallback callback = new RangingResultCallback() {
                        public void onRangingResults(List<RangingResult> results) {
                            // Handle result, e.g. get distance to Access Point
                        }

                        public void onRangingFailure(int code) {
                            // Handle failure
                        }
                    };
                    // Start ranging and return result on main thread.
                    wifiRttManager.startRanging(request, (Executor) callback, null);
                }
            }
        }
    }
}



