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

package com.quincyjo.stardrop.smapi.models

import com.quincyjo.stardrop.encoding.JsonFormat.DefaultConfig

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

final case class SmapiManifest(
    name: String,
    author: String,
    version: String,
    description: String,
    uniqueID: String,
    updateKeys: Option[Seq[String]] = None,
    contentPackFor: Option[Map[String, String]] = None
)

object SmapiManifest {
  implicit val configuration: Configuration = DefaultConfig
  implicit val codec: Codec[SmapiManifest] = deriveConfiguredCodec
}
