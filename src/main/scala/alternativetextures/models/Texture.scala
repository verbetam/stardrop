package quincyjo.stardew
package alternativetextures.models

import encoding.JsonFormat

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import quincyjo.stardew.content.models.FurnitureType

final case class Texture(itemName: String,
                         `type`: TextureType,
                         textureWidth: Int,
                         textureHeight: Int,
                         variations: Int,
                         keywords: Option[Set[String]] = None,
                         seasons: Option[Set[Season]] = None,
                         manualVariations: Option[Vector[ManualVariation]] =
                           None,
                         animation: Option[Vector[AnimationKeyFrame]] = None) {

  def textureType: TextureType = `type`

  def addKeyword(keyword: String): Texture =
    copy(keywords = Some(keywords.fold(Set(keyword))(_ + keyword)))

  def addKeywords(keywords: Set[String]): Texture =
    copy(keywords = Some(this.keywords.fold(keywords)(_ union keywords)))

  def withManualVariations(variations: Iterable[ManualVariation]): Texture =
    copy(manualVariations = Some(variations.toVector))
}

object Texture {

  implicit val config: Configuration = JsonFormat.DefaultConfig
  implicit val codecForTexture: Codec[Texture] = deriveConfiguredCodec
}
