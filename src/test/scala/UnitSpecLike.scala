package quincyjo.stardew

import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{BeforeAndAfter, EitherValues, OptionValues}

trait UnitSpecLike
    extends TableDrivenPropertyChecks
    with Matchers
    with MockFactory
    with BeforeAndAfter
    with OptionValues
    with EitherValues
