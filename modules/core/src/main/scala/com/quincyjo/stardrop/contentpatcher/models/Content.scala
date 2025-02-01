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

package com.quincyjo.stardrop.contentpatcher.models

import com.quincyjo.stardrop.encoding.JsonFormat.DefaultConfig

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.{Codec, JsonObject}

final case class Content(
    format: String,
    changes: Vector[Action],
    dynamicTokens: Option[Vector[DynamicToken]] = None,
    aliasTokenNames: Option[Map[String, String]] = None,
    configSchema: Option[JsonObject] = None
)

object Content {
  implicit val configuration: Configuration = DefaultConfig
  implicit val codec: Codec.AsObject[Content] = deriveConfiguredCodec
}
