package util.plugins

import collins.validation.File

import util.config.ConfigValue
import util.config.Configurable

object MetricsReporterConfig extends Configurable {

  override val namespace = "metricsreporter"
  override val referenceConfigFilename = "metricsreporter_reference.conf"

  def enabled = getBoolean("enabled", false)
  def configFile = getString("configFile")(ConfigValue.Required).get

  override def validateConfig() = enabled match {
    case true => File.requireFileIsReadable(configFile)
    case _ => false
  }

}
