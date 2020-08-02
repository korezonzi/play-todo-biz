package controllers

import play.api.mvc._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import javax.inject.Inject
import scalikejdbc._
import models._
//コンパニオンオブジェクトに定義したReads, Writesを参照するため
import JsonController._

//オブジェクトとJSONの変換を行うためにWrites, Reads でマッピングする必要がある
object JsonController {
  //USERSテーブルのUsersクラスをjsonに変換するためのWritesを定義
  //Play2のDSLを利用するver
  implicit val usersWrites = (
    (__ \ "id"       ).write[Long] and
    (__ \ "name"     ).write[String] and
    (__ \ "companyId").writeNullable[Int]
  )(unlift(Users.unapply))

//  //利用しない場合
//  implicit val usersWritesFormat = new Writes[Users] {
//    def writes(user: Users): JsValue = {
//      Json.obj(
//        "id" -> user.id,
//        "name"      -> user.name,
//        "companyId" -> user.companyId
//      )
//    }
//  }
}

class JsonController @Inject()(components: ControllerComponents) extends AbstractController(components) {
  private val u = Users.syntax("u")
  def list = Action { implicit request =>
    DB.readOnly { implicit session =>
      //ユーザのリストを取得
      val users = withSQL {
        select.from(Users as u).orderBy(u.id.asc)
      }.map(Users(u.resultName)).list.apply

      //ユーザ一覧のJSONで返す
      Ok(Json.obj("users" -> users))
    }
  }

  def create = TODO

  def update = TODO

  def remove(id: Long) = TODO
}
