package modules

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

case class Exp(
  id: String,
  name: String,
  location: String,
  start_at: String,
  end_at: Option[String] = None,
  user_id: String)


class ExpDao(dbConfigProvider: DatabaseConfigProvider) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class ExpTable(tag: Tag) extends Table[Exp](tag, "exps") {
    val id = column[String]("id", O.PrimaryKey)
    val name = column[String]("name")
    val location = column[String]("location")
    val start_at = column[String]("start_at")
    val end_at = column[Option[String]]("end_at")
    val user_id = column[String]("user_id")

    override def * = (id, name, location,
      start_at, end_at, user_id) <> ((Exp.apply _).tupled, Exp.unapply)
  }

  private val table = TableQuery[ExpTable]

  def findExpByUserIds(user_ids: Seq[String]): Future[Seq[Exp]] = {
    db.run(table.filter(_.user_id inSet user_ids).result)
  }

  def findByUserId(user_id: String): Future[Seq[Exp]] = {
    db.run(table.filter(_.user_id === user_id).result)
  }

  def findByIds(ids: Seq[String]): Future[Seq[Exp]] = {
    db.run(table.filter(_.id inSet ids).result)
  }

  def findById(id: String): Future[Exp] = {
    db.run(table.filter(_.id === id).result.head)
  }
}
