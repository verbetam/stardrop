package quincyjo.stardew
package contentpatcher.models

import encoding.JsonFormat.DefaultConfig

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.{Codec, JsonObject}

final case class Content(format: String,
                         changes: Vector[Action],
                         dynamicTokens: Option[Vector[DynamicToken]] = None,
                         aliasTokenNames: Option[Map[String, String]] = None,
                         configSchema: Option[JsonObject] = None)

object Content {
  implicit val configuration: Configuration = DefaultConfig
  implicit val codec: Codec.AsObject[Content] = deriveConfiguredCodec
}
