package quincyjo.stardew
package alternativetextures.models

import encoding.JsonFormat

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

final case class ManualVariation(id: Int,
                                 name: Option[String] = None,
                                 keywords: Option[Set[String]] = None,
                                 chanceWeight: Option[Double] = None)

object ManualVariation {

  implicit val config: Configuration = JsonFormat.DefaultConfig
  implicit val codecForManualVariation: Codec[ManualVariation] =
    deriveConfiguredCodec
}
