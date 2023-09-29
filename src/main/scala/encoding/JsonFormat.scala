package quincyjo.stardew.encoding

import io.circe.generic.extras.Configuration

object JsonFormat {

  final val DefaultConfig: Configuration =
    Configuration.default.copy(transformMemberNames = _.capitalize)
}
