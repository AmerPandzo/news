package mappings

import java.time.format.DateTimeFormatter

import models._
import spray.json._
import org.joda.time.DateTime
import org.joda.time.format.{ISODateTimeFormat, DateTimeFormatter}
import models.definitions.TagTable._

trait JsonMappings extends DefaultJsonProtocol {

    implicit object DateTimeFormat extends RootJsonFormat[DateTime] {

      val formatter = ISODateTimeFormat.basicDateTimeNoMillis

      def write(obj: DateTime): JsValue = {
        JsString(formatter.print(obj))
      }

      def read(json: JsValue): DateTime = json match {
        case JsString(s) => try {
          formatter.parseDateTime(s)
        }
        catch {
          case t: Throwable => error(s)
        }
        case _ =>
          error(json.toString())
      }

      def error(v: Any): DateTime = {
        val example = formatter.print(0)
        deserializationError(f"'$v' is not a valid date value. Dates must be in compact ISO-8601 format, e.g. '$example'")
      }
    }

  implicit object TagsRowFormat extends RootJsonFormat[TagsRow] {
    // some fields are optional so we produce a list of options and
    // then flatten it to only write the fields that were Some(..)
    def write(item: TagsRow) = JsObject(
      List(
        Some("newsId" -> item.newsId.toJson),
        Some("name" -> item.name.toJson),
      ).flatten: _*
    )

    // we use the "standard" getFields method to extract the mandatory fields.
    // For optional fields we extract them directly from the fields map using get,
    // which already handles the option wrapping for us so all we have to do is map the option
    def read(json: JsValue) = {
      val jsObject = json.asJsObject

      jsObject.getFields("newsId","name") match {
        case Seq(newsId, name) ⇒ TagsRow(
          name.convertTo[String],
          newsId.convertTo[Int]
        )
        case other ⇒ deserializationError("Cannot deserialize ProductItem: invalid input. Raw input: " + other)
      }
    }
  }

  implicit object ArticleFormat extends RootJsonFormat[Article] {
    // some fields are optional so we produce a list of options and
    // then flatten it to only write the fields that were Some(..)
    def write(item: Article) = JsObject(
      List(
        Some("author" -> item.author.toJson),
        Some("title" -> item.title.toJson),
        Some("description" -> item.description.toJson),
        Some("url" -> item.url.toJson),
        Some("urlToImage" -> item.urlToImage.toJson),
        Some("publishedAt" -> item.publishedAt.toJson)
      ).flatten: _*
    )

    // we use the "standard" getFields method to extract the mandatory fields.
    // For optional fields we extract them directly from the fields map using get,
    // which already handles the option wrapping for us so all we have to do is map the option
    def read(json: JsValue) = {
      val jsObject = json.asJsObject

      jsObject.getFields("author","title", "description", "url", "urlToImage", "publishedAt") match {
        case Seq(author,title, description, url, urlToImage, publishedAt) ⇒ Article(
          author.convertTo[String],
          title.convertTo[String],
          description.convertTo[String],
          url.convertTo[String],
          urlToImage.convertTo[String],
          publishedAt.convertTo[String]
        )
        case other ⇒ deserializationError("Cannot deserialize ProductItem: invalid input. Raw input: " + other)
      }
    }
  }
      implicit val tagFormat = jsonFormat1(NewsTag)
      implicit val newsFormat = jsonFormat6(NewsItem)
      implicit val articlesFormat = jsonFormat6(Article)
      implicit val techCrouchFormat = jsonFormat4(TechCrouch)
      implicit val searchFormat = jsonFormat1(NewsSearch)
    }
