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

package com.quincyjo.stardrop.shared.models

import TileSheet.{TILE_HEIGHT, TILE_WIDTH}

import java.awt.image.BufferedImage

/** Describes a single sprite, that is to say an image which is typically
  * associated with a single item.
  * @param image
  *   The image of the sprite.
  */
final case class Sprite(image: BufferedImage) {

  /** The width of the sprite in pixels.
    */
  val widthInPixels: Int =
    image.getWidth

  /** The height of the sprite in pixels.
    */
  val heightInPixels: Int =
    image.getHeight

  /** The width of the sprite in tiles.
    */
  val widthInTiles: Int =
    widthInPixels / TILE_WIDTH

  /** The height of the sprite in tiles.
    */
  val heightInTiles: Int =
    heightInPixels / TILE_HEIGHT
}

object Sprite {

  /** Constructs a sprite from an image only if the image is of valid size. This
    * is determined by it having both a width and height of at least one tile
    * and having a height and width that can be exactly measured in tiles. even
    * @param image
    *   The image containing the sprite.
    * @return
    *   An option containing the sprite, if the image is valid, otherwise None
    */
  def fromImage(image: BufferedImage): Option[Sprite] =
    Option.when(
      image.getWidth > 0 && image.getHeight > 0 &&
        image.getWidth % TILE_WIDTH == 0 && image.getHeight % TILE_HEIGHT == 0
    ) {
      Sprite(image)
    }
}
