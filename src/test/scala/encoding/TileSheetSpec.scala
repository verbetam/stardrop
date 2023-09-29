package quincyjo.stardew
package encoding

import shared.models.TileSheet
import shared.models.TileSheet.{TILE_HEIGHT, TILE_WIDTH}

import org.scalatest.Ignore
import org.scalatest.flatspec.AnyFlatSpecLike

import java.awt.image.BufferedImage

class TileSheetSpec extends AnyFlatSpecLike with UnitSpecLike {

  private val givenTileWidth = 16
  private val givenTileHeight = 32
  private val givenImageType = BufferedImage.TYPE_INT_ARGB
  private val givenImageWidth = TILE_WIDTH * givenTileWidth
  private val givenImageHeight = TILE_HEIGHT * givenTileHeight
  private val givenImageName = "TextTextureName.png"
  private val givenImage: BufferedImage =
    new BufferedImage(givenImageWidth, givenImageHeight, givenImageType)

  private val givenTileSheet: TileSheet =
    TileSheet.fromImage(givenImageName, givenImage).value

  "fromImage" should "calculate the tile width and height of the image" in {
    TileSheet.fromImage(givenImageName, givenImage).value match {
      case TileSheet(name, image, widthInTiles, heightInTiles) =>
        name should be(givenImageName)
        image should be(givenImage)
        widthInTiles should be(16)
        heightInTiles should be(32)
    }
  }

  // TODO: Moved to warning
  ignore should "fail if the image is not evenly divisible into tile" in {
    val badWidth = (TILE_WIDTH * 1.5).toInt
    val badHeight = (TILE_WIDTH * 1.5).toInt
    assert(badWidth % TILE_WIDTH != 0)
    assert(badHeight % TILE_HEIGHT != 0)
    val badImage = new BufferedImage(badWidth, badHeight, givenImageType)

    TileSheet.fromImage(givenImageName, badImage).left.value should (
      include(TILE_WIDTH.toString) and include(TILE_HEIGHT.toString) and include(
        badImage.getWidth.toString
      ) and include(badImage.getHeight.toString)
    )
  }

  "linearIndexToYIndex" should "convert the index into ax y index" in {
    val expectations = Table(
      ("linear", "x"),
      (0, 0),
      (givenTileWidth / 2, givenTileWidth / 2),
      (givenTileWidth, 0),
      (givenTileWidth + 1, 1),
      (givenTileWidth * 2, 0)
    )

    forAll(expectations) {
      case (linear, x) =>
        givenTileSheet.linearIndexToXIndex(linear) should be(x)
    }
  }

  "linearIndexToXIndex" should "convert the index into an x index" in {
    val expectations = Table(
      ("linear", "y"),
      (0, 0),
      (givenTileWidth / 2, 0),
      (givenTileWidth, 1),
      (givenTileWidth * 2, 2),
      (givenTileWidth * 2 + givenTileWidth / 2, 2)
    )

    forAll(expectations) {
      case (linear, y) =>
        givenTileSheet.linearIndexToYIndex(linear) should be(y)
    }
  }
}
