package marxtseng.a1knowlearner.common;

import com.android.volley.VolleyError;

import org.json.JSONArray;

/**
 * Created by marx on 06/02/2017.
 */

public interface JsonArrayRequestCallBack {
    void OnSuccess(JSONArray response);
    void OnError(VolleyError error);
}
