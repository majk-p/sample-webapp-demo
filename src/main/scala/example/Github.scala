package example

import cats.effect.IO
import cats.implicits._
import com.ocadotechnology.sttp.oauth2.Secret
import io.circe.generic.auto._
import sttp.client3._
import sttp.client3.circe._
import sttp.model.Header

trait Github {
  def userInfo(accessToken: Secret[String]): IO[Github.UserInfo]
}

object Github {

  final case class UserInfo(
    login: String,
    name: String
  )

  val baseUri = uri"https://api.github.com/"

  def instance(backend: SttpBackend[IO, Any]) = new Github {

    override def userInfo(accessToken: Secret[String]): IO[UserInfo] = {
      val header = Header("Authorization", s"Bearer ${accessToken.value}")
      basicRequest
        .get(baseUri.withPath("user"))
        .headers(header)
        .response(asJson[UserInfo])
        .send(backend)
        .map(_.body)
        .rethrow

    }

  }

}
