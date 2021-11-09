package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt

class AddPauseTime extends Simulation{

  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")

  val scn = scenario("Video Game DB Calls")
    .exec(getAllGames()) //Code Reuse
    .pause(5)
    .exec(http("Get specific Game").get("videogames/1")).pause(1,20)
    .exec(http("Get all video games - 2nd call").get("videogames")).pause(3000.milliseconds)

  val scn1 = scenario("Test saveAs Param")

    .exec(http("Get particular gameID").
      get("videogames").
      check(jsonPath("$[1].id").
        saveAs("gameId")))

    .exec(http("Get the game from stored param")
      .get("videogames/${gameId}")
      .check(jsonPath("$.name")
        .is("Grand Turismo 3"))
      .check(bodyString.saveAs("response")))

    .exec{session => println(session("response").as[String]); session}

    .exec{session => println(session); session}

  //Code Reuse

  def getAllGames() = {
    repeat(3){                  // Repeat HTTP Calls
      exec(http("Get all video games  - 1st call")
        .get("videogames")
        .check(status.is(200)))
    }
  }

  //Getting the property from the command line
  private def getProperty(propertyName: String, defaultVal : String): String = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultVal)
  }

  def userCount: Int = getProperty("USERS", "5").toInt

  //How to pass from Cmd Line : -DUSERS=10 or pass through Jenkins params
  //Getting from command line


  setUp(
    scn.inject(atOnceUsers(userCount))//Getting from external config
  ).protocols(httpConf)
    .assertions( //Assertions to test script
      global.responseTime.max.lt(200)
    )
}
