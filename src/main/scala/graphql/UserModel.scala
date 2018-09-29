package graphql

import sangria.schema.{StringType, _}


object UserModel {

  val RoleEnum = EnumType(
    "Role",
    Some("Auth Roles"),
    List(
      EnumValue("ADMIN",
        value = "ADMIN",
        description = Some("Admin.")),
      EnumValue("VIEW_PERMISSIONS",
        value = "VIEW_PERMISSIONS",
        description = Some("View user's permissions.")),
      EnumValue("ROLE1",
        value = "ROLE1",
        description = Some("Custom Role 1 (Example 1).")),
      EnumValue("ROLE2",
        value = "ROLE2",
        description = Some("Custom Role 2 (Example 2)."))
    )
  )

  case class QlUser(userName: String, permissions: List[String])

  val UserNameArg = Argument ( "userName", StringType )
  val PasswordArg = Argument ( "password", StringType )
  val PermissionsArg = Argument ( "permissions", ListInputType(RoleEnum) )

  
  val UserType = ObjectType ( "User", fields [SecureContext, QlUser](
    Field ( "userName", StringType, resolve = _.value.userName ),
    Field ( "permissions", OptionType ( ListType ( RoleEnum ) ),
      resolve = ctx ⇒ ctx.ctx.authorised ( "VIEW_PERMISSIONS" ) { _ ⇒
        ctx.value.permissions
      } )
  ) )
  


}
