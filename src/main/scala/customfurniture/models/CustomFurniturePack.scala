package quincyjo.stardew.customfurniture.models

import io.circe.Codec
import io.circe.generic.semiauto.deriveCodec

final case class CustomFurniturePack(furniture: Vector[CustomFurniture],
                                     useid: Option[String],
                                     author: Option[String],
                                     name: Option[String],
                                     version: Option[String])

object CustomFurniturePack {
  implicit val codecForPack: Codec[CustomFurniturePack] = deriveCodec
}
