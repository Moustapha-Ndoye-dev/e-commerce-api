package simulations;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.atOnceUsers;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.jsonPath;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;

public class EcommerceSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json");

    private final ScenarioBuilder browseProducts = scenario("Browse products")
            .exec(http("List products").get("/api/products").check(status().is(200)))
            .pause(Duration.ofMillis(100))
            .exec(http("Get product by id").get("/api/products/1").check(status().in(200, 404)));

    private final ScenarioBuilder orderFlow = scenario("Create order flow")
            .exec(http("List products for order").get("/api/products").check(status().is(200)))
            .exec(http("Create order")
                    .post("/api/orders")
                    .body(StringBody("""
                            {
                              "customerEmail": "perf-user@example.com",
                              "items": [{"productId": 1, "quantity": 1}]
                            }
                            """))
                    .check(status().in(201, 400))
                    .check(jsonPath("$.id").optional().saveAs("orderId")))
            .pause(Duration.ofMillis(50))
            .exec(http("Get order")
                    .get(session -> "/api/orders/" + session.getString("orderId"))
                    .check(status().in(200, 404)));

    {
        setUp(
                browseProducts.injectOpen(rampUsers(20).during(Duration.ofSeconds(10))),
                orderFlow.injectOpen(atOnceUsers(10))
        ).protocols(httpProtocol)
         .assertions(
                 global().successfulRequests().percent().gt(90.0),
                 global().responseTime().percentile3().lt(2000)
         );
    }
}
