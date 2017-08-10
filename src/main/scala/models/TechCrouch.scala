package models

import models.definitions.TagTable.TagsRow
import org.joda.time.DateTime

case class TechCrouch(
                    status: String,
                    source: String,
                    sortBy: String,
                    articles: List[Article]
                  )