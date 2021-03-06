package finalTask

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{ActorSystem, Behavior, PostStop}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, Route}
import akka.stream.ActorMaterializer
import finalTask.dao._
import finalTask.rest.{CourseRoutes, StudentRoutes, TeacherRoutes}
import finalTask.service.{CourseService, LogService, StudentService, TeacherService}

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}


object Server {

  sealed trait Message

  private final case class StartFailed(cause: Throwable) extends Message

  private final case class Started(binding: ServerBinding) extends Message

  case object Stop extends Message

  def apply(host: String, port: Int): Behavior[Message] = Behaviors.setup { ctx =>

    implicit val system = ctx.system
    implicit val untypedSystem: akka.actor.ActorSystem = ctx.system.toClassic
    implicit val materializer: ActorMaterializer = ActorMaterializer()(ctx.system.toClassic)
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

    val dbComponent = H2DBComponent()
    val tables = new Tables(dbComponent)
    val studentRepository = StudentRepository(dbComponent, tables)
    val teacherRepository = TeacherRepository(dbComponent, tables)
    val courseRepository = CourseRepository(dbComponent, tables)
    val studentServiceActor = ctx.spawn(StudentService(studentRepository), "StudentService")
    val teacherServiceActor = ctx.spawn(TeacherService(teacherRepository), "TeacherService")
    val courseServiceActor = ctx.spawn(CourseService(courseRepository), "CourseService")
    val studentRoutes = new StudentRoutes(studentServiceActor)
    val teacherRoutes = new TeacherRoutes(teacherServiceActor)
    val courseRoutes = new CourseRoutes(courseServiceActor)
    val logService = LogService()

    val restExceptionHandler = ExceptionHandler {
      case e: Exception => complete((StatusCodes.BadRequest, f"Error while request handling: ${e.getMessage}"))
    }

    val routes: Route = handleExceptions(restExceptionHandler) {
      extractRequest { req =>
        entity(as[String]) { payload =>
          logService.info(s"${req.method.name}: ${req.uri.path} payload: $payload")
          concat(studentRoutes.theStudentRoutes, teacherRoutes.theTeacherRoutes, courseRoutes.theCourseRoutes)
        }
      }
    }

    val serverBinding: Future[Http.ServerBinding] = Http.apply().bindAndHandle(routes, host, port)
    ctx.pipeToSelf(serverBinding) {
      case Success(binding) => Started(binding)
      case Failure(ex) => StartFailed(ex)
    }

    def running(binding: ServerBinding): Behavior[Message] =
      Behaviors.receiveMessagePartial[Message] {
        case Stop =>
          ctx.log.info(
            "Stopping server http://{}:{}/",
            binding.localAddress.getHostString,
            binding.localAddress.getPort)
          Behaviors.stopped
      }.receiveSignal {
        case (_, PostStop) =>
          binding.unbind()
          Behaviors.same
      }

    def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
      Behaviors.receiveMessage[Message] {
        case StartFailed(cause) =>
          throw new RuntimeException("Server failed to start", cause)
        case Started(binding) =>
          ctx.log.info(
            "Server online at http://{}:{}/",
            binding.localAddress.getHostString,
            binding.localAddress.getPort)
          if (wasStopped) ctx.self ! Stop
          running(binding)
        case Stop =>
          // we got a stop message but haven't completed starting yet,
          // we cannot stop until starting has completed
          starting(wasStopped = true)
      }

    starting(wasStopped = false)
  }


  def main(args: Array[String]) {
    val system: ActorSystem[Server.Message] =
      ActorSystem(Server("localhost", 8080), "BuildJobsServer")
  }
}
