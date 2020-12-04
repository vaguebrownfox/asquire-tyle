//
// Created by darwin on 23/11/20.
//

#include <logging_macros.h>
#include "AsqRecEngine.h"

AsqRecEngine::AsqRecEngine() {
	// Rec stream parameters
	mDirection = oboe::Direction::Input;
	mChannelCount = oboe::ChannelCount::Mono;
	mFormat = oboe::AudioFormat::I16;
	mSampleRate = 16000;
	mBitsPerSample = 16;
	setupRecordStreamParameters();
}

AsqRecEngine::~AsqRecEngine() {
	stop();
	closeRecStream();
}

void AsqRecEngine::setupRecordStreamParameters() {
	oboe::AudioStreamBuilder* builder = &mInStreamBuilder;
	builder->setCallback(nullptr)
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

bool AsqRecEngine::setRecOn(bool isOn) {
	bool success = true;

	if (isOn != mIsRecOn) {
		if (isOn) {
			success = openRecStream() == oboe::Result::OK;
			if (success) {
				success = start() == oboe::Result::OK;
				mIsRecOn = success;
				recordStream();
			}
		} else {
			mIsRecOn = isOn;
			success = stop() == oboe::Result::OK;
			closeRecStream();
		}
	}

	return success;
}



oboe::Result AsqRecEngine::openRecStream() {
	oboe::Result result;

	result = mInStreamBuilder.openStream(mRecordStream);
	if (result != oboe::Result::OK) {
		LOGD("Asquire Rec Engine: openStream: Failed - %s", oboe::convertToText(result));
		return result;
	} else {
		LOGD("Asquire Rec Engine: openStream: Success - %s", oboe::convertToText(result));
	}

	mSampleRate = mRecordStream->getSampleRate();
	mRecordStream->setBufferSizeInFrames(mRecordStream->getFramesPerBurst() * 2);

	warnIfNotLowLatency(mRecordStream);

	return result;
}

oboe::Result AsqRecEngine::start() {
	oboe::Result result;

	wavFileWriter();

	oboe::StreamState inputState = oboe::StreamState::Starting;
	oboe::StreamState nextState = oboe::StreamState::Uninitialized;
	int64_t timeoutNanos = 100 * oboe::kNanosPerMillisecond;
	result = mRecordStream->requestStart();
	if (result != oboe::Result::OK)
		result = mRecordStream->waitForStateChange(inputState, &nextState, timeoutNanos);

	if (result != oboe::Result::OK) {
		LOGD("Asquire Rec Engine: start: Failed - %s", oboe::convertToText(result));
		return result;
	} else {
		LOGD("Asquire Rec Engine: start: Success - %s", oboe::convertToText(result));
	}

	return result;
}

void AsqRecEngine::recordStream() {

	constexpr int kMillisecondsToRecord = 2;
	const auto requestedFrames = (int32_t) (kMillisecondsToRecord *
	                                        (mSampleRate / oboe::kMillisPerSecond));
	int16_t buffer[requestedFrames];

	constexpr int64_t kTimeoutValue = 3 * oboe::kNanosPerMillisecond;
	auto bufferSize = mRecordStream->getBufferSizeInFrames();

	int framesRead;
	do {
		auto result = mRecordStream->read(buffer, bufferSize, 0);
		if (result != oboe::Result::OK)
			break;
		framesRead = result.value();
	} while (framesRead != 0);

	while (mIsRecOn) {
		auto result = mRecordStream->read(buffer, requestedFrames, kTimeoutValue);

		if (result == oboe::Result::OK) {
			int32_t numFrames = result.value();
			LOGD("Asquire Rec Engine: recordStream: Read %d frames", numFrames);
			for (int i = 0; i < numFrames; i++) {
				little_endian_io::writeWord(mRecFile, buffer[i], 2);
				big_endian_io::writeWord(mPlyBuff, buffer[i]);
			}
		} else {
			LOGD("Asquire Rec Engine: recordStream: Failed reading stream");
		}
	}
}

oboe::Result AsqRecEngine::stop() {
	oboe::Result result = oboe::Result::OK;

	if (mRecFile.is_open()) {
		wavFileFinish();
		LOGD("Asquire Rec Engine: stop: wave file finish");
	}

	if (mRecordStream) {
		oboe::StreamState inputState = oboe::StreamState::Stopping;
		oboe::StreamState nextState = oboe::StreamState::Uninitialized;
		int64_t timeoutNanos = 100 * oboe::kNanosPerMillisecond;
		result = mRecordStream->requestStop();
		if (result != oboe::Result::OK)
			result = mRecordStream->waitForStateChange(inputState, &nextState, timeoutNanos);

		if (result != oboe::Result::OK) {
			LOGD("Asquire Rec Engine: stop: Failed - %s", oboe::convertToText(result));
			return result;
		} else {
			LOGD("Asquire Rec Engine: stop: Success - %s", oboe::convertToText(result));
		}
	} else {
		LOGD("Asquire Rec Engine: stop: Rec stream undefined");
	}

	return result;
}

void AsqRecEngine::closeRecStream() {

	oboe::Result result;

	if (mRecordStream) {
		result = mRecordStream->close();
		if (result != oboe::Result::OK) {
			LOGW("Asquire Rec Engine closeStream: Error closing stream: %s",
			     oboe::convertToText(result));
		} else {
			LOGW("Asquire Rec Engine closeStream: Successfully closed streams: %s",
			     oboe::convertToText(result));
		}
		mRecordStream.reset();
	}
}

void AsqRecEngine::wavFileWriter() {

	char plyBufferFilepath[200];
	strcpy(plyBufferFilepath, mRecFilePath);
	strcat(plyBufferFilepath, ".plybuff.pcm");

	mRecFile.open(mRecFilePath, std::ios::binary);
	mPlyBuff.open(plyBufferFilepath, std::ios::binary);

	int Fs = mSampleRate;
	int nCh = mChannelCount;
	int bPS = mBitsPerSample;

	using namespace little_endian_io;

	// Write the file headers
	mRecFile << "RIFF----WAVEfmt ";     // (chunk size to be filled in later)
	writeWord(mRecFile, 16, 4);  // no extension data
	writeWord(mRecFile, 1, 2);  // PCM - integer samples
	writeWord(mRecFile, nCh, 2);  // one channel (mono) or two channels (stereo file)
	writeWord(mRecFile, Fs, 4);  // samples per second (Hz)
	writeWord(mRecFile, (Fs * bPS * nCh)/8, 4);  // (Sample Rate * BitsPerSample * Channels) / 8
	writeWord(mRecFile, nCh * bPS/8, 2);  // data block size (size of two integer samples, one for each channel, in bytes)
	writeWord(mRecFile, bPS, 2);  // number of bits per sample (use a multiple of 8)

	// Write the data chunk header
	mDataChunkPos = mRecFile.tellp();
	mRecFile << "data----";  // (chunk size to be filled in later)
	mRecFile << std::flush;
}

void AsqRecEngine::wavFileFinish() {
	// wav file finishing
	// (We'll need the final file size to fix the chunk sizes above)
	size_t file_length = mRecFile.tellp();

	// Fix the data chunk header to contain the data size
	mTotalRecDataSize = file_length - mDataChunkPos + 8;
	mRecFile.seekp(mDataChunkPos + 4);
	little_endian_io::writeWord(mRecFile, static_cast<int32_t>(mTotalRecDataSize), 4);

	// Fix the file header to contain the proper RIFF chunk size, which is (file size - 8) bytes
	mRecFile.seekp(0 + 4);
	little_endian_io::writeWord(mRecFile, file_length - 8, 4);

	mRecFile.close();
	mPlyBuff.close();
}

void AsqRecEngine::warnIfNotLowLatency(std::shared_ptr<oboe::AudioStream> &stream) {
	if (stream->getPerformanceMode() != oboe::PerformanceMode::LowLatency) {
		oboe::PerformanceMode v = stream->getPerformanceMode();
		LOGW("Asquire Engine: Stream is not Low Latency - %s", oboe::convertToText(v));
	} else {
		LOGW("Asquire Engine: Stream is Low Latency");
	}
}
