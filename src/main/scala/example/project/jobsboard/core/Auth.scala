package example.project.jobsboard.core

import cats.syntax.all.*
import tsec.authentication.AugmentedJWT
import tsec.mac.jca.HMACSHA256
import example.project.jobsboard.domain.Auth.NewPasswordInfo
import example.project.jobsboard.domain.User
import example.project.jobsboard.domain.User.New
import example.project.jobsboard.domain.Aliases.JwtToken
import example.project.jobsboard.domain.Aliases.Authenticator
import cats.FlatMap
import cats.Applicative
import cats.Monad
import cats.Functor

trait Auth[F[_]]:
  def login(email: String, password: String): F[Option[JwtToken]]
  def signUp(user: User.New): F[Option[User]]
  def changePassword(email: String, newPassword: NewPasswordInfo): F[Either[String, Option[User]]]

object Auth:
  def make[F[_]: Monad](users: Users[F], authenticator: Authenticator[F]): Auth[F] = new Auth[F] {
    def login(email: String, password: String): F[Option[JwtToken]] =
      users.find(email).flatMap {
        case Some(user) if user.hashedPassword == password =>
          authenticator.create(user.email).map(_.some)
        case _ =>
          Applicative[F].pure(None)
      }

    def signUp(user: New): F[Option[User]] =
      users.find(user.email).flatMap {
        case Some(_) => Applicative[F].pure(None)
        case _ =>
          for {
            userId <- users.create(user)
            user   <- users.find(userId)
          } yield user
      }

    def changePassword(email: String, passwordInfo: NewPasswordInfo): F[Either[String, Option[User]]] =
      users.find(email).flatMap {
        case Some(user) if user.hashedPassword == passwordInfo.oldPassword =>
          users.update(user.copy(hashedPassword = passwordInfo.newPassword)).map(_.asRight)
        case Some(_) =>
          Applicative[F].pure("Invalid password".asLeft)
        case _ =>
          Applicative[F].pure("User with this email not found".asLeft)
      }
  }
