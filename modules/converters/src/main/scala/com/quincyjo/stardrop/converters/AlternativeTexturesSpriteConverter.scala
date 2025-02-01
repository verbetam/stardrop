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

import com.quincyjo.stardrop.customfurniture.models.CustomFurniture
import AlternativeTexturesSpriteConverter.AlternativeTexturesSpriteConverterOptions
import com.quincyjo.stardrop.content.models.FurnitureType
import com.quincyjo.stardrop.shared.models.Sprite

import java.awt.image.BufferedImage

class AlternativeTexturesSpriteConverter(
    options: AlternativeTexturesSpriteConverterOptions =
      AlternativeTexturesSpriteConverterOptions.default
) {

  def convert(customFurniture: CustomFurniture, sprite: Sprite): Sprite = {
    if (
      options.expandWithFurnitureFront && canCreateFurnitureFront(
        customFurniture,
        sprite
      )
    ) {
      expandSpriteWithFurnitureFront(customFurniture, sprite)
    } else sprite
  }

  def canCreateFurnitureFront(
      customFurniture: CustomFurniture,
      sprite: Sprite
  ): Boolean =
    customFurniture.`type` match {
      case _: FurnitureType.FurnitureFrontType =>
        sprite.heightInTiles == customFurniture.spriteHeight
      case _ =>
        false
    }

  def expandSpriteWithFurnitureFront(
      customFurniture: CustomFurniture,
      sprite: Sprite
  ): Sprite = {
    val expandedSprite = new BufferedImage(
      sprite.widthInPixels,
      sprite.heightInPixels * 2,
      BufferedImage.TYPE_INT_ARGB
    )
    val graphics = expandedSprite.createGraphics()
    graphics.drawImage(sprite.image, 0, 0, null)
    if (options.copyFourthRotationToFront && customFurniture.rotations == 4) {
      // Duplicate facing away rotation as layer 2 for that rotation
      graphics.drawImage(
        sprite.image.getSubimage(
          sprite.widthInPixels - customFurniture.width * 16,
          0,
          customFurniture.width * 16,
          customFurniture.height * 16
        ),
        customFurniture.rotatedWidth
          .fold(customFurniture.width * 2)(_ + customFurniture.width) * 16,
        customFurniture.rotatedHeight.toList
          .appended(customFurniture.height)
          .max * 16,
        null
      )
    }
    graphics.dispose()
    Sprite(expandedSprite)
  }
}

object AlternativeTexturesSpriteConverter {

  /** @param expandSpritesForFurnitureFront
    *   If sprites should be expanded to include space for alternative textures
    *   furniture front layer. IE, double the sprite height.
    * @param expandSpriteForRotations
    *   If a lower rotation sprite should be expanded into each rotation of the
    *   target. EG, expanding a stool sprite to be a four rotation sprite for a
    *   chair.
    * @param copyFourthRotationToFront
    *   If expanded sprites for the furniture front layer, copy the fourth
    *   rotation sprite into the furniture front rotation.
    */
  final case class AlternativeTexturesSpriteConverterOptions(
      expandWithFurnitureFront: Boolean = true,
      copyFourthRotationToFront: Boolean = true,
      expandSpritesRotations: Boolean = true
      //furnitureFront: FurnitureFrontOptions = FurnitureFrontOptions.default
  )

  def apply(
      options: AlternativeTexturesSpriteConverterOptions
  ): AlternativeTexturesSpriteConverter =
    new AlternativeTexturesSpriteConverter(options)

  final case class FurnitureFrontOptions(
      expandSprites: Boolean = true,
      copyFourthRotation: Boolean = true
  )

  object FurnitureFrontOptions {

    final val default: FurnitureFrontOptions = FurnitureFrontOptions()
  }

  object AlternativeTexturesSpriteConverterOptions {

    final val default: AlternativeTexturesSpriteConverterOptions =
      AlternativeTexturesSpriteConverterOptions()
  }
}
