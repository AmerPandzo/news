package api

import akka.actor.{ActorRef, ActorSystem}
import dao.NewsDAO

import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import mappings.JsonMappings
import models._
import akka.http.scaladsl.server.Directives._
import spray.json._
import models.RedisServant._
import akka.util.Timeout
import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.pattern.ask

case class UpsertRequest(username:String, password:String )

trait NewsAPI extends JsonMappings {

  import scala.concurrent.duration._

  def userHandler: ActorRef

  implicit def requestTimeout = Timeout(5 seconds)

  val newsAPI =
    (path("news"/LongNumber) & get ) {(newsId) =>
      complete (NewsDAO.findById(newsId).map(_.toJson))
    }~
      (path("news") & post) { entity(as[NewsItem]) { news =>
        complete {
          userHandler ? NewsItemForRedis(news.id, news.title, news.content, news.createdDate, news.popularity, news.tag)
          NewsDAO.create(news).map(_.toJson)
        }
      }~
        (path("news" / LongNumber) & put) { id =>
          entity(as[NewsItem]) { news =>
            complete(NewsDAO.update(id, news).map(_.toJson))
          }
        }
   }
}