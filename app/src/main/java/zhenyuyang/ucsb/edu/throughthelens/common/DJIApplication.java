package zhenyuyang.ucsb.edu.throughthelens.common;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import dji.common.error.DJIError;
import dji.common.error.DJISDKError;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.camera.DJICamera;
import dji.sdk.products.DJIAircraft;
import dji.sdk.products.DJIHandHeld;
import dji.sdk.sdkmanager.DJIBluetoothProductConnector;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.products.DJIAircraft;

/**
 * Created by Zhenyu on 2018-01-29.
 */

public class DJIApplication extends Application {

    private static final String TAG = DJIApplication.class.getName();

    public static final String FLAG_CONNECTION_CHANGE = "com_example_dji_sdkdemo3_connection_change";

    private static DJIBaseProduct mProduct;

    private static DJIBluetoothProductConnector bluetoothConnector = null;

    private Handler mHandler;

    private static boolean connected = false;

    /**
     * Gets instance of the specific product connected after the
     * API KEY is successfully validated. Please make sure the
     * API_KEY has been added in the Manifest
     */
    public static synchronized DJIBaseProduct getProductInstance() {
        if (null == mProduct) {
            mProduct = DJISDKManager.getInstance().getDJIProduct();
        }
        return mProduct;
    }

    public static synchronized DJIBluetoothProductConnector getBluetoothProductConnector(){
        if(null == bluetoothConnector){
            bluetoothConnector = DJISDKManager.getInstance().getDJIBluetoothProductConnector();
        }
        return bluetoothConnector;
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof DJIAircraft;
    }

    public static boolean isHandHeldConnected() {
        return getProductInstance() != null && getProductInstance() instanceof DJIHandHeld;
    }

    public static synchronized DJIAircraft getAircraftInstance() {
        if (!isAircraftConnected()) return null;
        return (DJIAircraft) getProductInstance();
    }

    public static synchronized DJIHandHeld getHandHeldInstance() {
        if (!isHandHeldConnected()) return null;
        return (DJIHandHeld) getProductInstance();
    }

    public static synchronized DJICamera getCameraInstance() {

        if (getProductInstance() == null) return null;

        DJICamera camera = null;

        if (getProductInstance() instanceof DJIAircraft){
            camera = ((DJIAircraft) getProductInstance()).getCamera();

        } else if (getProductInstance() instanceof DJIHandHeld) {
            camera = ((DJIHandHeld) getProductInstance()).getCamera();
        }

        return camera;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new Handler(Looper.getMainLooper());
        Toast.makeText(getApplicationContext(), "Comes into the initSDKManager", Toast.LENGTH_SHORT).show();
        /**
         * handles SDK Registration using the API_KEY
         */
        Log.d("Alex", "Comes into the initSDKManager");
        DJISDKManager.getInstance().initSDKManager(this, mDJISDKManagerCallback);
    }

    private DJISDKManager.DJISDKManagerCallback mDJISDKManagerCallback = new DJISDKManager.DJISDKManagerCallback() {

        @Override
        public void onGetRegisteredResult(DJIError error) {
            if(error == DJISDKError.REGISTRATION_SUCCESS) {
                DJISDKManager.getInstance().startConnectionToProduct();
            } else {
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "SDK Registration Failed. Please check the bundle ID and your network connectivity",
                                Toast.LENGTH_LONG).show();
                    }
                });

            }
            Log.v(TAG, error.getDescription());
        }

        @Override
        public void onProductChanged(DJIBaseProduct oldProduct, DJIBaseProduct newProduct) {

            Log.d("Alex", String.format("onProductChanged oldProduct:%s, newProduct:%s", oldProduct, newProduct));
            mProduct = newProduct;
            if(mProduct != null) {
                mProduct.setDJIBaseProductListener(mDJIBaseProductListener);
            }

            notifyStatusChange();
        }

        private DJIBaseProduct.DJIBaseProductListener mDJIBaseProductListener = new DJIBaseProduct.DJIBaseProductListener() {

            @Override
            public void onComponentChange(DJIBaseProduct.DJIComponentKey key, DJIBaseComponent oldComponent, DJIBaseComponent newComponent) {

                if(newComponent != null) {
                    newComponent.setDJIComponentListener(mDJIComponentListener);
                }
                Log.d("Alex", String.format("onComponentChange key:%s, oldComponent:%s, newComponent:%s", key, oldComponent, newComponent));

                notifyStatusChange();
            }

            @Override
            public void onProductConnectivityChanged(boolean isConnected) {

                Log.d("Alex", "onProductConnectivityChanged: " + isConnected);

                notifyStatusChange();
            }

        };

        private DJIBaseComponent.DJIComponentListener mDJIComponentListener = new DJIBaseComponent.DJIComponentListener() {

            @Override
            public void onComponentConnectivityChanged(boolean isConnected) {
                Log.d("Alex", "onComponentConnectivityChanged: " + isConnected);
                notifyStatusChange();
            }

        };

        private void notifyStatusChange() {
            mHandler.removeCallbacks(updateRunnable);
            mHandler.postDelayed(updateRunnable, 500);
        }

        private Runnable updateRunnable = new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(FLAG_CONNECTION_CHANGE);
                sendBroadcast(intent);
            }
        };
    };

}
