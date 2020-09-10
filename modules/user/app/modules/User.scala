package modules

import play.api.data.Form
import play.api.data.Forms._

import scala.concurrent.{ExecutionContext, Future}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

case class User(id: String, name: String, age: Int, sex: String, enterprise_id: Option[String] = None)

class UserDao(dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class UserTable(tag: Tag) extends Table[User](tag, "users") {

    val id = column[String]("id", O.PrimaryKey)
    val name = column[String]("name")
    val age = column[Int]("age")
    val sex = column[String]("sex")
    val enterprise_id = column[Option[String]]("enterprise_id")

    override def * = (id, name, age, sex, enterprise_id) <>
      ((User.apply _).tupled, User.unapply)
  }

  private val table = TableQuery[UserTable]

//  val userForm: Form[User] = Form{
//    mapping(
//      "id" -> nonEmptyText,
//      "name" -> nonEmptyText,
//      "age" -> number,
//      "sex" -> nonEmptyText,
//      "enterprise_id" -> optional(text)
//    )(User.apply)(User.unapply)
//  }

  def getAll(): Future[Seq[User]] = db.run(table.result)

  def findByIds(ids: Seq[String]):Future[Seq[User]] = {
    db.run(table.filter(_.id inSet ids).result)
  }

  def findById(id: String): Future[User] = {
    db.run(table.filter(_.id === id).result.head)
  }
}
