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

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.core.{
  JsonFactory,
  JsonLocation,
  JsonParser,
  JsonToken
}
import com.quincyjo.stardrop.encoding.JsonReader.JsonReaderException.messageWithLocation
import com.quincyjo.stardrop.encoding.JsonReader.{
  JsonReaderException,
  ReaderResult
}
import io.circe.{Decoder, Json}

import java.io.{File, InputStream}
import java.net.URL
import java.nio.file.{Path => JPath}
import scala.reflect.ClassTag
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

class JsonReader(parser: JsonParser) {

  private def parseObject: Json = parser.currentToken() match {
    case JsonToken.START_OBJECT =>
      val builder = Map.newBuilder[String, Json]
      while (parser.nextToken() != JsonToken.END_OBJECT) {
        builder.addOne(parseFieldName -> nextJson)
      }
      Json.fromFields(builder.result())
    case otherToken =>
      throw JsonReaderException(
        s"Cannot parse an object from a $otherToken",
        Some(parser.currentLocation())
      )
  }

  private def parseFieldName: String = parser.currentToken() match {
    case JsonToken.FIELD_NAME => parser.getValueAsString
    case otherToken =>
      throw JsonReaderException(
        s"Expected field name but was $otherToken",
        Some(parser.currentLocation())
      )
  }

  private def parseArray: Json = parser.currentToken() match {
    case JsonToken.START_ARRAY =>
      val builder = Vector.newBuilder[Json]
      while (parser.nextToken() != JsonToken.END_ARRAY) {
        builder.addOne(parseCurrent)
      }
      Json.fromValues(builder.result())
    case otherToken =>
      throw JsonReaderException(
        s"Cannot parse an array from a $otherToken",
        Some(parser.currentLocation())
      )
  }

  private def parseCurrent: Json = parser.currentToken() match {
    case JsonToken.START_OBJECT     => parseObject
    case JsonToken.START_ARRAY      => parseArray
    case JsonToken.VALUE_STRING     => Json.fromString(parser.getValueAsString)
    case JsonToken.VALUE_NUMBER_INT => Json.fromInt(parser.getIntValue)
    case JsonToken.VALUE_NUMBER_FLOAT =>
      Json.fromBigDecimal(parser.getDecimalValue)
    case JsonToken.VALUE_TRUE  => Json.True
    case JsonToken.VALUE_FALSE => Json.False
    case JsonToken.VALUE_NULL  => Json.Null
    case JsonToken.FIELD_NAME | JsonToken.END_ARRAY | JsonToken.END_ARRAY |
        JsonToken.END_OBJECT | JsonToken.NOT_AVAILABLE |
        JsonToken.VALUE_EMBEDDED_OBJECT =>
      throw JsonReaderException(
        s"Unexpected ${parser.currentToken()} token!",
        Some(parser.currentLocation())
      )
  }

  private def nextJson: Json = {
    parser.nextToken()
    parseCurrent
  }

  def parse: ReaderResult[Json] =
    Try {
      try nextJson
      finally parser.close()
    }.toEither.left
      .map(ex =>
        JsonReaderException(
          s"Failed ot read JSON from file",
          parser.currentLocation(),
          ex
        )
      )

  def decode[T: Decoder: ClassTag]: ReaderResult[T] =
    parse.flatMap(_.as[T].left.map { decodingFailure =>
      JsonReaderException(
        s"Failed to decode json as ${implicitly[ClassTag[T]]} ",
        cause = Some(decodingFailure)
      )
    })
}

object JsonReader {

  type ReaderResult[T] = Either[JsonReaderException, T]

  final case class JsonReaderException(
      message: String,
      location: Option[JsonLocation] = None,
      cause: Option[Throwable] = None
  ) extends Exception(
        location.fold(message)(messageWithLocation(message, _)),
        cause.orNull
      )

  object JsonReaderException {

    def messageWithLocation(message: String, location: JsonLocation): String =
      s"$message @ ${location.offsetDescription()}"

    def apply(
        message: String,
        location: JsonLocation,
        cause: Throwable
    ): JsonReaderException =
      new JsonReaderException(message, Some(location), Some(cause))
  }

  def apply(content: String): JsonReader =
    new JsonReader(jsonFactory.createParser(content))

  def apply(in: InputStream): JsonReader =
    new JsonReader(jsonFactory.createParser(in))

  def apply(url: URL): JsonReader =
    new JsonReader(jsonFactory.createParser(url))

  def apply(file: File): JsonReader =
    new JsonReader(jsonFactory.createParser(file))

  def apply(path: JPath): JsonReader =
    JsonReader(path.toFile)

  def apply(file: scala.reflect.io.File): JsonReader =
    new JsonReader(jsonFactory.createParser(file.jfile))

  final val enabledFeatures: Iterable[JsonParser.Feature] = Vector(
    JsonParser.Feature.ALLOW_COMMENTS,
    JsonParser.Feature.IGNORE_UNDEFINED,
    JsonParser.Feature.AUTO_CLOSE_SOURCE,
    JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature()
  )

  final val disabledFeatures: Iterable[JsonParser.Feature] = Vector.empty

  final val jsonFactory: JsonFactory =
    new JsonFactory().tap { factory =>
      enabledFeatures.map(factory.enable)
      disabledFeatures.map(factory.disable)
    }
}
