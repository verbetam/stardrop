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

package com.quincyjo.stardrop.cli.models

import cats.data.{Validated, ValidatedNel}
import com.monovore.decline.*

sealed trait ModType {
  def abbreviation: String

  def friendlyName: String
}

object ModType {

  final case object AlternativeTextures extends ModType {
    override val abbreviation: String = "AT"
    override val friendlyName: String = "Alternative Textures"
  }

  final case object CustomFurniture extends ModType {
    override val abbreviation: String = "CF"
    override val friendlyName: String = "Custom Furniture"
  }

  final case object ContentPatcher extends ModType {
    override val abbreviation: String = "CP"
    override val friendlyName: String = "Content Patcher"
  }

  implicit val readModType: Argument[ModType] = new Argument[ModType] {

    override def read(string: String): ValidatedNel[String, ModType] =
      string.toUpperCase match {
        case AlternativeTextures.abbreviation =>
          Validated.valid(AlternativeTextures)
        case CustomFurniture.abbreviation =>
          Validated.valid(CustomFurniture)
        case ContentPatcher.abbreviation =>
          Validated.valid(ContentPatcher)
        case other =>
          Validated.invalidNel(s"$other is not a known mod type.")
      }

    override def defaultMetavar: String = "AT|CF|CP"
  }
}
