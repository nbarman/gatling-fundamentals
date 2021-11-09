import io.gatling.core.Predef._
import io.gatling.http.Predef._

class MyFirstTest extends Simulation {

  //1 HTTP Conf
  val httpConf = http.baseUrl("http://localhost:8080/app/")
    .header("Accept","application/json")
    .proxy(Proxy("localhost", 8888));

  // 2 Scenario Definition
  val scn= scenario("My First Test")
    .exec(http("Gell All Games").get("videogames"));
  //.check(jsonPath("$.name").is("Resident Evil")));

  // 3 Load Scenario
  setUp(
    scn.inject(atOnceUsers(1))
  ).protocols(httpConf);
}
