package quincyjo.stardew
package smapi.models

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import quincyjo.stardew.encoding.JsonFormat.DefaultConfig

final case class SmapiManifest(name: String,
                               author: String,
                               version: String,
                               description: String,
                               uniqueID: String,
                               updateKeys: Option[Seq[String]] = None,
                               contentPackFor: Option[Map[String, String]] =
                                 None)

object SmapiManifest {
  implicit val configuration: Configuration = DefaultConfig
  implicit val codec: Codec[SmapiManifest] = deriveConfiguredCodec
}
