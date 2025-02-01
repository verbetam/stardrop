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
import com.quincyjo.stardrop.shared.models.{Sprite, TileSheet}

final case class CustomFurnitureModExploder(
    spriteExtractor: CustomFurnitureSpriteExtractor,
    mapSprites: Option[(CustomFurniture, Sprite) => Sprite] = None
) {

  def explode(mod: CustomFurnitureMod): CustomFurnitureMod = {
    spriteExtractor
      .extractSprites(mod)
      .map { case (furniture, sprite) =>
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
