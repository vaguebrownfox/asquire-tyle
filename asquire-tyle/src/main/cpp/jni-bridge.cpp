#include <jni.h>
#include <string>
#include <logging_macros.h>
#include "asqengine/AsqRecEngine.h"
#include "asqengine/AsqPlyEngine.h"
#include "prediction/Prediction.h"

AsqRecEngine *rengine = nullptr;
AsqPlyEngine *pengine = nullptr;

extern "C"
JNIEXPORT jboolean JNICALL
Java_aashi_fiaxco_asquiretyle0x0a_asqengine_AsqEngine_create(JNIEnv *env, jclass clazz) {
	if (rengine != nullptr && pengine != nullptr)
		return JNI_TRUE;

	rengine = new AsqRecEngine();
	pengine = new AsqPlyEngine();


	return (rengine != nullptr && pengine != nullptr) ? JNI_TRUE : JNI_FALSE;
}
extern "C"
JNIEXPORT void JNICALL
Java_aashi_fiaxco_asquiretyle0x0a_asqengine_AsqEngine_delete(JNIEnv *env, jclass clazz) {
	if (rengine != nullptr && pengine != nullptr) {
		delete rengine;
		rengine = nullptr;
		delete pengine;
		pengine = nullptr;
	} else {
		LOGE(
				"Engine is null, you must call createEngine before calling this "
				"method");
	}
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_aashi_fiaxco_asquiretyle0x0a_asqengine_AsqEngine_setRecOn(JNIEnv *env, jclass clazz,
                                                               jboolean is_rec_on) {
	if (rengine != nullptr) return rengine->setRecOn(is_rec_on) ? JNI_TRUE : JNI_FALSE;
	LOGE(
			"Engine is null, you must call createEngine before calling this "
			"method");
	return JNI_FALSE;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_aashi_fiaxco_asquiretyle0x0a_asqengine_AsqEngine_setPlyOn(JNIEnv *env, jclass clazz,
                                                               jboolean is_ply_on) {
	if (rengine != nullptr) {
		pengine->setPlyFilePath(rengine->getWavFilePath());
		return pengine->setPlyOn(is_ply_on) ? JNI_TRUE : JNI_FALSE;
	}
	LOGE(
			"Engine is null, you must call createEngine before calling this "
			"method");
	return JNI_FALSE;
}


extern "C"
JNIEXPORT void JNICALL
Java_aashi_fiaxco_asquiretyle0x0a_asqengine_AsqEngine_setRecFilePath(JNIEnv *env, jclass clazz,
                                                                     jstring rec_file_path) {
	if (rengine != nullptr) {
		const char *path = (*env).GetStringUTFChars(rec_file_path, nullptr);
		rengine->setRecFilePath(path);
	} else {
		LOGE(
				"Engine is null, you must call createEngine before calling this "
				"method");
	}
}
extern "C"
JNIEXPORT void JNICALL
Java_aashi_fiaxco_asquiretyle0x0a_asqengine_AsqEngine_setPlyFilePath(JNIEnv *env, jclass clazz,
                                                                     jstring ply_file_path) {
	if (rengine != nullptr) {
		const char *path = (*env).GetStringUTFChars(ply_file_path, nullptr);
		pengine->setPlyFilePath(path);
	} else {
		LOGE(
				"Engine is null, you must call createEngine before calling this "
				"method");
	}
}


extern "C"
JNIEXPORT jboolean JNICALL
Java_aashi_fiaxco_asquiretyle0x0a_asqengine_AsqEngine_isRecOn(JNIEnv *env, jclass clazz) {
	if (rengine != nullptr) return rengine->isRecOn() ? JNI_TRUE : JNI_FALSE;
	LOGE(
			"Engine is null, you must call createEngine before calling this "
			"method");
	return JNI_FALSE;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_aashi_fiaxco_asquiretyle0x0a_asqengine_AsqEngine_isPlyOn(JNIEnv *env, jclass clazz) {
	if (rengine != nullptr) return pengine->isPlyOn() ? JNI_TRUE : JNI_FALSE;
	LOGE(
			"Engine is null, you must call createEngine before calling this "
			"method");
	return JNI_FALSE;
}


extern "C"
JNIEXPORT void JNICALL
Java_aashi_fiaxco_asquiretyle0x0a_asqengine_AsqEngine_native_1setDefaultStreamValues(JNIEnv *env,
                                                                                     jclass clazz,
                                                                                     jint default_sample_rate,
                                                                                     jint default_frames_per_burst) {
	oboe::DefaultStreamValues::SampleRate = (int32_t) default_sample_rate;
	oboe::DefaultStreamValues::FramesPerBurst = (int32_t) default_frames_per_burst;
}


extern "C"
JNIEXPORT jfloat JNICALL
Java_aashi_fiaxco_asquiretyle0x0a_asqengine_AsqEngine_asqPredict(JNIEnv *env, jclass clazz,
                                                                 jstring model_file_path) {

	// Model and Wav filepath
	const char *modelFilePath = env->GetStringUTFChars(model_file_path, nullptr);
	const char *wavFilePath = rengine->getWavFilePath();

	// Prediction object
	auto* asqPrediction = new Prediction(wavFilePath, modelFilePath);

	asqPrediction->asqPredict();

	char outputFilepath[200];
	strcpy(outputFilepath, asqPrediction->getOutputFilePath());
	

	delete asqPrediction;

	float op = (rand() % 100);

	return op;
}