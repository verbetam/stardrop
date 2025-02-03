package com.quincyjo.stardrop.contentpatcher.models

import io.circe.JsonObject
import io.circe.syntax.EncoderOps
import org.scalatest.{EitherValues, OptionValues}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class ActionSpec
    extends AnyFlatSpecLike
    with Matchers
    with OptionValues
    with EitherValues {

  "encoding" should "include discriminators" in {
    val action: Action = Action.Load("foo", "bar")

    action.asJson
      .as[JsonObject]
      .value("Action")
      .value
      .as[String]
      .value should be("Load")
  }
}
