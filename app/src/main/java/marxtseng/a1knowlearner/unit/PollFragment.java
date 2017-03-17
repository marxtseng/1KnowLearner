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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

import marxtseng.a1knowlearner.R;
import marxtseng.a1knowlearner.common.JsonObjectRequestCallBack;
import marxtseng.a1knowlearner.common.Utility;

/**
 * Created by marx on 06/02/2017.
 */

public class PollFragment extends UnitFragment {

    private View mView;

    private float mDensity;

    private RadioGroup mRadioGroup;
    private ArrayList<CheckBox> mCheckBoxList = new ArrayList<>();

    public PollFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_unit_poll, container, false);

        mDensity = getContext().getResources().getDisplayMetrics().density;

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
            contentView.loadData(mUnit.getJSONObject("content").getString("content"), "text/html; charset=utf-8", "utf-8");

            LinearLayout optionsLayout = (LinearLayout) mView.findViewById(R.id.options);

            JSONArray options = mUnit.getJSONObject("content").getJSONArray("options");
            JSONArray result = mUnit.getJSONObject("study_result").getJSONArray("result");

            if (mUnit.getJSONObject("content").getString("type").equalsIgnoreCase("single")) {
                mRadioGroup = new RadioGroup(optionsLayout.getContext());
                optionsLayout.addView(mRadioGroup);

                View topLine = new View(optionsLayout.getContext());
                mRadioGroup.addView(topLine);
                topLine.getLayoutParams().height = (int)(mDensity * 1 + 0.5f);
                topLine.setBackgroundColor(getResources().getColor(R.color.colorDark12));

                for (int i = 0; i < options.length(); i++) {
                    RadioButton radioButton = new RadioButton(mRadioGroup.getContext());
                    radioButton.setId(options.getJSONObject(i).getInt("value"));
                    radioButton.setText(options.getJSONObject(i).getString("item"));

                    if (result.length() > 0) {
                        if (options.getJSONObject(i).getInt("value") == result.getInt(0))
                            radioButton.setChecked(true);
                    }

                    mRadioGroup.addView(radioButton);

                    int _16dp = (int)(mDensity * 16 + 0.5f);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(_16dp, _16dp/2, _16dp, _16dp/2);
                    radioButton.setLayoutParams(layoutParams);
                    radioButton.setPadding(_16dp/2, 0, _16dp/2, 0);
                    radioButton.setTextSize(18f);

                    View bottomLine = new View(optionsLayout.getContext());
                    mRadioGroup.addView(bottomLine);
                    bottomLine.getLayoutParams().height = (int)(mDensity * 1 + 0.5f);
                    bottomLine.setBackgroundColor(getResources().getColor(R.color.colorDark12));
                }
            }
            else if (mUnit.getJSONObject("content").getString("type").equalsIgnoreCase("multi")) {
                View topLine = new View(optionsLayout.getContext());
                optionsLayout.addView(topLine);
                topLine.getLayoutParams().height = (int)(mDensity * 1 + 0.5f);
                topLine.setBackgroundColor(getResources().getColor(R.color.colorDark12));

                for (int i = 0; i < options.length(); i++) {
                    CheckBox checkBox = new CheckBox(optionsLayout.getContext());
                    checkBox.setId(options.getJSONObject(i).getInt("value"));
                    checkBox.setText(options.getJSONObject(i).getString("item"));

                    for (int j = 0; j < result.length(); j++) {
                        if (options.getJSONObject(i).getInt("value") == result.getInt(j))
                            checkBox.setChecked(true);
                    }

                    optionsLayout.addView(checkBox);
                    mCheckBoxList.add(checkBox);

                    int _16dp = (int)(mDensity * 16 + 0.5f);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(_16dp, _16dp/2, _16dp, _16dp/2);
                    checkBox.setLayoutParams(layoutParams);
                    checkBox.setPadding(_16dp/2, 0, _16dp/2, 0);
                    checkBox.setTextSize(18f);

                    View bottomLine = new View(optionsLayout.getContext());
                    optionsLayout.addView(bottomLine);
                    bottomLine.getLayoutParams().height = (int)(mDensity * 1 + 0.5f);
                    bottomLine.setBackgroundColor(getResources().getColor(R.color.colorDark12));
                }
            }

            startInterval();
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void sendResult() {
        try {
            JSONArray data = new JSONArray();

            if (mUnit.getJSONObject("content").getString("type").equalsIgnoreCase("single")) {
                if (mRadioGroup.getCheckedRadioButtonId() != -1)
                    data.put(mRadioGroup.getCheckedRadioButtonId());
            } else if (mUnit.getJSONObject("content").getString("type").equalsIgnoreCase("multi")) {
                for(CheckBox checkBox : mCheckBoxList) {
                    if (checkBox.isChecked()) {
                        data.put(checkBox.getId());
                    }
                }
            }

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
