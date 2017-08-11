import akka.http.scaladsl.server.Directives._
import api._

trait Routes extends ApiErrorHandler with NewsAPI{
  val routes =
    pathPrefix("news") {
      newsAPI
    } ~ path("")(getFromResource("public/index.html"))
}