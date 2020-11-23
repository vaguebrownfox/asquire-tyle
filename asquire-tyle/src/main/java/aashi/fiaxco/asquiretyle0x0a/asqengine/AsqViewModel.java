package aashi.fiaxco.asquiretyle0x0a.asqengine;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AsqViewModel extends ViewModel {
	private static final String TAG = "AsqViewModel";

	/* -------------------------- Audio Service  -------------------------- */
	// Audio Service States
	private final MutableLiveData<Boolean> mIsRecording = new MutableLiveData<>();
	private final MutableLiveData<Boolean> mIsPlaying = new MutableLiveData<>();

	// Audio Service Binder
	private final MutableLiveData<AudioService.AudioBinder> mAudBinder = new MutableLiveData<>();

	// Audio Service Connection
	private final  ServiceConnection audioServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			Log.d(TAG, "onServiceConnected: Connected to audio service");
			AudioService.AudioBinder binder = (AudioService.AudioBinder) iBinder;
			mAudBinder.postValue(binder);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mAudBinder.postValue(null);

			mIsRecording.postValue(false);
			mIsPlaying.postValue(false);
		}
	};

	// Get, Sets - Audio Service Related
	public LiveData<Boolean> getIsRecording() {
		return mIsRecording;
	}
	public LiveData<Boolean> getIsPlaying() {
		return mIsPlaying;
	}

	public void setIsRecording(boolean r) {
		mIsRecording.postValue(r);
	}
	public void setIsPlaying(boolean p) {
		mIsPlaying.postValue(p);
	}

	public LiveData<AudioService.AudioBinder> getAudBinder() {
		return mAudBinder;
	}
	public ServiceConnection getAudioServiceConnection() {
		return audioServiceConnection;
	}





	/* -------------------------- Upload Service  -------------------------- */
	// Upload Service Binder
	private final MutableLiveData<UploadService.FireBinder> mUpBinder = new MutableLiveData<>();

	// Upload Service Connection
	private final ServiceConnection uploadServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			Log.d(TAG, "onServiceConnected: Connected to upload service");
			UploadService.FireBinder binder = (UploadService.FireBinder) iBinder;
			mUpBinder.postValue(binder);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mUpBinder.postValue(null);
		}
	};

	// Get, Sets - Upload Service
	public LiveData<UploadService.FireBinder> getUpBinder() {
		return mUpBinder;
	}
	public ServiceConnection getUploadServiceConnection() {
		return uploadServiceConnection;
	}
}
