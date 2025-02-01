package quincyjo.stardew
package alternativetextures.models

import encoding.JsonFormat

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

final case class AnimationKeyFrame(frame: Int, duration: Long)

object AnimationKeyFrame {

  implicit val config: Configuration = JsonFormat.DefaultConfig
  implicit val codecForAnimationKeyFrame: Codec[AnimationKeyFrame] =
    deriveConfiguredCodec
}
