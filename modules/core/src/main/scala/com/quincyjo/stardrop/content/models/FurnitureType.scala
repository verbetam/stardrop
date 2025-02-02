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

package com.quincyjo.stardrop.content.models

import io.circe.generic.extras.Configuration
import io.circe.{Decoder, Encoder}

/** ADT for the different furniture types that exist in Stardew. These contain
  * information about that furniture type such as the default tilesheet and
  * bounding box sizes. This is useful for inferring size information for a
  * piece of furniture that does not have it explicitly stated. This is used for
  * finding appropriate matches across textures to furniture.
  */
sealed trait FurnitureType {

  /** The default tilesheet size for this furniture type.
    * @return
    *   Default tilesheet size as a [[FurnitureSize.Size]].
    */
  def defaultTilesheetSize: Option[FurnitureSize.Size]

  /** The default bounding box size for this furniture type.
    * @return
    *   Default bounding box size as a [[FurnitureSize.Size]].
    */
  def defaultBoundingBoxSize: Option[FurnitureSize.Size]

  /** The default rotated bounding box size for this furniture type. By default,
    * this is inferred from the default bounding box size via
    * [[com.quincyjo.stardrop.content.models.FurnitureSize.Size.inverse]], but
    * may be overridden when necessary such as in armchair furniture.
    * @return
    *   Default rotated bounding box size as a [[FurnitureSize.Size]].
    */
  def defaultRotatedBoundingBoxSize: Option[FurnitureSize.Size] =
    defaultBoundingBoxSize.map(_.inverse)
}

object FurnitureType {

  /** Mixin type that has a furniture front layer, meaning that there is a
    * separate sprite for the front layer which is overlayed in front of the
    * player. This allows for the arms and backs of furniture such as couches to
    * be visible in front of the player sprite. Such as a couch which is
    * oriented with the seat pointing upward, when the player sits in it, the
    * back of the couch is displayed on top of the player sprite.
    */
  sealed trait HasFrontLayer { self: FurnitureType => }

  final case object Chair extends FurnitureType with HasFrontLayer {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 1))
  }

  final case object Bench extends FurnitureType with HasFrontLayer {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))
  }

  final case object Armchair extends FurnitureType with HasFrontLayer {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))

    override val defaultRotatedBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))
  }

  final case object Couch extends FurnitureType with HasFrontLayer {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 2))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 1))

    override val defaultRotatedBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))
  }

  final case object Dresser extends FurnitureType {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))
  }

  final case object LongTable extends FurnitureType {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(5, 3))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(5, 2))

    override val defaultRotatedBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 4))
  }

  final case object Table extends FurnitureType {

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 3))
  }

  final case object Bookcase extends FurnitureType {

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 3))
  }

  final case object Painting extends FurnitureType {

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 2))
  }

  sealed trait LampLike extends FurnitureType

  final case object Lamp extends LampLike {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 3))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 1))
  }

  final case object Sconce extends LampLike {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))
  }

  final case object Rug extends FurnitureType {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 2))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 2))
  }

  final case object Window extends FurnitureType {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))
  }

  final case object Fireplace extends FurnitureType {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 5))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 1))
  }

  sealed trait BedLike extends FurnitureType

  final case object Bed extends BedLike {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 3))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 4))
  }

  final case object BedDouble extends BedLike {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 3))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(3, 4))
  }

  final case object BedChild extends BedLike {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 3))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(2, 4))
  }

  final case object Torch extends FurnitureType {

    override val defaultTilesheetSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 2))

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] =
      Some(FurnitureSize.Size(1, 1))
  }

  final case object Fishtank extends FurnitureType { // No Defaults

    override val defaultTilesheetSize: Option[FurnitureSize.Size] = None

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] = None
  }

  final case object Decor extends FurnitureType { // No defaults

    override val defaultTilesheetSize: Option[FurnitureSize.Size] = None

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] = None
  }

  final case object Other extends FurnitureType { // Only a CF type

    override val defaultTilesheetSize: Option[FurnitureSize.Size] = None

    override val defaultBoundingBoxSize: Option[FurnitureSize.Size] = None
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
      case Decor     => "decorFurnitureFrontType"
      case Other     => "other"
    }

  implicit val decodeFurnitureType: Decoder[FurnitureType] =
    Decoder.decodeString.emap { string =>
      fromString(string.toLowerCase)
        .toRight(s"$string is not a valid furniture type")
    }
}
