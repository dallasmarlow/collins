package collins.callbacks

import java.beans.PropertyChangeSupport

import akka.actor.Actor
import play.api.Logger

case class CallbackMessageQueue(pcs: PropertyChangeSupport) extends Actor {
  private[this] val logger = Logger("CallbackMessageQueue")

  override def receive = {
    case CallbackMessage(name, oldValue, newValue) =>
      pcs.firePropertyChange(name, oldValue, newValue)
  }
}
