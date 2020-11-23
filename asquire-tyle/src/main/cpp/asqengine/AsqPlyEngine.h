//
// Created by darwin on 23/11/20.
//

#ifndef ASQUIRE_TYLE_0X0A_ASQPLYENGINE_H
#define ASQUIRE_TYLE_0X0A_ASQPLYENGINE_H

#include <oboe/Oboe.h>
#include <fstream>

class AsqPlyEngine : public oboe::AudioStreamCallback {

private:
	// Boolean Flags
	bool mIsPlyOn = false;

	// Oboe variables
	std::shared_ptr<oboe::AudioStream> mPlayStream;
	oboe::AudioStreamBuilder mOutStreamBuilder;
	oboe::AudioFormat mFormat;
	int32_t mSampleRate;
	oboe::Direction mDirection;
	oboe::ChannelCount mChannelCount;

	// Rec Engine variables
	const char* mPlyFilePath{};
	std::ifstream mPlyFile;
	int32_t mBytesPerFrame{};

public:
	AsqPlyEngine();
	~AsqPlyEngine();

	oboe::DataCallbackResult
	onAudioReady(oboe::AudioStream *oboeStream, void *audioData, int32_t numFrames) override;

	bool setPlyOn(bool isOn);

private: // Support Methods
	void setupPlayStreamParameters();
	oboe::Result openPlyStream();

	oboe::Result start();
	oboe::Result stop();
	void closeRecStream();

	void wavFileReader();
	void wavFileFinish();

	static void warnIfNotLowLatency(std::shared_ptr<oboe::AudioStream> &stream);

public: // Is, Get, Set
	bool isPlyOn() {
		return mIsPlyOn;
	}

	void setPlyFilePath(const char* plyFilePath) {
		mPlyFilePath = plyFilePath;
	}

};


#endif //ASQUIRE_TYLE_0X0A_ASQPLYENGINE_H
