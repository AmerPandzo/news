package models

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import org.joda.time.DateTime
import repository.Repo

object RedisServant {
  def props(db: Repo): Props = Props(new RedisServant(db))
  case class SearchNews(username: String)
  case class NewsItemForRedis( id: Long,
                       title: String,
                       content: String,
                       createdDate: DateTime,
                       popularity: Int,
                       tag: Option[List[String]])
  case class Update(username: String, details: String)
  case class GetNews(username: String)
  case class DeleteNews(username: String)
  case class UserNotFound(username: String)
  case class UserDeleted(username: String)
}
//simple DI through constructor but of course you can use any DI //framework (e.g Guice, Spring ...) or DI pattern (e.g cake pattern) //but this is out of scope of this post
class RedisServant(db: Repo) extends Actor with ActorLogging {
  import RedisServant._
  implicit val ec = context.dispatcher
  override def receive: Receive = {
    case NewsItemForRedis(id, title, content, createdDate, popularity, tag) =>
      db.upsert(title.toLowerCase, Map("id" -> id.toString , "title" -> title, "content" -> content, "createdDate" -> createdDate.toString, "popularity" -> popularity.toString, "tags" -> tag.getOrElse(Nil).mkString(","))) pipeTo sender()

    case GetNews(title) =>
      //closing over the sender in Future is not safe. http://helenaedelson.com/?p=879
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
