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

final case class Texture(
    itemName: String,
    `type`: TextureType,
    textureWidth: Int,
    textureHeight: Int,
    variations: Int,
    keywords: Option[Set[String]] = None,
    seasons: Option[Set[Season]] = None,
    manualVariations: Option[Vector[ManualVariation]] = None,
    animation: Option[Vector[AnimationKeyFrame]] = None
) {

  def textureType: TextureType = `type`

  def addKeyword(keyword: String): Texture =
    copy(keywords = Some(keywords.fold(Set(keyword))(_ + keyword)))

  def addKeywords(keywords: Set[String]): Texture =
    copy(keywords = Some(this.keywords.fold(keywords)(_ union keywords)))

  def withManualVariations(variations: Iterable[ManualVariation]): Texture =
    copy(manualVariations = Some(variations.toVector))
}

object Texture {

  implicit val config: Configuration = JsonFormat.DefaultConfig
  implicit val codecForTexture: Codec[Texture] = deriveConfiguredCodec
}
