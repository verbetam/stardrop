/*
 * Copyright 2023 Quincy Jo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quincyjo.stardrop.alternativetextures.models

import com.quincyjo.stardrop.encoding.JsonFormat
import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

final case class AnimationKeyFrame(frame: Int, duration: Long)

object AnimationKeyFrame {

  implicit val config: Configuration = JsonFormat.DefaultConfig
  implicit val codecForAnimationKeyFrame: Codec[AnimationKeyFrame] =
    deriveConfiguredCodec
}
