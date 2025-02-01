package quincyjo.stardew
package converters

import content.models.FurnitureType
import converters.CustomFurnitureSpriteExtractor.SpriteExtractorOptions
import customfurniture.CustomFurnitureMod
import customfurniture.models.CustomFurniture
import shared.models.Sprite

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

  def extractSprite(customFurniture: CustomFurniture,
                    mod: CustomFurnitureMod): Sprite = {
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
          case _: FurnitureType.FurnitureFrontType
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
    takeFurnitureFrontFromTilesheet: Boolean = false,
  )

  object SpriteExtractorOptions {
    final val default = SpriteExtractorOptions()
  }
}
