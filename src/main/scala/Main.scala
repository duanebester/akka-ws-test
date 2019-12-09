import akka.actor._
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{ Flow, Sink, Source }
import akka.http.scaladsl.model.ws.{ Message, TextMessage }
import scala.language.postfixOps
import akka.stream.OverflowStrategy
import akka.http.scaladsl.model.StatusCodes
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import akka.http.javadsl.common.JsonEntityStreamingSupport
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import akka.NotUsed

case class StatusMessage(uuid: String, message: Message)
case class ImageProcessed(status: String)

object Main extends App with SprayJsonSupport with DefaultJsonProtocol {
  implicit val system: ActorSystem = ActorSystem("akka-ws-test")
  implicit val executor: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val jsonStreamingSupport: JsonEntityStreamingSupport = EntityStreamingSupport.json()
  implicit val format = jsonFormat1(ImageProcessed.apply)

  val (wsActor, wsSource) = Source.actorRef[StatusMessage](32, OverflowStrategy.dropNew).preMaterialize()

  def wsStatusFlow(uuid: String): Flow[Message, Message, Any] =
    Flow.fromSinkAndSource(
      Sink.ignore,
      wsSource
        .filter(statusMessage => statusMessage.uuid == uuid)
        .map(statusMessage => {
          println(statusMessage)
          statusMessage.message
        })
    )

  def processStage(stageNum: Int, uuid: String) = Flow[BufferedImage].async
    .delay(1 seconds)
    .map(bi => {
      val info = s"Processing Stage: ${stageNum}"
      println(info)
      wsActor ! StatusMessage(uuid, TextMessage(info))
      bi
    })

  def processImageFlow(uuid: String): Flow[BufferedImage, ImageProcessed, NotUsed] =
    processStage(1, uuid)
      .via(processStage(2, uuid))
      .via(processStage(3, uuid))
      .via(processStage(4, uuid))
      .map(_ => {
        wsActor ! StatusMessage(uuid, TextMessage("Finished"))
        ImageProcessed("Complete!")
      })

  def processImage(bi: BufferedImage, uuid: String) = Source.single(bi).via(processImageFlow(uuid)).runWith(Sink.ignore)

  val uploadRoute =
    pathPrefix("image") {
      path(Segment / "status") { uuid: String =>
        get {
          handleWebSocketMessages(wsStatusFlow(uuid))
        }
      } ~ path(Segment / "upload") { uuid: String =>
        post {
          uploadedFile("fileUpload") {
            case (_, file) =>
              val image = ImageIO.read(file)
              processImage(image, uuid)
              complete(StatusCodes.OK)
          }
        }
      }
    }

  val staticRoute =
    get {
      (pathEndOrSingleSlash & redirectToTrailingSlashIfMissing(StatusCodes.TemporaryRedirect)) {
        getFromResource("public/index.html")
      } ~ {
        getFromResourceDirectory("public")
      }
    }

  Http().bindAndHandle(uploadRoute ~ staticRoute, "localhost", 8080).map { _ =>
    println(s"Server is running at http://localhost:8080/")
  }
}
