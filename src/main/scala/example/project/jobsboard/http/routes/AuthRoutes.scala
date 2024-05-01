package example.project.jobsboard.http.routes

import cats.Monad
import cats.syntax.all.toSemigroupKOps
import io.circe.generic.auto.*
import org.http4s.FormDataDecoder.formEntityDecoder
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import example.project.jobsboard.core.Auth

class AuthRoutes[F[_]: Monad] private (auth: Auth[F]) extends Http4sDsl[F]:
  // POST /auth/login json { login info } => 200 Ok with JWT as Authorization: Bearer header
  private val loginRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "login" => ???
  }

  // POST /auth/signup json { new user } => 201 Created with User
  private val signupRoute: HttpRoutes[F] = HttpRoutes.of[F] {
    case req @ POST -> Root / "signup" => ???
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
  def make[F[_]: Monad](auth: Auth[F]): AuthRoutes[F] = new AuthRoutes[F](auth)
