package de.turing85.quarkus.camel.jms.non.xa;

import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.jms.ConnectionFactory;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.smallrye.common.annotation.Identifier;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.jta.JtaTransactionManager;

import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.jms;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.quartz;

@ApplicationScoped
public class JmsEndpoint extends RouteBuilder {
  public static final String QUEUE_NON_TRANSACTED = "non-transacted";
  public static final String QUEUE_TRANSACTED = "transacted";
  public static final String CRON_NON_TRANSACTED_ROUTE_ID =
      "cron-%s".formatted(QUEUE_NON_TRANSACTED);
  public static final String CRON_TRANSACTED_ROUTE_ID = "cron-%s".formatted(QUEUE_TRANSACTED);

  private final ConnectionFactory connectionFactory;
  private final ConnectionFactory nonXaConnectionFactory;
  private final PlatformTransactionManager platformTransactionManager;
  private final AtomicInteger transactedCounter = new AtomicInteger();
  private final AtomicInteger nonTransactedCounter = new AtomicInteger();

  // @formatter:off
  public JmsEndpoint(
      @SuppressWarnings("CdiInjectionPointsInspection")
      @Identifier(ArtemisUtil.DEFAULT_CONFIG_NAME)
      ConnectionFactory connectionFactory,

      @Identifier(NonXaConnectionFactoryProducer.IDENTIFIER)
      ConnectionFactory nonXaConnectionFactory,

      UserTransaction userTransaction,
      @SuppressWarnings("CdiInjectionPointsInspection") TransactionManager transactionManager) {
    this.connectionFactory = connectionFactory;
    this.nonXaConnectionFactory = nonXaConnectionFactory;
    platformTransactionManager = new JtaTransactionManager(userTransaction, transactionManager);
  }
  // @formatter:on

  @Override
  public void configure() {
    // @formatter:off
    from(quartz(CRON_TRANSACTED_ROUTE_ID).cron("0/10 * * * * ?"))
        .routeId(CRON_TRANSACTED_ROUTE_ID)
        .setVariable("%s-counter".formatted(QUEUE_TRANSACTED), transactedCounter::getAndIncrement)
        .setBody().simple("%1$s-${variable.%1$s-counter}".formatted(QUEUE_TRANSACTED))
        .log("Sending %s message ${body}".formatted(QUEUE_TRANSACTED))
        .to(jms(QUEUE_TRANSACTED).connectionFactory(connectionFactory));

    from(quartz(CRON_NON_TRANSACTED_ROUTE_ID).cron("5/10 * * * * ?"))
        .routeId(CRON_NON_TRANSACTED_ROUTE_ID)
        .setVariable("%s-counter".formatted(
            QUEUE_NON_TRANSACTED),
            nonTransactedCounter::getAndIncrement)
        .setBody().simple("%1$s-${variable.%1$s-counter}".formatted(QUEUE_NON_TRANSACTED))
        .log("Sending %s message ${body}".formatted(QUEUE_NON_TRANSACTED))
        .to(jms(QUEUE_NON_TRANSACTED).connectionFactory(connectionFactory));

    from(
        jms(QUEUE_TRANSACTED)
            .connectionFactory(connectionFactory)
            .advanced()
                .transactionManager(platformTransactionManager))
        .routeId("jms-%s".formatted(QUEUE_TRANSACTED))
        .log("Received %s: ${body}".formatted(QUEUE_TRANSACTED));

    from(jms(QUEUE_NON_TRANSACTED).connectionFactory(nonXaConnectionFactory))
        .routeId("jms-%s".formatted(QUEUE_NON_TRANSACTED))
        .log("Received %s: ${body}".formatted(QUEUE_NON_TRANSACTED));
    // @formatter:on
  }
}
