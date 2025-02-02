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
import scala.util.control.NoStackTrace

// TODO: Remove exceptions and handle asynchronously with cats effect.
/** A custom JSON reader that uses a configured Jackson parser under the hood.
  * The reason for this is to allow options for non-strict parsing. This is
  * because many JSONs for mods are handwritten, so things like comment strings
  * (which are not valid JSON), trailing commas, etc. are common.
  * @param parser
  *   The Jackson exposing the input to parse.
  */
class JsonReader(parser: JsonParser) {

  /** Parses a JSON object from the underlying input.
    * @return
    *   The parsed JSON object or an error.
    */
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

  /** Decodes the underlying input into a value of type `T`.
    * @tparam T
    *   The type to decode.
    * @return
    *   The decoded value or an error.
    */
  def decode[T: Decoder: ClassTag]: ReaderResult[T] =
    parse.flatMap(_.as[T].left.map { decodingFailure =>
      JsonReaderException(
        s"Failed to decode json as ${implicitly[ClassTag[T]]} ",
        cause = Some(decodingFailure)
      )
    })

  /** Parses a JSON object from the current token.
    * @return
    *   The parsed JSON object.
    */
  @throws[JsonReaderException](
    "If the current token is not a valid JSON object."
  )
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

  /** Parses a field name from the current token.
    * @return
    *   The parsed field name.
    */
  @throws[JsonReaderException](
    "If the current token is not a valid field name."
  )
  private def parseFieldName: String = parser.currentToken() match {
    case JsonToken.FIELD_NAME => parser.getValueAsString
    case otherToken =>
      throw JsonReaderException(
        s"Expected field name but was $otherToken",
        Some(parser.currentLocation())
      )
  }

  /** Parses a JSON array from the current token.
    * @return
    *   The parsed JSON array.
    */
  @throws[JsonReaderException](
    "If the current token is not a valid JSON array."
  )
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

  /** Parses a JSON value from the current token.
    * @return
    *   The parsed JSON value.
    */
  @throws[JsonReaderException](
    "If the current token is not a valid JSON value."
  )
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

  /** Advances the parser to the next JSON token and then parses it as a JSON.
    * Specifically, this is used on initial parse to advance to the first token.
    * @return
    *   The parsed JSON object.
    */
  @throws[JsonReaderException](
    "If the current token is not a valid JSON object."
  )
  private def nextJson: Json = {
    parser.nextToken()
    parseCurrent
  }
}

object JsonReader {

  type ReaderResult[T] = Either[JsonReaderException, T]

  /** Describes a failure to parse JSON value.
    * @param message
    *   A message describing the failure.
    * @param location
    *   The location of the failure.
    * @param cause
    *   The underlying cause of the failure, if defined.
    */
  final case class JsonReaderException(
      message: String,
      location: Option[JsonLocation] = None,
      cause: Option[Throwable] = None
  ) extends Exception(
        location.fold(message)(messageWithLocation(message, _)),
        cause.orNull
      )
      with NoStackTrace

  object JsonReaderException {

    /** Creates a message with location information.
      * @param message
      *   The message.
      * @param location
      *   The location of the error.
      * @return
      *   The message with location information
      */
    def messageWithLocation(message: String, location: JsonLocation): String =
      s"$message @ ${location.offsetDescription()}"

    /** Creates a [[JsonReaderException]] with location information.
      * @param message
      *   The message describing the failure.
      * @param location
      *   The location of the failure.
      * @param cause
      *   The underlying cause of the failure.
      * @return
      *   The [[JsonReaderException]].
      */
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

  private final val enabledFeatures: Iterable[JsonParser.Feature] = Vector(
    JsonParser.Feature.ALLOW_COMMENTS,
    JsonParser.Feature.IGNORE_UNDEFINED,
    JsonParser.Feature.AUTO_CLOSE_SOURCE,
    JsonReadFeature.ALLOW_TRAILING_COMMA.mappedFeature()
  )

  private final val disabledFeatures: Iterable[JsonParser.Feature] =
    Vector.empty

  private final val jsonFactory: JsonFactory =
    new JsonFactory().tap { factory =>
      enabledFeatures.map(factory.enable)
      disabledFeatures.map(factory.disable)
    }
}
