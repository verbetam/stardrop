package quincyjo.stardew.contentpatcher.models

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

sealed trait PatchMode
object PatchMode {
  final case object Replace extends PatchMode
  final case object Overlay extends PatchMode

  implicit val codecForPatchMode: Codec[PatchMode] = deriveEnumerationCodec
}
