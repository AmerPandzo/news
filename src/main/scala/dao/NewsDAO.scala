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

  def update(id: Long, model: NewsItem): Future[Long] = {

    val futureOldNews = db.run(News.filter(_.id === id).result)
    futureOldNews.flatMap { oldNewsRows =>
      val oldNews = oldNewsRows.head
      val updateNews = News.filter(_.id === id).map { n =>
        /* The columns which will be available to update: */
        (
          n.content,
          n.title,
          n.popularity,
          n.createdDate,
        )
      }.update(
        /* The values to set the columns on update */
        (
          model.content,
          model.title,
          model.popularity,
          new Date(model.createdDate.toDate.getTime)
        )
      )

      db.run(updateNews).flatMap { update =>

        val deleteNewsTags = db.run(Tags.filter(_.newsId === oldNews.id).delete) // delete old images fields

        val newTags = model.tag.toList.flatMap {
          listOfTags => listOfTags.map{tag => TagsRow(tag, oldNews.id)}
        }

        val futureDeleteNewsData = for {
          _ <- deleteNewsTags
        } yield ()
        futureDeleteNewsData.flatMap { _ => // when old recipes are delete, then add new
          val futureInsertNewsData = for {
            _ <- db.run(Tags ++= newTags)
          } yield ()
          futureInsertNewsData map (_ => update)
        }
      }
    }
  }
}