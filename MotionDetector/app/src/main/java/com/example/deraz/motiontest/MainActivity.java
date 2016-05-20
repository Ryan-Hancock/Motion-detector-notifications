package com.example.deraz.motiontest;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.example.deraz.motiontest.AndroidMultiPartEntity.ProgressListener;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import app.AppController;
import app.CustomRequest;
import data.GlobalData;
import data.Preferences;
import detection.AggregateLumaMotionDetection;
import detection.IMotionDetection;
import detection.LumaMotionDetection;
import detection.RgbMotionDetection;
import helper.SQLiteHandler;
import image.ImageProcessing;

/**
 * Created by deraz on 14/12/2015.
 */

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "MotionDetectionActivity";

    private static SurfaceView preview = null;
    private static SurfaceHolder previewHolder = null;
    private static Camera camera = null;
    private static boolean inPreview = false;
    private static long mReferenceTime = 0;
    private static IMotionDetection detector = null;
    private static boolean detected = false;
    private static final String tag = "Main";
    private MalibuCountDownTimer countDownTimer;
    private long timeElapsed;
    private boolean timerHasStarted = false;
    private Button startB;
    private TextView text;
    private TextView timeElapsedView;
    public boolean timerbool = false;
    String userid;
    String newemail;

    SharedPreferences SP;
    boolean bAppUpdates;


    private final long startTime = 5000;
    private final long interval = 1000;

    private static volatile AtomicBoolean processing = new AtomicBoolean(false);
    private boolean saveAppPref;
    private boolean emailAppPref;
    private String smsAppPref;
    private String username;
    private int timeDelay;
    private TextView tw;
    private boolean remoteAppPref = false;
    private Handler mHandler;
    private String filename = "null";
    private SQLiteHandler db;
    private boolean beepAppPref;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        startB = (Button) this.findViewById(R.id.button);
        startB.setOnClickListener(this);
        text = (TextView) this.findViewById(R.id.timer);
        tw = (TextView) this.findViewById(R.id.textView);
        timeElapsedView = (TextView) this.findViewById(R.id.timeElapsed);
        countDownTimer = new MalibuCountDownTimer(startTime, interval);
        text.setText(text.getText() + String.valueOf(startTime));
        SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        bAppUpdates = SP.getBoolean("cloudBoxPref", false);
        saveAppPref = SP.getBoolean("saveBoxPref", false);
        emailAppPref = SP.getBoolean("emailBoxPref", false);
        smsAppPref = SP.getString("smsTextPref","null");
        remoteAppPref = SP.getBoolean("remoteBoxPref",false);
        beepAppPref = SP.getBoolean("beepBoxPref",false);
        timeDelay = Integer.parseInt(SP.getString("timelist","10000"));
        mediaPlayer = MediaPlayer.create(this, R.raw.song);


        //timeDelay = Integer.parseInt(SP.getString("prefList", "1"));

        if (data.Preferences.USE_RGB) {
            detector = new RgbMotionDetection();
        } else if (data.Preferences.USE_LUMA) {
            detector = new LumaMotionDetection();
        } else {
            detector = new AggregateLumaMotionDetection();
        }
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                username = "";
                newemail = "";
                userid = "";
            } else {
                userid = extras.getString("user");
                newemail = extras.getString("email");
                username = extras.getString("name");
            }
        } else {
            userid = (String) savedInstanceState.getSerializable("user");
            newemail = (String) savedInstanceState.getSerializable("email");
            username = (String) savedInstanceState.getSerializable("name");
        }
        //ShowToast("hello");
        if (remoteAppPref){
            mHandler = new Handler();
            startRepeatingTask();
        }
    }

    @Override
    public void onClick(View v) {
        if (!timerHasStarted) {
            countDownTimer.start();
            timerHasStarted = true;
            startB.setText("Start");
        } else {

            countDownTimer.cancel();
            timerHasStarted = false;
            startB.setText("RESET");
            timerbool = false;
            if (remoteAppPref)remoteStart(0, filename);
        }
    }

    public class MalibuCountDownTimer extends CountDownTimer {

        public MalibuCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {
            text.setText("Time's up!");
            timeElapsedView.setText("Time Elapsed: " + String.valueOf(startTime));
            Log.d("timer", String.valueOf(startTime));
            timerbool = true;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            text.setText("Time remain:" + millisUntilFinished);
            timeElapsed = startTime - millisUntilFinished;
            timeElapsedView.setText("Time Elapsed: " + String.valueOf(timeElapsed));
            Log.d("timer", String.valueOf(timeElapsed));
            tw.setText("");
        }

    }

    private long mInterval = 10000;
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                remoteStart(0, filename); //this function can change value of mInterval.
            } finally {
                // 100% guarantee that this always happens, even if
                // your update method throws an exception
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    void startRepeatingTask() {
        mStatusChecker.run();
    }

    void stopRepeatingTask() {
        mHandler.removeCallbacks(mStatusChecker);
    }

    public void remoteStart(final int MotionSet, final String filepath){
        String tag_json_obj = "json_obj_req";
        String FEED_URL = "http://ryanhancock.co.uk/AndroidFileUpload/remote.php";

        //JsonObjectRequest
        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,
                FEED_URL, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            int remoteStat = Integer.parseInt(response.getString("status"));
                            //Log.d(TAG,getString(remoteStat));
                            if (remoteStat == 1){
                                countDownTimer.start();
                                Log.d("lol",response.getString("status"));
                            }
                            else if (remoteStat == 2){
                                Log.d("lol",response.getString("status"));
                                recreate();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        //
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());

            }
        }) {


            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", userid);
                if (detected){
                    params.put("motion", "1");
                    params.put("image",filepath);
                }
                else if (MotionSet == 1)params.put("motion", "2");
                else params.put("motion","0");

                params.put("set","0");
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        super.onPause();

        camera.setPreviewCallback(null);
        if (inPreview) camera.stopPreview();
        inPreview = false;
        camera.release();
        camera = null;
        if (remoteAppPref)stopRepeatingTask();
    }

    @Override
    public void onResume() {
        super.onResume();

        camera = Camera.open();


        db = new SQLiteHandler(getApplicationContext());
        HashMap<String, String> user = db.getUserDetails();

        username = user.get("name");
        newemail = user.get("email");
        userid = user.get("uid");


    }


    private PreviewCallback previewCallback = new PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera cam) {
            if (data == null) return;
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null) return;

            if (timerbool) {
                if (!GlobalData.isPhoneInMotion()) {
                    DetectionThread thread = new DetectionThread(data, size.width, size.height,timeDelay);
                    thread.start();
                    //tw.setText("Phone is in motion");
                }
                else tw.setText("");
            }
        }
    };
    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            } catch (Throwable t) {
                Log.e("PreDemo-surfaceCallback", "Exception in setPreviewDisplay()", t);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (camera != null) {
                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = getBestPreviewSize(width, height, parameters);
                if (size != null) {
                    parameters.setPreviewSize(size.width, size.height);
                    Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
                }
                camera.setParameters(parameters);
                camera.startPreview();
                inPreview = true;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // Ignore
        }
    };

    private static Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea > resultArea) result = size;
                }
            }
        }

        return result;
    }


    private final class DetectionThread extends Thread {

        private byte[] data;
        private int width;
        private int height;
        private int timeDelay;


        public DetectionThread(byte[] data, int width, int height,int timeDelay) {
            this.data = data;
            this.width = width;
            this.height = height;
            this.timeDelay = timeDelay;

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {

            if (!processing.compareAndSet(false, true)) return;

            // Log.d(TAG, "BEGIN PROCESSING...");
            try {
                // Previous frame
                int[] pre = null;
                if (Preferences.SAVE_PREVIOUS) pre = detector.getPrevious();

                // Current frame (with changes)
                // long bConversion = System.currentTimeMillis();
                int[] img = null;
                if (Preferences.USE_RGB) {
                    img = ImageProcessing.decodeYUV420SPtoRGB(data, width, height);
                } else {
                    img = ImageProcessing.decodeYUV420SPtoLuma(data, width, height);
                }
                // long aConversion = System.currentTimeMillis();
                // Log.d(TAG, "Converstion="+(aConversion-bConversion));

                // Current frame (without changes)
                int[] org = null;
                if (Preferences.SAVE_ORIGINAL && img != null) org = img.clone();

                if (img != null && detector.detect(img, width, height)) {
                    displayMessage("Motion Detected",0);
                    detected = detector.detect(img, width, height);
                    if (beepAppPref)mediaPlayer.start();
                    // The delay is necessary to avoid taking a picture while in
                    // the
                    // middle of taking another. This problem can causes some
                    // phones
                    // to reboot.
                    long now = System.currentTimeMillis();
                    if (now > (mReferenceTime + timeDelay)) {
                        mReferenceTime = now;

                        Bitmap previous = null;
                        if (Preferences.SAVE_PREVIOUS && pre != null) {
                            if (Preferences.USE_RGB)
                                previous = ImageProcessing.rgbToBitmap(pre, width, height);
                            else previous = ImageProcessing.lumaToGreyscale(pre, width, height);
                        }

                        Bitmap original = null;
                        if (Preferences.SAVE_ORIGINAL && org != null) {
                            if (Preferences.USE_RGB)
                                original = ImageProcessing.rgbToBitmap(org, width, height);
                            else original = ImageProcessing.lumaToGreyscale(org, width, height);
                        }

                        Bitmap bitmap = null;
                        if (Preferences.SAVE_CHANGES) {
                            if (Preferences.USE_RGB)
                                bitmap = ImageProcessing.rgbToBitmap(img, width, height);
                            else bitmap = ImageProcessing.lumaToGreyscale(img, width, height);
                        }

                        Log.i(TAG, "Saving.. previous=" + previous + " original=" + original + " bitmap=" + bitmap);
                        if (emailAppPref)new EmailTask().execute();
                        if (smsAppPref != "null") new smsTask().execute();

                        Looper.prepare();

                        new SavePhotoTask().execute(previous, original, bitmap);

                        if (saveAppPref)displayMessage("Saving",0);


                    } else {
                        //Log.i(TAG, "Not taking picture because not enough time has passed since the creation of the Surface");

                    }
                }else{
                    if (!detector.detect(img, width, height)){
                        displayMessage("No Motion",0);
                        detected = false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                processing.set(false);
                //displayMessage("No Motion");
            }
            // Log.d(TAG, "END PROCESSING...");

            processing.set(false);

        }

    }



    private static final int MSG_SHOW_TOAST = 1;

    private Handler messageHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_SHOW_TOAST) {
                String message = (String)msg.obj;
                //Toast toast = Toast.makeText(MainActivity.this, message , Toast.LENGTH_SHORT);
                tw.setText(message);
                //toast.show();
                if (msg.arg1 == 1){
                    //toast.cancel();
                }
            }
        }
    };

    private void displayMessage(String Message,int status) {
        Message msg = new Message();
        msg.what = MSG_SHOW_TOAST;
        msg.obj = Message;
        msg.arg1 = status;
        messageHandler.sendMessage(msg);
    }
    ;

    private final class SavePhotoTask extends AsyncTask<Bitmap, Integer, String> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected String doInBackground(Bitmap... data) {
            for (int i = 0; i < data.length; i++) {
                Bitmap bitmap = data[i];
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateandTime = sdf.format(new Date());
                String name = currentDateandTime;

                if (bitmap != null) {
                    if (saveAppPref) save(name, bitmap);
                    if (bAppUpdates) uploadfile(name, bitmap);

                }
            }
            return "1";
        }

        private void save(String name, Bitmap bitmap) {
            File photo = new File(Environment.getExternalStorageDirectory()+"/MotionDetector", name + ".jpg");
            photo.mkdirs();
            if (photo.exists()) photo.delete();

            try {
                FileOutputStream fos = new FileOutputStream(photo.getPath());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();
            } catch (java.io.IOException e) {
                Log.e("PictureDemo", "Exception in photoCallback", e);
            }
        }

        private String uploadfile(String name, Bitmap bitmap) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://ryanhancock.co.uk/AndroidFileUpload/fileUpload.php");
            String responseString = null;
            Log.e("here", "got here");
            filename = name+".jpg";
            try {


                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) 1) * 100));
                            }
                        });
                File photoup = new File(Environment.getExternalStorageDirectory()+"/MotionDetector", name + ".jpg");
                //File photoup = new File(Environment.getExternalStorageDirectory()+"/MotionDetector", name + ".jpg");
                Log.e("here2", String.valueOf(photoup));
                //File sourceFile = new File(photoup.getPath());
                Bitmap bmp = BitmapFactory.decodeFile(photoup.getPath());
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG,70,bos);
                //InputStream in = new ByteArrayInputStream(bos.toByteArray());
                ByteArrayBody sourceFile = new ByteArrayBody(bos.toByteArray(),name+".jpg");

                Log.e("here5", String.valueOf(sourceFile));
                entity.addPart("image", sourceFile);
                entity.addPart("website", new StringBody("www.ryan.com"));
                entity.addPart("email", new StringBody(userid));
                entity.addPart("newemail", new StringBody(newemail));
                long totalSize = entity.getContentLength();
                httppost.setEntity(entity);
                remoteStart(1,name+".jpg");
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();


                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    Log.e("here3", String.valueOf(statusCode));
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                    Log.e("here6", responseString);
                } else {
                    Log.e("here", String.valueOf(statusCode));
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                    Log.e("here7", responseString);
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return name;


        }

        protected void onPostExecute(String name) {
            //Log.e(TAG, "Response from server: ");


        }


    }
    private final class smsTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://ryanhancock.co.uk/AndroidFileUpload/SMS.php?num="+smsAppPref);
            String responseString = "";
                    try{
                        AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                                new ProgressListener() {

                                    @Override
                                    public void transferred(long num) {

                                    }
                                });
                        entity.addPart("num", new StringBody(smsAppPref));
                        Log.d("here",smsAppPref);
                        httppost.setEntity(entity);

                        HttpResponse response = httpclient.execute(httppost);
                        HttpEntity r_entity = response.getEntity();


                        int statusCode = response.getStatusLine().getStatusCode();
                        if (statusCode == 200) {
                            Log.e("here3", String.valueOf(statusCode));
                            // Server response
                            responseString = EntityUtils.toString(r_entity);
                            Log.e("here6", responseString);
                        } else {
                            responseString = "Error occurred! Http Status Code: "
                                    + statusCode;
                            Log.e("here7", responseString);
                        }
                    } catch (ClientProtocolException e) {
                        responseString = e.toString();
                    } catch (IOException e) {
                        responseString = e.toString();
                    }return null;

                    }
        }


    private final class EmailTask extends AsyncTask<Bitmap, Void, String> {
        protected String doInBackground(Bitmap... param) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://ryanhancock.co.uk/AndroidFileUpload/Mailer.php");
            String responseString = "";
            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new ProgressListener() {

                            @Override
                            public void transferred(long num) {

                            }
                        });
                entity.addPart("email", new StringBody(newemail));
                entity.addPart("user", new StringBody(username));
                httppost.setEntity(entity);

                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();


                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    Log.e("here3", String.valueOf(statusCode));
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                    Log.e("here6", responseString);
                } else {
                    Log.e("here", String.valueOf(statusCode));
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                    Log.e("here7", responseString);
                }
            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }

            return "1";
        }


    }

}




