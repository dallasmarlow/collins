package controllers
package actors

import akka.util.Duration
import play.api.mvc.AnyContent
import play.api.mvc.Request
import util.concurrent.BackgroundProcess
import util.plugins.SoftLayer

case class ActivationProcessor(slId: Long, userTimeout: Option[Duration] = None)(implicit req: Request[AnyContent]) extends BackgroundProcess[Boolean] {
  override def defaultTimeout: Duration = Duration.parse("60 seconds")
  val timeout = userTimeout.getOrElse(defaultTimeout)

  def run(): Boolean = {
    val plugin = SoftLayer.pluginEnabled.get
    plugin.activateServer(slId)()
  }
}

