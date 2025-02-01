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

package com.quincyjo.stardrop.cli.implicits

import cats.implicits.catsSyntaxTuple3Semigroupal
import com.monovore.decline.*
import com.quincyjo.stardrop.converters.AlternativeTexturesSpriteConverter.AlternativeTexturesSpriteConverterOptions
import com.quincyjo.stardrop.converters.CustomFurnitureSpriteExtractor.SpriteExtractorOptions

trait StardropImplicits {

  implicit val optsForATSpriteConverterOptions
      : Opts[AlternativeTexturesSpriteConverterOptions] =
    (
      Opts
        .flag(
          long = "expand-furniture-front",
          help = "Expand CF sprites to have a furniture front."
        )
        .orFalse,
      Opts
        .flag(
          long = "copy-fourth-rotation",
          help =
            "If expanding sprites with the furniture front layer, copy the fourth rotation sprite into the furniture front rotation."
        )
        .orFalse,
      Opts
        .flag(
          long = "expand-rotations",
          help =
            "When converting a 1 rotation CF sprite to a rotating sprite, create rotated sprites by copying the original."
        )
        .orFalse
    ).mapN {
      case (expandFurnitureFront, copyFourthRotationToFront, expandRotations) =>
        AlternativeTexturesSpriteConverterOptions(
          expandFurnitureFront,
          copyFourthRotationToFront,
          expandRotations
        )
    }

  implicit val optsForCFSpriteExtractorOptions: Opts[SpriteExtractorOptions] =
    Opts
      .flag(
        long = "read-furniture-front",
        help =
          "Read furniture front from CF tilesheets. Note that CF does not support furniture front sprites, so this is rarely desirable."
      )
      .orFalse
      .map { readFurnitureFront =>
        SpriteExtractorOptions(readFurnitureFront)
      }
}
