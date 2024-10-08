/*
 * Copyright (C) 2024 The Android Open Source Project
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

#include <binder/IBinder.h>
#include <gui/InputTransferToken.h>
#include <gui/SurfaceControl.h>
#include <input/InputTransport.h>

namespace android {
extern std::shared_ptr<InputChannel> createInputChannel(
        const sp<IBinder>& clientToken, const InputTransferToken& hostInputTransferToken,
        const SurfaceControl& surfaceControl,
        const InputTransferToken& clientInputTransferTokenObj);
extern void removeInputChannel(const sp<IBinder>& clientToken);

} // namespace android