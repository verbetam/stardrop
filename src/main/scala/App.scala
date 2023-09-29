package quincyjo.stardew

import cli.commands.{ConvertATMod, Explode}

import com.monovore.decline._

object App
    extends CommandApp(
      name = "stardrop",
      header = "Utility tools for working with SMAPI mods.",
      main = Opts.subcommand(ConvertATMod.command) orElse
        Opts.subcommand(Explode.command)
    )
