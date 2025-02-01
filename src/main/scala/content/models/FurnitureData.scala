package quincyjo.stardew
package content.models

import cats.syntax.traverse._
import io.circe.Decoder

case class FurnitureData(id: String,
                         name: String,
                         furnitureType: FurnitureType,
                         tilesheetSize: FurnitureSize,
                         boundingBoxSize: FurnitureSize,
                         rotations: Rotations,
                         price: Int,
                         displayName: Option[String] = None) {

  override def toString: String =
    s"$name/$furnitureType/$tilesheetSize/$boundingBoxSize/$rotations/$price"

  def boundingBoxWidth: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultBoundingBoxSize.get.width)(_.width)
  def boundingBoxHeight: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultBoundingBoxSize.get.height)(_.height)
  def tilesheetWidth: Int =
    tilesheetSize.asSize
      .fold(furnitureType.defaultTilesheetSize.get.width)(_.width)
  def tilesheetHeight: Int =
    tilesheetSize.asSize
      .fold(furnitureType.defaultTilesheetSize.get.height)(_.height)
  def rotatedBoundingBoxWidth: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultRotatedBoundingBoxSize.get.width)(_.height)
  def rotatedBoundingBoxHeight: Int =
    boundingBoxSize.asSize
      .fold(furnitureType.defaultRotatedBoundingBoxSize.get.height)(_.width)
}

object FurnitureData {

  def fromString(id: String, string: String): Option[FurnitureData] = {
    val values = string.split('/').toList
    for {
      name <- values.headOption
      furnitureType <- values.lift(1).flatMap(FurnitureType.fromString)
      tilesheetSize <- values.lift(2).flatMap(FurnitureSize.fromString)
      boundingBlockSize <- values.lift(3).flatMap(FurnitureSize.fromString)
      rotations <- values.lift(4).flatMap(Rotations.fromString)
      price <- values.lift(5).flatMap(_.toIntOption)
    } yield
      FurnitureData(
        id,
        name,
        furnitureType,
        tilesheetSize,
        boundingBlockSize,
        rotations,
        price
      )
  }

  implicit val decodeFurnitureData: Decoder[Seq[FurnitureData]] =
    implicitly[Decoder[Map[String, String]]].emap {
      _.toSeq.traverse {
        case (key, value) =>
          fromString(key, value).toRight(
            s"$value is not a valid furniture data encoding'"
          )
      }
    }
}
