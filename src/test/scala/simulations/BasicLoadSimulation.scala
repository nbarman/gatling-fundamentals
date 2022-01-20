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

  def getAllVideoGames(): ChainBuilder ={
    exec(
      http("Get All video games")
        .get("videogames")
        .check(status.is(200))
    )
  }

  def getSpecificGame(): ChainBuilder ={
    exec(

      http("get specific game")
        .get("videogames/2")
        .check(status.not(404))
        .check(status.in(200 to 210)) //Status Range
    )
  }

  //PAUSE Scenario and Scenario 1
  val scn1 = scenario("Basic Load simulation")
    .exec(getAllVideoGames()) //Code Reuse
    .pause(5000.milliseconds)
    .exec(getSpecificGame())

  //JSONPATH and Scenario 2
  val scn2 = scenario("Gatling Load Testing")

    .exec(http("Get particular gameID").
      get("videogames")
      .check(jsonPath("$[1].id").//JSONPATH Definition
        saveAs("gameId")))

    .exec(http("Get particular gameID").
      get("videogames"). //ENDPOINT
      check(jsonPath("$[1].id").
        saveAs("gameId")))

    .exec(http("Get the game from stored param")
      .get("videogames/${gameId}")
      .check(jsonPath("$.name")
        .is("Grand Turismo 3"))
      .check(bodyString.saveAs("response")))

    .exec{session => println(session("response").as[String]); session}

    .exec{session => println(session); session}


  //Load Scenarios
  setUp(
    scn1.inject(
      nothingFor(5 seconds),
      atOnceUsers(5),
      // rampUsers(10) during(10) // 10 users for 10 secs
      rampUsersPerSec(1) to(10) during(20 seconds)
    ).protocols(httpConf.inferHtmlResources())
  ).maxDuration(1 minute) //Specifying a fixed duration of time


}
