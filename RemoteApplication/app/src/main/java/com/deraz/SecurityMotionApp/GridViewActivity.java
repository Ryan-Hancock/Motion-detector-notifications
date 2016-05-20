package com.deraz.SecurityMotionApp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import app.AppController;
import app.CustomRequest;

public class GridViewActivity extends ActionBarActivity {
    private static final String TAG = GridViewActivity.class.getSimpleName();

    private GridView mGridView;
    private ProgressBar mProgressBar;

    private GridViewAdapter mGridAdapter;
    private ArrayList<GridItem> mGridData;
    String userid;
    String newemail;
    private String username;
    private String FEED_URL = "http://ryanhancock.co.uk/AndroidFileUpload/index.php?name=";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);

        mGridView = (GridView) findViewById(R.id.gridView);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Initialize with empty data
        mGridData = new ArrayList<>();
        mGridAdapter = new GridViewAdapter(this, R.layout.grid_item_layout, mGridData);
        mGridView.setAdapter(mGridAdapter);

        //Grid view click event
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //Get item at position
                GridItem item = (GridItem) parent.getItemAtPosition(position);

                Intent intent = new Intent(GridViewActivity.this, DetailsActivity.class);
                ImageView imageView = (ImageView) v.findViewById(R.id.grid_item_image);

                // Interesting data to pass across are the thumbnail size/location, the
                // resourceId of the source bitmap, the picture description, and the
                // orientation (to avoid returning back to an obsolete configuration if
                // the device rotates again in the meantime)

                int[] screenLocation = new int[2];
                imageView.getLocationOnScreen(screenLocation);

                //Pass the image title and url to DetailsActivity
                intent.putExtra("left", screenLocation[0]).
                        putExtra("top", screenLocation[1]).
                        putExtra("width", imageView.getWidth()).
                        putExtra("height", imageView.getHeight()).
                        putExtra("title", item.getTitle()).
                        putExtra("image", item.getImage());

                //Start details activity
                startActivity(intent);
            }
        });
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                newemail = "";
                userid = "";
                username = "";
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
        Log.d("hello",userid);
        //Start download
        //new AsyncHttpTask().execute(FEED_URL);
        LoadPicture(userid);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void LoadPicture(final String userid) {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req";
        String FEED_URLnew = "http://ryanhancock.co.uk/AndroidFileUpload/index.php?name="+userid;


        //JsonObjectRequest
               CustomRequest jsonObjReq = new CustomRequest(Request.Method.POST,
                FEED_URLnew, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        new AsyncHttpTask().execute(response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());

            }
        }) {

                   @Override
                   protected Map<String, String> getParams() {
                       Map<String, String> params = new HashMap<String, String>();
                       params.put("lol", userid);
                       //params.put("email", "abc@androidhive.info");
                       //params.put("password", "password123");

                       return params;
                   }

               };
        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
    }


    //Downloading data asynchronously
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            Integer result =1;
            try {
                JSONObject response = new JSONObject(params[0]);
                JSONObject jr = response.getJSONObject("images");
                JSONArray jd = jr.getJSONArray("image");
                GridItem item;
                for (int i = 0; i < jd.length(); i++) {
                    JSONObject post = jd.optJSONObject(i);
                    String title = post.optString("title");
                    item = new GridItem();
                    item.setTitle(title);
                    item.setImage(post.getString("url"));
                    /**
                     JSONArray attachments = post.getJSONArray("attachments");
                     if (null != attachments && attachments.length() > 0) {
                     JSONObject attachment = attachments.getJSONObject(0);
                     if (attachment != null)
                     item.setImage(attachment.getString("url"));
                     }
                     **/
                    mGridData.add(item);
                    //hour 25
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            // Download complete. Lets update UI

            if (result == 1) {
                mGridAdapter.setGridData(mGridData);
            } else {
                Toast.makeText(GridViewActivity.this, "Failed to fetch data!", Toast.LENGTH_SHORT).show();
            }

            //Hide progressbar
            mProgressBar.setVisibility(View.GONE);
        }
    }


    String streamToString(InputStream stream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        // Close stream
        if (null != stream) {
            stream.close();
        }
        return result;
    }

    /**
     * Parsing the feed results and get the list
     *
     * @param result
     */
    private void parseResult(String result) {
        try {
            JSONObject response = new JSONObject(result);
            JSONObject jr = response.getJSONObject("images");
            JSONArray jd = jr.getJSONArray("image");
            GridItem item;
            for (int i = 0; i < jd.length(); i++) {
                JSONObject post = jd.optJSONObject(i);
                String title = post.optString("title");
                item = new GridItem();
                item.setTitle(title);
                item.setImage(post.getString("url"));
                /**
                JSONArray attachments = post.getJSONArray("attachments");
                    if (null != attachments && attachments.length() > 0) {
                        JSONObject attachment = attachments.getJSONObject(0);
                        if (attachment != null)
                        item.setImage(attachment.getString("url"));
                }
                 **/
                mGridData.add(item);
                //hour 25
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}