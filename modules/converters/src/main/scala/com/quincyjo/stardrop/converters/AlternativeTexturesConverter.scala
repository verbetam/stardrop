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

package com.quincyjo.stardrop.converters

import com.quincyjo.stardrop.alternativetextures.Util.{
  textureHeightFromFurnitureData,
  textureWidthFromFurnitureData
}
import com.quincyjo.stardrop.alternativetextures.models.{
  AlternativeTexturesMod,
  ManualVariation,
  Texture,
  TextureType
}
import com.quincyjo.stardrop.customfurniture.CustomFurnitureMod
import com.quincyjo.stardrop.customfurniture.models.CustomFurniture
import CustomFurnitureMatcher.*
import com.quincyjo.stardrop.content.models.FurnitureData
import com.quincyjo.stardrop.smapi.models.SmapiManifest
import org.scalactic.anyvals.NonEmptySet
import org.slf4j.{Logger, LoggerFactory}

import scala.util.chaining.scalaUtilChainingOps

class AlternativeTexturesConverter(
    spriteExtractor: CustomFurnitureSpriteExtractor,
    alternativeTexturesSpriteConverter: AlternativeTexturesSpriteConverter
) {

  private val logger: Logger =
    LoggerFactory.getLogger("AlternativeTexturesConverter")

  def convertManifest(manifest: SmapiManifest): SmapiManifest =
    SmapiManifest(
      manifest.name,
      manifest.author,
      manifest.version,
      s"Auto converted from parent CF mod. ${manifest.description}",
      s"quincyjo.conversions.${manifest.uniqueID}",
      manifest.updateKeys,
      contentPackFor = Some(
        Map(
          "UniqueID" -> "PeacefulEnd.AlternativeTextures",
          "MinimumVersion" -> "6.4.3"
        )
      )
    )

  def modKeywords(mod: CustomFurnitureMod): Option[NonEmptySet[String]] =
    NonEmptySet.from(
      mod.pack.name
        .fold(Set.empty[String])(guessKeywords) union guessKeywords(
        mod.manifest.name
      )
    )

  def createAlternateTexturesMod(
      mod: CustomFurnitureMod,
      alternateTexturesFor: Iterable[FurnitureData]
  ): AlternativeTexturesMod = {
    val validTargets = alternateTexturesFor.filterNot(data =>
      data.name.contains(':') || data.name.startsWith("'")
    )
    val extractedSprites =
      spriteExtractor.extractSprites(mod).map { case (cf, sprite) =>
        cf -> alternativeTexturesSpriteConverter.convert(cf, sprite)
      }
    val textures = mod.pack.furniture
      .flatMap { customFurniture =>
        if (
          customFurniture.`type`.defaultBoundingBoxSize.exists(default =>
            default.width != customFurniture.boxWidth || default.height != customFurniture.boxHeight
          ) //|| customFurniture.`type`.defaultTilesheetSize.exists( default => default.width != customFurniture.width || default.height != customFurniture.height )
        ) {
          logger.warn(
            s"${customFurniture.id} ${customFurniture.name} has non-default sizing and so matching will be unusual. Double checking these values may be a good idea."
          )
        }
        findMatches(customFurniture, validTargets).toSeq
          .tap { matches =>
            if (matches.isEmpty)
              logger.warn(
                s"No matches found for ${customFurniture.id} ${customFurniture.name} so it will not be converted"
              )
          }
          .map(_ -> customFurniture)
      }
      .groupMap(_._1)(_._2)
      .map { case (alternateFor, cfs) =>
        alternateFor -> cfs.sortBy(_.id)
      }
      .map { case (alternateFor, cfs) =>
        Texture(
          alternateFor.name,
          TextureType.Furniture,
          textureWidthFromFurnitureData(alternateFor),
          textureHeightFromFurnitureData(alternateFor),
          cfs.size,
          keywords = modKeywords(mod).map(_.toSet),
          manualVariations = Some(cfs.zipWithIndex.map { case (cf, i) =>
            ManualVariation(
              i,
              name = Some(cf.name),
              keywords = NonEmptySet
                .from(guessKeywords(s"${cf.name} ${cf.description}"))
                .map(_.toSet)
            )
          })
        ) -> cfs
          .map(extractedSprites)
      }
    textures.foreach { case (texture, sprites) =>
      sprites.zipWithIndex.foreach { case (sprite, i) =>
        if (
          sprite.widthInPixels != texture.textureWidth || sprite.heightInPixels != texture.textureHeight
        )
          logger.warn(
            s"Sprite $i for texture ${texture.itemName} has size ${sprite.widthInPixels} x ${sprite.heightInPixels} while texture is set to ${texture.textureWidth} x ${texture.textureHeight}"
          )
      }
    }
    // TODO: Model conversion results
    AlternativeTexturesMod(convertManifest(mod.manifest), textures)
  }
}

object AlternativeTexturesConverter {

  final case class CustomFurnitureVariation(customFurniture: CustomFurniture)

  def apply(
      spriteExtractor: CustomFurnitureSpriteExtractor,
      spriteConverter: AlternativeTexturesSpriteConverter
  ): AlternativeTexturesConverter =
    new AlternativeTexturesConverter(spriteExtractor, spriteConverter)
}
