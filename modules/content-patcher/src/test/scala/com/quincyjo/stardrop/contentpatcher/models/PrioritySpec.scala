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

import com.quincyjo.stardrop.contentpatcher.models.Priority._
import io.circe.syntax.EncoderOps
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class PrioritySpec
    extends AnyFlatSpecLike
    with Matchers
    with EitherValues
    with TableDrivenPropertyChecks {

  "encoding" should "encode defined priorities" in {
    val cases = Table[Priority, String](
      "priority" -> "expected",
      Low -> "Low",
      Medium -> "Medium",
      High -> "High",
      Exclusive -> "Exclusive"
    )

    forAll(cases) { (priority, expected) =>
      priority.asJson shouldBe expected.asJson
    }
  }

  it should "encode offset priorities" in {
    val cases = Table[Priority, String](
      "priority" -> "expected",
      WithOffset(Low, 0) -> "Low",
      WithOffset(Low, 1) -> "Low + 1",
      WithOffset(Low, -1) -> "Low - 1"
    )

    forAll(cases) { (priority, expected) =>
      priority.asJson shouldBe expected.asJson
    }
  }

  "decoding" should "decode defined priorities" in {
    val cases = Table[String, Priority](
      "priority" -> "expected",
      "Low" -> Low,
      "Medium" -> Medium,
      "High" -> High,
      "Exclusive" -> Exclusive
    )

    forAll(cases) { (string, expected) =>
      string.asJson.as[Priority].value shouldBe expected
    }
  }

  it should "be case insensitive" in {
    val cases = Table[String, Priority](
      "priority" -> "expected",
      "low" -> Low,
      "medium" -> Medium,
      "high" -> High,
      "exclusive" -> Exclusive
    )

    forAll(cases) { (string, expected) =>
      string.asJson.as[Priority].value shouldBe expected
    }
  }

  it should "decode offset priorities" in {
    val cases = Table[String, Priority](
      "priority" -> "expected",
      "Medium + 1" -> WithOffset(Medium, 1),
      "High - 1" -> WithOffset(High, -1)
    )

    forAll(cases) { (string, expected) =>
      string.asJson.as[Priority].value shouldBe expected
    }
  }

  it should "simplify zero offsets" in {
    "Low + 0".asJson.as[Priority].value shouldBe Low
  }
}
