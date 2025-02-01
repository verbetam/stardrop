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

package com.quincyjo.stardrop.cli.implicits

import cats.data.{Validated, ValidatedNel}
import com.monovore.decline.*

import scala.reflect.io.{Directory, File, Path}

trait ScalaIOImplicits {

  implicit val readPath: Argument[Path] =
    implicitly[Argument[java.nio.file.Path]].map { jpath =>
      Path(jpath.toFile)
    }

  implicit val readFile: Argument[File] = new Argument[File] {
    override def read(string: String): ValidatedNel[String, File] =
      readPath.read(string).andThen { path =>
        if (path.isFile) Validated.Valid(path.toFile)
        else Validated.invalidNel(s"$path is not a file")
      }

    override def defaultMetavar: String = "file"
  }

  implicit val readDirectory: Argument[Directory] = new Argument[Directory] {
    override def read(string: String): ValidatedNel[String, Directory] =
      readPath.read(string).andThen { path =>
        if (path.isDirectory) Validated.Valid(path.toDirectory)
        else Validated.invalidNel(s"$path is not a directory")
      }

    override def defaultMetavar: String = "dir"
  }
}
