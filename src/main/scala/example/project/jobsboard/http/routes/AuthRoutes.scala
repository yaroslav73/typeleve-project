package example.project.jobsboard.http.routes

import cats.effect.Concurrent
import cats.syntax.all.toFlatMapOps
import cats.syntax.all.toFunctorOps
import cats.syntax.all.toSemigroupKOps
import io.circe.generic.auto.*
import org.http4s.FormDataDecoder.formEntityDecoder
import org.http4s.HttpRoutes
import org.http4s.EntityEncoder
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import example.project.jobsboard.core.Auth
import example.project.jobsboard.domain.Auth.LoginInfo
import example.project.jobsboard.http.responses.FailureResponse
import org.typelevel.log4cats.Logger
import org.http4s.Response
import example.project.jobsboard.domain.User

class AuthRoutes[F[_]: Concurrent: Logger] private (auth: Auth[F]) extends Http4sDsl[F]:
  private val authenticator = auth.authenticator

  // POST /auth/login json { login info } => 200 Ok with JWT as Authorization: Bearer header
  private val loginRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" =>
      for {
        loginInfo <- req.as[LoginInfo]
        token     <- auth.login(loginInfo.email, loginInfo.password)
        _         <- Logger[F].info(s"User ${loginInfo.email} logged in")
        response = token.fold(
          Response[F](
            Unauthorized,
            body = EntityEncoder[F, FailureResponse].toEntity(FailureResponse("User or password is incorrect")).body
          )
        )(token => authenticator.embed(Response[F](Ok), token))
      } yield response
  }

  // POST /auth/signup json { new user } => 201 Created with User
  private val signupRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "signup" =>
      for {
        newUser  <- req.as[User.New]
        user     <- auth.signUp(newUser)
        response <- user.fold(BadRequest(FailureResponse("User already exists")))(user => Created(user))
      } yield response
  }

  // POST /auth/change-password json { new password info } { Authorization: Bearer } => Ok with updated user
  private val changePasswordRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "change-password" => ???
  }

  // POST /auth/logout { Authorization: Bearer } => Ok
  private val logoutRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "logout" => ???
  }

  val routes: HttpRoutes[F] = Router[F]("/auth" -> (loginRoute <+> signupRoute <+> changePasswordRoute <+> logoutRoute))

object AuthRoutes:
  def make[F[_]: Concurrent: Logger](auth: Auth[F]): AuthRoutes[F] = new AuthRoutes[F](auth)
