package quincyjo.stardew
package contentpatcher.models

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import quincyjo.stardew.contentpatcher.models.Action.Conditions
import quincyjo.stardew.encoding.JsonFormat.DefaultConfig

final case class DynamicToken(name: String,
                              value: String,
                              when: Option[Conditions] = None)

object DynamicToken {
  implicit val configuration: Configuration = DefaultConfig
  implicit val codec: Codec[DynamicToken] = deriveConfiguredCodec
}
