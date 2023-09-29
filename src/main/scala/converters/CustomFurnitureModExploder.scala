package quincyjo.stardew
package converters

import customfurniture.CustomFurnitureMod
import customfurniture.models.CustomFurniture
import shared.models.{Sprite, TileSheet}

final case class CustomFurnitureModExploder(
  spriteExtractor: CustomFurnitureSpriteExtractor,
  mapSprites: Option[(CustomFurniture, Sprite) => Sprite] = None
) {

  def explode(mod: CustomFurnitureMod): CustomFurnitureMod = {
    spriteExtractor
      .extractSprites(mod)
      .map {
        case (furniture, sprite) =>
          //val textureName = furniture.name.replaceAll(" ", "_") + ".png"
          val textureName = furniture.id.toString + ".png"
          val finalSprite = mapSprites.fold(sprite)(_(furniture, sprite))
          furniture.copy(texture = textureName, index = 0) ->
            TileSheet
              .fromImage(textureName, finalSprite.image)
              .fold(m => throw new RuntimeException(m), identity)
      }
      .unzip match {
      case (furniture, tilesheets) =>
        CustomFurnitureMod(
          mod.manifest,
          mod.pack.copy(furniture = furniture.toVector),
          tilesheets.toVector
        )
    }
  }
}

object CustomFurnitureModExploder {

  def apply(
    spriteExtractor: CustomFurnitureSpriteExtractor,
    mapSprites: Option[(CustomFurniture, Sprite) => Sprite] = None
  ): CustomFurnitureModExploder =
    new CustomFurnitureModExploder(spriteExtractor, mapSprites)
}
