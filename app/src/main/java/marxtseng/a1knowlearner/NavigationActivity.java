package marxtseng.a1knowlearner;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import marxtseng.a1knowlearner.common.JsonArrayRequestCallBack;
import marxtseng.a1knowlearner.common.Utility;
import marxtseng.a1knowlearner.unit.UnitFactory;
import marxtseng.a1knowlearner.unit.UnitFragment;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String TAG = "NavigationActivity";

    private NavigationView mNavigationView;
    private MenuItem mPreviousMenuItem;
    private UnitFragment mPreviousUnitFragment;

    private JSONArray mUnits;
    private JSONObject mPreviousUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
        }

        startup();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_navigation_unit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_description)
            mPreviousUnitFragment.ShowDescription();
        else if (id == R.id.action_send) {
            mPreviousUnitFragment.SendDone();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        try {
            item.setCheckable(true);
            item.setChecked(true);

            if (mPreviousMenuItem != null)
                mPreviousMenuItem.setChecked(false);

            mPreviousMenuItem = item;
            mPreviousUnit = mUnits.getJSONObject(item.getItemId());

            Map<String, String> map = new HashMap<>();
            map.put("uqid", mPreviousUnit.getString("uqid"));
            map.put("unit_type", mPreviousUnit.getString("unit_type"));

            mPreviousUnitFragment = UnitFactory.createInstance(map);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.unit_container, mPreviousUnitFragment)
                    .commit();

            getSupportActionBar().setTitle(item.getTitle());
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getSupportActionBar().hide();
        } else {
            getSupportActionBar().show();
        }

        super.onConfigurationChanged(newConfig);
    }

    private void startup() {
        final Bundle bundle = getIntent().getBundleExtra("COURSE_TASK_ITEM");
        String logoUqid = bundle.getString("uqid");

        getSupportActionBar().setTitle(bundle.getString("name"));

        final View headerView = mNavigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.nav_progress)).setText(bundle.getString("done_units") + " / " + bundle.getString("total_units"));
        ((TextView) headerView.findViewById(R.id.nav_title)).setText(bundle.getString("name"));

        if (bundle.getString("type").equalsIgnoreCase("task")) {
            headerView.findViewById(R.id.nav_sub_title).setVisibility(View.VISIBLE);
            ((TextView) headerView.findViewById(R.id.nav_sub_title)).setText(bundle.getString("group_name"));
            logoUqid = bundle.getString("group_uqid");
        }

        Utility.SetImageView(((ImageView) headerView.findViewById(R.id.nav_logo)), bundle.getString("type"), logoUqid, true, 8);

        Utility.GetUnits(this, bundle.getString("type").toLowerCase(), bundle.getString("uqid"), new JsonArrayRequestCallBack() {
            @Override
            public void OnSuccess(JSONArray response) {
                mUnits = response;

                try {
                    String topic = "";
                    Menu parentMenu = mNavigationView.getMenu();

                    MenuItem lastItem = null;
                    JSONObject lastUnit = mUnits.getJSONObject(0);

                    for (int i = 0; i < mUnits.length(); i++) {
                        final JSONObject unit = mUnits.getJSONObject(i);

                        if (bundle.getString("type").equalsIgnoreCase("course")) {
                            if (!unit.getString("topic").equalsIgnoreCase(topic)) {
                                topic = unit.getString("topic");
                                parentMenu = mNavigationView.getMenu().addSubMenu(topic);
                            }
                        }

                        MenuItem item = parentMenu.add(Menu.NONE, i, Menu.NONE, unit.getString("name"));

                        switch (unit.getString("unit_type")) {
                            case "video":
                                item.setIcon(R.drawable.ic_unit_video);
                                break;
                            case "web":
                                item.setIcon(R.drawable.ic_unit_web);
                                break;
                            case "embed":
                                item.setIcon(R.drawable.ic_unit_embed);
                                break;
                            case "quiz":
                                item.setIcon(R.drawable.ic_unit_quiz);
                                break;
                            case "exam":
                                item.setIcon(R.drawable.ic_unit_exam);
                                break;
                            case "qa":
                                item.setIcon(R.drawable.ic_unit_qa);
                                break;
                            case "poll":
                                item.setIcon(R.drawable.ic_unit_poll);
                                break;
                            case "draw":
                                item.setIcon(R.drawable.ic_unit_draw);
                                break;
                            case "gdrive-file":
                                item.setIcon(R.drawable.ic_unit_drive);
                                break;
                        }

                        if (unit.getString("status").equalsIgnoreCase("4"))
                            item.setIcon(R.drawable.ic_status_4);
                        else if (unit.getString("status").equalsIgnoreCase("3"))
                            item.setIcon(R.drawable.ic_status_3);
                        else if (unit.getString("status").equalsIgnoreCase("2"))
                            item.setIcon(R.drawable.ic_status_2);
                        else if (unit.getString("status").equalsIgnoreCase("1"))
                            item.setIcon(R.drawable.ic_status_1);

                        if (unit.getLong("last_time") > lastUnit.getLong("last_time")) {
                            lastUnit = unit;
                            lastItem = item;
                        }

                        if (i == 0)
                            lastItem = item;
                    }

                    if (lastItem != null)
                        onNavigationItemSelected(lastItem);

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
}
