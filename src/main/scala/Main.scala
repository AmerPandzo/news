import akka.actor.ActorSystem
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import redis.RedisClient
import repository.RedisRepoImpl
import utils.Config
import models._
import scala.concurrent.ExecutionContext
import repository.{ConcreteRedis, RedisRepoImpl}

object Main extends App with Config with Routes with ConcreteRedis{
  private implicit val system = ActorSystem()
  protected implicit val executor: ExecutionContext = system.dispatcher
  protected val log: LoggingAdapter = Logging(system, getClass)
  protected implicit val materializer: ActorMaterializer = ActorMaterializer()

  val prodDb = new RedisRepoImpl {
    override def db = RedisClient(host = redisUrl.getHost, port = redisUrl.getPort, password = pwd)
  }

  val userHandler = system.actorOf(RedisServant.props(prodDb))

  Http().bindAndHandle(handler = logRequestResult("log")(routes), interface = httpInterface, port = httpPort)
}


