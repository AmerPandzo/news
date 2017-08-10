package repository

import redis.{ByteStringSerializer, RedisClient}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.Duration


trait Repo {

  def del(key: String): Future[Long]

  def upsert[V](key: String, value: Map[String,V] , expire: Option[Duration] = None)(implicit ev: ByteStringSerializer[V]): Future[Boolean]

  def get(key: String): Future[Option[String]]

  def search(key: String): Future[String]
}

abstract class RedisRepoImpl extends Repo {

  def db: RedisClient

  def del(key: String): Future[Long] = db.del(key)

  def upsert[V](key: String, value: Map[String,V], expire: Option[Duration] = None)(implicit ev: ByteStringSerializer[V]): Future[Boolean] =
    db.hmset(key, value)

  def get(key: String): Future[Option[String]] = db.get[String](key)

  def search(key: String): Future[String] = db.keys(key).map(_.head)

}