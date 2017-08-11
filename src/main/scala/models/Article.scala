package models

case class Article(
                    author: String,
                    title: String,
                    description: String,
                    url: String,
                    urlToImage: String,
                    publishedAt: String
                  )