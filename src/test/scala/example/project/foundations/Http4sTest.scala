package example.project.foundations

import cats.effect._
import cats.effect.testing.scalatest.AsyncIOSpec

import org.http4s._
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

class Http4sTest extends AsyncFunSuite with AsyncIOSpec with Matchers:
  private val endpoints = Http4s.endpoints[IO].orNotFound

  test("GET /courses/{courseId}/students returns 200") {
    val getStudentsRequest = Request[IO](Method.GET, uri"/courses/2f1f7e39-88e9-433d-8d45-19cac429e67a/students")

    endpoints.run(getStudentsRequest).asserting(_.status shouldBe Status.Ok)
  }

  test("GET /courses/{courseId}/students returns 404 for non-existent course") {
    val getStudentsRequest = Request[IO](Method.GET, uri"/courses/non-existent-course/students")

    endpoints.run(getStudentsRequest).asserting(_.status shouldBe Status.NotFound)
  }

  test("GET /health returns 200") {
    val getHealthRequest = Request[IO](Method.GET, uri"/health")

    endpoints.run(getHealthRequest).asserting(_.status shouldBe Status.Ok)
  }
