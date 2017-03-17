package marxtseng.a1knowlearner;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import marxtseng.a1knowlearner.common.JsonObjectRequestCallBack;
import marxtseng.a1knowlearner.common.Utility;
import marxtseng.a1knowlearner.unit.UnitFactory;
import marxtseng.a1knowlearner.unit.UnitFragment;

public class UnitActivity extends AppCompatActivity {

    private String TAG = "UnitActivity";

    private UnitFragment mUnitFragment;
    private MenuItem mDoneMenuItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit);

        String uqid = getIntent().getStringExtra("UQID");

        Utility.GetUnit(this, uqid, new JsonObjectRequestCallBack() {
            @Override
            public void OnSuccess(JSONObject response) {
                try {
                    getSupportActionBar().setTitle(response.getString("name"));

                    Map<String, String> item = new HashMap<>();
                    item.put("uqid", response.getString("uqid"));
                    item.put("unit_type", response.getString("unit_type"));

                    mUnitFragment = UnitFactory.createInstance(item);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.unit_container, mUnitFragment)
                            .commit();

                    mDoneMenuItem.setVisible(true);

//                    switch (response.getString("unit_type")) {
//                        case "video":
//                        case "web":
//                        case "embed":
//                            mDoneMenuItem.setVisible(false);
//                            break;
//                        case "quiz":
//                        case "exam":
//                        case "poll":
//                        case "qa":
//                        case "draw":
//                            if (response.getString("status").equalsIgnoreCase("3") ||
//                                    response.getString("status").equalsIgnoreCase("4")) {
//                                mDoneMenuItem.setVisible(false);
//                            }
//                            break;
//                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }

            @Override
            public void OnError(VolleyError error) {
                Log.d(TAG, error.getMessage());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_unit_menu, menu);
        mDoneMenuItem = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_description)
            mUnitFragment.ShowDescription();
        else if (id == R.id.action_send) {
            mUnitFragment.SendDone();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
            getSupportActionBar().hide();
        else
            getSupportActionBar().show();

        super.onConfigurationChanged(newConfig);
    }
}
