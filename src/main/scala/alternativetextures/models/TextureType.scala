package quincyjo.stardew
package alternativetextures.models

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

sealed trait TextureType

object TextureType {
  final case object Character extends TextureType
  final case object Craftable extends TextureType
  final case object Decoration extends TextureType
  final case object Grass extends TextureType
  final case object Tree extends TextureType
  final case object Flooring extends TextureType
  final case object FruitTree extends TextureType
  final case object Furniture extends TextureType
  final case object Crop extends TextureType
  final case object GiantCrop extends TextureType
  final case object ResourceClump extends TextureType
  final case object Bush extends TextureType

  implicit val codecForTextureType: Codec[TextureType] = deriveEnumerationCodec
}
