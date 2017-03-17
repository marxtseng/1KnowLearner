package marxtseng.a1knowlearner.unit;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import marxtseng.a1knowlearner.R;
import marxtseng.a1knowlearner.common.JsonObjectRequestCallBack;
import marxtseng.a1knowlearner.common.Utility;

/**
 * Created by marx on 06/02/2017.
 */

public class QuizFragment extends UnitFragment
        implements View.OnClickListener {

    private View mView;

    private float mDensity;
    private int mColorDark12;
    private int mQuizIdx;

    private MenuItem mDoneMenuItem;

    private Spinner mSpinner;
    private LinearLayout mOptionsLayout;
    private RadioGroup mRadioGroup;
    private ArrayList<CheckBox> mCheckBoxList = new ArrayList<>();

    private JSONArray mQuizzes = new JSONArray();
    private JSONArray mCorrect = new JSONArray();
    private JSONArray mResults = new JSONArray();

    public QuizFragment() {}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_unit_quiz, container, false);

        mView.findViewById(R.id.start).setOnClickListener(this);
        mView.findViewById(R.id.previous).setOnClickListener(this);
        mView.findViewById(R.id.next).setOnClickListener(this);
        mView.findViewById(R.id.confirmRestart).setOnClickListener(this);

        mSpinner = (Spinner) mView.findViewById(R.id.spinner);

        mDensity = getContext().getResources().getDisplayMetrics().density;
        mColorDark12 = getResources().getColor(R.color.colorDark12);

        setUnit(getArguments().getString("UQID"));

        return mView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mDoneMenuItem = menu.findItem(R.id.action_send);
        mDoneMenuItem.setVisible(false);
    }

    @Override
    public void onDestroy() {
        ((WebView) mView.findViewById(R.id.description)).loadData("<div></div>", "text/html", null);
        ((WebView) mView.findViewById(R.id.content)).loadData("<div></div>", "text/html", null);

        if (mTimer != null) mTimer.cancel();

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.start) {
            mView.findViewById(R.id.startLayout).setVisibility(View.GONE);
            mView.findViewById(R.id.contentLayout).setVisibility(View.VISIBLE);
            mDoneMenuItem.setVisible(true);

            startInterval();
        }
        else if (id == R.id.previous)
            previous();
        else if (id == R.id.next)
            next();
        else if (id == R.id.confirmRestart)
            confirmRestart();
    }

    @Override
    protected void initFragment() {
        try {
            WebView descriptionView = (WebView) mView.findViewById(R.id.description);
            descriptionView.getSettings().setJavaScriptEnabled(true);
            descriptionView.getSettings().setDefaultTextEncodingName("utf-8");
            descriptionView.getSettings().setTextZoom(120);
            descriptionView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    mView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;

                }
            });
            descriptionView.loadData(mUnit.getString("description"), "text/html; charset=utf-8", "utf-8");

            mQuizzes = mUnit.getJSONArray("quizzes");

            String[] items = new String[mQuizzes.length()];

            for (int i = 0; i < mQuizzes.length(); i++) {
                items[i] = i+1+"";
                mResults.put(i, new JSONArray());

                JSONArray correct = new JSONArray();
                JSONArray options = mQuizzes.getJSONObject(i).getJSONArray("options");
                for (int j = 0; j < options.length(); j++) {
                    if (options.getJSONObject(j).getBoolean("correct"))
                        correct.put(options.getJSONObject(j).getInt("value"));
                }
                mCorrect.put(i, correct);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(mView.getContext(), R.layout.spinner_dropdown_title, items);
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    jump(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            mQuizIdx = 0;
            setQuiz();
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    @Override
    protected void startInterval() {
        mLastTime = 0;
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mLastTime += 1000;

                final TextView timerView = (TextView) mView.findViewById(R.id.timer);
                timerView.post(new Runnable() {
                    @Override
                    public void run() {
                        SimpleDateFormat format = new SimpleDateFormat("mm:ss");
                        timerView.setText(format.format(mLastTime));
                    }
                });
            }
        }, 0, 1000);
    }

    @Override
    protected void sendResult() {

        mTimer.cancel();
        setResult(mQuizIdx);

        try {
            int count = 0;

            for (int i = 0; i < mResults.length(); i++) {
                if (mResults.getJSONArray(i).toString().equalsIgnoreCase(mCorrect.getJSONArray(i).toString()))
                    count += 1;
            }

            final int correct = count;

            JSONArray quizzes = new JSONArray();

            for (int i = 0; i < mQuizzes.length(); i++) {
                JSONObject quiz = new JSONObject();
                quiz.put("uqid", mQuizzes.getJSONObject(i).getString("uqid"));
                quiz.put("correct", mCorrect.getJSONArray(i));
                quiz.put("answer", mResults.getJSONArray(i));

                quizzes.put(i, quiz);
            }

            JSONObject content = new JSONObject();
            content.put("unit_type", mUnit.getString("unit_type"));
            content.put("result", quizzes);
            content.put("score", correct);
            content.put("duration", mLastTime);
            JSONObject result = new JSONObject();
            result.put("content", content.toString());

            Utility.SendResult(mView.getContext(), mUnit.getString("uqid"), result, new JsonObjectRequestCallBack() {
                @Override
                public void OnSuccess(JSONObject response) {
                    Log.d(TAG, "SendResult - " + response.toString());

                    mDoneMenuItem.setVisible(false);
                    Toast.makeText(getActivity(), getString(R.string.quiz_toast_message, correct) , Toast.LENGTH_SHORT).show();
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

    private void setQuiz() {
        try {
            JSONObject quiz = mQuizzes.getJSONObject(mQuizIdx);

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
            contentView.loadData(quiz.getString("content"), "text/html; charset=utf-8", "utf-8");

            mOptionsLayout = (LinearLayout) mView.findViewById(R.id.options);
            mOptionsLayout.removeAllViews();

            JSONArray options = quiz.getJSONArray("options");

            if (quiz.getString("quiz_type").equalsIgnoreCase("single")) {
                mRadioGroup = new RadioGroup(mOptionsLayout.getContext());
                mOptionsLayout.addView(mRadioGroup);

                View topLine = new View(mOptionsLayout.getContext());
                mRadioGroup.addView(topLine);
                topLine.getLayoutParams().height = (int)(mDensity * 1 + 0.5f);
                topLine.setBackgroundColor(mColorDark12);

                for (int i = 0; i < options.length(); i++) {
                    RadioButton radioButton = new RadioButton(mRadioGroup.getContext());
                    radioButton.setId(options.getJSONObject(i).getInt("value"));
                    radioButton.setText(options.getJSONObject(i).getString("item"));

                    if (mResults.getJSONArray(mQuizIdx).length() > 0) {
                        if (options.getJSONObject(i).getInt("value") == mResults.getJSONArray(mQuizIdx).getInt(0))
                            radioButton.setChecked(true);
                    }

                    mRadioGroup.addView(radioButton);

                    int _16dp = (int)(mDensity * 16 + 0.5f);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(_16dp, _16dp/2, _16dp, _16dp/2);
                    radioButton.setLayoutParams(layoutParams);
                    radioButton.setPadding(_16dp/2, 0, _16dp/2, 0);
                    radioButton.setTextSize(18f);

                    View bottomLine = new View(mOptionsLayout.getContext());
                    mRadioGroup.addView(bottomLine);
                    bottomLine.getLayoutParams().height = (int)(mDensity * 1 + 0.5f);
                    bottomLine.setBackgroundColor(mColorDark12);
                }
            }
            else if (quiz.getString("quiz_type").equalsIgnoreCase("multi")) {
                mCheckBoxList.clear();

                View topLine = new View(mOptionsLayout.getContext());
                mOptionsLayout.addView(topLine);
                topLine.getLayoutParams().height = (int)(mDensity * 1 + 0.5f);
                topLine.setBackgroundColor(mColorDark12);

                for (int i = 0; i < options.length(); i++) {
                    CheckBox checkBox = new CheckBox(mOptionsLayout.getContext());
                    checkBox.setId(options.getJSONObject(i).getInt("value"));
                    checkBox.setText(options.getJSONObject(i).getString("item"));

                    for (int j = 0; j < mResults.getJSONArray(mQuizIdx).length(); j++) {
                        if (options.getJSONObject(i).getInt("value") == mResults.getJSONArray(mQuizIdx).getInt(j))
                            checkBox.setChecked(true);
                    }

                    mOptionsLayout.addView(checkBox);
                    mCheckBoxList.add(checkBox);

                    int _16dp = (int)(mDensity * 16 + 0.5f);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(_16dp, _16dp/2, _16dp, _16dp/2);
                    checkBox.setLayoutParams(layoutParams);
                    checkBox.setPadding(_16dp/2, 0, _16dp/2, 0);
                    checkBox.setTextSize(18f);

                    View bottomLine = new View(mOptionsLayout.getContext());
                    mOptionsLayout.addView(bottomLine);
                    bottomLine.getLayoutParams().height = (int)(mDensity * 1 + 0.5f);
                    bottomLine.setBackgroundColor(mColorDark12);
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void setResult(int quizIdx) {
        if (quizIdx == -1) {
            mOptionsLayout.removeAllViews();
            return;
        }

        try {
            String quiz_type = mQuizzes.getJSONObject(quizIdx).getString("quiz_type");
            JSONArray result = new JSONArray();

            if (quiz_type.equalsIgnoreCase("single")) {
                if (mRadioGroup.getCheckedRadioButtonId() != -1)
                    result.put(mRadioGroup.getCheckedRadioButtonId());
            } else if (quiz_type.equalsIgnoreCase("multi")) {
                for(CheckBox checkBox : mCheckBoxList) {
                    if (checkBox.isChecked()) {
                        result.put(checkBox.getId());
                    }
                }
            }

            mResults.put(quizIdx, result);
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void previous() {
        int idx = (mQuizIdx - 1 >= 0) ? mQuizIdx - 1 : mQuizIdx;
        if (idx != mQuizIdx) mSpinner.setSelection(idx);
    }

    private void next() {
        int idx = (mQuizIdx + 1 < mQuizzes.length()) ? mQuizIdx + 1 : mQuizIdx;
        if (idx != mQuizIdx) mSpinner.setSelection(idx);
    }

    private void jump(int position) {
        setResult(mQuizIdx);
        mQuizIdx = position;
        setQuiz();
    }

    private void confirmRestart() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.quiz_restart_message)
                .setPositiveButton(R.string.quiz_restart_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restart();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.quiz_restart_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private void restart() {
        mDoneMenuItem.setVisible(true);

        mTimer.cancel();
        startInterval();

        try {
            mQuizIdx = -1;
            mResults = new JSONArray();
            for (int i = 0; i < mCorrect.length(); i++) {
                mResults.put(i, new JSONArray());
            }

            mSpinner.setSelection(0);
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
