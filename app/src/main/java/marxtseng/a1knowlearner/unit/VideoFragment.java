package marxtseng.a1knowlearner.unit;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import marxtseng.a1knowlearner.R;
import marxtseng.a1knowlearner.common.JsonObjectRequestCallBack;
import marxtseng.a1knowlearner.common.Utility;

/**
 * Created by marx on 06/02/2017.
 */

public class VideoFragment extends UnitFragment {

    private View mView;

    private JSONObject mHistory = null;
    private ArrayList<CheckBox> mCheckBoxList = new ArrayList<>();
    private RadioGroup mRadioGroup;

    public VideoFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_unit_video, container, false);

        setUnit(getArguments().getString("UQID"));

        return mView;
    }

    @Override
    public void onDestroy() {
        WebView webView = (WebView) mView.findViewById(R.id.content);

        webView.loadUrl("javascript:video.updateHistory()");
        webView.loadData("<div></div>", "text/html", null);

        super.onDestroy();
    }

    @Override
    protected void initFragment() {
        WebView webView = (WebView) mView.findViewById(R.id.content);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.addJavascriptInterface(this, "android");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("video.html", consoleMessage.message() + " -- From line "
                        + consoleMessage.lineNumber() + " of "
                        + consoleMessage.sourceId() );
                return true;
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                return true;

            }
        });
        webView.loadUrl("file:///android_res/raw/video.html");
    }

    @JavascriptInterface
    public String getUnit() {
        return mUnit.toString();
    }

    @JavascriptInterface
    public void addHistory(String video_time) {
        try {
            JSONObject request = new JSONObject();
            request.put("video_time", video_time);

            Utility.AddHistory(mView.getContext(), mUnit.getString("uqid"), request, new JsonObjectRequestCallBack() {
                @Override
                public void OnSuccess(JSONObject response) {
                    Log.d(TAG, "AddHistory - " + response.toString());
                    mHistory = response;
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

    @JavascriptInterface
    public void updateHistory(String video_time, String gained) {
        if (mHistory == null) return;

        try {
            JSONObject request = new JSONObject();
            request.put("video_time", video_time);
            request.put("gained", gained);

            Utility.UpdateHistory(mView.getContext(), mHistory.getJSONObject("history").getString("uqid"), request, new JsonObjectRequestCallBack() {
                @Override
                public void OnSuccess(JSONObject response) {
                    Log.d(TAG, "UpdateHistory - " + response.toString());
                }

                @Override
                public void OnError(VolleyError error) {
                    Log.d(TAG, "VolleyError - " + error.toString());
                }
            });
        } catch (JSONException e) {
            Log.d(TAG, "JSONException - " + e.toString());
        }
    }

    @JavascriptInterface
    public void showPopupQuiz(String data) {
        try {
            if (mRadioGroup != null)
                mRadioGroup.removeAllViews();
            mCheckBoxList.clear();

            final JSONObject quiz = new JSONObject(data);
            final JSONArray options = quiz.getJSONArray("options");

            final ArrayList correct = new ArrayList();
            final ArrayList answer = new ArrayList();

            for (int i = 0; i < options.length(); i++) {
                if (options.getJSONObject(i).getBoolean("correct"))
                    correct.add(options.getJSONObject(i).getInt("value"));
            }

            LayoutInflater inflater = getActivity().getLayoutInflater();

            View view = inflater.inflate(R.layout.dialog_video_quiz, null);
            ((TextView)view.findViewById(R.id.content)).setText(Html.fromHtml(quiz.getString("content")));

            LinearLayout optionsLayout = (LinearLayout) view.findViewById(R.id.options);

            if (quiz.getString("quiz_type").equalsIgnoreCase("single")) {
                mRadioGroup = new RadioGroup(optionsLayout.getContext());
                optionsLayout.addView(mRadioGroup);

                for (int i = 0; i < options.length(); i++) {
                    RadioButton radioButton = new RadioButton(mRadioGroup.getContext());
                    radioButton.setId(options.getJSONObject(i).getInt("value"));
                    radioButton.setText(options.getJSONObject(i).getString("item"));

                    int padding = (int) (getResources().getDisplayMetrics().density * 8 + 0.5f);
                    radioButton.setPadding(0, padding, 0, padding);

                    mRadioGroup.addView(radioButton);
                }
            }
            else if (quiz.getString("quiz_type").equalsIgnoreCase("multi")) {
                for (int i = 0; i < options.length(); i++) {
                    CheckBox checkBox = new CheckBox(optionsLayout.getContext());
                    checkBox.setId(options.getJSONObject(i).getInt("value"));
                    checkBox.setText(options.getJSONObject(i).getString("item"));

                    int padding = (int) (getResources().getDisplayMetrics().density * 8 + 0.5f);
                    checkBox.setPadding(0, padding, 0, padding);

                    optionsLayout.addView(checkBox);
                    mCheckBoxList.add(checkBox);
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(view).setPositiveButton(R.string.popup_quiz_submit, null);

            final AlertDialog dialog = builder.create();
            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface d) {
                    Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            answer.clear();

                            try {
                                if (quiz.getString("quiz_type").equalsIgnoreCase("single"))
                                    answer.add(mRadioGroup.getCheckedRadioButtonId());
                                else if (quiz.getString("quiz_type").equalsIgnoreCase("multi")) {
                                    for(CheckBox checkBox : mCheckBoxList) {
                                        if (checkBox.isChecked()) {
                                            answer.add(checkBox.getId());
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                Log.d(TAG, "JSONException - " + e.toString());
                            }

                            Log.d(TAG, "correct - " + correct.toString());
                            Log.d(TAG, "answer - " + answer.toString());

                            if (correct.toString().equalsIgnoreCase(answer.toString())) {
                                dialog.dismiss();
                                Toast.makeText(getActivity(), R.string.popup_quiz_correct, Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(getActivity(), R.string.popup_quiz_wrong, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
            dialog.show();
        } catch (JSONException e) {
            Log.d(TAG, "JSONException - " + e.toString());
        }
    }
}
