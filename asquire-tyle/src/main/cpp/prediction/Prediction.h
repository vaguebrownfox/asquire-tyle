//
// Created by darwin on 25/11/20.
//

#ifndef ASQUIRE_TYLE_0X0A_PREDICTION_H
#define ASQUIRE_TYLE_0X0A_PREDICTION_H


#include "base/kaldi-common.h"
#include "base/kaldi-common.h"
#include "util/common-utils.h"
#include "feat/feature-mfcc.h"
#include "feat/wave-reader.h"
#include "feat/resample.h"
#include "matrix/kaldi-matrix.h"
#include "transform/cmvn.h"

#include "ivectorbin/compute-vad.h"
#include <svm.h>
#include <svm-predict.h>

#include<algorithm>
#include<fstream>

#include <random>
class Prediction {
private:
	const char* mWavFilePath;
	const char* mModelFilePath;
	char mStatsFilePath[200]{};
	char mOutputFilePath[200]{};
	float *mStats{};

	kaldi::BaseFloat mMinDuration;
	kaldi::MfccOptions mMfccOptions;
	int32 mNumMfccCoeffs;

	kaldi::Matrix<kaldi::BaseFloat> mFeatures;

public:
	Prediction(const char* filepath, const char* modelFilePath);
	~Prediction();

	void asqPredict();

private:
	void computeMfccFeats();
	void calculateMfccStats();
	void writeFeatStats(int size);
	void writeFeats();

};


#endif //ASQUIRE_TYLE_0X0A_PREDICTION_H
