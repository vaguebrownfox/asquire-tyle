//
// Created by darwin on 25/11/20.
//

#include "Prediction.h"

Prediction::Prediction(const char *wavFilepath, const char *modelFilePath) {
    mWavFilePath = wavFilepath;
    mModelFilePath = modelFilePath;

    strcpy(mStatsFilePath, mWavFilePath);
    strcat(mStatsFilePath, ".featstats.txt");

    strcpy(mOutputFilePath, mWavFilePath);
    strcat(mOutputFilePath, ".out.txt");

    mMinDuration = 0.0;
    mNumMfccCoeffs = 13;

    // MFCC Options
    {
        mMfccOptions.num_ceps = mNumMfccCoeffs;
        mMfccOptions.frame_opts.samp_freq = 16000;
        mMfccOptions.energy_floor = 0.0;
        mMfccOptions.cepstral_lifter = 0.22;
        mMfccOptions.use_energy = true;
        mMfccOptions.raw_energy = true;
        mMfccOptions.htk_compat = false;

        mMfccOptions.frame_opts.window_type = "hamming";
        mMfccOptions.frame_opts.frame_length_ms = 20;
        mMfccOptions.frame_opts.frame_shift_ms = 10;
        mMfccOptions.frame_opts.dither = 0.0;
        mMfccOptions.frame_opts.preemph_coeff = 0.97;
        mMfccOptions.frame_opts.remove_dc_offset = true;
        mMfccOptions.frame_opts.round_to_power_of_two = true;
        mMfccOptions.frame_opts.snip_edges = true;
        mMfccOptions.frame_opts.allow_downsample = false;

        mMfccOptions.mel_opts.num_bins = 25;
        mMfccOptions.mel_opts.low_freq = 40;
        mMfccOptions.mel_opts.high_freq = 7800;
        mMfccOptions.mel_opts.vtln_low = 60;
        mMfccOptions.mel_opts.vtln_high = 7200;
        mMfccOptions.mel_opts.debug_mel = false;
        mMfccOptions.mel_opts.htk_mode = false;
    }

}

Prediction::~Prediction() = default;

void Prediction::computeMfccFeats() {
    try {
        // Apply mfcc options
        kaldi::Mfcc mfcc(mMfccOptions);

        // Read wav file
        kaldi::Input wavFile;
        wavFile.Open(mWavFilePath);
        kaldi::WaveHolder holder;
        holder.Read(wavFile.Stream());
        kaldi::WaveData waveData = holder.Value();
        wavFile.Close();

        // Validate wav file
        if (waveData.Duration() < mMinDuration) {
            KALDI_WARN << "File is too short ("
                       << waveData.Duration() << " sec): producing no output.";
            exit(-1);
        }

        // Wave file parameters
        int32 sampleFreq = waveData.SampFreq();
        int32 numChannel = waveData.Data().NumRows();
        int32 channel = 0; // (0=left, 1=right...)

        // This block works out the channel (0=left, 1=right...)
        KALDI_ASSERT(numChannel > 0);  // at least one channel required
        if (numChannel < 1) {
            KALDI_WARN << "At least one channel required ("
                       << numChannel << " channels): provided.";
            exit(-1);
        }

        // MFCC calculation
        kaldi::SubVector<kaldi::BaseFloat> waveform(waveData.Data(), channel);
        mfcc.ComputeFeatures(waveform, sampleFreq, 1.0, &mFeatures);
//        mfcc.Compute(waveform, 1.0, &mFeatures);
        KALDI_LOG << "Computed MFCCs for " << mWavFilePath;

    } catch (const std::exception &e) {
        std::cerr << e.what();
        exit(-1);
    }
}

void Prediction::calculateMfccStats() {

    kaldi::Matrix<float> features = mFeatures;
    features.Transpose();

    int numStatType = 6, pos; // Six types of stats - mean, median, mode, var, std, rms
    int nRows = features.NumRows();
    int nCols = features.NumCols();
    int featSize = numStatType * nRows;
    float mean, var;

    // Allocate stats vector
    mStats = new float[featSize];

    // Stat calc loop
    for (int i = 0; i < nRows; i++) {
        mean = 0;
        var = 0;
        SubVector<BaseFloat> row = features.Row(i);

        // mean - 0
        pos = 0;
        mStats[pos + i] = 0.0f;
        for (int j = 0; j < nCols; j++) {
            mStats[pos + i] += row.Data()[j];
        }
        mStats[pos + i] /= (float) nCols;
        mean = mStats[pos + i];

        // median - 1
        pos += nRows;
        mStats[pos + i] = 0.0f;
        std::sort(row.Data(), row.Data() + nCols);
        if (nCols % 2) {
            mStats[pos + i] = row.Data()[(nCols - 1) / 2];
        } else {
            mStats[pos + i] = (row.Data()[(nCols - 1) / 2] + row.Data()[(nCols + 1) / 2]) / 2.0f;
        }

        // mode - 2
        pos += nRows;
        mStats[pos + i] = 0.0f;
        float number = row.Data()[0];
        mStats[pos + i] = number;
        int count = 1;
        int countMode = 1;
        for (int m = 1; m < nCols; m++) {
            if (row.Data()[m] == number) {
                ++count; // increment the count of occurrences for the current number
                if (count > countMode) {
                    countMode = count; // this number now has the most occurrences
                    mStats[pos + i] = number; // this number is now the mode
                }
            } else {
                // now this is a different number
                count = 1; // reset count for the new number
                number = row.Data()[m]; // set the new number
            }
        }

        // variance - 3
        pos += nRows;
        mStats[pos + i] = 0.0f;
        std::for_each(static_cast<float *>(&row.Data()[0]),
                      static_cast<float *>(&row.Data()[nCols - 1]), [&](const float f) {
                    mStats[pos + i] += (f - mean) * (f - mean);
                });
        mStats[pos + i] /= (float) nCols;
        var = mStats[pos + i];

        // std-dev - 4
        pos += nRows;
        mStats[pos + i] = 0.0f;
        mStats[pos + i] = std::sqrt(var);

        // rms - 5
        pos += nRows;
        mStats[pos + i] = 0.0f;
        std::for_each(static_cast<float *>(&row.Data()[0]),
                      static_cast<float *>(&row.Data()[nCols - 1]), [&](const float f) {
                    mStats[pos + i] += f * f;
                });
        mStats[pos + i] /= (float) nCols;
        mStats[pos + i] = std::sqrt(mStats[pos + i]);
    }

    // Write to file
    writeFeatStats(featSize);

    // Clean
    delete[] mStats;

}

void Prediction::writeFeatStats(int size) {

    try {
        std::ofstream stats(mStatsFilePath, std::ofstream::app);

        int i = 1;
        stats << i << " "; // dummy labels

        std::for_each(&mStats[0], &mStats[size], [&](const float f) {
            stats << i++ << ":" << std::setprecision(10) << f << " ";
        });
        stats << std::endl;
        stats.close();

    } catch (const std::exception &e) {
        std::cerr << e.what();
        exit(-1);
    }

}


void Prediction::asqPredict() {

    // Feature extraction
    computeMfccFeats();
    calculateMfccStats();

    // Initialize
    struct svm_model *asqModel = svm_load_model(mModelFilePath); // model
    FILE *input = fopen(mStatsFilePath, "r");            // input file - stats
    FILE *output = fopen(mOutputFilePath, "w");           // output file

    // SVM Predict
    predict(input, asqModel, output);

    // Clean
    fclose(input);
    fclose(output);

    char testf[200];
    strcpy(testf, mWavFilePath);
    strcat(testf, ".testfeats.txt");
    std::ofstream test(testf, std::ios::binary);


    for (int i = 0; i < mFeatures.NumRows(); i++) {
        for (int j = 0; j < mFeatures.NumCols(); j++) {
            test << j << ":" << std::setprecision(10) << mFeatures.Index(i, j) << "  ";
        }
        test << std::endl;
    }
    test.flush();
    test.close();
}

//--------------------------purgeable - write mfcc feats to a file
/*char testf[200];
strcpy(testf, mWavFilePath);
strcat(testf, ".testfeats.txt");
std::ofstream test(testf, std::ios::binary);

for (int i = 0; i < mFeatures.NumCols(); i++) {
	for (int j = 0; j < mFeatures.NumRows(); j++) {
		test << j << ":" << std::setprecision(10) << mFeatures.Index(j, i) << "  ";
	}
	test << std::endl;
}
test.flush();
test.close();*/
//---------------------------------------
