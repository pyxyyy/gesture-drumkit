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

#ifndef DRUMMACHINE_H
#define DRUMMACHINE_H

#include <android/asset_manager.h>
#include <oboe/Oboe.h>
#include <tuple>
#include <vector>
#include <string>

#include "audio/Mixer.h"
#include "audio/Player.h"
#include "audio/AAssetDataSource.h"
#include "utils/LockFreeQueue.h"
#include "DrumMachineConstants.h"

using namespace oboe;

class DrumMachine : public AudioStreamCallback {
public:
    explicit DrumMachine(AAssetManager&);

    void start();
    void stop();
    // void onSurfaceChanged(int widthInPixels, int heightInPixels);


    // Inherited from oboe::AudioStreamCallback
    DataCallbackResult
    onAudioReady(AudioStream *oboeStream, void *audioData, int32_t numFrames) override;

private:
    void preparePlayerEvents();

    AAssetManager& mAssetManager;
    AudioStream *mAudioStream{nullptr};
    std::vector<std::shared_ptr<Player>> mPlayerList;
    Mixer mMixer;

    LockFreeQueue<std::tuple<int64_t, int>, kMaxQueueItems> mPlayerEvents;
    std::atomic<int64_t> mCurrentFrame { 0 };
    //int beat_map[kTotalBeat][kTotalBeat] = {{ 0 }};
    int mTempo = 60;
};


#endif //DRUMMACHINE_H
