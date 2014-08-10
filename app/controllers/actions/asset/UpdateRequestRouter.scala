package controllers
package actions
package asset

import controllers.actions.SecureAction
import models.AssetMeta.Enum.ChassisTag
import models.AssetMeta.Enum.RackPosition
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Request

/**
 * Determines if a request is for modifying status or not
 */
object UpdateRequestRouter {

  sealed trait Matcher
  object Matcher {
    object StatusOnly extends Matcher
    object SomethingSomethingDarkSide extends Matcher
  }

  def getMatchType(req: Request[AnyContent]): Matcher = {
    val map = ActionAttributeHelper.getInputMapFromRequest(req)
    val nonStatus = Set("lshw", "lldp", ChassisTag.toString, RackPosition.toString, "groupId", "attribute")
    val foundNonStatus = map.filter(kv => nonStatus.contains(kv._1)).size > 0
    if (foundNonStatus) {
      Matcher.SomethingSomethingDarkSide
    } else {
      Matcher.StatusOnly
    }
  }

  def apply(matcher: PartialFunction[Matcher,SecureAction]): Action[AnyContent] = Action { implicit req =>
    matcher(getMatchType(req))(req)
  }
}
