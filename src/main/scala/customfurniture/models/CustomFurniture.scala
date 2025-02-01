package quincyjo.stardew
package customfurniture.models

import content.models.FurnitureType

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

final case class CustomFurniture(id: Int,
                                 texture: String,
                                 name: String,
                                 description: String,
                                 `type`: FurnitureType,
                                 index: Int,
                                 width: Int,
                                 height: Int,
                                 boxWidth: Int,
                                 boxHeight: Int,
                                 rotations: Int = 1,
                                 instantGift: Option[String] = None,
                                 shopkeeper: Option[String] = None,
                                 conditions: Option[String] = None,
                                 sellAtShop: Option[Boolean] = None,
                                 textureOverlay: Option[String] = None,
                                 textureUnderlay: Option[String] = None,
                                 price: Option[Int] = None,
                                 rotatedWidth: Option[Int] = None,
                                 rotatedHeight: Option[Int] = None,
                                 rotatedBoxWidth: Option[Int] = None,
                                 rotatedBoxHeight: Option[Int] = None,
                                 setWidth: Option[Int] = None,
                                 fps: Option[Int] = None,
                                 folderName: Option[String] = None,
                                 fromContent: Option[Boolean] = None) {

  def spriteWidth: Int = {
    val base = rotations match {
      case 1 => width
      case 2 => width + rotatedWidth.getOrElse(width)
      case 4 => width * 2 + rotatedWidth.getOrElse(width)
    }
    `type` match { // CustomFurniture does not support furniture front layer
      case _: FurnitureType.LampLike | _: FurnitureType.BedLike => base * 2
      case _                                                    => base
    }
  }

  def spriteHeight: Int = rotatedHeight.fold(height)(
    rotatedHeight => if (rotatedHeight > height) rotatedHeight else height
  )
}

object CustomFurniture {

  implicit val config: Configuration = Configuration.default.withDefaults
  implicit val codecForCustomFurniture: Codec[CustomFurniture] =
    deriveConfiguredCodec
}
