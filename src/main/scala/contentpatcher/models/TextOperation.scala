package quincyjo.stardew
package contentpatcher.models

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{
  deriveConfiguredCodec,
  deriveEnumerationCodec
}
import quincyjo.stardew.encoding.JsonFormat.DefaultConfig

sealed trait TextOperation

object TextOperation {
  implicit val configuration: Configuration =
    DefaultConfig.withDiscriminator("Operation")
  implicit val codec: Codec[TextOperation] = deriveConfiguredCodec

  type BreadcrumbPath = Vector[String]

  final val DEFAULT_DELIMITER: String = "/"

  final case class Append(target: BreadcrumbPath,
                          value: String,
                          delimiter: Option[String] = None)
      extends TextOperation
  object Append {
    implicit val configuration: Configuration = DefaultConfig
    implicit val codec: Codec[Append] = deriveConfiguredCodec
  }

  final case class Prepend(target: BreadcrumbPath,
                           value: String,
                           delimiter: Option[String] = None)
      extends TextOperation
  object Prepend {
    implicit val configuration: Configuration = DefaultConfig
    implicit val codec: Codec[Prepend] = deriveConfiguredCodec
  }

  final case class RemoveDelimited(target: BreadcrumbPath,
                                   search: String,
                                   delimiter: String,
                                   replaceMode: Option[ReplaceMode] = None)
      extends TextOperation
  object RemoveDelimited {
    implicit val configuration: Configuration = DefaultConfig
    implicit val codec: Codec[RemoveDelimited] = deriveConfiguredCodec
  }

  sealed trait ReplaceMode
  object ReplaceMode {
    final case object First extends ReplaceMode
    final case object Last extends ReplaceMode
    final case object All extends ReplaceMode
    implicit val codecForReplaceMode: Codec[ReplaceMode] =
      deriveEnumerationCodec
  }
}
