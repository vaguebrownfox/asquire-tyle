//
// Created by darwin on 23/11/20.
//

#include <logging_macros.h>
#include "AsqPlyEngine.h"

AsqPlyEngine::AsqPlyEngine() {
	// Rec stream parameters
	mDirection = oboe::Direction::Output;
	mChannelCount = oboe::ChannelCount::Mono;
	mFormat = oboe::AudioFormat::I16;
	mSampleRate = 16000;
	setupPlayStreamParameters();
}

AsqPlyEngine::~AsqPlyEngine() {
	stop();
	closeRecStream();
}

oboe::DataCallbackResult
AsqPlyEngine::onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) {
	oboe::DataCallbackResult callbackResult = oboe::DataCallbackResult::Continue;

	// Silence the output.
	int32_t numBytes = numFrames * mBytesPerFrame;
	memset(audioData, 0 /* value */, numBytes);

	// Cast to required type
	auto *outputData = static_cast<int16_t *>(audioData);

	if (!mPlyFile.eof()) {


		mPlyFile.read(reinterpret_cast<char *>(outputData),  numBytes);
		LOGD("Asquire Ply Engine: numFrames: %d", numFrames);
	} else {
		callbackResult = oboe::DataCallbackResult::Stop;
	}

	if (!mIsPlyOn) {
		callbackResult = oboe::DataCallbackResult::Stop;
	}
	return callbackResult;
}

bool AsqPlyEngine::setPlyOn(bool isOn) {
	bool success = true;

	if (isOn != mIsPlyOn) {
		if (isOn) {
			success = openPlyStream() == oboe::Result::OK;
			if (success) {
				mIsPlyOn = isOn;
				start();
			}
		} else {
			stop();
			closeRecStream();
			mIsPlyOn = isOn;
		}
	}

	return success;
}

void AsqPlyEngine::setupPlayStreamParameters() {
	oboe::AudioStreamBuilder* builder = &mOutStreamBuilder;
	builder->setCallback(this)
			->setDirection(mDirection)
			->setSharingMode(oboe::SharingMode::Exclusive)
			->setPerformanceMode(oboe::PerformanceMode::LowLatency)
			->setFormat(mFormat)
			->setChannelCount(mChannelCount)
			->setSampleRate(mSampleRate)
			->setUsage(oboe::Usage::Media)
			->setContentType(oboe::ContentType::Speech)
			->setInputPreset(oboe::InputPreset::VoicePerformance);
}

oboe::Result AsqPlyEngine::openPlyStream() {
	oboe::Result result;

	result = mOutStreamBuilder.openStream(mPlayStream);
	if (result != oboe::Result::OK) {
		LOGD("Asquire Ply Engine: openStream: Failed - %s", oboe::convertToText(result));
		return result;
	} else {
		LOGD("Asquire Ply Engine: openStream: Success - %s", oboe::convertToText(result));
	}

	mPlayStream->setBufferSizeInFrames(mPlayStream->getFramesPerBurst() * 2);
	mBytesPerFrame = mPlayStream->getBytesPerFrame();

	warnIfNotLowLatency(mPlayStream);

	return result;
}

oboe::Result AsqPlyEngine::start() {
	oboe::Result result;

	wavFileReader();

	oboe::StreamState inputState = oboe::StreamState::Starting;
	oboe::StreamState nextState = oboe::StreamState::Uninitialized;
	int64_t timeoutNanos = 100 * oboe::kNanosPerMillisecond;
	result = mPlayStream->requestStart();
	if (result != oboe::Result::OK)
		result = mPlayStream->waitForStateChange(inputState, &nextState, timeoutNanos);

	if (result != oboe::Result::OK) {
		LOGD("Asquire Ply Engine: start: Failed - %s", oboe::convertToText(result));
		return result;
	} else {
		LOGD("Asquire Ply Engine: start: Success - %s", oboe::convertToText(result));
	}

	return result;
}

oboe::Result AsqPlyEngine::stop() {
	oboe::Result result = oboe::Result::OK;

	if (mPlyFile.is_open()) {
		wavFileFinish();
		LOGD("Asquire Ply Engine: stop: wave file finish");
	}

	if (mPlayStream) {
		oboe::StreamState inputState = oboe::StreamState::Stopping;
		oboe::StreamState nextState = oboe::StreamState::Uninitialized;
		int64_t timeoutNanos = 100 * oboe::kNanosPerMillisecond;
		result = mPlayStream->requestStop();
		if (result != oboe::Result::OK)
			result = mPlayStream->waitForStateChange(inputState, &nextState, timeoutNanos);

		if (result != oboe::Result::OK) {
			LOGD("Asquire Ply Engine: stop: Failed - %s", oboe::convertToText(result));
			return result;
		} else {
			LOGD("Asquire Ply Engine: stop: Success - %s", oboe::convertToText(result));
		}
	} else {
		LOGD("Asquire Ply Engine: stop: Ply stream undefined");
	}

	return result;
}

void AsqPlyEngine::closeRecStream() {
	oboe::Result result;

	if (mPlayStream) {
		result = mPlayStream->close();
		if (result != oboe::Result::OK) {
			LOGW("Asquire Ply Engine closeStream: Error closing stream: %s",
			     oboe::convertToText(result));
		} else {
			LOGW("Asquire Ply Engine closeStream: Successfully closed streams: %s",
			     oboe::convertToText(result));
		}
		mPlayStream.reset();
	}
}

void AsqPlyEngine::wavFileReader() {

	char plyBufferFilepath[200];
	strcpy(plyBufferFilepath, mPlyFilePath);
	strcat(plyBufferFilepath, ".plybuff.pcm");

	mPlyFile.open(plyBufferFilepath, std::ios::binary);

	// Read values from wav header - Later

//	FILE * wavFile = fopen(mPlyFilePath, "r");
}

void AsqPlyEngine::wavFileFinish() {
	mPlyFile.close();
}

void AsqPlyEngine::warnIfNotLowLatency(std::shared_ptr<oboe::AudioStream> &stream) {
	if (stream->getPerformanceMode() != oboe::PerformanceMode::LowLatency) {
		oboe::PerformanceMode v = stream->getPerformanceMode();
		LOGW("Asquire Ply Engine: Stream is not Low Latency - %s", oboe::convertToText(v));
	} else {
		LOGW("Asquire Ply Engine: Stream is Low Latency");
	}
}
