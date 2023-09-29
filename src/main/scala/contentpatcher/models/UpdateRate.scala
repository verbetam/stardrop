package quincyjo.stardew.contentpatcher.models

import io.circe.generic.extras.semiauto.deriveEnumerationCodec
import io.circe.syntax.EncoderOps
import io.circe.{Codec, Decoder, Encoder}

sealed trait UpdateRate
object UpdateRate {
  sealed trait SingleRate extends UpdateRate {

    def +(that: SingleRate): Multiple = Multiple(this, that)
  }
  object SingleRate {

    implicit val singleRateCodec: Codec[SingleRate] = deriveEnumerationCodec
  }
  final case object OnDayStart extends SingleRate
  final case object OnLocationChange extends SingleRate
  final case object OnTimeChange extends SingleRate

  final case class Multiple(updateRates: Set[SingleRate]) extends UpdateRate {

    def +(that: SingleRate): Multiple = this.copy(updateRates + that)
    def ++(that: Multiple): Multiple =
      this.copy(updateRates ++ that.updateRates)
  }
  object Multiple {

    def apply(rate: SingleRate, rates: SingleRate*): Multiple =
      new Multiple(rates.appended(rate).toSet)

    implicit val encodeMultiple: Encoder[Multiple] = Encoder[String].contramap(
      _.updateRates.map(_.asJson.asString.get).mkString(", ")
    )
    implicit val decodeMultiple: Decoder[Multiple] = Decoder[String].emap {
      string =>
        string
          .split(',')
          .map(_.trim)
          .foldLeft(
            Right(Set.empty[SingleRate]): Decoder.Result[Set[SingleRate]]
          ) {
            case (acc, next) =>
              acc.flatMap { acc =>
                next.asJson.as[SingleRate].map(acc + _)
              }
          }
          .map(Multiple(_))
          .left
          .map(_.message)
    }
  }

  implicit val encodeUpdateRate: Encoder[UpdateRate] = Encoder.instance {
    case singleRate: SingleRate => singleRate.asJson
    case Multiple(updateRates) =>
      updateRates.map(_.asJson.asString.get).mkString(", ").asJson
  }
  implicit val decodeUpdateRate: Decoder[UpdateRate] = Decoder[String].emap {
    string =>
      val json = string.asJson
      json.as[SingleRate].orElse(json.as[Multiple]).left.map(_.message)
  }
}
