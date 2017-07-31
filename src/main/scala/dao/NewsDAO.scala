package dao

import java.sql.Date

import dao.NewsDAO._
import models._
import models.definitions.NewsTable._
import models.definitions.TagTable._
import org.joda.time.DateTime
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import utils.DatabaseConfig

object NewsDAO extends BaseDao {

  def findById(id: Long): Future[NewsItem] = {

    val newsQuery = for {
      news <- News.filter(_.id === id)
    } yield (news)

    val newsAndTags = for {
      (news, tags) <- newsQuery.joinLeft(Tags).on(_.id === _.newsId)
    } yield (news , tags)

    val newsAndTagsResult = newsAndTags.to[List].result


    val newsQueryResult = newsQuery.result.headOption

    db.run(newsAndTagsResult).map { tuples =>
      val groupedByNewsSeq = tuples.groupBy(_._1).toSeq

      val seqNewsRows = groupedByNewsSeq.map {
        case (newsRow, tuples) =>
          val tagsNames = tuples.flatMap(_._2.map(_.name))
          NewsItem(
            id = newsRow.id,
            title = newsRow.title,
            content = newsRow.content,
            createdDate = new DateTime(newsRow.createdDate.getTime()),
            popularity = newsRow.popularity,
            tag = Option(tagsNames)
          )
      }
      seqNewsRows.head
    }
  }

  def create(model: NewsItem): Future[Long] = {

    val newsRow = NewsRow(
      content = model.content,
      createdDate = new Date(model.createdDate.toDate.getTime),
      id = 0,
      popularity = model.popularity,
      title = model.title
    )

    val insertNews = db.run((News returning News.map(_.id)) += newsRow)
    insertNews.flatMap { newId =>

      val imagesFields = model.tag.map { listOfTags => listOfTags.map {tag => TagsRow(newsId = newId, name = tag )} }.get

      for {
        _ <- db.run(Tags ++= imagesFields)
      } yield newId
    }


  }
}