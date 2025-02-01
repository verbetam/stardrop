package quincyjo.stardew
package alternativetextures

import content.models.FurnitureType.{BedLike, FurnitureFrontType, LampLike}
import content.models.Rotations.{Four, One, Two}
import content.models.{FurnitureData, Rotations}

object Util {

  def textureWidthFromFurnitureData(data: FurnitureData): Int = {
    data.furnitureType match {
      case _: LampLike | _: BedLike =>
        data.tilesheetWidth * 2 * 16
      case other =>
        (data.rotations match {
          case One => data.tilesheetWidth
          case Two | Four =>
            data.rotations.value / 2 * data.tilesheetWidth + // TODO: One off for couch because of change in bounding box ratio through rotation
              data.rotatedBoundingBoxWidth
        }) * 16
    }
  }

  def textureHeightFromFurnitureData(data: FurnitureData): Int = {
    val layerHeight = (data.rotations match {
      case Rotations.One => data.tilesheetHeight
      case _ => //Vector(data.tilesheetHeight, data.tilesheetWidth).max
        Vector(
          data.tilesheetHeight - data.boundingBoxHeight + data.rotatedBoundingBoxHeight,
          data.tilesheetHeight
        ).max // TODO: Add to rotated bounding box height instead of regular bounding box width
    }) * 16
    data.furnitureType match {
      case f: FurnitureFrontType => layerHeight * 2
      case _                     => layerHeight
    }
  }
}
