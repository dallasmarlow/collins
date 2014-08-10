package controllers
package actions
package asset

import forms._
import collins.power.PowerAction

import util.security.SecuritySpecification

import play.api.data.Form
import play.api.data.Forms._

case class PowerManagementAction(
  assetTag: String,
  spec: SecuritySpecification,
  handler: SecureController
) extends PowerManagementActionHelper(assetTag, spec, handler) {

  override lazy val powerAction: Option[PowerAction] = Form(single(
    "action" -> of[PowerAction]
  )).bindFromRequest()(request).fold(
    err => None,
    suc => Some(suc)
  )
}
