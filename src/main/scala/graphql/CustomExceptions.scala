package graphql


object CustomExceptions {

  class CustomException(private val message: String = "",
                        private val cause: Throwable = None.orNull) extends Exception(message, cause)

  case class AuthenticationException(message: String) extends Exception(message)

  case class AuthorisationException(message: String) extends Exception(message)

}

