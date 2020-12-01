package aashi.fiaxco.asquiretyle0x0a;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import aashi.fiaxco.asquiretyle0x0a.asqengine.AsqEngine;
import aashi.fiaxco.asquiretyle0x0a.asqengine.AsqViewModel;
import aashi.fiaxco.asquiretyle0x0a.asqengine.AudioService;
import aashi.fiaxco.asquiretyle0x0a.asqengine.UploadService;
import aashi.fiaxco.asquiretyle0x0a.providestuff.Stimulus;
import aashi.fiaxco.asquiretyle0x0a.providestuff.Timer;

public class RecordActivity extends AppCompatActivity implements Timer.MessageConstants {

	// Constants
	private static final String TAG = "RecordActivity";
	private static final int AUDIO_EFFECT_REQUEST = 999;

	// UI
	private ImageButton mCloseButton, mInfoButton;
	private TextView mStimuliDescriptionTv, mResultTv, mNAsthmaTv, mHAsthmaTv, mTimerTv;
	private Button mRecordButton, mPlayButton, mNextButton;
	private SwitchCompat mAssessSwitch, mDarkModeSwitch;
	private ProgressBar mAsthmaProgress;

	// Data
	private String mUserID;
	private String mUserTimeStamp;
	private AsqViewModel mAsqViewModel;
	private String[] mStimulus;
	private int mNStimuli;
	private long mRecDuration;

	// Services
	private AudioService mAudService;
	private UploadService mUploadService;
	private Timer mTimer;
	private Timer.TimerHandler mTimerHandler;
	private FirebaseAnalytics mFirebaseAnalytics;


	// Boolean Flags


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_AsquireTyle0x0A);
		setContentView(R.layout.activity_record);

		// Get Data from Previous Activity
		Intent pIntent = getIntent();
		mUserID = pIntent.getStringExtra(UserIDActivity.USER_ID);
		mUserTimeStamp = pIntent.getStringExtra(SurveyActivity.TIMESTAMP);

		// Find View
		{
			mCloseButton = findViewById(R.id.record_close_button);
			mInfoButton = findViewById(R.id.record_info);
			mStimuliDescriptionTv = findViewById(R.id.record_task_descp_tv);
			mResultTv = findViewById(R.id.record_prediction_result_tv);
			mNAsthmaTv = findViewById(R.id.record_low_pred_tv);
			mHAsthmaTv = findViewById(R.id.record_high_pred_tv);
			mTimerTv = findViewById(R.id.record_timer_tv);
			mRecordButton = findViewById(R.id.control_button_record);
			mPlayButton = findViewById(R.id.control_button_play);
			mNextButton = findViewById(R.id.control_button_next);
			mAssessSwitch = findViewById(R.id.record_en_pred_switch);
			mAsthmaProgress = findViewById(R.id.record_asthma_progressBar);
			mDarkModeSwitch = findViewById(R.id.dark_mode_switch);
		}

		// Initialize Data
		initVarData();

		// Initialize UI
		initUIValues();

		// View Model Stuff
		setViewModelObservers();

		// Set On Click Listeners
		setOnClickListeners();

	}

	@Override
	protected void onResume() {
		super.onResume();
		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		startServices();
	}
	@Override
	protected void onPause() {
		unbindService(mAsqViewModel.getAudioServiceConnection());
		unbindService(mAsqViewModel.getUploadServiceConnection());
		stopServices();
		finish();
		super.onPause();
	}

	// On Create Methods
	private void initVarData() {
		// Firebase
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
		mFirebaseAnalytics.setUserId(mUserID);
		// Stimulus Description Array
		mStimulus = Stimulus.getStimulus();
		// Timer
		mTimer = new Timer();
		mTimerHandler = new Timer.TimerHandler(this, mTimer);
	}

	private void initUIValues() {
		mNStimuli = 0;
		mStimuliDescriptionTv.setText(mStimulus[0]);
	}

	private void setViewModelObservers() {
		// Init View model
		mAsqViewModel = ViewModelProviders.of(this).get(AsqViewModel.class);

		// Observers on live data
		// Audio Service Binder
		mAsqViewModel.getAudBinder().observe(this, audioBinder -> {
			if (audioBinder != null) {
				mAudService = audioBinder.getAudioService();
				Log.d(TAG, "onCreate: Connected to Audio Service");
			} else {
				mAudService = null;
				Log.d(TAG, "onCreate: Unbound from the service");
			}
		});

		// Audio Service Live Variables
		// Recording
		mAsqViewModel.getIsRecording().observe(this, isRec -> {
			mTimerTv.setTextColor(getResources().getColor(R.color.red_200));
			mTimerHandler.sendEmptyMessage(isRec ? MSG_START_TIMER : MSG_STOP_TIMER);
			if (isRec) {

				mNextButton.setEnabled(false);
				mPlayButton.setEnabled(false);
				mRecordButton.setEnabled(false);
				mRecordButton.postDelayed(() -> mRecordButton.setEnabled(true), 1000);

			} else {
				firebaseRecSelect();
				mRecDuration = mTimer.getElapsedTime();
				mRecordButton.setEnabled(false);
				mRecordButton.postDelayed(() -> {
//					doPredict();
					mNextButton.setEnabled(true);
					mPlayButton.setEnabled(true);
					mRecordButton.setEnabled(true);
				}, 1000);

			}
			mRecordButton.setText(isRec ? R.string.stop : R.string.record);
		});

		// Playing
		mAsqViewModel.getIsPlaying().observe(this, isPly -> {
			mTimerTv.setTextColor(getResources().getColor(R.color.colorGreen));
			if (isPly) mTimerHandler.sendEmptyMessage(MSG_RESET_TIMER);
			long ms = mTimer.getElapsedTime();
			Log.d(TAG, "setViewModelObservers: LOL ms: " + ms);
			mTimerHandler.sendEmptyMessage(isPly ? MSG_START_TIMER : MSG_STOP_TIMER);
			if (isPly) {
				mNextButton.setEnabled(false);
				mRecordButton.setEnabled(false);
				mPlayButton.setEnabled(false);
				//mPlayButton.postDelayed(() -> mPlayButton.setEnabled(true), 1000);
				mPlayButton.postDelayed(this::playControl, mRecDuration);
			} else {

				mPlayButton.setEnabled(false);
				mPlayButton.postDelayed(() -> {
					mNextButton.setEnabled(true);
					mRecordButton.setEnabled(true);
					mPlayButton.setEnabled(true);
				}, 1000);

			}
			mPlayButton.setText(isPly ? R.string.playing : R.string.play);
		});


		// Upload Service Binder
		mAsqViewModel.getUpBinder().observe(this, fireBinder -> {
			if (fireBinder != null) {
				mUploadService = fireBinder.getUploadService();
				Log.d(TAG, "onCreate: Connected to Upload Service");
			} else {
				mUploadService = null;
				Log.d(TAG, "onCreate: Unbound from the service");
			}
		});
	}

	private void firebaseRecSelect()  {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, mUserID);
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "stimuli_" + mNStimuli);
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "audio");
		bundle.putString(FirebaseAnalytics.Param.SOURCE, "record_button");
		bundle.putString("record_duration_ms", String.valueOf(mRecDuration));
		mFirebaseAnalytics.logEvent("record_action", bundle);
	}

	private void firebaseNxtSelect() {
		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, mUserID);
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "stimuli_" + mNStimuli);
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "audio_upload");
		bundle.putString(FirebaseAnalytics.Param.SOURCE, "next_button");
		mFirebaseAnalytics.logEvent("upload_action", bundle);
	}

	// Button Functions
	private void setOnClickListeners() {

		// Exit Record Activity
		mCloseButton.setOnClickListener(view -> {

			Intent intent = new Intent(RecordActivity.this, UserIDActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
		});

		// View Task Info
		mInfoButton.setOnClickListener(view -> {

		});

		// Record Control
		mRecordButton.setOnClickListener(view -> {
			if (mAudService != null) {
				if (!isRecordPermissionGranted()) {
					requestRecordPermission();
					return;
				}

				// Start / Stop Recording
				mAudService.setUserId(mNStimuli + "-" + mUserID + "-" + mUserTimeStamp);
				mAudService.recFunction();

				mAsqViewModel.setIsRecording(mAudService.isRecording());

			} else {
				Log.d(TAG, "Service object is null");
			}
		});

		// Play Control
		mPlayButton.setOnClickListener(view -> {
			playControl();
		});

		// Next Control
		mNextButton.setOnClickListener(view -> {
			if (mUploadService != null && mAudService != null) {
				if (mAudService.isRecordDone()) {
					Log.d(TAG, "onCreate: Uploading started");
					mUploadService.uploadData(mAudService.getRecFilePath(), mAudService.getRecFilename());
					firebaseNxtSelect();

					mAudService.setIsRecordingDone(false);
					mResultTv.setText("");
					mAsthmaProgress.setProgress(0);

					mTimerTv.setTextColor(getResources().getColor(R.color.red_200));
					mTimerHandler.sendEmptyMessage(MSG_RESET_TIMER);

					mNStimuli = ++mNStimuli % mStimulus.length;
					mStimuliDescriptionTv.setText(mStimulus[mNStimuli]);

				} else {
					makeToast("Finish recording before next!");
				}
			} else {
				Log.d(TAG, "Service objects are null");
			}
		});

		mAssessSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
			int v = b ? View.VISIBLE : View.INVISIBLE;
			mResultTv.setVisibility(v);
			mNAsthmaTv.setVisibility(v);
			mHAsthmaTv.setVisibility(v);
			mAsthmaProgress.setVisibility(v);
		});

		mDarkModeSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
			if (b) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
			else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
		});


	}

	private void playControl() {
		if (mAudService != null) {
			if (mAudService.isRecordDone()) {
				// Start / Stop Playing
				mAudService.plyFunction();
				mAsqViewModel.setIsPlaying(mAudService.isPlaying());
			} else {
				makeToast("You have to record first");
			}
		} else {
			Log.d(TAG, "Service object is null");
		}
	}

	private void doPredict() {
		if (mAudService.isRecordDone()) {
			mResultTv.setText(R.string.analysing_msg);
			mAsthmaProgress.setIndeterminate(true);

			// Prediction result
			float op = 0.5f; //AsqEngine.asqPredict("lol", "lol"); // TODO: temp param featfile

			mResultTv.postDelayed(() -> {
				mResultTv.setText(R.string.pred_result_message);
				Log.d(TAG, "doPredict: prediction " + (1 - op / 100)); // TODO: update progress bar
				mAsthmaProgress.setIndeterminate(false);
				mAsthmaProgress.setProgress((int) op);

			}, 1000);

		} else {
			makeToast("You have to record first");
		}
	}


	// Service Functions
	private void startServices() {
		// Audio Service - Pass User ID here
		Intent audServiceIntent = new Intent(this, AudioService.class);
		startService(audServiceIntent);
		bindService(audServiceIntent, mAsqViewModel.getAudioServiceConnection(), Context.BIND_AUTO_CREATE);

		// Upload Service - Pass User ID here
		Intent upServiceIntent = new Intent(this, UploadService.class);
		startService(upServiceIntent);
		bindService(upServiceIntent, mAsqViewModel.getUploadServiceConnection(), Context.BIND_AUTO_CREATE);
	}

	private void stopServices() {
		// Audio Service - Pass User ID here
		Intent audServiceIntent = new Intent(this, AudioService.class);
		stopService(audServiceIntent);

//		// Upload Service - Pass User ID here
//		Intent upServiceIntent = new Intent(this, UploadService.class);
//		stopService(upServiceIntent);
	}

	@Override
	public void onBackPressed() {
		makeToast("Press X to eXit");
	}

	// Permissions
	private boolean isRecordPermissionGranted() {
		return (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
				PackageManager.PERMISSION_GRANTED);
	}

	private void requestRecordPermission() {
		ActivityCompat.requestPermissions(
				this,
				new String[]{Manifest.permission.RECORD_AUDIO},
				AUDIO_EFFECT_REQUEST);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (AUDIO_EFFECT_REQUEST != requestCode) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
			return;
		}

		if (grantResults.length != 1 ||
				grantResults[0] != PackageManager.PERMISSION_GRANTED) {

			makeToast(getString(R.string.need_record_audio_permission));
		}
	}


	// Misc
	private void makeToast(String msg) {
		Toast.makeText(RecordActivity.this, msg, Toast.LENGTH_SHORT).show();
	}
}