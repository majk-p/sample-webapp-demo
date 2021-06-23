package example

import sttp.tapir._
import sttp.tapir.CodecFormat.TextPlain


object OAuthEndpoints {

  val loginRedirect: Endpoint[Unit, Unit, RedirectUrl, Any] =
    endpoint
      .get
      .in("api" / "login-redirect")
      .out(header[RedirectUrl]("Location"))
      .out(statusCode(sttp.model.StatusCode.SeeOther))

  val postLogin: Endpoint[(AuthorizationCode, State), Unit, String, Any] =
    endpoint
      .get
      .in("api" / "post-login")
      .in(query[AuthorizationCode]("code"))
      .in(query[State]("state"))
      .out(stringBody)

  final case class AuthorizationCode(value: String) extends AnyVal

  object AuthorizationCode {
    implicit val endpointCodec: Codec[String, AuthorizationCode, TextPlain] =
      Codec.string.map(AuthorizationCode(_))(_.value)
  }

  final case class State(value: String) extends AnyVal

  object State {
    implicit val endpointCodec: Codec[String, State, TextPlain] =
      Codec.string.map(State(_))(_.value)
  }

  final case class RedirectUrl(value: String) extends AnyVal

  object RedirectUrl {
    implicit val endpointCodec: Codec[String, RedirectUrl, TextPlain] =
      Codec.string.map(RedirectUrl(_))(_.value)
  }

}
