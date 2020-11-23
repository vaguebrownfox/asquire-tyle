//
// Created by darwin on 23/11/20.
//

#ifndef ASQUIRE_TYLE_0X0A_ASQRECENGINE_H
#define ASQUIRE_TYLE_0X0A_ASQRECENGINE_H

#include <oboe/Oboe.h>
#include <fstream>

class AsqRecEngine {

private: // Variables
	// Boolean Flags
	bool mIsRecOn = false;

	// Oboe variables
	std::shared_ptr<oboe::AudioStream> mRecordStream;
	oboe::AudioStreamBuilder mInStreamBuilder;
	oboe::AudioFormat mFormat;
	int32_t mSampleRate;
	oboe::Direction mDirection;
	oboe::ChannelCount mChannelCount;

	// Rec Engine variables
	const char* mRecFilePath{};
	std::ofstream mRecFile;
	std::ofstream mPlyBuff;
	size_t mDataChunkPos{};
	int32_t mBitsPerSample;
	size_t mTotalRecDataSize{};

public: // Class Methods
	AsqRecEngine();
	~AsqRecEngine();

	bool setRecOn(bool isOn);

private: // Support Methods
	void setupRecordStreamParameters();
	oboe::Result openRecStream();

	oboe::Result start();
	void recordStream();
	oboe::Result stop();
	void closeRecStream();

	void wavFileWriter();
	void wavFileFinish();

	static void warnIfNotLowLatency(std::shared_ptr<oboe::AudioStream> &stream);

public: // Is, Set, Gets
	bool isRecOn() {
		return mIsRecOn;
	}

	void setRecFilePath(const char* recFilePath) {
		mRecFilePath = recFilePath;
	}

	const char* getWavFilePath() {
		return mRecFilePath;
	}

	size_t getTotalRecDataSize() {
		return mTotalRecDataSize;
	}

};

namespace little_endian_io {
	template<typename Word>
	std::ostream &writeWord(std::ostream &outs, Word value, unsigned size = sizeof(Word)) {
		for (; size; --size, value >>= 8)
			outs.put(static_cast<char>(value & 0xFF));
		return outs;
	}
}

namespace big_endian_io {
	template <typename Word>
	std::ostream& writeWord(std::ostream& outs, Word value) {
		outs.write(reinterpret_cast<const char *>(&value), sizeof(value));
		return outs;
	}
}

#endif //ASQUIRE_TYLE_0X0A_ASQRECENGINE_H
