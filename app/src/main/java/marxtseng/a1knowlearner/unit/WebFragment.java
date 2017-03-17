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

import org.json.JSONException;

import java.util.Date;

import marxtseng.a1knowlearner.R;

/**
 * Created by marx on 06/02/2017.
 */

public class WebFragment extends UnitFragment {

    private View mView;

    public WebFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_unit_web, container, false);

        setUnit(getArguments().getString("UQID"));

        return mView;
    }

    @Override
    public void onDestroy() {
        ((WebView) mView.findViewById(R.id.content)).loadData("<div></div>", "text/html", null);

        if (mTimer != null) mTimer.cancel();
        updateHistory((int)((new Date()).getTime() - mLastTime)/1000);

        super.onDestroy();
    }

    @Override
    protected void initFragment() {
        try {
            final String content_url = mUnit.getString("content_url");

            WebView webView = (WebView) mView.findViewById(R.id.content);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setDefaultTextEncodingName("utf-8");
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    mView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;

                }
            });
            webView.loadUrl(content_url);

            startInterval();
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
