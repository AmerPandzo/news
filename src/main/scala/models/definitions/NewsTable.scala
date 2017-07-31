package models.definitions

import slick.model.ForeignKeyAction
// NOTE: GetResult mappers for plain SQL are only generated for tables where Slick knows how to map the types of all columns.
//import slick.driver.PostgresDriver.api._
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.{GetResult => GR}

object NewsTable {

  /** Entity class storing rows of table News
    *  @param id Database column id SqlType(bigserial), AutoInc, PrimaryKey
    *  @param createdDate Database column createdDate SqlType(date)
    *  @param popularity Database column popularity SqlType(int4)
    *  @param title Database column title SqlType(text)
    *  @param content Database column content SqlType(text) */
  case class NewsRow(id: Long, createdDate: java.sql.Date, popularity: Int, title: String, content: String)
  /** GetResult implicit for fetching NewsRow objects using plain SQL queries */
  implicit def GetResultNewsRow(implicit e0: GR[Long], e1: GR[java.sql.Date], e2: GR[Int], e3: GR[String]): GR[NewsRow] = GR{
    prs => import prs._
      NewsRow.tupled((<<[Long], <<[java.sql.Date], <<[Int], <<[String], <<[String]))
  }
  /** Table description of table news. Objects of this class serve as prototypes for rows in queries. */
  class News(_tableTag: Tag) extends Table[NewsRow](_tableTag, "news") {
    def * = (id, createdDate, popularity, title, content) <> (NewsRow.tupled, NewsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(createdDate), Rep.Some(popularity), Rep.Some(title), Rep.Some(content)).shaped.<>({r=>import r._; _1.map(_=> NewsRow.tupled((_1.get, _2.get, _3.get, _4.get, _5.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column id SqlType(bigserial), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("id", O.AutoInc, O.PrimaryKey)
    /** Database column createdDate SqlType(date) */
    val createdDate: Rep[java.sql.Date] = column[java.sql.Date]("createdDate")
    /** Database column popularity SqlType(int4) */
    val popularity: Rep[Int] = column[Int]("popularity")
    /** Database column title SqlType(text) */
    val title: Rep[String] = column[String]("title")
    /** Database column content SqlType(text) */
    val content: Rep[String] = column[String]("content")
  }
  /** Collection-like TableQuery object for table News */
  lazy val News = new TableQuery(tag => new News(tag))

  }