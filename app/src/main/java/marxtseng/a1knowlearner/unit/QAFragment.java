package marxtseng.a1knowlearner.unit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import marxtseng.a1knowlearner.R;
import marxtseng.a1knowlearner.common.JsonObjectRequestCallBack;
import marxtseng.a1knowlearner.common.Utility;

/**
 * Created by marx on 06/02/2017.
 */

public class QAFragment extends UnitFragment {

    private View mView;

    public QAFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_unit_qa, container, false);

        setUnit(getArguments().getString("UQID"));

        return mView;
    }

    @Override
    public void onDestroy() {
        ((WebView) mView.findViewById(R.id.content)).loadData("<div></div>", "text/html", null);

        if (mTimer != null) mTimer.cancel();
        updateHistory((int)((new Date()).getTime() - mLastTime));

        super.onDestroy();
    }

    @Override
    protected void initFragment() {
        try {
            WebView contentView = (WebView) mView.findViewById(R.id.content);
            contentView.getSettings().setJavaScriptEnabled(true);
            contentView.getSettings().setDefaultTextEncodingName("utf-8");
            contentView.getSettings().setTextZoom(120);
            contentView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    mView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;

                }
            });
            contentView.loadData(mUnit.getString("content"), "text/html; charset=utf-8", "utf-8");

            String result = mUnit.getJSONObject("study_result").getString("result");
            ((EditText)mView.findViewById(R.id.result)).setText(result);

            startInterval();
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void sendResult() {
        try {
            String data = ((EditText)mView.findViewById(R.id.result)).getText().toString();

            JSONObject content = new JSONObject();
            content.put("unit_type", mUnit.getString("unit_type"));
            content.put("result", data);
            JSONObject result = new JSONObject();
            result.put("content", content.toString());

            Utility.SendResult(mView.getContext(), mUnit.getString("uqid"), result, new JsonObjectRequestCallBack() {
                @Override
                public void OnSuccess(JSONObject response) {
                    Log.d(TAG, "SendResult - " + response.toString());
                    Toast.makeText(getActivity(), R.string.the_answer_has_been_sent, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void OnError(VolleyError error) {
                    Log.d(TAG, error.getMessage());
                }
            });
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
