package util
package concurrent

import java.util.concurrent.TimeUnit

import akka.util.Duration

trait BackgroundProcess[T] {
  val timeout: Duration
  def run(): T

  protected def defaultTimeout: Duration = Duration(ConcurrencyConfig.timeoutMs, TimeUnit.MILLISECONDS)
}
