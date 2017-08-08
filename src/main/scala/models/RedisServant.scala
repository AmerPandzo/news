package models

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.pipe
import org.joda.time.DateTime
import repository.Repo

object RedisServant {
  def props(db: Repo): Props = Props(new RedisServant(db))
  case class User(username: String, details: String)
  case class NewsItemForRedis( id: Long,
                       title: String,
                       content: String,
                       createdDate: DateTime,
                       popularity: Int,
                       tag: Option[List[String]])
  case class Update(username: String, details: String)
  case class GetUser(username: String)
  case class DeleteUser(username: String)
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
  }
}
