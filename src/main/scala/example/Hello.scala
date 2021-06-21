package example

import cats.effect._
import sttp.tapir._

import sttp.tapir.server.http4s.Http4sServerInterpreter
import org.http4s.HttpRoutes

import cats.syntax.all._

import scala.concurrent.ExecutionContext
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.Router
import org.http4s.syntax.kleisli._

object Hello extends IOApp {

  val helloWorld: Endpoint[String, Unit, String, Any] =
    endpoint.get.in("hello").in(query[String]("name")).out(stringBody)

  def endpointLogic(name: String) = IO(s"Hello, $name!".asRight[Unit])
  
  val helloWorldRoutes: HttpRoutes[IO] = 
    Http4sServerInterpreter.toRoutes(helloWorld)(endpointLogic _)

  def serverResource(routes: HttpRoutes[IO])(ec: ExecutionContext) = 
    BlazeServerBuilder[IO](ec)
      .bindHttp(8080, "localhost")
      .withHttpApp(Router("/" -> routes).orNotFound)
      .resource 

  override def run(args: List[String]): IO[ExitCode] = 
    serverResource(helloWorldRoutes)(executionContext)
      .use(_ => IO.never)
      .as(ExitCode.Success)
  
}
