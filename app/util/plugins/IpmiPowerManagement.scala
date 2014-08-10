package util.plugins

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import com.twitter.util.Future
import com.twitter.util.FuturePool

import collins.power.Identify
import collins.power.PowerAction
import collins.power.PowerOff
import collins.power.PowerOn
import collins.power.PowerSoft
import collins.power.PowerState
import collins.power.RebootHard
import collins.power.RebootSoft
import collins.power.Verify
import collins.power.management.{ PowerManagement => PM }
import collins.power.management.PowerManagementConfig

import akka.util.Duration
import akka.util.duration._
import models.Asset
import models.IpmiInfo
import play.api.Application
import play.api.Plugin
import util.IpmiCommand

case class IpmiPowerCommand(
  override val ipmiCommand: String,
  override val ipmiInfo: IpmiInfo,
  override val interval: Duration = 60.seconds,
  val verify: Boolean = false,
  val userTimeout: Option[Duration] = None)
  extends IpmiCommand {
  override def defaultTimeout = Duration(PowerManagementConfig.timeoutMs, TimeUnit.MILLISECONDS)
  override val timeout = userTimeout.getOrElse(defaultTimeout)
}

object IpmiPowerCommand {
  val PMC = PowerManagementConfig
  def commandFor(k: PowerAction): String = k match {
    case PowerOff => PMC.powerOffCommand
    case PowerOn => PMC.powerOnCommand
    case PowerSoft => PMC.powerSoftCommand
    case PowerState => PMC.powerStateCommand
    case RebootSoft => PMC.rebootSoftCommand
    case RebootHard => PMC.rebootHardCommand
    case Verify => PMC.verifyCommand
    case Identify => PMC.identifyCommand
  }

  private def ipmiErr(a: Asset) =
    throw new IllegalStateException("Could not find IPMI info for asset %s".format(a.tag))

  def fromPowerAction(asset: Asset, action: PowerAction) = IpmiInfo.findByAsset(asset) match {
    case None => ipmiErr(asset)
    case Some(ipmi) =>
      val cmd = commandFor(action)
      new IpmiPowerCommand(cmd, ipmi)
  }
}

class IpmiPowerManagement(app: Application) extends Plugin with PM {
  protected[this] val executor = Executors.newCachedThreadPool()
  protected[this] val pool = FuturePool(executor)

  override def enabled: Boolean = {
    PowerManagementConfig.pluginInitialize(app.configuration)
    val isEnabled = PowerManagementConfig.enabled
    val isMe = PowerManagementConfig.getClassOption.getOrElse("").contains("IpmiPowerManagement")
    isEnabled && isMe
  }

  override def onStart() {
  }

  override def onStop() {
    try executor.shutdown() catch {
      case _ => // swallow this
    }
  }

  def powerOff(e: Asset): PowerStatus = run(e, PowerOff)
  def powerOn(e: Asset): PowerStatus = run(e, PowerOn)
  def powerSoft(e: Asset): PowerStatus = run(e, PowerSoft)
  def powerState(e: Asset): PowerStatus = run(e, PowerState)
  def rebootHard(e: Asset): PowerStatus = run(e, RebootHard)
  def rebootSoft(e: Asset): PowerStatus = run(e, RebootSoft)
  def identify(e: Asset): PowerStatus = run(e, Identify)
  def verify(e: Asset): PowerStatus = run(e, Verify)

  protected[this] def run(e: Asset, action: PowerAction): PowerStatus = pool {
    IpmiPowerCommand.fromPowerAction(getAsset(e), action).run() match {
      case None => Failure("powermanagement not enabled or available in environment")
      case Some(status) => status.isSuccess match {
        case true => Success(status.stdout)
        case false => Failure(status.stderr.getOrElse("Error running command for %s".format(action)))
      }
    }
  }
  protected[this] def getAsset(e: Asset): Asset = Asset.findByTag(e.tag) match {
    case Some(a) => a
    case None => throw new IllegalArgumentException(
      "Could not find asset with tag %s".format(e.tag))
  }
}