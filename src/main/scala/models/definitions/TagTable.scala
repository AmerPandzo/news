package models.definitions
import slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
//import slick.driver.PostgresDriver.api._
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{GetResult => GR}
import NewsTable._

object TagTable {

  /** Entity class storing rows of table Tags
    *  @param name Database column name SqlType(text)
    *  @param newsId Database column news_id SqlType(int8) */
  case class TagsRow(name: String, newsId: Long)
  /** GetResult implicit for fetching TagsRow objects using plain SQL queries */
  implicit def GetResultTagsRow(implicit e0: GR[String], e1: GR[Long]): GR[TagsRow] = GR{
    prs => import prs._
      TagsRow.tupled((<<[String], <<[Long]))
  }
  /** Table description of table tags. Objects of this class serve as prototypes for rows in queries. */
  class Tags(_tableTag: Tag) extends Table[TagsRow](_tableTag, "tags") {
    def * = (name, newsId) <> (TagsRow.tupled, TagsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(name), Rep.Some(newsId)).shaped.<>({r=>import r._; _1.map(_=> TagsRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column name SqlType(text) */
    val name: Rep[String] = column[String]("name")
    /** Database column news_id SqlType(int8) */
    val newsId: Rep[Long] = column[Long]("news_id")

    /** Foreign key referencing News (database name news_id) */
    lazy val newsFk = foreignKey("news_id", newsId, News)(r => r.id, onUpdate=ForeignKeyAction.NoAction, onDelete=ForeignKeyAction.NoAction)
  }
  /** Collection-like TableQuery object for table Tags */
  lazy val Tags = new TableQuery(tag => new Tags(tag))
}
