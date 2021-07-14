package example

import sttp.model.Uri

final case class Config(
  oauth2: Config.OAuth2,
  server: Config.Server
)

object Config {

  final case class Server(
    host: String,
    port: Int
  )

  final case class OAuth2(
    providerBaseUrl: Uri,
    appId: String,
    appSecret: String
  )

}