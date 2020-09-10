package graphql

import modules.{EntDao, ExpDao, UserDao}
import sangria.execution.deferred.DeferredResolver
import sangria.schema._

case class ProjectCtx(
 user_dao: UserDao,
 exp_dao: ExpDao,
 ent_dao: EntDao
)

object ProjectSchema {
  lazy val mainQuery = ObjectType(
    "fields",
    fields[ProjectCtx, Unit](
      (UserSchema.query ++
    EntSchema.query ++
    ExpSchema.query ): _*)
  )

  def schema() = {
    Schema(mainQuery)
  }

  def fetchers() = {
    val fetchers = List(UserSchema.userFetcher,
      ExpSchema.expFetcher,
      ExpSchema.expUserFetcher,
      EntSchema.entFetcher,
      EntSchema.enterpriseUserFetcher)
    DeferredResolver.fetchers(fetchers: _*)
  }
}


