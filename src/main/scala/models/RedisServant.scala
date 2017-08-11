package models

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import org.joda.time.DateTime
import repository.Repo

object RedisServant {
  def props(db: Repo): Props = Props(new RedisServant(db))
  case class SearchNews(title: String)
  case class NewsItemForRedis( id: Long,
                       title: String,
                       content: String,
                       createdDate: DateTime,
                       popularity: Int,
                       tag: Option[List[String]])
  case class GetNews(title: String)
  case class DeleteNews(title: String)
}

class RedisServant(db: Repo) extends Actor with ActorLogging {
  import RedisServant._
  implicit val ec = context.dispatcher
  override def receive: Receive = {
    case NewsItemForRedis(id, title, content, createdDate, popularity, tag) =>
      db.upsert(title.toLowerCase, Map("id" -> id.toString , "title" -> title, "content" -> content, "createdDate" -> createdDate.toString, "popularity" -> popularity.toString, "tags" -> tag.getOrElse(Nil).mkString(","))) pipeTo sender()

    case GetNews(title) =>
      val requestor = sender()
      db.get(title).foreach{
        case Some(i) => requestor ! true
        case None => requestor ! NewsItemForRedis
      }
    case SearchNews(title) =>
      db.search(title.toLowerCase()) pipeTo sender()

    case DeleteNews(title) =>
      db.del(title.toLowerCase()) pipeTo sender()
  }
}
