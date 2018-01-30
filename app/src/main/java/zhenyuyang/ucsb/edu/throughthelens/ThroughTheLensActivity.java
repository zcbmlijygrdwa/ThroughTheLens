package zhenyuyang.ucsb.edu.throughthelens;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.util.DJICommonCallbacks;
import dji.sdk.airlink.DJILBAirLink;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.camera.DJICamera;
import dji.sdk.codec.DJICodecManager;
import dji.sdk.flightcontroller.DJIFlightController;
import dji.sdk.flightcontroller.DJIFlightControllerDelegate;
import dji.sdk.products.DJIAircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import zhenyuyang.ucsb.edu.throughthelens.common.DJIApplication;
import zhenyuyang.ucsb.edu.throughthelens.gdx.GameFragment;

/**
 * Created by Zhenyu on 2018-01-29.
 */

public class ThroughTheLensActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener,  AndroidFragmentApplication.Callbacks{
    private TextureView mVideoSurface = null;
    private TextView responseTextView = null;
    private TextView textView_test = null;
    private EditText addressEditText = null;
    private EditText portEditText = null;
    private Button button_test1 = null;
    private Button button_connect_gps_server = null;
    private Button button_disconnect_gps_server = null;

    private DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallback = null;
    private DJILBAirLink.DJIOnReceivedVideoCallback mOnReceivedVideoCallback = null;
    private DJICodecManager mCodecManager = null;
    private DJIBaseProduct mProduct = null;
    private DJIAircraft mAircraft;
    private DJIFlightController mFlightController;
    public static float[] boundingBox = {0,0,0,0};
    public static StringBuilder builder = new StringBuilder();

    private  Thread GSPSocketClientThread;

    private volatile boolean gpsSocketClientIsRunning = true;
    private float gpsSocketClientUpdateInterval = 0.5f;  //seconds
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_throught_the_lens);

        initUI();



        //libGDX part
        // Create libgdx fragment
        GameFragment libgdxFragment = new GameFragment();

        // Put it inside the framelayout (which is defined in the layout.xml file).
        getSupportFragmentManager().beginTransaction().
                add(R.id.content_framelayout, libgdxFragment).
                commit();

    }




    private void initUI() {
//        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Service.LAYOUT_INFLATER_SERVICE);
//
//        View content = layoutInflater.inflate(R.layout.view_fpv_display, null, false);
//        addView(content, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT));
//
//        Log.v("TAG","Start to test");

        mVideoSurface = (TextureView) findViewById(R.id.texture_video_previewer_surface);
        textView_test = (TextView)findViewById(R.id.textView_test);
        button_test1 = (Button)findViewById(R.id.button_test1);



        //register button listeners
        button_test1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();
                mFlightController = DJIApplication.getAircraftInstance().getFlightController();
                byte[] message = FloatArray2ByteArray(DrawBoxView.coordinate);
                mFlightController.sendDataToOnboardSDKDevice(message,
                        new DJICommonCallbacks.DJICompletionCallback() {
                            @Override
                            public void onResult(DJIError djiError) {
                                if (djiError == null) {
                                    Toast.makeText(getApplicationContext(), "Success upstream from Mobile Device to OES", Toast.LENGTH_SHORT).show();
                                    //DJIDialog.showDialog(getApplicationContext(),"Success upstream from Mobile Device to OES");
                                } else {
                                    Toast.makeText(getApplicationContext(), "Error on upstream from Mobile Device to OES. Description:" + djiError.getDescription(), Toast.LENGTH_SHORT).show();
                                    //DJIDialog.showDialog(getApplicationContext(), "Error on upstream from Mobile Device to OES. Description:" + djiError.getDescription());
                                }
                            }
                        });
            }
        });

        if (null != mVideoSurface) {
            mVideoSurface.setSurfaceTextureListener(this);

            // This callback is for
            mOnReceivedVideoCallback = new DJILBAirLink.DJIOnReceivedVideoCallback() {
                @Override
                public void onResult(byte[] videoBuffer, int size) {
                    if (mCodecManager != null) {
                        mCodecManager.sendDataToDecoder(videoBuffer, size);
                    }
                }
            };

            mReceivedVideoDataCallback = new DJICamera.CameraReceivedVideoDataCallback() {
                @Override
                public void onResult(byte[] videoBuffer, int size) {
                    if (null != mCodecManager) {
                        mCodecManager.sendDataToDecoder(videoBuffer, size);
                    }
                }
            };
        }
        initSDKCallback();
    }

    private void initSDKCallback() {
        try {
            mProduct = DJIApplication.getProductInstance();

            if (mProduct.getModel() != Model.UnknownAircraft) {
                mProduct.getCamera().setDJICameraReceivedVideoDataCallback(mReceivedVideoDataCallback);

            } else {
                mProduct.getAirLink().getLBAirLink().setDJIOnReceivedVideoCallback(mOnReceivedVideoCallback);
            }
        } catch (Exception exception) {}
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCodecManager == null) {
            mCodecManager = new DJICodecManager(getApplicationContext(), surface, width, height);

        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //videoFrameCount++;
        try {
            //Utils.setResultToToast(getContext(), "tsurface = "+surface.getTimestamp());

            int p = mVideoSurface.getBitmap().getPixel(200,200);

            int R = (p >> 16) & 0xff;
            int G = (p >> 8) & 0xff;
            int B = p & 0xff;
            //textView_test.setText("R = "+R+", G = "+G+", B = "+B);
            //setResultToToast(getContext(), "mVideoSurface = "+mVideoSurface);





            //b = mVideoSurface.getBitmap();
            //            tmpMAT = new Mat (b.getWidth(), b.getHeight(), CvType.CV_8UC1);  //something is null here....


        } catch(Exception ex) {};




        DJIAircraft mAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();
        DJIFlightController mFlightController = mAircraft.getFlightController();
        mFlightController.setReceiveExternalDeviceDataCallback(new DJIFlightControllerDelegate.FlightControllerReceivedDataFromExternalDeviceCallback() {
            @Override
            public void onResult(byte[] data) {
                boundingBox = toFloatArray(data);
                builder = new StringBuilder();
                for(float i : boundingBox)
                {
                    builder.append("" + i + " ");
                }
//                ((EditText)findViewById(R.id.debug)).setText("data = "+builder.toString());
                textView_test.setText("data = "+builder.toString());
//                setResultToToast(getContext(), "data received: " + builder.toString());
                ((DrawBoxView)findViewById(R.id.draw_box_view)).invalidate();
            }
        });
    }

    private static float[] toFloatArray(byte[] bytes) {

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        FloatBuffer fb = buffer.asFloatBuffer();
        float[] floatArray = new float[fb.limit()];
        fb.get(floatArray);
        return floatArray;
    }

    private byte[] FloatArray2ByteArray(float[] values){
        ByteBuffer buffer = ByteBuffer.allocate(4 * values.length);

        for (float value : values){
            buffer.putFloat(value);
        }
        return buffer.array();
    }

    @Override
    public void exit() {

    }
}
