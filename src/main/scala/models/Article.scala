package models

import models.definitions.TagTable.TagsRow
import org.joda.time.DateTime

case class Article(
                     author: String,
                     title: String,
                     description: String,
                     url: String,
                     urlToImage: String,
                     publishedAt: String
                   )