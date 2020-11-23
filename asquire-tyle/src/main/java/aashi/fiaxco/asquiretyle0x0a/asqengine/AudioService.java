package aashi.fiaxco.asquiretyle0x0a.asqengine;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Random;

public class AudioService extends Service {
	// Constants
	private static final String TAG = "AudioService";

	// Data
	private final AudioBinder mBinder = new AudioBinder();
	private String RecFilePath;
	private String RecFilename;
	private String mUserId;
	private static final String[] mModels = {"cough.model", "cough_1.model", "feats_0.txt"};
	private final HashMap<String, String> mModelCacheFiles = new HashMap<>();
	private String mCodeName;



	// Flags and State
	enum STATE {
		START, RSTOP, PLAY, PSTOP;
	}
	private STATE mRecState;
	private STATE mPlyState;
	private Boolean mIsRecording, mIsPlaying;
	private Boolean mRecordDone, mPlayDone;


	public AudioService() {}

	public class AudioBinder extends Binder {
		public AudioService getAudioService() {
			return AudioService.this;
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		AsqEngine.setDefaultStreamValues(this);
		clearCache();
		cacheModelFiles();
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		// Instantiate Audio Engine
		boolean e = AsqEngine.create();
		if (!e) {
			makeToast("Audio engine error, restart App");
			stopSelf();
		}

		// Init Flags and State
		initFlagsAndState();

	}

	private void initFlagsAndState() {
		mRecState  = STATE.START;
		mPlyState = STATE.PLAY;

		mIsRecording = false;
		mIsPlaying = false;
		mRecordDone = false;
		mPlayDone = false;

		Field[] fields = Build.VERSION_CODES.class.getFields();
		String codeName = "UNKNOWN";
		for (Field field : fields) {
			try {
				if (field.getInt(Build.VERSION_CODES.class) == Build.VERSION.SDK_INT) {
					codeName = field.getName();
				}
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		mCodeName = codeName;
	}

	public void recFunction() {
		switch (mRecState) {
			case START:
				mIsRecording = true;
				mRecordDone = false;
				startRecord();
				break;
			case RSTOP:
				mIsRecording = false;
				stopRecord();
				mRecordDone = true;
				break;
			default:
				break;
		}
	}

	public void plyFunction() {
		switch (mPlyState) {
			case PLAY:
				mIsPlaying = true;
				mPlayDone = false;
				startPlaying();
				break;
			case PSTOP:
				mIsPlaying = false;
				stopPlaying();
				mPlayDone = true;
				break;
			default:
				break;
		}
	}

	private void startRecord() {
		Log.d(TAG, "Attempting to start recording");

		{ // Filename generation
			Random rand = new Random();
			int id = rand.nextInt(2000);
			RecFilename = mUserId + "_"
					+ mCodeName + "_"
					+ Build.MANUFACTURER + "_"
					+ Build.BRAND + "_" + id + ".wav";
		}

		// Create File
		File recFile = new File(getCacheDir(), RecFilename);
		RecFilePath = recFile.getAbsolutePath();

		// Set Record file path and start recording
		AsqEngine.setRecFilePath(recFile.getAbsolutePath());
		Thread thread = new Thread(() -> AsqEngine.setRecOn(true));
		thread.start();

		// Update Next State
		mRecState = STATE.RSTOP;
	}

	private void stopRecord() {
		Log.d(TAG, "Attempting to stop recording");

		boolean res = AsqEngine.setRecOn(false);
		mRecState = STATE.START;
	}


	private void startPlaying() {
		Log.d(TAG, "Attempting to start playing");

		Thread thread = new Thread(() -> AsqEngine.setPlyOn(true));
		thread.start();

		// Update Next State
		mPlyState = STATE.PSTOP;
	}

	private void stopPlaying() {
		Log.d(TAG, "Attempting to stop playing");

		boolean res = AsqEngine.setPlyOn(false);
		mPlyState = STATE.PLAY;
	}


	// Is, Sets, Gets

	public Boolean isRecording() {
		return mIsRecording;
	}

	public Boolean isRecordDone() {
		return mRecordDone;
	}

	public Boolean isPlaying() {
		return mIsPlaying;
	}

	public Boolean isPlayDone() {
		return mPlayDone;
	}

	public String getRecFilename() {
		return RecFilename;
	}

	public String getRecFilePath() {
		return RecFilePath;
	}

	public void setUserId(String mUserId) {
		this.mUserId = mUserId;
	}

	public void setIsRecordingDone(boolean b) {
		this.mRecordDone = b;
	}


	@Override
	public void onDestroy() {
		AsqEngine.setRecOn(false);
		AsqEngine.setPlyOn(false);
		AsqEngine.delete();
		super.onDestroy();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);

		AsqEngine.setRecOn(false);
		AsqEngine.setPlyOn(false);
		AsqEngine.delete();

		stopSelf();

		clearCache();
	}

	// Misc
	private void cacheModelFiles() {
		AssetManager asquireAssets = getAssets();
		for (String model : mModels) {
			try {

				InputStream inputStream = asquireAssets.open(model);
				int size = inputStream.available();
				byte[] buffer = new byte[size];
				int n = inputStream.read(buffer);
				inputStream.close();
				Log.d(TAG, "onCreate: asset ip stream - " + n);

				File modelCacheFile = new File(getCacheDir(), model);
				FileOutputStream fos = new FileOutputStream(modelCacheFile);
				fos.write(buffer);
				fos.close();

				mModelCacheFiles.put(model, modelCacheFile.getAbsolutePath());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void clearCache() {
		File[] files = getCacheDir().listFiles();
		if (files != null) {
			for (File f : files) {
				boolean res = f.delete();
				if (res) Log.d(TAG, "onStart: deleted file " + f.getName());
			}
		}
	}
	private void makeToast(String msg) {
		Toast.makeText(AudioService.this, msg, Toast.LENGTH_SHORT).show();
	}
}