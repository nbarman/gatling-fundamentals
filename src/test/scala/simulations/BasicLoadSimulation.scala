package simulations
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration.DurationInt
import scala.language.postfixOps

class BasicLoadSimulation extends Simulation {

  //HTTP Config
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")

  //Scenario Definition
  //Method definition
  //Getting the property from the command line
  private def getProperty(propertyName: String, defaultVal : String): String = {
    Option(System.getenv(propertyName))
      .orElse(Option(System.getProperty(propertyName)))
      .getOrElse(defaultVal)
  }

  def userCount: Int = getProperty("USERS", "5").toInt

  def getAllVideoGames(): ChainBuilder ={
    exec(
      http("Get All video games")
        .get("videogames")
        .check(status.is(200))
    )
  }

  def getSpecificGame(): ChainBuilder ={
    repeat(2){ //Looping two times
        exec(
          http("get specific game")
            .get("videogames/2")
            .check(status.not(404))
            .check(status.in(200 to 210)) //Status Range
        )
    }
  }

  //Feeder

  val csvFeeder = csv("data/gameCsvFile.csv").circular

  def getSpecificVideoGameFromFeeder() = {
    repeat(5) {
      feed(csvFeeder)
        .exec(http("Get specific video game")
          .get("videogames/${gameID}")
          .check(jsonPath("$.name").is("${gameName}"))
          .check(status.is(200)))
        .pause(1.second)
    }
  }

  //PAUSE Scenario and Scenario 1
  val scn = scenario("Basic Load simulation")

    //***************** Begin OF SCENARIO****************

    .exec(getAllVideoGames()) //Code Reuse
    .pause(5.seconds)

    .exec(getSpecificGame()) //Loop HTTP Calls
    .pause(5.seconds)

    //SaveAs Param
    .exec(http("Get particular gameID").
      get("videogames")
      .check(jsonPath("$[1].id").//JSONPATH Definition
        saveAs("gameId"))) //SaveAs Param for later use

    .exec(http("Get the game from stored param")
      .get("videogames/${gameId}") //Using from the SaveAs
      .check(jsonPath("$.name")
        .is("Gran Turismo 3"))
      .check(bodyString.saveAs("response")))
    .pause(5.seconds)

    //Feeder
    .exec(getSpecificVideoGameFromFeeder())

    //*****************END OF SCENARIO****************
    .exec{session => println(session);session} // Print session vars for debugging



  //Load Scenarios
  setUp(          scn.inject(
                  nothingFor(5 seconds),
                  atOnceUsers(userCount)
                ).protocols(httpConf.inferHtmlResources()),
      ).maxDuration(4 minute) //Specifying a fixed duration of time

}
