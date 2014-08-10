package collins.graphs

import util.config.ConfigAccessor
import util.config.ConfigSource
import util.config.ConfigValue
import util.config.TypesafeConfiguration

case class CustomMetricConfig(
  override val source: TypesafeConfiguration
) extends ConfigAccessor with ConfigSource {
  def selector = getString("selector")(ConfigValue.Required).get
  def metrics = getStringSet("metrics")
  def validateConfig() {
    selector
    metrics
  }
}
