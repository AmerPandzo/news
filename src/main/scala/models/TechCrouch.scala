package models

case class TechCrouch(
                    status: String,
                    source: String,
                    sortBy: String,
                    articles: List[Article]
                  )