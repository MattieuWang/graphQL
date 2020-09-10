package graphql

import modules.User
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.macros.derive.{AddFields, Interfaces, ReplaceField, deriveObjectType}
import sangria.schema._

object UserSchema {
  val userFetcher = Fetcher.caching(
    (ctx: ProjectCtx, ids: Seq[String]) =>
      ctx.user_dao.findByIds(ids)
  )(HasId(_.id))

  val basicUser: InterfaceType[ProjectCtx, User] = InterfaceType(
    "user",
    "basic user interface",
    fields = fields[ProjectCtx, User](
      Field("id", StringType, resolve = _.value.id),
      Field("name", StringType, resolve = _.value.name),
      Field("age", IntType, resolve = _.value.age),
      Field("sex", StringType, resolve = _.value.sex)
    )
  )

  val userType: ObjectType[ProjectCtx, User] =
    deriveObjectType(
      Interfaces(basicUser),
      AddFields(
        Field("exp", ListType(ExpSchema.expType),
          resolve = ctx => ExpSchema.expUserFetcher.deferRelSeq(ExpSchema.expByUserIdRel, ctx.value.id)),
      ),
//      AddFields(
//        Field(
//          "ent",
//          ListType(EntSchema.entType),
//          resolve = ctx => EntSchema.enterpriseUserFetcher.deferRelSeq(EntSchema.enterpriseIdRel, ctx.value.enterprise_id.getOrElse(""))
//        )
//      )
      ReplaceField(
        "enterprise_id",
        Field(
          "ent",
          OptionType(EntSchema.entType),
          resolve = ctx => EntSchema.entFetcher.deferOpt(ctx.value.enterprise_id.getOrElse(""))
        )
      )
    )

  val ID = Argument("id", StringType, "id of user")
  val IDS = Argument("ids", ListInputType(StringType), "id of users")

  val query = List(
    Field("user", userType,
      arguments = ID :: Nil,
      resolve = (ctx: Context[ProjectCtx, Unit]) => ctx.ctx.user_dao.findById(ctx.arg(ID))),
    Field("users", ListType(userType),
      arguments = IDS :: Nil,
      resolve = (ctx: Context[ProjectCtx, Unit]) => ctx.ctx.user_dao.findByIds(ctx.arg(IDS)))
  )
}
