package marxtseng.a1knowlearner.common;

import com.android.volley.VolleyError;

import org.json.JSONObject;

/**
 * Created by marx on 06/02/2017.
 */

public interface JsonObjectRequestCallBack {
    void OnSuccess(JSONObject response);
    void OnError(VolleyError error);
}
