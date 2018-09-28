package zhenyuyang.ucsb.edu.throughthelens;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.mygdx.game.MyGdxGame;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import dji.common.camera.DJICameraSettingsDef;
import dji.common.error.DJIError;
import dji.common.product.Model;
import dji.common.util.DJICommonCallbacks;
import dji.internal.geofeature.flyforbid.Utils;
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
//import zhenyuyang.ucsb.edu.throughthelens.gdx.MyGdxGame2;
import zhenyuyang.ucsb.edu.throughthelens.utils.DJIModuleVerificationUtil;



/**
 * Created by Zhenyu on 2018-01-29.
 */



public class ThroughTheLensActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener,  AndroidFragmentApplication.Callbacks{
    private TextureView mVideoSurface = null;
    private TextView responseTextView = null;
    private TextView textView_test = null;
    private EditText addressEditText = null;
    private EditText portEditText = null;
    private Button button_sendWayPoint = null;
    private Button button_previewWayPoint = null;
    private Button button_video = null;

    Timer timer = new Timer();
    private int timeCounter = 0;
    private long hours = 0;
    private long minutes = 0;
    private long seconds = 0;
    String time = "";
    boolean isCameraRecording = false;

    private DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallback = null;
    private DJILBAirLink.DJIOnReceivedVideoCallback mOnReceivedVideoCallback = null;
    private DJICodecManager mCodecManager = null;
    private DJIBaseProduct mProduct = null;
    private DJIAircraft mAircraft;
    private DJIFlightController mFlightController;
    public static float[] boundingBox = {0,0,0,0};
    public static float[] localization = new float[3];
    public static StringBuilder builder = new StringBuilder();

    private  Thread GSPSocketClientThread;

    private volatile boolean gpsSocketClientIsRunning = true;
    private float gpsSocketClientUpdateInterval = 0.5f;  //seconds

    float[] skeleton = new float[42];
    ArrayList<float[]> skeletonSet = new ArrayList<>();
    MyGdxGame myGdxGame;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_throught_the_lens);

        initUI();

        //load skeleton data
        String file = "norm_skt.txt";
        try {
            System.out.println("trying read");
            BufferedReader br = new BufferedReader(new InputStreamReader(getAssets().open(file)));
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                //System.out.println("line[] = "+line);
                String[] splited = line.split("\\s+");
                float[] tempSkeleton = new float[42];
                //System.out.println("splited.length = "+splited.length);
                //System.out.println("tempSkeleton.length = "+tempSkeleton.length);
                if(tempSkeleton.length==splited.length){
                    for(int i = 0 ; i < tempSkeleton.length;i++){
                        tempSkeleton[i] = Float.parseFloat((splited[i]));
                    }
                    skeletonSet.add(tempSkeleton);
                }

//                ApplicationListener applicationListener = Gdx.app.getApplicationListener();
//                myGdxGame =(MyGdxGame) applicationListener;
//                myGdxGame.setData(skeleton);

            }
            System.out.println("skeletonSet.size() = "+skeletonSet.size());


        }
        catch(IOException e){
            System.out.println("Reading txt Error: "+e.toString());
            return;
        }

        //libGDX part
        // Create libgdx fragment
        GameFragment libgdxFragment = new GameFragment();


        // Put it inside the framelayout (which is defined in the layout.xml file).
        getSupportFragmentManager().beginTransaction().
                add(R.id.content_framelayout, libgdxFragment).
                commit();
    }




    private void initUI() {

        mVideoSurface = (TextureView) findViewById(R.id.texture_video_previewer_surface);
        textView_test = (TextView)findViewById(R.id.textView_test);
        button_sendWayPoint = (Button)findViewById(R.id.button_sendWayPoint);
        button_previewWayPoint = (Button)findViewById(R.id.button_previeWayPoint);
        button_video = (Button)findViewById(R.id.button_video);

        //register button listeners
        button_sendWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        button_previewWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        button_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!isCameraRecording){
//Utils.setResultToText(context, mTexInfo, "00:00:00");
                    textView_test.setText("00:00:00");
                    Toast.makeText(getApplicationContext(), "Start record", Toast.LENGTH_SHORT).show();
                    runOnUiThread (new Thread(new Runnable() {
                        public void run() {
                            button_video.setBackgroundResource(R.color.endRecord);
                            button_video.setText("STOP");
                        }
                    }));
                    isCameraRecording = true;
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {

                            hours = TimeUnit.MILLISECONDS.toHours(timeCounter);
                            minutes = TimeUnit.MILLISECONDS.toMinutes(timeCounter) - (hours * 60);
                            seconds = TimeUnit.MILLISECONDS.toSeconds(timeCounter) - ((hours * 60 * 60) + (minutes * 60));
                            time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                            //Utils.setResultToText(context, mTexInfo, time);
                            runOnUiThread (new Thread(new Runnable() {
                                public void run() {
                                    textView_test.setText(time);
                                    //Log.i("UI","setTime");
                                }
                            }));
                            ApplicationListener applicationListener = Gdx.app.getApplicationListener();
                            myGdxGame =(MyGdxGame) applicationListener;
                            myGdxGame.setData(skeletonSet.get(timeCounter));
                            if(timeCounter+1!=skeletonSet.size()){
                                timeCounter = timeCounter + 1;
                            }
                            else{
                                timeCounter = 0;
                            }

                        }
                    }, 0, 1);



                }
                else{
                    //stop recording
                    //Utils.setResultToToast(getContext(), "StopRecord");
                    Toast.makeText(getApplicationContext(), "Stop Record", Toast.LENGTH_SHORT).show();
                    isCameraRecording = false;
                    //Utils.setResultToText(context, mTexInfo, "00:00:00");
                    runOnUiThread (new Thread(new Runnable() {
                        public void run() {
                            textView_test.setText("00:00:00");
                            button_video.setBackgroundResource(R.color.startRecord);
                            button_video.setText("RECORD");
                        }
                    }));
                    timer.cancel();
                    timeCounter = 0;

                }
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
        //initSDKCallback();




//        DJIAircraft mAircraft = (DJIAircraft) DJISDKManager.getInstance().getDJIProduct();
//        DJIFlightController mFlightController = mAircraft.getFlightController();
//        mFlightController.setReceiveExternalDeviceDataCallback(new DJIFlightControllerDelegate.FlightControllerReceivedDataFromExternalDeviceCallback() {
//            @Override
//            public void onResult(byte[] data) {
//                float[] dataTemp = toFloatArray(data);
//
//                if(dataTemp[0]>0){
//                    for(int i = 1;i<22;i++){
//                        skeleton[i-1] = dataTemp[i];
//                    }
//                }
//                else{
//                    for(int i = 1;i<22;i++){
//                        skeleton[21+i-1] = dataTemp[i];
//                    }
//                }
//
//
//
//
////                ((EditText)findViewById(R.id.debug)).setText("data = "+builder.toString());
//                if(dataTemp[0]<0){
//
//                    ApplicationListener applicationListener = Gdx.app.getApplicationListener();
//                    myGdxGame =(MyGdxGame) applicationListener;
//                    myGdxGame.setData(skeleton);
//
//                    //textView_test.setText("data = "+data.toString());
//                    String test = "";
//                    for(int i = 0;i<skeleton.length;i++){
//                        test+=skeleton[i]+",";
//                    }
//                    //textView_test.setText("test = "+test);
//
//
//                }
//
//            }
//        });
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
