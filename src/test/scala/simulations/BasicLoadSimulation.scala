package simulations
import io.gatling.core.Predef._
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef._

import scala.concurrent.duration._

class BasicLoadSimulation extends Simulation {

  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept", "application/json")
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
        .check(status.is(200))
    )
  }

  //Scenario Defination

  val scn = scenario("Basic Load simulation")
    .exec(getAllVideoGames()) //Code Reuse
    .pause(5)
    .exec(getSpecificGame())
    .pause(5)
    .exec(getAllVideoGames())

  setUp(
    scn.inject(
      nothingFor(5 seconds),
      atOnceUsers(5),
      // rampUsers(10) during(10) // 10 users for 10 secs
      rampUsersPerSec(1) to(10) during(20 seconds)
    ).protocols(httpConf.inferHtmlResources())
  ).maxDuration(1 minute) //Specifying a fixed duration of time


}
