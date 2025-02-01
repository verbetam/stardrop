package quincyjo.stardew
package shared.models

import shared.models.TileSheet.{TILE_HEIGHT, TILE_WIDTH}

import java.awt.image.BufferedImage

case class Sprite(image: BufferedImage) {

  val widthInPixels: Int = image.getWidth
  val heightInPixels: Int = image.getHeight
  val widthInTiles: Int = widthInPixels / TILE_WIDTH
  val heightInTiles: Int = heightInPixels / TILE_HEIGHT
}

object Sprite {}
