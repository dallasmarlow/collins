package controllers

import models.User
import models.UserImpl
import play.api.http.HeaderNames
import play.api.templates.Txt
import play.api.mvc.Action
import play.api.mvc.RequestHeader
import play.api.mvc.Results

trait ControllerSpec {
  def getApi(user: Option[User]) = new Api with SecureController {
    override def authenticate(request: RequestHeader) = user
    override def getUser(request: RequestHeader) = user.get
    override def onUnauthorized = Action { req =>
      Results.Unauthorized(Txt("Invalid username/password specified"))
    }
  }
  def getLoggedInUser(group: String) = Some(UserImpl("test", "*", Set(group), 123, true))
}
