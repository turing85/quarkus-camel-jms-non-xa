package de.turing85.quarkus.camel.jms.non.xa;

import java.util.Objects;

import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;

import io.quarkiverse.messaginghub.pooled.jms.PooledJmsRuntimeConfig;
import io.quarkiverse.messaginghub.pooled.jms.PooledJmsWrapper;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.smallrye.common.annotation.Identifier;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;

public class NonXaConnectionFactoryProducer {
  public static final String IDENTIFIER = ArtemisUtil.DEFAULT_CONFIG_NAME + "-non-xa";

  @Produces
  @Identifier(IDENTIFIER)
  ConnectionFactory nonXaConnectionFactory(final ArtemisRuntimeConfigs runtimeConfigs,
      final PooledJmsRuntimeConfig poolConfig) {
    return constructPooledNonXaConnectionFactory(
        Objects.requireNonNull(runtimeConfigs.configs().get(ArtemisUtil.DEFAULT_CONFIG_NAME)),
        poolConfig);
  }

  private static JmsPoolConnectionFactory constructPooledNonXaConnectionFactory(
      ArtemisRuntimeConfig config, PooledJmsRuntimeConfig poolConfig) {
    final JmsPoolConnectionFactory poolConnectionFactory = new JmsPoolConnectionFactory();
    PooledJmsWrapper.pooledJmsRuntimeConfigureConnectionFactory(poolConnectionFactory,
        new ActiveMQConnectionFactory(config.getUrl(), config.getUsername(), config.getPassword()),
        poolConfig);
    return poolConnectionFactory;
  }

}
