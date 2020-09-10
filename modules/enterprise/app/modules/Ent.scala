package modules

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import scala.concurrent.Future

case class Ent(
  id: String,
  name: String,
  location: String
)

class EntDao(dbConfigProvider: DatabaseConfigProvider) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class EnterpriseTable(tag: Tag) extends Table[Ent](tag, "enterprises") {
    val id = column[String]("id", O.PrimaryKey)
    val name = column[String]("name")
    val location = column[String]("location")

    override def * = (id, name, location) <>
      ((Ent.apply _).tupled, Ent.unapply)
  }

  private val table = TableQuery[EnterpriseTable]

  def getAll(): Future[Seq[Ent]] = db.run(table.result)

  def findByIds(ids: Seq[String]):Future[Seq[Ent]] = {
    db.run(table.filter(_.id inSet ids).result)
  }

  def findById(id: String): Future[Ent] = {
    db.run(table.filter(_.id === id).result.head)
  }
}