package de.turing85.quarkus.camel.jms.non.xa;

import java.util.Objects;

import jakarta.enterprise.inject.Produces;
import jakarta.jms.ConnectionFactory;

import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfig;
import io.quarkus.artemis.core.runtime.ArtemisRuntimeConfigs;
import io.quarkus.artemis.core.runtime.ArtemisUtil;
import io.smallrye.common.annotation.Identifier;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;

public class NonXaConnectionFactoryProducer {
  public static final String IDENTIFIER = ArtemisUtil.DEFAULT_CONFIG_NAME + "-non-xa";

  @Produces
  @Identifier(IDENTIFIER)
  ConnectionFactory nonXaConnectionFactory(ArtemisRuntimeConfigs runtimeConfigs) {
    final ArtemisRuntimeConfig defaultConfig =
        Objects.requireNonNull(runtimeConfigs.configs().get(ArtemisUtil.DEFAULT_CONFIG_NAME));
    return new ActiveMQConnectionFactory(defaultConfig.getUrl(), defaultConfig.getUsername(),
        defaultConfig.getPassword());
  }
}
