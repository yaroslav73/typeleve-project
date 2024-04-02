package example.project.jobsboard.core

import cats.syntax.all.*
import tsec.authentication.AugmentedJWT
import tsec.mac.jca.HMACSHA256
import example.project.jobsboard.domain.Auth.NewPasswordInfo
import example.project.jobsboard.domain.User
import example.project.jobsboard.domain.User.New
import example.project.jobsboard.domain.Aliases.JwtToken
import example.project.jobsboard.domain.Aliases.Authenticator
import cats.Applicative
import tsec.passwordhashers.jca.BCrypt
import tsec.passwordhashers.PasswordHash
import cats.effect.kernel.Sync

trait Auth[F[_]]:
  def login(email: String, password: String): F[Option[JwtToken]]
  def signUp(user: User.New): F[Option[User]]
  def changePassword(email: String, newPassword: NewPasswordInfo): F[Either[String, Option[User]]]

object Auth:
  def make[F[_]: Sync](users: Users[F], authenticator: Authenticator[F]): Auth[F] = new Auth[F] {
    def login(email: String, password: String): F[Option[JwtToken]] =
      for {
        user  <- users.find(email)
        user  <- user.filterA(user => checkPassword(password, user.hashedPassword))
        token <- user.traverse(user => authenticator.create(user.email))
      } yield token

    def signUp(user: New): F[Option[User]] =
      for {
        userOpt <- users.find(user.email)
        user <- userOpt match
          case Some(_) => None.pure[F]
          case _ =>
            for {
              hashedPassword <- BCrypt.hashpw[F](user.password)
              userId         <- users.create(user.withHashedPassword(hashedPassword))
              user           <- users.find(userId)
            } yield user
      } yield user

    def changePassword(email: String, passwordInfo: NewPasswordInfo): F[Either[String, Option[User]]] =
      for {
        user <- users.find(email)
        user <- user match
          case Some(user) =>
            checkPassword(passwordInfo.oldPassword, user.hashedPassword).ifM(
              updatePassword(user, passwordInfo.newPassword).map(_.asRight),
              "Invalid password".asLeft.pure[F]
            )
          case None => "User with this email not found".asLeft.pure[F]
      } yield user

    private def updatePassword(user: User, newPassword: String): F[Option[User]] =
      for {
        hashedPassword <- BCrypt.hashpw[F](newPassword)
        user           <- users.update(user.copy(hashedPassword = hashedPassword))
      } yield user

    private def checkPassword(password: String, hashedPassword: String): F[Boolean] =
      BCrypt.checkpwBool[F](password, PasswordHash[BCrypt](hashedPassword))
  }
