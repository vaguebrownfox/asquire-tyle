package aashi.fiaxco.asquiretyle0x0a.asqengine;

import android.content.Context;
import android.media.AudioManager;

public enum AsqEngine {

	INSTANCE;

	static {
		System.loadLibrary("asq-lib");
	}

	public static native boolean create();
	public static native void    delete();

	public static native boolean setRecOn(boolean isRecOn);
	public static native boolean setPlyOn(boolean isPlyOn);

	public static native void    setRecFilePath(String recFilePath);
	public static native void    setPlyFilePath(String plyBufferPath);

	public static native boolean isRecOn();
	public static native boolean isPlyOn();

	public static native void native_setDefaultStreamValues(
			int defaultSampleRate, int defaultFramesPerBurst);

	public static void setDefaultStreamValues(Context context) {
		AudioManager myAudioMgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		String sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
		int defaultSampleRate = Integer.parseInt(sampleRateStr);
		String framesPerBurstStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
		int defaultFramesPerBurst = Integer.parseInt(framesPerBurstStr);

		native_setDefaultStreamValues(defaultSampleRate, defaultFramesPerBurst);
	}

	public static native float asqPredict(String modelFilePath); // TODO: tmp param feat file
}
