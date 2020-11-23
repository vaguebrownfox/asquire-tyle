package aashi.fiaxco.asquiretyle0x0a;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import aashi.fiaxco.asquiretyle0x0a.providestuff.SurveyWebView;

public class SurveyActivity extends AppCompatActivity {

	// Constants
	private static final String TAG = "SurveyActivity";
	public static final String TIMESTAMP = "timestampSurveyForm";

	// UI
	private Button mStartRecButton;
	private WebView mWebView;

	// Data
	private String mUserID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_survey);

		// Get Data from Previous Activity
		Intent pIntent = getIntent();
		mUserID = pIntent.getStringExtra(UserIDActivity.USER_ID);

		// Find UI
		{
			mStartRecButton = findViewById(R.id.next_button);
			mWebView = findViewById(R.id.survey_activity_webview);
		}

		// Setup UI - Click, View
		setupUIFunction();
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void setupUIFunction() {
		// Next Button
		mStartRecButton.setOnClickListener(view -> {
			Intent intent = new Intent(SurveyActivity.this, UserIDActivity.class);
			setResult(Activity.RESULT_OK, intent);
			finish();
		});

		// WebView
		WebSettings webSettings = mWebView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		mWebView.setWebViewClient(new SurveyWebView());

		// REMOTE RESOURCE
		mWebView.loadUrl(
				"https://docs.google.com/forms/d/e/1FAIpQLSeoU1W6Xh1wICI7hJA8ku01aHO2sPFQDqLyGVwUXA9NfEkdfg/viewform?usp=sf_link");
	}


}
