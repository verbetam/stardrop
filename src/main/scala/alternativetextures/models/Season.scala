package quincyjo.stardew.alternativetextures.models

import io.circe.Codec
import io.circe.generic.extras.semiauto.deriveEnumerationCodec

sealed trait Season

object Season {
  final case object Spring extends Season
  final case object Summer extends Season
  final case object Fall extends Season
  final case object Winter extends Season

  implicit val codecForSeason: Codec[Season] = deriveEnumerationCodec
}
