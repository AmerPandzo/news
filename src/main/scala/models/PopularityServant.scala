package models

import akka.actor.{Actor, ActorLogging, Props}
import mappings.JsonMappings
import org.joda.time.DateTime
import repository.Repo
import spray.json._

object PopularityServant  {
  def props(db: Repo): Props = Props(new PopularityServant (db))
  case class NewsItemForPopularity( id: Long,
                               title: String,
                               content: String,
                               createdDate: DateTime,
                               popularity: Int,
                               tag: Option[List[String]])
  case class FetchPopularity(item: NewsItemForPopularity)
}

class PopularityServant (db: Repo) extends Actor with ActorLogging with JsonMappings {
  import PopularityServant ._
  implicit val ec = context.dispatcher
  override def receive: Receive = {

    case FetchPopularity(NewsItemForPopularity( id: Long,
          title: String,
          content: String,
          createdDate: DateTime,
          popularity: Int,
          tag: Option[List[String]])) =>

      val requestor = sender()
      val url = "https://newsapi.org/v1/articles?source=techcrunch&apiKey=02403e7d233944159c9e9cdab52036f9"
      val result = scala.io.Source.fromURL(url).mkString
      val techCrouch = result.parseJson.convertTo[TechCrouch]
      val articles = techCrouch.articles
      val wordsOfNewsItem = title.toLowerCase.split(" ").toList
      val wordsOfArticles = articles.map(article => article.title.toLowerCase.split(" ").toList).flatten

      val countOfWords = wordsOfNewsItem.map( x => wordsOfArticles.count(y => y == x))
      val popularity = countOfWords.sum
      requestor ! popularity
  }
}
