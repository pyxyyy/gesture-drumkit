/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <utils/logging.h>
#include <thread>

#include "DrumMachine.h"

DrumMachine::DrumMachine(AAssetManager &assetManager): mAssetManager(assetManager) {
}

void DrumMachine::start() {
    std::vector<std::string> asset_list = { "clap.wav", "finger-cymbal.wav", "hihat.wav", "kick.wav", "rim.wav",
            "scratch.wav", "snare.wav", "splash.wav" };
    for(std::string wav_file : asset_list){
        // Load the RAW PCM data files for both the sample sound and backing track into memory.
        std::shared_ptr<AAssetDataSource> mSampleSource(AAssetDataSource::newFromAssetManager(mAssetManager,
                                                                                            wav_file.c_str(),
                                                                                            oboe::ChannelCount::Stereo));
        if (mSampleSource == nullptr){
            LOGE("Could not load source data for kick sound");
            return;
        }
        std::shared_ptr<Player> mSamplePlayer = std::make_shared<Player>(mSampleSource);
        mPlayerList.push_back(mSamplePlayer);
        // Add the sample sounds to a mixer so that they can be played together
        // simultaneously using a single audio stream.
        mMixer.addTrack(mSamplePlayer);
    }

    // Add the audio frame numbers on which the clap sound should be played to the clap event queue.
    // The backing track tempo is 120 beats per minute, which is 2 beats per second. At a sample
    // rate of 48000 frames per second this means a beat occurs every 24000 frames, starting at
    // zero. So the first 3 beats are: 0, 24000, 48000
    //    mClapEvents.push(0);
    //    mClapEvents.push(24000);
    //    mClapEvents.push(48000);
    mPlayerEvents.push(std::make_tuple((int64_t)0,0));
    mPlayerEvents.push(std::make_tuple((int64_t)60000, 1));
    mPlayerEvents.push(std::make_tuple((int64_t)120000, 2));
    mPlayerEvents.push(std::make_tuple((int64_t)180000, 3));
    mPlayerEvents.push(std::make_tuple((int64_t)240000, 4));
    mPlayerEvents.push(std::make_tuple((int64_t)300000, 5));
    mPlayerEvents.push(std::make_tuple((int64_t)360000, 6));
    mPlayerEvents.push(std::make_tuple((int64_t)420000, 7));

    // Create a builder
    AudioStreamBuilder builder;
    builder.setFormat(AudioFormat::I16);
    builder.setChannelCount(2);
    builder.setSampleRate(kSampleRateHz);
    builder.setCallback(this);
    builder.setPerformanceMode(PerformanceMode::LowLatency);
    builder.setSharingMode(SharingMode::Exclusive);

    Result result = builder.openStream(&mAudioStream);
    if (result != Result::OK){
        LOGE("Failed to open stream. Error: %s", convertToText(result));
    }

    // Reduce stream latency by setting the buffer size to a multiple of the burst size
    auto setBufferSizeResult = mAudioStream->setBufferSizeInFrames(
            mAudioStream->getFramesPerBurst() * kBufferSizeInBursts);
    if (setBufferSizeResult != Result::OK){
        LOGW("Failed to set buffer size. Error: %s", convertToText(setBufferSizeResult.error()));
    }

    result = mAudioStream->requestStart();
    if (result != Result::OK){
        LOGE("Failed to start stream. Error: %s", convertToText(result));
    }
}

void DrumMachine::stop(){

    if (mAudioStream != nullptr){
        mAudioStream->close();
        delete mAudioStream;
        mAudioStream = nullptr;
    }
}

DataCallbackResult DrumMachine::onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) {

    std::tuple<int64_t, int> nextClapEvent;

    for (int i = 0; i < numFrames; ++i) {

        if (mPlayerEvents.peek(nextClapEvent) && mCurrentFrame == std::get<0>(nextClapEvent)) {
            mPlayerList[std::get<1>(nextClapEvent)]->setPlaying(true);
            mPlayerEvents.pop(nextClapEvent);
        }

        mMixer.renderAudio(static_cast<int16_t*>(audioData)+(kChannelCount*i), 1);
        mCurrentFrame++;
    }

    return DataCallbackResult::Continue;
}

