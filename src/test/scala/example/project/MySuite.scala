package example.project

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class MySuite extends AnyFunSuite with Matchers {
  test("example test that succeeds") {
    val obtained = 42
    val expected = 42
    obtained shouldBe expected
  }
}
