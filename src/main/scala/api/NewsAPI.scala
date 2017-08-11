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

trait NewsAPI extends JsonMappings {

  import scala.concurrent.duration._

  def newsHandler: ActorRef

  def popularityHandler: ActorRef

  implicit def requestTimeout = Timeout(5 seconds)

  val newsAPI =
    (path(LongNumber) & get ) {(newsId) =>
      complete {
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
      (post) { entity(as[NewsItem]) { news =>
        complete {
          val popularityFuture = popularityHandler ? FetchPopularity(NewsItemForPopularity(news.id, news.title, news.content, news.createdDate, news.popularity, news.tag))
          popularityFuture.map { popularity =>
            newsHandler ? NewsItemForRedis(news.id, news.title, news.content, news.createdDate, popularity.toString.toInt, news.tag)
            val newNews = news.copy(popularity = popularity.toString.toInt)
            NewsDAO.create(newNews).map(_.toJson)
          }
        }
       }
      }~
        (path(LongNumber) & put) { id =>
          entity(as[NewsItem]) { news =>
            complete{
              newsHandler ? NewsItemForRedis(news.id, news.title, news.content, news.createdDate, news.popularity, news.tag)
              NewsDAO.update(id, news).map(_.toJson)
            }
          }
        }~
        (path("search") & post) { entity(as[NewsSearch]) { news =>
          complete {
            val response = newsHandler ? SearchNews(news.title)
            response.map(_.toString.toJson)
          }
        }
    }~
      (path(LongNumber) & delete ) { newsId =>
        complete {
          NewsDAO.findById(newsId).flatMap{ news =>
            newsHandler ? DeleteNews(news.title)
            }
          NewsDAO.delete(newsId).map(_.toJson)
          }
    }
}