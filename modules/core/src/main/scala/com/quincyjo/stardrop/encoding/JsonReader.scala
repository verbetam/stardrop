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

import cats.Monad
import cats.data.{EitherT, Nested}
import cats.effect.{Async, Resource}
import cats.implicits._
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
import scala.util.chaining.scalaUtilChainingOps
import scala.util.control.NoStackTrace

/** A custom JSON reader that uses a configured Jackson parser under the hood.
  * The reason for this is to allow options for non-strict parsing. This is
  * because many JSONs for mods are handwritten, so things like comment strings
  * (which are not valid JSON), trailing commas, etc. are common.
  * @param makeParser
  *   Factory method to create the Jackson exposing the input to parse.
  */
class JsonReader[F[_]: Async](jsonParser: Resource[F, JsonParser]) {

  /** Parses a JSON object from the underlying input.
    * @return
    *   The parsed JSON object or an error.
    */
  def parse: F[ReaderResult[Json]] =
    jsonParser.use { jsonParser =>
      nextJson(jsonParser: JsonParser).map(_.left.map { ex =>
        JsonReaderException(
          s"Failed to read JSON from file",
          jsonParser.currentLocation(),
          ex
        )
      })
    }

  /** Decodes the underlying input into a value of type `T`.
    * @tparam T
    *   The type to decode.
    * @return
    *   The decoded value or an error.
    */
  def decode[T: Decoder: ClassTag]: F[ReaderResult[T]] =
    parse.map(_.flatMap { json =>
      json
        .as[T]
        .fold(
          ex =>
            Left(
              JsonReaderException(
                s"Failed to decode json as ${implicitly[ClassTag[T]]} ",
                cause = Some(ex)
              )
            ),
          Right.apply
        )
    })

  /** Parses a JSON object from the current token.
    * @return
    *   The parsed JSON object.
    */
  private def parseObject(
      parser: JsonParser
  ): F[ReaderResult[Json]] =
    parser.currentToken() match {
      case JsonToken.START_OBJECT =>
        val builder = Map.newBuilder[String, Json]
        Monad[EitherT[F, JsonReaderException, *]]
          .whileM_(
            EitherT(nextToken(parser)).map(_ != JsonToken.END_OBJECT)
          )(for {
            fieldName <- EitherT(parseFieldName(parser))
            json <- EitherT(parseCurrent(parser))
          } yield builder.addOne(fieldName -> json))
          .map { _ => Json.fromFields(builder.result()) }
          .value
      case otherToken =>
        Async[F].pure(
          Left(
            JsonReaderException(
              s"Expected ${JsonToken.START_OBJECT} but found $otherToken",
              Some(parser.currentLocation())
            )
          )
        )
    }

  /** Parses a field name from the current token.
    * @return
    *   The parsed field name.
    */
  private def parseFieldName(
      parser: JsonParser
  ): F[ReaderResult[String]] =
    parser.currentToken() match {
      case JsonToken.FIELD_NAME => getValueAsString(parser)
      case otherToken =>
        Async[F].pure(
          Left(
            JsonReaderException(
              s"Expected field name but was $otherToken",
              Some(parser.currentLocation())
            )
          )
        )
    }

  private def getValueAsString(
      parser: JsonParser
  ): F[ReaderResult[String]] =
    Async[F]
      .blocking[ReaderResult[String]](
        Right[JsonReaderException, String](parser.getValueAsString)
      )
      .recover { ex =>
        Left(
          JsonReaderException(
            "Failed to read JSON string.",
            parser.currentLocation(),
            ex
          )
        )
      }

  private def getIntValue(
      parser: JsonParser
  ): F[ReaderResult[Int]] =
    Async[F]
      .blocking[ReaderResult[Int]](
        Right[JsonReaderException, Int](parser.getIntValue)
      )
      .recover { ex =>
        Left(
          JsonReaderException(
            "Failed to read JSON integer.",
            parser.currentLocation(),
            ex
          )
        )
      }

  private def nextToken(
      parser: JsonParser
  ): F[ReaderResult[JsonToken]] =
    Async[F]
      .blocking[ReaderResult[JsonToken]](Right(parser.nextToken()))
      .recover { ex =>
        Left(
          JsonReaderException(
            "Failed to read JSON token.",
            parser.currentLocation(),
            ex
          )
        )
      }

  private def getDecimalValue(
      parser: JsonParser
  ): F[ReaderResult[BigDecimal]] =
    Async[F]
      .blocking[ReaderResult[BigDecimal]](
        Right(scala.math.BigDecimal(parser.getDecimalValue))
      )
      .recover { ex =>
        Left(
          JsonReaderException(
            "Failed to JSON number.",
            parser.currentLocation(),
            ex
          )
        )
      }

  /** Parses a JSON array from the current token.
    * @return
    *   The parsed JSON array.
    */
  private def parseArray(
      parser: JsonParser
  ): F[ReaderResult[Json]] =
    parser.currentToken() match {
      case JsonToken.START_ARRAY =>
        val builder = Vector.newBuilder[Json]
        Monad[EitherT[F, JsonReaderException, *]]
          .whileM_(
            EitherT(nextToken(parser)).map(_ != JsonToken.END_ARRAY)
          )(
            EitherT(parseCurrent(parser))
              .map(builder.addOne)
          )
          .map { _ => Json.fromValues(builder.result()) }
          .value
      case otherToken =>
        Async[F].raiseError(
          JsonReaderException(
            s"Expected ${JsonToken.START_ARRAY} but found $otherToken",
            Some(parser.currentLocation())
          )
        )
    }

  /** Parses a JSON value from the current token.
    * @return
    *   The parsed JSON value.
    */
  private def parseCurrent(
      parser: JsonParser
  ): F[ReaderResult[Json]] =
    parser.currentToken() match {
      case JsonToken.START_OBJECT => parseObject(parser)
      case JsonToken.START_ARRAY  => parseArray(parser)
      case JsonToken.VALUE_STRING =>
        Nested(getValueAsString(parser)).map(Json.fromString).value
      case JsonToken.VALUE_NUMBER_INT =>
        Nested(getIntValue(parser)).map(Json.fromInt).value
      case JsonToken.VALUE_NUMBER_FLOAT =>
        Nested(getDecimalValue(parser)).map(Json.fromBigDecimal).value
      case JsonToken.VALUE_TRUE =>
        Async[F].pure(Right(Json.True))
      case JsonToken.VALUE_FALSE =>
        Async[F].pure(Right(Json.False))
      case JsonToken.VALUE_NULL =>
        Async[F].pure(Right(Json.Null))
      case JsonToken.FIELD_NAME | JsonToken.END_ARRAY | JsonToken.END_ARRAY |
          JsonToken.END_OBJECT | JsonToken.NOT_AVAILABLE |
          JsonToken.VALUE_EMBEDDED_OBJECT =>
        Async[F].pure(
          Left(
            JsonReaderException(
              s"Unexpected ${parser.currentToken()} token!",
              Some(parser.currentLocation())
            )
          )
        )
    }

  /** Advances the parser to the next JSON token and then parses it as a JSON.
    * Specifically, this is used on initial parse to advance to the first token.
    * @return
    *   The parsed JSON object.
    */
  private def nextJson(parser: JsonParser): F[ReaderResult[Json]] =
    nextToken(parser) >> parseCurrent(parser)
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

  private def makeWithResource[F[_]: Async](
      makeParser: () => JsonParser
  ): JsonReader[F] =
    new JsonReader[F](Resource.make(Async[F].blocking(makeParser())) { parser =>
      Async[F].blocking(parser.close())
    })

  def apply[F[_]: Async](content: String): JsonReader[F] =
    makeWithResource[F](() => jsonFactory.createParser(content))

  def apply[F[_]: Async](in: InputStream): JsonReader[F] =
    makeWithResource[F](() => jsonFactory.createParser(in))

  def apply[F[_]: Async](url: URL): JsonReader[F] =
    makeWithResource[F](() => jsonFactory.createParser(url))

  def apply[F[_]: Async](file: File): JsonReader[F] =
    makeWithResource[F](() => jsonFactory.createParser(file))

  def apply[F[_]: Async](path: JPath): JsonReader[F] =
    JsonReader[F](path.toFile)

  def apply[F[_]: Async](file: scala.reflect.io.File): JsonReader[F] =
    makeWithResource[F](() => jsonFactory.createParser(file.jfile))

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
