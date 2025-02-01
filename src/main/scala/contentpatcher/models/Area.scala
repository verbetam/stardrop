package quincyjo.stardew
package contentpatcher.models

import encoding.JsonFormat.DefaultConfig

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

final case class Area(x: Int, y: Int, width: Int, height: Int)

object Area {
  implicit val configuration: Configuration = DefaultConfig
  implicit val codec: Codec[Area] = deriveConfiguredCodec
}
