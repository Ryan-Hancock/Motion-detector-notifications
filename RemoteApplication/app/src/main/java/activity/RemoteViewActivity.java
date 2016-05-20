package activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.deraz.SecurityMotionApp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import app.AppController;
import app.CustomRequest;

/**
 * Created by deraz on 08/04/2016.
 */
public class RemoteViewActivity extends AppCompatActivity {
    private Button startbtn;
    private Button stopbtn;
    private TextView startTV;
    private TextView detectTV;
    private ImageView imageView;
    private String userid;
    private Handler mHandler;
    private String IMAGE_URL = "http://ryanhancock.co.uk/AndroidFileUpload/status.php" ;
    private String START_URL = "http://ryanhancock.co.uk/AndroidFileUpload/start.php" ;
    private int start;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        startbtn = (Button)findViewById(R.id.btnstartremote);
        stopbtn = (Button)findViewById(R.id.btnremotestop);
        startTV = (TextView)findViewById(R.id.startedTV);
        detectTV = (TextView)findViewById(R.id.detectedTV);
        imageView = (ImageView)findViewById(R.id.imageView);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                userid = "";
            } else {
                userid = extras.getString("user");
            }
        } else {
            userid = (String) savedInstanceState.getSerializable("user");
        }

        startbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = 1;
                remoteStart(IMAGE_URL);
                mHandler = new Handler();
                startRepeatingTask();
                remoteStart(START_URL);
            }
        });

        stopbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start = 0;
                remoteStart(START_URL);
                if (start == 1){
                    stopRepeatingTask();
                }

            }
        });
    }
    private long mInterval = 20000;
    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            try {
                remoteStart(IMAGE_URL);
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


    private void remoteStart(final String FEED_URL) {
        String tag_json_obj = "json_obj_req";

        //JsonObjectRequest
        CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,
                FEED_URL, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("response", response.toString());
                        try {
                            if (FEED_URL.equals(IMAGE_URL)){
                            String imageurl = response.getString("imageurl");
                            Log.d("url",imageurl);
                            Picasso.with(getBaseContext())
                                    .load(imageurl)
                                    .into(imageView);
                                loadVaules(response);
                            }
                            else if(FEED_URL.equals(START_URL)) {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("error", "Error: " + error.getMessage());

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("name", userid);
                if(FEED_URL.equals(START_URL)){
                    if (start ==1){
                        params.put("set","1");
                        Log.d("set","1");
                    }
                    else if(start == 0){
                        params.put("set","2");
                        Log.d("set","0");
                    }
                }
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    private void loadVaules(JSONObject response) throws JSONException {
       String motion = response.getString("motion");
        String started = response.getString("status");
        if(motion.equals("1")){
            detectTV.setText("Motion Detected: Detected");
        }else{
            detectTV.setText("Motion Detected: No Motion");
        }

        if(started.equals("0")){
            startTV.setText("Motion Started: Connected");
        }
        else if(started.equals("1")){
            startTV.setText("Motion Started: Started");
        }
    }
}

