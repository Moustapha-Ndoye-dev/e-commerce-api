package simulations;

import static io.gatling.javaapi.core.CoreDsl.StringBody;
import static io.gatling.javaapi.core.CoreDsl.constantUsersPerSec;
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
import java.util.concurrent.ThreadLocalRandom;

public class EcommerceSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8081");

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(BASE_URL)
            .acceptHeader("application/json")
            .contentTypeHeader("application/json")
            .userAgentHeader("Gatling-ProductionLoadTest/1.0");

    private final ScenarioBuilder browseProducts = scenario("Browse products")
            .exec(session -> session.set("productId", ThreadLocalRandom.current().nextInt(1, 6)))
            .exec(http("List products").get("/api/products").check(status().is(200)))
            .pause(Duration.ofMillis(200), Duration.ofMillis(800))
            .exec(http("Get product by id")
                    .get(session -> "/api/products/" + session.getInt("productId"))
                    .check(status().in(200, 404)));

    private final ScenarioBuilder orderFlow = scenario("Create order flow")
            .exec(session -> {
                int productId = ThreadLocalRandom.current().nextInt(1, 6);
                int userSuffix = ThreadLocalRandom.current().nextInt(1, 100_000);
                return session
                        .set("productId", productId)
                        .set("userSuffix", userSuffix);
            })
            .exec(http("List products for order").get("/api/products").check(status().is(200)))
            .pause(Duration.ofMillis(100), Duration.ofMillis(400))
            .exec(http("Create order")
                    .post("/api/orders")
                    .body(StringBody(session -> """
                            {
                              "customerEmail": "perf-user-%d@example.com",
                              "items": [{"productId": %d, "quantity": 1}]
                            }
                            """.formatted(session.getInt("userSuffix"), session.getInt("productId"))))
                    .check(status().in(201, 400))
                    .check(jsonPath("$.id").optional().saveAs("orderId")))
            .pause(Duration.ofMillis(50), Duration.ofMillis(200))
            .exec(http("Get order")
                    .get(session -> {
                        String orderId = session.getString("orderId");
                        return orderId != null ? "/api/orders/" + orderId : "/api/orders/0";
                    })
                    .check(status().in(200, 404)));

    private final ScenarioBuilder sustainedTraffic = scenario("Sustained peak traffic")
            .exec(http("Health check products").get("/api/products").check(status().is(200)))
            .pause(Duration.ofMillis(100), Duration.ofMillis(300));

    {
        setUp(
                // Montée en charge progressive — pic navigation (500 users / 60 s)
                browseProducts.injectOpen(rampUsers(500).during(Duration.ofSeconds(60))),
                // Flux commande — charge élevée (200 users / 45 s)
                orderFlow.injectOpen(rampUsers(200).during(Duration.ofSeconds(45))),
                // Trafic soutenu post-pic (15 req/s pendant 30 s)
                sustainedTraffic.injectOpen(constantUsersPerSec(15.0).during(Duration.ofSeconds(30)))
        ).protocols(httpProtocol)
         .maxDuration(Duration.ofMinutes(3))
         .assertions(
                 global().successfulRequests().percent().gt(90.0),
                 global().responseTime().percentile3().lt(5000)
         );
    }
}
