package marxtseng.a1knowlearner.unit;

import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import marxtseng.a1knowlearner.R;
import marxtseng.a1knowlearner.common.JsonObjectRequestCallBack;
import marxtseng.a1knowlearner.common.Utility;

/**
 * Created by marx on 06/02/2017.
 */

public class UnitFragment extends Fragment {

    protected String TAG = "UnitFragment";

    protected JSONObject mUnit;
    protected JSONObject mHistory = null;

    protected Timer mTimer;
    protected long mLastTime;

    public UnitFragment() {}

    protected void setUnit(String uqid) {
        Utility.GetUnit(getContext(), uqid, new JsonObjectRequestCallBack() {
            @Override
            public void OnSuccess(JSONObject response) {
                mUnit = response;

                try {
                    switch (response.getString("unit_type")) {
                        case "video": TAG = "VideoFragment"; break;
                        case "web": TAG = "WebFragment"; break;
                        case "quiz": TAG = "QuizFragment"; break;
                        case "exam": TAG = "ExamFragment"; break;
                        case "qa": TAG = "QAFragment"; break;
                        case "poll": TAG = "PollFragment"; break;
                        case "embed": TAG = "EmbedFragment"; break;
                        case "draw": TAG = "DrawFragment"; break;
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }

                initFragment();
            }

            @Override
            public void OnError(VolleyError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    protected void initFragment() {
        Log.d(TAG, "initFragment - " + mUnit.toString());
    }

    public void ShowDescription() {
        try {
            if (!mUnit.getString("description").equalsIgnoreCase("")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(Html.fromHtml(mUnit.getString("description")))
                        .setTitle(mUnit.getString("name"));
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void SendDone() {
        try {
            switch(mUnit.getString("unit_type")) {
                case "video": sendDone(); break;
                case "web": sendDone(); break;
                case "quiz": sendResult(); break;
                case "exam": sendResult(); break;
                case "qa": sendResult(); break;
                case "poll": sendResult(); break;
                case "embed": sendDone(); break;
                case "draw": sendResult(); break;
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    protected void sendDone() {
        try {
            Utility.SendDone(getContext(), mUnit.getString("uqid"), new JsonObjectRequestCallBack() {
                @Override
                public void OnSuccess(JSONObject response) {
                    Log.d(TAG, "SendDone - " + response.toString());
                    Toast.makeText(getActivity(), R.string.i_am_done, Toast.LENGTH_SHORT).show();
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

    protected void sendResult() {
        Log.d(TAG, "Send Result!");
    }

    protected void startInterval() {
        int gained = 10000;

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mLastTime = (new Date()).getTime();

                if (mHistory == null)
                    addHistory();
                else
                    updateHistory(10000);
            }
        }, 0, gained);
    }

    protected void addHistory() {
        try {
            Utility.AddHistory(getContext(), mUnit.getString("uqid"), null, new JsonObjectRequestCallBack() {
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

    protected void updateHistory(int gained) {
        if (mHistory == null) return;

        try {
            JSONObject request = new JSONObject();
            request.put("gained", (gained / 1000));

            Utility.UpdateHistory(getContext(), mHistory.getJSONObject("history").getString("uqid"), request, new JsonObjectRequestCallBack() {
                @Override
                public void OnSuccess(JSONObject response) {
                    Log.d(TAG, "UpdateHistory - " + response.toString());
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
