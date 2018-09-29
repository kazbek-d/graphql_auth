package common

import scala.concurrent.{ExecutionContext, Future}

object Helpers {

  /**
    * Convert Option[Future[T]] to Future[Option[T]]
    */
  def OptFuture_Revert[A](x: Option[Future[A]])(implicit ec: ExecutionContext): Future[Option[A]] =
    x match {
      case Some ( f ) => f.map ( Some ( _ ) )
      case None => Future.successful ( None )
    }

}
