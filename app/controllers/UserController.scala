package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import javax.inject.Inject
import scalikejdbc._
import models._

//以下、2つをGoogle Guice のDI機能を使用するために、@Injectされてる
//@Inject: DIのためのアノテーション

//MessagesControllerComponents: Playの国際化機能を使用するため
//MessagesAbstractController:   コントローラ内でdbアクセスや国際化機能を利用するため
class UserController @Inject()(components: MessagesControllerComponents) extends MessagesAbstractController(components) {
  //TODOメソッドは、501レスポンスを返す

  //一覧表示
  def list = TODO

  //編集画面表示
  def edit(id: Option[Long]) = TODO

  //登録実行
  def create = TODO

  //更新作業
  def update = TODO

  //削除実行
  def remove(id: Long) = TODO
}
