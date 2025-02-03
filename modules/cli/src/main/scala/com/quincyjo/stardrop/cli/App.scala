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

package com.quincyjo.stardrop.cli

import cats.effect.{ExitCode, IO}
import com.monovore.decline._
import com.monovore.decline.effect.CommandIOApp
import com.quincyjo.stardrop.cli.commands.{ConvertATMod, Explode}

object App
    extends CommandIOApp(
      name = "stardrop",
      header = "Utility tools for working with SMAPI mods."
    ) {

  override def main: Opts[IO[ExitCode]] =
    (Explode.subcommand orElse ConvertATMod.subcommand).map {
      case explode: Explode      => explode.execute[IO]
      case convert: ConvertATMod => convert.execute[IO]
      case _ =>
        IO.println(
          s"Unrecognized command, use --help to see available commands."
        ) >> IO.pure(ExitCode.Error)
    }
}
