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

import com.quincyjo.stardrop.customfurniture.CustomFurnitureMod
import com.quincyjo.stardrop.customfurniture.models.CustomFurniture
import CustomFurnitureSpriteExtractor.SpriteExtractorOptions
import com.quincyjo.stardrop.content.models.FurnitureType
import com.quincyjo.stardrop.shared.models.Sprite
import org.slf4j.{Logger, LoggerFactory}

import scala.util.chaining.scalaUtilChainingOps

class CustomFurnitureSpriteExtractor(
    options: SpriteExtractorOptions = SpriteExtractorOptions.default
) {

  private val logger: Logger =
    LoggerFactory.getLogger("CustomFurnitureSpriteExtractor")

  def extractSprites(mod: CustomFurnitureMod): Map[CustomFurniture, Sprite] =
    mod.pack.furniture.map { customfurniture =>
      customfurniture -> extractSprite(customfurniture, mod)
    }.toMap

  def extractSprite(
      customFurniture: CustomFurniture,
      mod: CustomFurnitureMod
  ): Sprite = {
    logger.debug(
      s"Extracting sprite ${customFurniture.id}: ${customFurniture.name}"
    )
    mod.tileSheets
      .find(_.name == customFurniture.texture)
      .orElse {
        mod.tileSheets
          .find(_.name.toLowerCase == customFurniture.texture.toLowerCase)
          .tap { alternate =>
            if (alternate.isDefined) {
              logger.warn(
                s"Could note find tile sheet ${customFurniture.texture} for ${customFurniture.id}: ${customFurniture.name}, but found a match ignoring case."
              )
            }
          }
      }
      .getOrElse(
        throw new RuntimeException(
          s"Could note find tile sheet ${customFurniture.texture} for ${customFurniture.id}: ${customFurniture.name}"
        )
      )
      .getSprite(
        customFurniture.index,
        customFurniture.spriteWidth,
        customFurniture.`type` match {
          case _: FurnitureType.HasFrontLayer
              if options.takeFurnitureFrontFromTilesheet =>
            customFurniture.spriteHeight * 2
          case _ =>
            customFurniture.spriteHeight
        }
      )
  }

}

object CustomFurnitureSpriteExtractor {

  def apply(options: SpriteExtractorOptions): CustomFurnitureSpriteExtractor =
    new CustomFurnitureSpriteExtractor(options)

  final case class SpriteExtractorOptions(
      takeFurnitureFrontFromTilesheet: Boolean = false
  )

  object SpriteExtractorOptions {
    final val default = SpriteExtractorOptions()
  }
}
