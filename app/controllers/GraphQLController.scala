package controllers

import akka.actor.ActorSystem
import graphql.{ProjectCtx, ProjectSchema}
import javax.inject.Inject
import modules.{EntDao, ExpDao, UserDao}
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.json.{JsObject, JsString, Json}
import play.api.mvc.{AbstractController, ControllerComponents, InjectedController, Request}
import sangria.marshalling.playJson._
import sangria.execution.{ErrorWithResolver, ExceptionHandler, Executor, HandledException, MaxQueryDepthReachedError, QueryAnalysisError, QueryReducer}
import sangria.parser.{QueryParser, SyntaxError}
import sangria.renderer.SchemaRenderer

import scala.concurrent.Future
import scala.util.{Failure, Success}

class GraphQLController @Inject()
  (system: ActorSystem,
  config: Configuration,
  dbConfig: DatabaseConfigProvider,
  cc: ControllerComponents) extends AbstractController(cc){

  import system.dispatcher

  val user_dao = new UserDao(dbConfig)
  val exp_dao = new ExpDao(dbConfig)
  val ent_dao = new EntDao(dbConfig)

  def graphql(query: String, variables: Option[String], operation: Option[String]) = Action.async { request =>
    executeQuery(query, variables map parseVariables, operation, isTracingEnabled(request))
  }

  def graphqlBody = Action.async(parse.json) { request =>
    val query = (request.body \ "query").as[String]
    val operation = (request.body \ "operationName").asOpt[String]

    val variables = (request.body \ "variables").toOption.flatMap {
      case JsString(vars) => Some(parseVariables(vars))
      case obj: JsObject => Some(obj)
      case _ => None
    }

    executeQuery(query, variables, operation, isTracingEnabled(request))
  }

  private def parseVariables(variables: String) =
    if (variables.trim == "" || variables.trim == "null") Json.obj() else Json.parse(variables).as[JsObject]

  private def executeQuery(query: String, variables: Option[JsObject], operation: Option[String], tracing: Boolean) =
    QueryParser.parse(query) match {

      // query parsed successfully, time to execute it!
      case Success(queryAst) =>
        Executor.execute(ProjectSchema.schema(), queryAst,
          ProjectCtx(user_dao, exp_dao, ent_dao),
          operationName = operation,
          variables = variables getOrElse Json.obj(),
          deferredResolver = ProjectSchema.fetchers(),
          exceptionHandler = exceptionHandler)    //import sangria.marshalling.playJson._
          .map(Status(200).apply(_))
          .recover {
            case error: QueryAnalysisError => BadRequest(error.resolveError)
            case error: ErrorWithResolver => InternalServerError(error.resolveError)
          }

      // can't parse GraphQL query, return error
      case Failure(error: SyntaxError) =>
        Future.successful(BadRequest(Json.obj(
          "syntaxError" -> error.getMessage,
          "locations" -> Json.arr(Json.obj(
            "line" -> error.originalError.position.line,
            "column" -> error.originalError.position.column)))))

      case Failure(error) =>
        throw error
    }

  def isTracingEnabled(request: Request[_]) = request.headers.get("X-Apollo-Tracing").isDefined

  def renderSchema = Action {
    Ok(SchemaRenderer.renderSchema(ProjectSchema.schema()))
  }

  lazy val exceptionHandler = ExceptionHandler {
    case (_, error @ TooComplexQueryError) => HandledException(error.getMessage)
    case (_, error @ MaxQueryDepthReachedError(_)) => HandledException(error.getMessage)
  }

  case object TooComplexQueryError extends Exception("Query is too expensive.")
}
