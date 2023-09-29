package quincyjo.stardew
package contentpatcher.models

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec
import io.circe.{Codec, Json}
import quincyjo.stardew.encoding.JsonFormat.DefaultConfig

sealed trait Action

object Action {

  implicit val configuration: Configuration =
    DefaultConfig.withDiscriminator("Action")
  implicit val codec: Codec[Action] = deriveConfiguredCodec

  type AssetName = String
  type RelativePath = String
  type Conditions = Map[String, Json]

  final case class Load(target: AssetName, fromFile: RelativePath)
      extends Action
  object Load {
    implicit val config: Configuration = DefaultConfig
    implicit val codec: Codec[Load] = deriveConfiguredCodec
  }

  final case class EditData(
    target: AssetName,
    fields: Map[String, Json],
    entries: Map[String, Json],
    moveEntries: Map[String, Json],
    textOperations: Vector[TextOperation], // TODO: Fix TextOperation
    targetField: Option[String] = None,
    when: Option[Conditions] = None,
    logName: Option[String] = None,
    updateRate: Option[UpdateRate] = None
  ) extends Action
  object EditData {
    implicit val config: Configuration = DefaultConfig
    implicit val codec: Codec[EditData] = deriveConfiguredCodec
  }

  final case class EditImage(target: AssetName,
                             fromFile: RelativePath,
                             fromArea: Option[Area] = None,
                             toArea: Option[Area] = None,
                             patchMode: Option[PatchMode],
                             when: Option[Conditions] = None,
                             logName: Option[String] = None,
                             update: Option[UpdateRate] = None)
      extends Action
  object EditImage {
    implicit val config: Configuration = DefaultConfig
    implicit val codec: Codec[EditImage] = deriveConfiguredCodec
  }

  final case class EditMap(target: AssetName,
                           fromFile: RelativePath,
                           fromArea: Option[Area] = None,
                           toArea: Option[Area] = None,
                           patchMode: Option[PatchMode],
                           when: Option[Conditions] = None,
                           logName: Option[String] = None,
                           update: Option[UpdateRate] = None)
      extends Action
  object EditMap {
    implicit val config: Configuration = DefaultConfig
    implicit val codec: Codec[EditMap] = deriveConfiguredCodec
  }

  final case class Include(fromFile: RelativePath,
                           when: Option[Conditions] = None,
                           logName: Option[String] = None,
                           update: Option[UpdateRate] = None)
      extends Action
  object Include {
    implicit val config: Configuration = DefaultConfig
    implicit val codec: Codec[Include] = deriveConfiguredCodec
  }

}
