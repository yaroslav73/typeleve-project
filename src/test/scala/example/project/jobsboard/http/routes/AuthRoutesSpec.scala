package example.project.jobsboard.http.routes

import org.scalatest.freespec.AsyncFreeSpec
import cats.effect.testing.scalatest.AsyncIOSpec
import org.scalatest.matchers.should.Matchers
import io.circe.Json
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.*
import org.http4s.dsl.io.*
import org.http4s.implicits.*
import org.http4s.headers.Authorization
import org.http4s.server.Router
import org.typelevel.ci.CIStringSyntax
import cats.effect.IO
import example.project.jobsboard.core.Auth
import example.project.jobsboard.domain.Aliases.JwtToken
import example.project.jobsboard.domain.User
import example.project.jobsboard.domain.Auth.NewPasswordInfo
import example.project.jobsboard.domain.Auth.LoginInfo
import example.project.jobsboard.fixtures.UserFixture
import example.project.jobsboard.http.responses.FailureResponse
import example.project.jobsboard.domain.Aliases.Authenticator
import tsec.mac.jca.HMACSHA256
import tsec.authentication.IdentityStore
import cats.data.OptionT
import tsec.authentication.JWTAuthenticator
import concurrent.duration.DurationInt
import tsec.jws.mac.JWTMac
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

class AuthRoutesSpec extends AsyncFreeSpec with AsyncIOSpec with Matchers with Http4sDsl[IO] with UserFixture:
  "AuthRoutes" - {
    "login should return unauthorized if user not found" in {
      val request = Request[IO](Method.POST, uri"/auth/login").withEntity(LoginInfo(NotFoundUserEmail, "password1"))

      for {
        response <- authRoutes.run(request)
        payload  <- response.as[FailureResponse]
      } yield {
        response.status shouldBe Status.Unauthorized
        payload         shouldBe FailureResponse("User or password is incorrect")
      }
    }

    "login should return unauthorized if password is incorrect" in {
      val request = Request[IO](Method.POST, uri"/auth/login").withEntity(LoginInfo(john.email, "wrongpassword"))

      for {
        response <- authRoutes.run(request)
        payload  <- response.as[FailureResponse]
      } yield {
        response.status shouldBe Status.Unauthorized
        payload         shouldBe FailureResponse("User or password is incorrect")
      }
    }

    "login should return JWT token if user found and password is correct" in {
      val request = Request[IO](Method.POST, uri"/auth/login").withEntity(LoginInfo(john.email, "password1"))

      for {
        response <- authRoutes.run(request)
        token     = response.headers.get[Authorization]
      } yield {
        response.status                         shouldBe Status.Ok
        response.headers.get(ci"Authorization") shouldBe defined
        token                                   shouldBe defined
      }
    }

    "signup should return bad request if user already exists" in {
      val request = Request[IO](Method.POST, uri"/auth/signup").withEntity(johnNewUser)

      for {
        response <- authRoutes.run(request)
        payload  <- response.as[FailureResponse]
      } yield {
        response.status shouldBe Status.BadRequest
        payload         shouldBe FailureResponse("User already exists")
      }
    }

    "signup should return user if user created successfully" in {
      val request = Request[IO](Method.POST, uri"/auth/signup").withEntity(annaNewUser)

      for {
        response <- authRoutes.run(request)
        user     <- response.as[User]
      } yield {
        response.status shouldBe Status.Created
        user            shouldBe anna
      }
    }

    "change password should return not found if user does not exist" in {
      val request = Request[IO](Method.POST, uri"/auth/change-password")
        .withEntity(NewPasswordInfo("password1", "password2"))

      for {
        jwtToken <- authenticatorStub.create(anna.email)
        response <- authRoutes.run(request.withBearerToken(jwtToken))
        payload  <- response.as[FailureResponse]
      } yield {
        response.status shouldBe Status.NotFound
        payload         shouldBe FailureResponse(s"User ${anna.email} not found")
      }
    }

    "change password should return forbidden if old password is incorrect" in {
      val request = Request[IO](Method.POST, uri"/auth/change-password")
        .withEntity(NewPasswordInfo("wrongpassword", "password2"))

      for {
        jwtToken <- authenticatorStub.create(john.email)
        response <- authRoutes.run(request.withBearerToken(jwtToken))
        payload  <- response.as[FailureResponse]
      } yield {
        response.status shouldBe Status.Forbidden
        payload         shouldBe FailureResponse("Invalid password")
      }
    }

    "change password should return unauthorized if JWT is invalid" in {
      val request = Request[IO](Method.POST, uri"/auth/change-password")
        .withEntity(NewPasswordInfo("password1", "password2"))

      for {
        jwtToken <- authenticatorStub.create(NotFoundUserEmail)
        response <- authRoutes.run(request.withBearerToken(jwtToken))
      } yield {
        response.status shouldBe Status.Unauthorized
      }
    }

    "change password should return ok if old password is correct" in {
      val request = Request[IO](Method.POST, uri"/auth/change-password")
        .withEntity(NewPasswordInfo("password1", "password2"))

      for {
        jwtToken <- authenticatorStub.create(john.email)
        response <- authRoutes.run(request.withBearerToken(jwtToken))
        user     <- response.as[User]
      } yield {
        response.status shouldBe Status.Ok
        user            shouldBe john
      }
    }

    "logout should return unauthorized if JWT is missing" in {
      val request = Request[IO](Method.POST, uri"/auth/logout")

      for {
        response <- authRoutes.run(request)
      } yield {
        response.status shouldBe Status.Unauthorized
      }
    }

    "logout should return ok if JWT is valid" in {
      val request = Request[IO](Method.POST, uri"/auth/logout")

      for {
        jwtToken <- authenticatorStub.create(john.email)
        response <- authRoutes.run(request.withBearerToken(jwtToken))
      } yield {
        response.status shouldBe Status.Ok
      }
    }

    "delete user should return unauthorized if non-admin tries to delete a user" in {
      val request = Request[IO](Method.DELETE, uri"/auth/users/john_test@email.com")

      for {
        jwtToken <- authenticatorStub.create(anna.email)
        response <- authRoutes.run(request.withBearerToken(jwtToken))
      } yield {
        response.status shouldBe Status.Unauthorized
      }
    }

    "delete user should be success if admin tries to delete a user" in {
      val request = Request[IO](Method.DELETE, uri"/auth/users/anna_test@email.com")

      for {
        jwtToken <- authenticatorStub.create(john.email)
        response <- authRoutes.run(request.withBearerToken(jwtToken))
      } yield {
        response.status shouldBe Status.Ok
      }
    }

    "delete user should return Not Found if admin tries to delete not existing user" in {
      val request = Request[IO](Method.DELETE, uri"/auth/users/non_exists@email.com")

      for {
        jwtToken <- authenticatorStub.create(john.email)
        response <- authRoutes.run(request.withBearerToken(jwtToken))
      } yield {
        response.status shouldBe Status.NotFound
      }
    }
  }

  private val auth = new Auth[IO] {
    def login(email: String, password: String): IO[Option[JwtToken]] =
      if (email == john.email && password == "password1") authenticator.create(john.email).map(Some(_))
      else IO.pure(None)
    def signUp(user: User.New): IO[Option[User]] =
      if (user.email == anna.email) IO.pure(Some(anna)) else IO.pure(None)
    def delete(email: String): IO[Boolean] =
      if (email == anna.email) IO.pure(true) else IO.pure(false)
    def changePassword(email: String, passwordInfo: NewPasswordInfo): IO[Either[String, Option[User]]] =
      if (email == john.email)
        if (passwordInfo.oldPassword == "password1") IO.pure(Right(Some(john))) else IO.pure(Left("Invalid password"))
      else IO.pure(Right(None))
    def authenticator: Authenticator[IO] =
      authenticatorStub
  }

  private val authenticatorStub: Authenticator[IO] = {
    // 1. Key for hashing
    val key = HMACSHA256.unsafeGenerateKey

    // 2. Indentity store to retrieve users
    val idStore: IdentityStore[IO, String, User] = (email: String) =>
      email match {
        case john.email => OptionT.pure(john)
        case anna.email => OptionT.pure(anna)
        case _          => OptionT.none[IO, User]
      }

    // 3. jwt authenticator
    JWTAuthenticator.unbacked.inBearerToken(
      1.day,
      None,
      idStore,
      key,
    )
  }

  given logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  private val authRoutes = AuthRoutes.make[IO](auth).routes.orNotFound

  extension (request: Request[IO])
    def withBearerToken(token: JwtToken): Request[IO] =
      request.putHeaders {
        val jwtString = JWTMac.toEncodedString[IO, HMACSHA256](token.jwt)
        Authorization(Credentials.Token(AuthScheme.Bearer, jwtString))
      }
