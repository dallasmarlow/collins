package collins.provisioning

import com.twitter.util.Future

import collins.shell.CommandResult
import models.Asset
import play.api.Logger

trait Provisioner {
  protected[this] val logger = Logger(getClass)
  def profiles: Set[ProvisionerProfile]
  def canProvision(asset: Asset): Boolean
  def provision(request: ProvisionerRequest): Future[CommandResult]
  def test(request: ProvisionerRequest): Future[CommandResult]
  def profile(id: String): Option[ProvisionerProfile] = {
    profiles.find(_.identifier == id)
  }
  def makeRequest(token: String, id: String, notification: Option[String] = None, suffix: Option[String] = None): Option[ProvisionerRequest] = {
    profile(id).map { p =>
      ProvisionerRequest(token, p, notification, suffix)
    }
  }
}
