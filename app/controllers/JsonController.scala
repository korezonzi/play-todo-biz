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
    (__ \ "id").write[Long] and
      (__ \ "name").write[String] and
      (__ \ "companyId").writeNullable[Int]
    ) (unlift(Users.unapply))

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

  //ユーザ情報を受け取るためのケースクラス
  case class UserForm(
    id: Option[Long],
    name: String,
    companyId: Option[Int]
  )

  //JSONをUserFormに変換するためのReads
  //明示的にマッピングする方法
  implicit val userFormReads = (
    (__ \ "id").readNullable[Long] and
      (__ \ "name").read[String] and
      (__ \ "companyId").readNullable[Int]
    ) (UserForm)

  //マクロを使ってシンプルに書くやり方
  /*implicit val userFormReads  = Json.reads[UserForm]
  implicit val userFormWrites = Json.writes[UserForm]*/

  //reads,writes両方が必要な時にシンプル
  /*implicit val userFormFormat = Json.format[UserForm]*/
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

  //JSONリクエストを受け取る際: Action(parse.json)を指定
  def create = Action(parse.json) { implicit request =>
    request.body.validate[UserForm].map { form =>
      //OK: ユーザを登録
      DB.localTx { implicit session =>
        Users.create(form.name, form.companyId)
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { e =>
      //NG: バリデーションエラー
      BadRequest(Json.obj("resutl" -> "failure", "error" -> JsError.toJson(e)))
    }
  }

  def update = Action(parse.json) { implicit request =>
    request.body.validate[UserForm].map { form =>
      //Ok: ユーザ情報を更新
      DB.localTx { implicit session =>
        Users.find(form.id.get).foreach { user =>
          Users.save(user.copy(name = form.name, companyId = form.companyId))
        }
        Ok(Json.obj("result" -> "success"))
      }
    }.recoverTotal { e =>
      //NG: バリデーションエラー
      BadRequest(Json.obj("result" -> "failure", "error" -> JsError.toJson(e)))
    }
  }

  def remove(id: Long) = Action {implicit request =>
    DB.localTx { implicit session =>
      //ユーザ削除
      Users.find(id).foreach{ user =>
        Users.destroy(user)
      }
      Ok(Json.obj("result" -> "success"))
    }
  }
}
