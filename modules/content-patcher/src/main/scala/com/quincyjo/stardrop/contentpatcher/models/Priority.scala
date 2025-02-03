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

import io.circe.{Decoder, Encoder}

sealed trait Priority

object Priority {

  trait DefinedPriority extends Priority

  final case object Low extends DefinedPriority

  final case object Medium extends DefinedPriority

  final case object High extends DefinedPriority

  final case object Exclusive extends DefinedPriority

  final case class WithOffset(priority: DefinedPriority, offset: Int)
      extends Priority {

    override def toString: String = offset match {
      case 0                    => priority.toString
      case offset if offset > 0 => s"$priority + $offset"
      case offset if offset < 0 => s"$priority - ${-offset}"
    }
  }

  def fromString(str: String): Option[Priority] = str.toLowerCase match {
    case "low"       => Some(Low)
    case "medium"    => Some(Medium)
    case "high"      => Some(High)
    case "exclusive" => Some(Exclusive)
    case other =>
      val parts = other.split("[+-]").map(_.trim.toLowerCase)
      for {
        priority <- Option
          .when(parts.length == 2)(parts.headOption)
          .flatten
          .collect {
            case "low"       => Low
            case "medium"    => Medium
            case "high"      => High
            case "exclusive" => Exclusive
          }
        offset <- parts.lift(1).flatMap(_.toIntOption).map { int =>
          if (str.contains('-')) int * -1 else int
        }
      } yield
        if (offset == 0) priority
        else WithOffset(priority, offset)
  }

  implicit val encoderForPriority: Encoder[Priority] =
    Encoder.encodeString.contramap(_.toString)

  implicit val decoderForPriority: Decoder[Priority] =
    Decoder.decodeString.emap { string =>
      fromString(string.toLowerCase)
        .toRight(s"$string is not a valid priority")
    }
}
