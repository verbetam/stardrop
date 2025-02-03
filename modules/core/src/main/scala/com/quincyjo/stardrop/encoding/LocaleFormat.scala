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

package com.quincyjo.stardrop.encoding

import io.circe.{Decoder, Encoder}

import java.util.Locale
import scala.util.Try

trait LocaleFormat {

  implicit val encoderForLocale: Encoder[Locale] =
    Encoder.encodeString.contramap { locale =>
      s"${locale.getLanguage}-${locale.getCountry}"
    }

  implicit val decoderForLocale: Decoder[Locale] =
    Decoder.decodeString.emap {
      case string @ s"$language-$country" =>
        Try(new Locale(language, country)).fold(
          _ => Left(s"$string is not a valid locale"),
          Right.apply
        )
      case language =>
        Try(new Locale(language)).fold(
          _ => Left(s"$language is not a valid language"),
          Right.apply
        )
    }
}

object LocaleFormat extends LocaleFormat
