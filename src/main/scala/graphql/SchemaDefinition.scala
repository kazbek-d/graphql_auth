package graphql

import sangria.schema._

object QueryDefinition {

  val queries = ObjectType(
    "Query",
    "Get User by token",
    fields[SecureContext, Unit](
      Field(
        name = "me",
        fieldType = OptionType ( UserModel.UserType ),
        resolve = ctx ⇒ ctx.ctx.authorised()(user ⇒ user))
    ))
}


object MutationDefinition {
  val mutations = ObjectType(
    "Mutation",
    "Authentication, Create/Update Users",
    fields[SecureContext, Unit](

      Field("login", OptionType(StringType),
        arguments = UserModel.UserNameArg :: UserModel.PasswordArg :: Nil,
        resolve = ctx ⇒ UpdateCtx( ctx.ctx.login(ctx.arg(UserModel.UserNameArg), ctx.arg(UserModel.PasswordArg)) ) { token ⇒
          ctx.ctx.copy(token = Some(token))
        }),

      Field("set", OptionType(StringType),
        arguments = UserModel.UserNameArg :: UserModel.PasswordArg :: UserModel.PermissionsArg  :: Nil,
        resolve = ctx ⇒ UpdateCtx {
          ctx.ctx.authorised ( "ADMIN" ) { _ =>
            ctx.ctx.set (
              ctx.arg ( UserModel.UserNameArg ),
              ctx.arg ( UserModel.PasswordArg ),
              ctx.arg ( UserModel.PermissionsArg.name ) )
          } flatten
        } { token ⇒
          ctx.ctx.copy(token = Some(token))
        })
      
    ))
}



object SchemaDefinition {

  val AuthSchema = Schema ( query = QueryDefinition.queries, mutation = Some ( MutationDefinition.mutations ) )

}