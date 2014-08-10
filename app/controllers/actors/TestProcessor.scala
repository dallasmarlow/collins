package controllers.actors

import com.twitter.util.Future

import akka.util.Duration
import util.concurrent.BackgroundProcess

case class TestProcessor(sleepMs: Long, userTimeout: Option[Duration] = None)
  extends BackgroundProcess[Boolean] {
  override def defaultTimeout: Duration = Duration.parse("5 seconds")
  val timeout = userTimeout.getOrElse(defaultTimeout)

  def run(): Boolean = {
    val future = Future {
      println("Sleeping for %d millis".format(sleepMs))
      Thread.sleep(sleepMs)
      println("Done sleeping for %d millins".format(sleepMs))
    }
    future()
    true
  }
}
