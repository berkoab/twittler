package com.berko.tweetsaroundyou;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebActivity extends Activity {
	private WebView webview;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_web);
		
//		TapForTap.initialize(this, getString(R.string.tapfortap_id));
		Bundle b = getIntent().getExtras(); 
	    String name = b.getString("SCREEN_NAME");  
	    
	    webview = (WebView) findViewById(R.id.web);
	    webview.getSettings().setJavaScriptEnabled(true);
	    webview.getSettings().setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.0; en-us; Droid Build/ESD20) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
	    
	    final Activity activity = this;
	    webview.setWebChromeClient(new WebChromeClient() {
	      @Override
		public void onProgressChanged(WebView view, int progress) {
	        // Activities and WebViews measure progress with different scales.
	        // The progress meter will automatically disappear when we reach 100%
	        
	        activity.setTitle(getString(R.string.loading));
            activity.setProgress(progress * 100);

            if(progress == 100)
            	
                activity.setTitle(R.string.app_name);
	      }
	      
	    });
	    webview.setWebViewClient(new WebViewClient() {
	      @Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
	        Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
	      }
	    });
	    
	    String url = "";
	    try {
			url = "https://mobile.twitter.com/search/realtime?q=" + URLEncoder.encode("from:"+name, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    webview.loadUrl(url);

	}
}

