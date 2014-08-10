package collins.database

import models.Model
import play.api.Application
import play.api.Plugin

class DatabasePlugin(app: Application) extends Plugin {
  override def enabled = true

  override def onStart() {
    if (enabled) {
      Model.initialize()
    }
  }

  override def onStop() {
    if (enabled) {
      Model.shutdown()
    }
  }

  def closeConnection() {
    if (enabled) {
      Model.shutdown()
    }
  }
}
