/*
 * Copyright 2023 Quincy Jo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quincyjo.stardrop.contentpatcher.models

import com.quincyjo.stardrop.encoding.JsonFormat.DefaultConfig
import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{
  deriveConfiguredCodec,
  deriveEnumerationCodec
}

sealed trait TextOperation

object TextOperation {

  implicit val configuration: Configuration =
    DefaultConfig.withDiscriminator("Operation")

  implicit val codec: Codec[TextOperation] = deriveConfiguredCodec

  type BreadcrumbPath = Vector[String]

  final val DEFAULT_DELIMITER: String = "/"

  final case class Append(
      target: BreadcrumbPath,
      value: String,
      delimiter: Option[String] = None
  ) extends TextOperation

  object Append {

    implicit val configuration: Configuration = DefaultConfig

    implicit val codec: Codec[Append] = deriveConfiguredCodec
  }

  final case class Prepend(
      target: BreadcrumbPath,
      value: String,
      delimiter: Option[String] = None
  ) extends TextOperation

  object Prepend {

    implicit val configuration: Configuration = DefaultConfig

    implicit val codec: Codec[Prepend] = deriveConfiguredCodec
  }

  final case class RemoveDelimited(
      target: BreadcrumbPath,
      search: String,
      delimiter: String,
      replaceMode: Option[ReplaceMode] = None
  ) extends TextOperation

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
