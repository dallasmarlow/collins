package models

import util.AuthenticationProvider
import org.specs2.mutable._
import java.io.File

object UserSpec extends Specification {

  "The User Model" should {
    "handle authentication" in {
      "with default authentication" in {
        val provider = AuthenticationProvider.Default
        User.authenticate("blake", "admin:first", Some(provider)) must beSome[User]
        User.authenticate("no", "suchuser", Some(provider)) must beNone
      }
    }
    "serialize/deserialize" in {
      val u = UserImpl("blake", "*", Seq("engineering"), 125, true)
      val uMap = u.toMap()
      val newU = User.fromMap(uMap).get
      newU.username mustEqual u.username
      newU.isAuthenticated mustEqual u.isAuthenticated
      newU.id mustEqual u.id
      newU.roles mustEqual u.roles
    }
  }

}
