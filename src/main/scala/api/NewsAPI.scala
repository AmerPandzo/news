package api

import dao.NewsDAO

import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import mappings.JsonMappings
import models._
import akka.http.scaladsl.server.Directives._
import spray.json._

trait NewsAPI extends JsonMappings{
  val newsAPI =
    (path("news"/LongNumber) & get ) {(newsId) =>
      complete (NewsDAO.findById(newsId).map(_.toJson))
    }~
      (path("news") & post) { entity(as[NewsItem]) { news =>
        complete (NewsDAO.create(news).map(_.toJson))
      }
   }
}