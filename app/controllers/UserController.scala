package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import javax.inject.Inject
import scalikejdbc._
import models._
import UserController._

//コンパニオンオブジェクト: 同じ名前のクラスと同ファイルに定義されたもの
//対応するクラスやトレイトは互いにprivateなメンバーにアクセス出来る
object UserController {
  //フォームの値を格納するケースクラス
  case class UserForm(
    id:        Option[Long],
    name:      String,
    companyId: Option[Int]
  )
  ///formから送信されたデータ <-> ケースクラスへの変換を行う
  val userForm = Form(
    mapping(
      "id"        -> optional(longNumber),
      "name"      -> nonEmptyText(maxLength = 20),
      "companyId" -> optional(number)
    )(UserForm.apply)(UserForm.unapply)
  )
}

//以下、2つをGoogle Guice のDI機能を使用するために、@Injectされてる
//@Inject: DIのためのアノテーション

//MessagesControllerComponents: Playの国際化機能を使用するため
//MessagesAbstractController:   コントローラ内でdbアクセスや国際化機能を利用するため
class UserController @Inject()(components: MessagesControllerComponents) extends MessagesAbstractController(components) {
  private val u = Users.syntax("u")
  private val c = Companies.syntax("c")

  //TODOメソッドは、501レスポンスを返す
  //一覧表示
  def list = Action { implicit request =>
    DB.readOnly { implicit session =>
      //ユーザのリストを取得
      val users = withSQL {
        select.from(Users as u).orderBy(u.id.asc)
      }.map(Users(u.resultName)).list.apply()
      //一覧表示を表示する
      Ok(views.html.user.list(users))
    }
  }

  //編集画面表示
  def edit(id: Option[Long]) = Action { implicit request =>
    DB.readOnly { implicit session =>
      //リクエストパラメータにIDが存在する場合
      val form = id match {
          //IDない時は新規登録フォーム
        //亜kらのFormを渡してる
        case None     => userForm
        case Some(id) => Users.find(id) match {
          case Some(user) => userForm.fill(UserForm(Some(user.id), user.name, user.companyId))
          case None       => userForm
        }
      }
      //プルダウンに表示する会社のリストを取得
      val companies = withSQL {
        select.from(Companies as c).orderBy(c.id.asc)
      }.map(Companies(c.resultName)).list().apply()

      Ok(views.html.user.edit(form, companies))
    }
  }

  //登録実行
  def create = Action { implicit request =>
    //トランザクションかんりされたセッションを取得できる
    //中の処理が正常: コミット, 例外発生時: ロールバックされる
    DB.localTx { implicit session =>
      //リクエストの内容をバインド
      userForm.bindFromRequest.fold(
        //エラー時
        error => {
          BadRequest(views.html.user.edit(error, Companies.findAll()))
        },
        //OK
        form => {
          Users.create(form.name, form.companyId)
          //一覧画面へリダイレクト
          Redirect(routes.UserController.list)
        }
      )
    }
  }

  //更新作業
  def update = Action { implicit request =>
    DB.localTx { implicit session =>
      //リクエストの内容をバインド
    userForm.bindFromRequest.fold(
      error => {
        BadRequest(views.html.user.edit(error, Companies.findAll()))
      },
      //OK; 更新を行い,一覧画面へリダイレクト
      form => {
        Users.find(form.id.get).foreach{ user =>
          Users.save(user.copy(name = form.name, companyId = form.companyId))
        }
        //一覧画面をリダイレクト
        Redirect(routes.UserController.list)
      }
    )}
  }

  //削除実行
  def remove(id: Long) = TODO
}
