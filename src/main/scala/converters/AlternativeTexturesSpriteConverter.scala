package quincyjo.stardew
package converters

import content.models.FurnitureType
import converters.AlternativeTexturesSpriteConverter.AlternativeTexturesSpriteConverterOptions
import customfurniture.models.CustomFurniture
import shared.models.Sprite

import java.awt.image.BufferedImage

class AlternativeTexturesSpriteConverter(
  options: AlternativeTexturesSpriteConverterOptions =
    AlternativeTexturesSpriteConverterOptions.default
) {

  def convert(customFurniture: CustomFurniture, sprite: Sprite): Sprite = {
    if (options.expandWithFurnitureFront && canCreateFurnitureFront(
          customFurniture,
          sprite
        )) {
      expandSpriteWithFurnitureFront(customFurniture, sprite)
    } else sprite
  }

  def canCreateFurnitureFront(customFurniture: CustomFurniture,
                              sprite: Sprite): Boolean =
    customFurniture.`type` match {
      case hasFront: FurnitureType.FurnitureFrontType =>
        sprite.heightInTiles == customFurniture.spriteHeight
      case _ =>
        false
    }

  def expandSpriteWithFurnitureFront(customFurniture: CustomFurniture,
                                     sprite: Sprite): Sprite = {
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

  /**
    *
    * @param expandSpritesForFurnitureFront If sprites should be expanded to include space for alternative textures furniture front layer. IE, double the sprite height.
    * @param expandSpriteForRotations If a lower rotation sprite should be expanded into each rotation of the target. EG, expanding a stool sprite to be a four rotation sprite for a chair.
    * @param copyFourthRotationToFront If expanded sprites for the furniture front layer, copy the fourth rotation sprite into the furniture front rotation.
    */
  final case class AlternativeTexturesSpriteConverterOptions(
    expandWithFurnitureFront: Boolean = true,
    copyFourthRotationToFront: Boolean = true,
    expandSpritesRotations: Boolean = true,
    //furnitureFront: FurnitureFrontOptions = FurnitureFrontOptions.default
  )

  def apply(
    options: AlternativeTexturesSpriteConverterOptions
  ): AlternativeTexturesSpriteConverter =
    new AlternativeTexturesSpriteConverter(options)

  final case class FurnitureFrontOptions(expandSprites: Boolean = true,
                                         copyFourthRotation: Boolean = true)

  object FurnitureFrontOptions {

    final val default: FurnitureFrontOptions = FurnitureFrontOptions()
  }

  object AlternativeTexturesSpriteConverterOptions {

    final val default: AlternativeTexturesSpriteConverterOptions =
      AlternativeTexturesSpriteConverterOptions()
  }
}
