package graphql

import modules.Ent
import sangria.execution.deferred.{Fetcher, HasId, Relation, RelationIds}
import sangria.schema._

object EntSchema {

  val entFetcher = Fetcher.caching(
    (ctx: ProjectCtx, ids: Seq[String]) =>
      ctx.ent_dao.findByIds(ids)
  )(HasId(_.id))

  val enterpriseIdRel = Relation[Ent, String]("byUser", i => Seq(i.id))

  val enterpriseUserFetcher = Fetcher.rel(
    (ctx: ProjectCtx, ids: Seq[String]) => ctx.ent_dao.findByIds(ids),
    (ctx: ProjectCtx, ids: RelationIds[Ent]) => ctx.ent_dao.findByIds(ids(enterpriseIdRel))
  )(HasId(_.id))


  val entType: ObjectType[ProjectCtx, Ent] = ObjectType(
    "ent",
    "type of enterprise",
    fields[ProjectCtx, Ent](
      Field("id", StringType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("location", StringType, resolve = _.value.location)
    )
  )

  val query = List(
    Field("ent", entType,
      arguments = List(Argument("id", StringType, "id of ent")),
      resolve = (ctx: Context[ProjectCtx, Unit]) => ctx.ctx.ent_dao.findById(ctx.arg("id")))
  )

}
