package quincyjo.stardew
package shared.models

import shared.models.TileSheet.{TILE_HEIGHT, TILE_WIDTH}

import org.slf4j.{Logger, LoggerFactory}

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import scala.reflect.io.File

case class TileSheet private (name: String,
                              image: BufferedImage,
                              widthInTiles: Int,
                              heightInTiles: Int) {

  val tileCount: Int = heightInTiles * widthInTiles

  def linearIndexToXIndex(index: Int): Int = index % widthInTiles
  def linearIndexToYIndex(index: Int): Int = index / widthInTiles

  def getSprite(index: Int, width: Int, height: Int): Sprite =
    Sprite(
      image.getSubimage(
        linearIndexToXIndex(index) * TILE_WIDTH,
        linearIndexToYIndex(index) * TILE_HEIGHT,
        width * TILE_WIDTH,
        height * TILE_HEIGHT
      )
    )

  def toSprite: Sprite =
    Sprite(image)
}

object TileSheet {

  private val logger: Logger = LoggerFactory.getLogger("Tilesheet")

  final val TILE_HEIGHT = 16
  final val TILE_WIDTH = 16

  def fromFile(file: File): Either[String, TileSheet] =
    fromImage(file.name, ImageIO.read(file.jfile))

  def fromFile(file: java.io.File): Either[String, TileSheet] =
    fromImage(file.getName, ImageIO.read(file))

  def fromImage(name: String,
                image: BufferedImage): Either[String, TileSheet] = {
    if (!(image.getWidth % TILE_WIDTH == 0 && image.getHeight % TILE_HEIGHT == 0))
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
