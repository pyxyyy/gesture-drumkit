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

    // Load the RAW PCM data files for both the clap sound and backing track into memory.
    std::shared_ptr<AAssetDataSource> mClapSource(AAssetDataSource::newFromAssetManager(mAssetManager,
        "kick-low-vol.wav",
        oboe::ChannelCount::Stereo));
    if (mClapSource == nullptr){
        LOGE("Could not load source data for kick sound");
        return;
    }
    mClap = std::make_shared<Player>(mClapSource);

    // Add the clap and backing track sounds to a mixer so that they can be played together
    // simultaneously using a single audio stream.
    mMixer.addTrack(mClap);

    // Add the audio frame numbers on which the clap sound should be played to the clap event queue.
    // The backing track tempo is 120 beats per minute, which is 2 beats per second. At a sample
    // rate of 48000 frames per second this means a beat occurs every 24000 frames, starting at
    // zero. So the first 3 beats are: 0, 24000, 48000
    mClapEvents.push(0);
    mClapEvents.push(24000);
    mClapEvents.push(48000);

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

    int64_t nextClapEvent;

    for (int i = 0; i < numFrames; ++i) {

        if (mClapEvents.peek(nextClapEvent) && mCurrentFrame == nextClapEvent){
            mClap->setPlaying(true);
            mClapEvents.pop(nextClapEvent);
        }
        mMixer.renderAudio(static_cast<int16_t*>(audioData)+(kChannelCount*i), 1);
        mCurrentFrame++;
    }

    return DataCallbackResult::Continue;
}

