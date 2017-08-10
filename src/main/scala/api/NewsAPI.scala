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
import akka.pattern.ask
import models.PopularityServant.{FetchPopularity, NewsItemForPopularity}

case class UpsertRequest(username:String, password:String )

trait NewsAPI extends JsonMappings {

  import scala.concurrent.duration._

  def newsHandler: ActorRef

  def popularityHandler: ActorRef

  implicit def requestTimeout = Timeout(5 seconds)

  val newsAPI =
    (path("news"/LongNumber) & get ) {(newsId) =>
      complete {
        //newsHandler ? GetNews(newsId)
        NewsDAO.findById(newsId).flatMap{ news =>
          val newsResponseFuture = newsHandler ? GetNews(news.title)
          newsResponseFuture.map{ newsResponse => newsResponse match {
            case true =>
            case NewsItemForRedis => newsHandler ? NewsItemForRedis(news.id, news.title, news.content, news.createdDate, news.popularity, news.tag)
            }
          }
        }
        NewsDAO.findById(newsId).map(_.toJson)
      }
    }~
      (path("news") & post) { entity(as[NewsItem]) { news =>
        complete {
          val popularityfuture = popularityHandler ? FetchPopularity(NewsItemForPopularity(news.id, news.title, news.content, news.createdDate, news.popularity, news.tag))
          popularityfuture.map { popularity =>
            newsHandler ? NewsItemForRedis(news.id, news.title, news.content, news.createdDate, popularity.toString.toInt, news.tag)
            val newnews = news.copy(popularity = popularity.toString.toInt)
            NewsDAO.create(newnews).map(_.toJson)
          }
        }
       }
      }~
        (path("news" / LongNumber) & put) { id =>
          entity(as[NewsItem]) { news =>
            complete{
              newsHandler ? NewsItemForRedis(news.id, news.title, news.content, news.createdDate, news.popularity, news.tag)
              NewsDAO.update(id, news).map(_.toJson)
            }
          }
        }~
        (path("search" / Segment) & get ) { (title) =>
        complete {
           val nesto = newsHandler ? SearchNews(title)
          nesto.map(_.toString.toJson)
        }
    }~
      (path("delete" / LongNumber) & delete ) { newsId =>
        complete {
          NewsDAO.findById(newsId).flatMap{ news =>
            newsHandler ? DeleteNews(news.title)
            }
          NewsDAO.delete(newsId).map(_.toJson)
          }
    }
}