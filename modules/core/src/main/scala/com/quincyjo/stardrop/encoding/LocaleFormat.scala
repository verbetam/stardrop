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
