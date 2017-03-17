package marxtseng.a1knowlearner;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import marxtseng.a1knowlearner.common.Utility;
import marxtseng.a1knowlearner.course.CourseFragment;
import marxtseng.a1knowlearner.task.TaskFragment;

public class MainActivity extends AppCompatActivity
        implements View.OnClickListener {

    private String TAG = "MainActivity";
    private String LOGOUT_URL = "https://auth.ischool.com.tw/logout.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fab).setOnClickListener(this);

        checkCookie();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout)
            logout();

        return super.onOptionsItemSelected(item);
    }

    private AlertDialog mAlertDialog;
    private BarcodeDetector mBarcodeDetector;
    private CameraSource mCameraSource;
    private SurfaceView mCameraView;

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.fab) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == -1) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
                return;
            }

            final View view = getLayoutInflater().inflate(R.layout.dialog_qrcode, null);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(view)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            mCameraSource.release();
                            mBarcodeDetector.release();
                        }
                    });
            mAlertDialog = builder.create();
            mAlertDialog.show();

            mCameraView = (SurfaceView) view.findViewById(R.id.surface_view);
            mCameraView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mCameraView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mCameraView.getLayoutParams().height = mCameraView.getWidth();
                }
            });

            mBarcodeDetector = new BarcodeDetector.Builder(this)
                    .setBarcodeFormats(Barcode.ALL_FORMATS)
                    .build();

            mCameraSource = new CameraSource.Builder(this, mBarcodeDetector)
                    .setRequestedPreviewSize(1080, 1920)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(15.0f)
                    .build();

            mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {
                        mCameraSource.start(mCameraView.getHolder());
                    } catch (IOException e) {
                        Log.d(TAG, e.getMessage());
                    } catch (SecurityException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    mCameraSource.stop();
                }
            });

            mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {

                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                    if (barcodes.size() != 0) {
                        String url = barcodes.valueAt(0).displayValue;

                        Pattern pattern = Pattern.compile("^.*?1know(?:.net|.com|.org)/#!/learn/unit/?([a-z0-9_-]+)");
                        Matcher matcher = pattern.matcher(url);

                        if (matcher.matches()){
                            String uqid = matcher.group(1);
                            mAlertDialog.dismiss();

                            Intent intent = new Intent(getApplicationContext(), UnitActivity.class);
                            intent.putExtra("UQID", uqid);
                            startActivity(intent);
                        }
                    }
                }
            });
        }
    }

    private void checkCookie() {
        if (!getSharedPreferences("COOKIE_INFO", 0).getBoolean("HAS_COOKIE", false)) {
            login();
        } else
            startup();
    }

    private void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void logout() {
        WebView webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDefaultTextEncodingName("utf-8");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.equalsIgnoreCase("https://auth.ischool.com.tw/logout.php")) {
                    Utility.COOKIE = "";
                    getSharedPreferences("COOKIE_INFO", 0).edit().clear().commit();
                    finish();
                }
            }
        });
        webView.loadUrl(LOGOUT_URL);
    }

    private void startup() {
        Utility.COOKIE = getSharedPreferences("COOKIE_INFO", 0).getString("COOKIE", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.course));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.task));

        PagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        CourseFragment courseFragment = new CourseFragment();
                        return courseFragment;
                    case 1:
                        TaskFragment taskFragment = new TaskFragment();
                        return taskFragment;
                    default:
                        return null;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };

        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }
}
