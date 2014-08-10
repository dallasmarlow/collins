package util.plugins

import com.addthis.metrics.reporter.config.ReporterConfig

import play.api.Application
import play.api.Logger
import play.api.Plugin

class MetricsReporterPlugin(app: Application) extends Plugin {

  override def enabled: Boolean = {
    MetricsReporterConfig.pluginInitialize(app.configuration)
    MetricsReporterConfig.enabled
  }

  override def onStart() {
    MetricsReporterConfig.validateConfig()
    try {
      Logger.info("Trying to load metrics-reporter-config...")
      ReporterConfig.loadFromFileAndValidate(MetricsReporterConfig.configFile).enableAll()
    } catch {
      case e: Exception => Logger.warn("Failed to load metrics-reporter-config", e)
    }
  }

  override def onStop() {
  }
}
