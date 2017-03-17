package marxtseng.a1knowlearner.common;

import android.content.Context;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marx on 06/02/2017.
 */

public class Utility {

    public static String COOKIE;

    /**
     * SetImageView
     * https://s3.amazonaws.com/1know.net/:type/:uqid
     * @param view
     * @param type course | group
     * @param uqid
     */
    public static void SetImageView(ImageView view, String type, String uqid, boolean rounded, int radius) {
        type = type.equalsIgnoreCase("course") ? "knowledge" : type;
        type = type.equalsIgnoreCase("task") ? "group" : type;

        String url = "https://s3.amazonaws.com/1know.net/" + type + "/" + uqid + ".png";

        if (rounded) {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(view.getContext()).build();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .displayer(new RoundedBitmapDisplayer(dip2px(view.getContext(), radius)))
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
            imageLoader.displayImage(url, view, options);
        }
        else {
            ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(view.getContext()).build();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
            ImageLoader imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);
            imageLoader.displayImage(url, view, options);
        }
    }

    private static int dip2px(Context context, float dpValue) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * GetCourses
     * http://1know.net/services/learning/courses
     * @param context
     * @param callBack
     */
    public static void GetCourses(Context context, final JsonArrayRequestCallBack callBack) {
        String url = "http://1know.net/services/learning/courses";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        callBack.OnSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.OnError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", COOKIE);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonArrayRequest);
    }

    /**
     * GetTasks
     * http://1know.net/services/learning/tasks
     * @param context
     * @param callBack
     */
    public static void GetTasks(Context context, final JsonArrayRequestCallBack callBack) {
        String url = "http://1know.net/services/learning/tasks";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        callBack.OnSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.OnError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", COOKIE);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonArrayRequest);
    }

    /**
     * GetUnits
     * http://1know.net/services/learning/:type/:uqid
     * @param context
     * @param type course | task
     * @param uqid
     * @param callBack
     */
    public static void GetUnits(Context context, String type, String uqid, final JsonArrayRequestCallBack callBack) {
        String url = "http://1know.net/services/learning/" + type + "/" + uqid;
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        callBack.OnSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.OnError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", COOKIE);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonArrayRequest);
    }

    /**
     * GetUnit
     * http://1know.net/services/learning/unit/:uqid
     * @param context
     * @param uqid
     * @param callBack
     */
    public static void GetUnit(Context context, String uqid, final JsonObjectRequestCallBack callBack) {
        String url = "http://1know.net/services/learning/unit/" + uqid;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callBack.OnSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.OnError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", COOKIE);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * AddHistory
     * http://1know.net/services/learning/unit/:uqid/history
     * @param context
     * @param uqid
     * @param request
     * @param callBack
     */
    public static void AddHistory(Context context, String uqid, JSONObject request, final JsonObjectRequestCallBack callBack) {
        String url = "http://1know.net/services/learning/unit/" + uqid + "/history";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callBack.OnSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.OnError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", COOKIE);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * UpdateHistory
     * http://1know.net/services/learning/history/:uqid
     * @param context
     * @param uqid
     * @param request
     * @param callBack
     */
    public static void UpdateHistory(Context context, String uqid, JSONObject request, final JsonObjectRequestCallBack callBack) {
        String url = "http://1know.net/services/learning/history/" + uqid;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, request,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callBack.OnSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.OnError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", COOKIE);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * SendDone
     * http://1know.net/services/learning/unit/:uqid/done
     * @param context
     * @param uqid
     * @param callBack
     */
    public static void SendDone(Context context, String uqid, final JsonObjectRequestCallBack callBack) {
        String url = "http://1know.net/services/learning/unit/" + uqid + "/done";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callBack.OnSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.OnError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", COOKIE);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * SendResult
     * http://1know.net/services/learning/unit/:uqid/result
     * @param context
     * @param uqid
     * @param result
     * @param callBack
     */
    public static void SendResult(Context context, String uqid, JSONObject result, final JsonObjectRequestCallBack callBack) {
        String url = "http://1know.net/services/learning/unit/" + uqid + "/result";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, result,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callBack.OnSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callBack.OnError(error);
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Cookie", COOKIE);
                return headers;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(jsonObjectRequest);
    }
}
