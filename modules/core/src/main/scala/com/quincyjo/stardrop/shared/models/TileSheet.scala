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

import cats.implicits._
import TileSheet.{TILE_HEIGHT, TILE_WIDTH}
import cats.effect.Async
import org.slf4j.{Logger, LoggerFactory}

import java.awt.image.BufferedImage
import java.io.IOException
import javax.imageio.ImageIO
import scala.reflect.io.File

// TODO: Add error handling for out of range.
/** Represents a tilesheet containing one or more sprites. Tiles within a
  * Tilesheet are indexed from 0 from left to right in rows.
  * @param name
  *   The name of the tile sheet.
  * @param image
  *   The image of the tile sheet.
  * @param widthInTiles
  *   The width of the tile sheet in tiles.
  * @param heightInTiles
  *   The height of the tile sheet in tiles.
  */
final case class TileSheet private (
    name: String,
    image: BufferedImage,
    widthInTiles: Int,
    heightInTiles: Int
) {

  /** Determines the total number of tiles in the tile sheet.
    */
  val tileCount: Int = heightInTiles * widthInTiles

  /** Determines the x index of the tile at the given index.
    * @param index
    *   The index of the desired tile.
    * @return
    *   The x index of the tile.
    */
  def linearIndexToXIndex(index: Int): Int =
    index % widthInTiles

  /** Determines the y index of the tile at the given index.
    * @param index
    *   The index of the desired tile.
    * @return
    *   The y index of the tile.
    */
  def linearIndexToYIndex(index: Int): Int =
    index / widthInTiles

  /** Retrieves the sprite at the given index with the given width and height.
    * @param index
    *   The index of the sprite, taken as the top left corner.
    * @param width
    *   The width of the sprite in tiles.
    * @param height
    *   The height of the sprite in tiles.
    * @return
    *   The sprite at the given index.
    */
  def getSprite(index: Int, width: Int, height: Int): Sprite =
    Sprite(
      image.getSubimage(
        linearIndexToXIndex(index) * TILE_WIDTH,
        linearIndexToYIndex(index) * TILE_HEIGHT,
        width * TILE_WIDTH,
        height * TILE_HEIGHT
      )
    )

  /** Converts the tile sheet to a sprite.
    * @return
    *   The sprite.
    */
  def toSprite: Sprite =
    Sprite(image)
}

object TileSheet {

  private val logger: Logger = LoggerFactory.getLogger("Tilesheet")

  /** The height of a tile in pixels.
    */
  final val TILE_HEIGHT = 16

  /** The width of a tile in pixels.
    */
  final val TILE_WIDTH = 16

  def fromFile[F[_]: Async](
      file: java.io.File
  ): F[TileSheet] =
    Async[F].blocking(ImageIO.read(file)).flatMap { image =>
      fromImage(file.getName, image).fold(
        error => Async[F].raiseError(new IOException(error)),
        Async[F].pure(_)
      )
    }

  def fromFile[F[_]: Async](file: File): F[TileSheet] =
    fromFile[F](file.jfile)

  /** Creates a tile sheet from an image. If the image is not divisible into
    * tiles, then an error string is returned in the left instead.
    * @param name
    *   The name of the tilesheet.
    * @param image
    *   The image of the tilesheet.
    * @return
    *   Either the tile sheet or an error string.
    */
  def fromImage(
      name: String,
      image: BufferedImage
  ): Either[String, TileSheet] = {
    if (
      !(image.getWidth % TILE_WIDTH == 0 && image.getHeight % TILE_HEIGHT == 0)
    )
      logger.warn(
        s"Image must be buildable in $TILE_WIDTH x $TILE_HEIGHT tiles, but was ${image.getWidth} x ${image.getHeight}"
      )
    Right(
      new TileSheet(
        name,
        image,
        image.getWidth / TILE_WIDTH,
        image.getHeight / TILE_HEIGHT
      )
    )
  }
}
