package collins.provisioning

import collins.validation.File
import models.AssetType
import models.Status
import util.concurrent.RateLimit
import util.config.ConfigValue
import util.config.Configurable

object ProvisionerConfig extends Configurable {
  override val namespace = "provisioner"
  override val referenceConfigFilename = "provisioner_reference.conf"

  def allowedStatus: Set[Int] = getStringSet("allowedStatus", Status.statusNames).map { s =>
    Status.findByName(s).get.id
  }
  def allowedType: Set[Int] = getStringSet("allowedType", AssetType.typeNames).map { name =>
    AssetType.findByName(name) match {
      case None => throw globalError("%s is not a valid asset type".format(name))
      case Some(a) => a.id
    }
  }.toSet
  def cacheTimeout = getMilliseconds("cacheTimeout").getOrElse(30000L)
  def checkCommand = getString("checkCommand").filter(_.nonEmpty)
  def command = getString("command").filter(_.nonEmpty)
  def enabled = getBoolean("enabled", false)
  def profilesFile = getString("profiles")(ConfigValue.Required).filter(_.nonEmpty).get
  def rate = getString("rate", "1/10 seconds")

  override def validateConfig() {
    if (enabled) {
      allowedStatus
      RateLimit.fromString(rate)
      tryOption("command", command.get)
      tryOption("profiles", profilesFile)
      File.requireFileIsReadable(profilesFile)
      require(
        ProfileLoader.fromFile(profilesFile).size > 0,
        "Must have at least one profile in %s".format(profilesFile)
      )
    }
  }

  protected def tryOption(name: String, fn: => AnyRef) {
    try fn catch {
      case e =>
        throw globalError("provisioner.%s must be specified".format(name))
    }
  }
}
