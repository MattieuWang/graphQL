package graphql

import modules.Exp
import sangria.execution.deferred.{Fetcher, HasId, Relation, RelationIds}
import sangria.schema.{Argument, Context, Field, ListType, ObjectType, StringType, fields}

object ExpSchema {
  val expFetcher = Fetcher.caching(
    (ctx: ProjectCtx, ids: Seq[String]) =>
      ctx.exp_dao.findByIds(ids)
  )(HasId(_.id))

  val expByUserIdRel = Relation[Exp, String]("byUserId", i => Seq(i.user_id))

  val expUserFetcher = Fetcher.rel(
    (ctx: ProjectCtx, user_ids: Seq[String]) => ctx.exp_dao.findExpByUserIds(user_ids),
    (ctx: ProjectCtx, ids: RelationIds[Exp]) => ctx.exp_dao .findExpByUserIds(ids(expByUserIdRel))
  )(HasId(_.user_id))

  val expType = ObjectType(
    "exp",
    "expériences de user",
    fields[ProjectCtx, Exp](
      Field("id", StringType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("location", StringType, resolve = _.value.location),
      Field("start_at", StringType, resolve = _.value.start_at),
      Field("end_at", StringType, resolve = _.value.end_at.getOrElse("")),
      Field("user_id", StringType, resolve = _.value.user_id)
    )
  )

  val ID = Argument("id", StringType, description = "id de l'exp" )

  val USER_ID = Argument("user_id", StringType, description = "id de l'user")

  val query = List(
    Field("exp_id", expType,
      arguments = ID :: Nil,
      resolve = (ctx: Context[ProjectCtx, Unit]) => ctx.ctx.exp_dao.findById(ctx.arg(ID))),
    Field("exp_by_user_id", ListType(expType),
      arguments = USER_ID :: Nil,
      resolve = (ctx: Context[ProjectCtx, Unit]) => ctx.ctx.exp_dao.findByUserId(ctx.arg(USER_ID)))
  )

}
