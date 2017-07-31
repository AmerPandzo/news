package models

import models.definitions.TagTable.TagsRow
import org.joda.time.DateTime

case class NewsItem(
                     id: Long,
                     title: String,
                     content: String,
                     createdDate: DateTime,
                     popularity: Int,
                     tag: Option[List[String]]
)