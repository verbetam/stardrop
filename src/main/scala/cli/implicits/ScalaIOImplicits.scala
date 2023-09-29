package quincyjo.stardew
package cli.implicits

import cats.data.{Validated, ValidatedNel}
import com.monovore.decline._

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
