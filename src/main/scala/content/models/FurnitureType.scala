package quincyjo.stardew
package content.models

import io.circe.generic.extras.Configuration
import io.circe.{Decoder, Encoder}

sealed trait FurnitureType {
  def defaultTilesheetSize: Option[FurnitureSize.Size]
  def defaultBoundingBoxSize: Option[FurnitureSize.Size]
  def defaultRotatedBoundingBoxSize: Option[FurnitureSize.Size] =
    defaultBoundingBoxSize.map(_.inverse)
}

object FurnitureType {

  /**
    * Furniture type that has a furniture front layer.
    */
  sealed trait FurnitureFrontType extends FurnitureType

  final case object Chair extends FurnitureFrontType {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 1))
  }

  final case object Bench extends FurnitureFrontType {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))
  }

  final case object Armchair extends FurnitureFrontType {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))
    override val defaultRotatedBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))
  }

  final case object Couch extends FurnitureFrontType {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 2))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 1))
    override val defaultRotatedBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))
  }

  final case object Dresser extends FurnitureType {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))
  }

  final case object LongTable extends FurnitureType {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(5, 3))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(5, 2))
    override val defaultRotatedBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 4))
  }

  final case object Table extends FurnitureType {
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 3))
  }

  final case object Bookcase extends FurnitureType {
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 3))
  }

  final case object Painting extends FurnitureType {
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))
  }

  sealed trait LampLike extends FurnitureType
  final case object Lamp extends LampLike {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 3))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 1))
  }

  final case object Sconce extends LampLike {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))
  }

  final case object Rug extends FurnitureType {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 2))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 2))
  }

  final case object Window extends FurnitureType {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))
  }

  final case object Fireplace extends FurnitureType {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 5))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))
  }

  sealed trait BedLike extends FurnitureType
  final case object Bed extends BedLike {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 3))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 4))
  }

  final case object BedDouble extends BedLike {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 3))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 4))
  }

  final case object BedChild extends BedLike {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 3))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 4))
  }

  final case object Torch extends FurnitureType {
    val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 1))
  }

  final case object Fishtank extends FurnitureType { // No Defaults
    val defaultTilesheetSize: Option[FurnitureSize.Size] = None
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] = None
  }

  final case object Decor extends FurnitureType { // No defaults
    val defaultTilesheetSize: Option[FurnitureSize.Size] = None
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] = None
  }

  final case object Other extends FurnitureType { // Only a CF type
    val defaultTilesheetSize: Option[FurnitureSize.Size] = None
    val defaultBoundingBoxSize: Option[FurnitureSize.Size] = None
  }

  def fromString(string: String): Option[FurnitureType] = string match {
    case "chair" | "stool" => Some(Chair)
    case "bench"           => Some(Bench)
    case "armchair"        => Some(Armchair)
    case "couch" | "sofa"  => Some(Couch)
    case "dresser"         => Some(Dresser)
    case "long table"      => Some(LongTable)
    case "table"           => Some(Table)
    case "bookcase"        => Some(Bookcase)
    case "painting"        => Some(Painting)
    case "lamp"            => Some(Lamp)
    case "rug"             => Some(Rug)
    case "window"          => Some(Window)
    case "fireplace"       => Some(Fireplace)
    case "bed"             => Some(Bed)
    case "bed double"      => Some(BedDouble)
    case "bed child"       => Some(BedChild)
    case "torch"           => Some(Torch)
    case "sconce"          => Some(Sconce)
    case "fishtank"        => Some(Fishtank)
    case "decor"           => Some(Decor)
    case "other"           => Some(Other)
    case _                 => None
  }

  implicit val config: Configuration =
    Configuration.default.copy(transformConstructorNames = string => {
      val (first, tail) = string.splitAt(1)
      tail
        .prependedAll(first.toLowerCase)
        .replaceAll("[A-Z]", " $0")
        .toLowerCase
    })

  implicit val encodeFurnitureType: Encoder[FurnitureType] =
    Encoder.encodeString.contramap {
      case Chair     => "chair"
      case Bench     => "bench"
      case Armchair  => "armchair"
      case Couch     => "couch"
      case Dresser   => "dresser"
      case LongTable => "long table"
      case Table     => "table"
      case Bookcase  => "bookcase"
      case Painting  => "painting"
      case Lamp      => "lamp"
      case Rug       => "rug"
      case Window    => "window"
      case Fireplace => "fireplace"
      case Bed       => "bed"
      case BedDouble => "bed double"
      case BedChild  => "bed child"
      case Torch     => "torch"
      case Sconce    => "sconce"
      case Fishtank  => "fishtank"
      case Decor     => "decor"
      case Other     => "other"
    }

  implicit val decodeFurnitureType: Decoder[FurnitureType] =
    Decoder.decodeString.emap { string =>
      fromString(string.toLowerCase)
        .toRight(s"$string is not a valid furniture type")
    }
}
