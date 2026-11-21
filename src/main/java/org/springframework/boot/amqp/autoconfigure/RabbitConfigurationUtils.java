/*
 * Copyright (c) 2020 Jiovan Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.amqp.autoconfigure;


import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.rabbit.config.ContainerCustomizer;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.DirectMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.core.io.ResourceLoader;
import com.rabbitmq.client.impl.CredentialsProvider;
import com.rabbitmq.client.impl.CredentialsRefreshService;
import com.jiovan.plexus.spring.toolkit.support.SingletonObjectProvider;
import lombok.NonNull;


/**
 * Utilities for {@link org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration}
 *
 * @author David Hsing
 */
@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted", "UnusedReturnValue"})
public abstract class RabbitConfigurationUtils {
    /**
     * Returns rabbit connection details derived from the given properties and SSL bundles
     *
     * @param properties the rabbit properties holding host, port, virtual host and credentials
     * @param bundles the SSL bundles used to resolve TLS settings for the connection, may be {@code null}
     *
     * @return rabbit connection details derived from the given properties and SSL bundles
     */
    @Nonnull
    public static RabbitConnectionDetails rabbitConnectionDetails(@NonNull RabbitProperties properties, @Nullable SslBundles bundles) {
        return new PropertiesRabbitConnectionDetails(properties, bundles);
    }

    /**
     * Returns a configurer that builds a rabbit connection factory bean from the given properties
     *
     * @param properties the rabbit properties holding host, port and connection settings
     * @param loader the resource loader used to resolve SSL material
     * @param details the rabbit connection details to use, may be {@code null} to derive them from the properties
     * @param credentials the credentials provider used for authentication, may be {@code null}
     * @param refresh the credentials refresh service used to rotate credentials, may be {@code null}
     * @param bundles the SSL bundles used to configure TLS, may be {@code null}
     *
     * @return a configurer that builds a rabbit connection factory bean from the given properties
     */
    @Nonnull
    public static RabbitConnectionFactoryBeanConfigurer rabbitConnectionFactoryBeanConfigurer(@NonNull RabbitProperties properties, @NonNull ResourceLoader loader, @Nullable RabbitConnectionDetails details, @Nullable CredentialsProvider credentials, @Nullable CredentialsRefreshService refresh, @Nullable SslBundles bundles) {
        RabbitConnectionDetails alias = ObjectUtils.getIfNull(details, rabbitConnectionDetails(properties, bundles));
        return new RabbitAutoConfiguration.RabbitConnectionFactoryCreator(properties).rabbitConnectionFactoryBeanConfigurer(loader, alias, SingletonObjectProvider.ofNullable(credentials), SingletonObjectProvider.ofNullable(refresh));
    }

    /**
     * Returns a configurer that applies cache and connection settings to a caching connection factory
     *
     * @param properties the rabbit properties holding cache mode, channel cache size and connection settings
     * @param details the rabbit connection details to use, may be {@code null} to derive them from the properties
     * @param strategy the strategy that names connections, may be {@code null}
     * @param bundles the SSL bundles used to configure TLS, may be {@code null}
     *
     * @return a configurer that applies cache and connection settings to a caching connection factory
     */
    @Nonnull
    public static CachingConnectionFactoryConfigurer cachingConnectionFactoryConfigurer(@NonNull RabbitProperties properties, @Nullable RabbitConnectionDetails details, @Nullable ConnectionNameStrategy strategy, @Nullable SslBundles bundles) {
        RabbitConnectionDetails alias = ObjectUtils.getIfNull(details, rabbitConnectionDetails(properties, bundles));
        return new RabbitAutoConfiguration.RabbitConnectionFactoryCreator(properties).rabbitConnectionFactoryConfigurer(alias, SingletonObjectProvider.ofNullable(strategy));
    }

    /**
     * Returns a caching connection factory configured from the given rabbit configurers
     *
     * @param properties the rabbit properties holding host, port and connection settings
     * @param rabbitConfigurer the configurer that builds the underlying connection factory bean
     * @param cachingConfigurer the configurer that applies cache and connection settings
     * @param customizer an optional customizer applied to the resulting connection factory
     *
     * @return a caching connection factory configured from the given rabbit configurers
     *
     * @throws Exception if the connection factory cannot be created
     */
    @NonNull
    public static CachingConnectionFactory cachingConnectionFactory(@NonNull RabbitProperties properties, @NonNull RabbitConnectionFactoryBeanConfigurer rabbitConfigurer, @NonNull CachingConnectionFactoryConfigurer cachingConfigurer, @Nullable ConnectionFactoryCustomizer customizer) throws Exception {
        return new RabbitAutoConfiguration.RabbitConnectionFactoryCreator(properties).rabbitConnectionFactory(rabbitConfigurer, cachingConfigurer, SingletonObjectProvider.ofNullable(customizer));
    }

    /**
     * Returns a configurer that applies the given rabbit properties to a rabbit template
     *
     * @param properties the rabbit properties holding template settings such as exchange, routing key and retry options
     * @param converter the message converter applied to outgoing and incoming messages, may be {@code null}
     * @param customizer an optional customizer of the retry template used by the message listener
     *
     * @return a configurer that applies the given rabbit properties to a rabbit template
     */
    @NonNull
    public static RabbitTemplateConfigurer rabbitTemplateConfigurer(@NonNull RabbitProperties properties, @Nullable MessageConverter converter, @Nullable RabbitTemplateRetrySettingsCustomizer customizer) {
        return new RabbitAutoConfiguration.RabbitTemplateConfiguration().rabbitTemplateConfigurer(properties, SingletonObjectProvider.ofNullable(converter), SingletonObjectProvider.ofNullable(customizer), SingletonObjectProvider.empty());
    }

    /**
     * Returns a rabbit template configured with the given configurer and connection factory
     *
     * @param configurer the configurer that applies the rabbit properties to the template
     * @param factory the connection factory the template uses to connect to the broker
     * @param customizer an optional customizer applied to the resulting rabbit template
     *
     * @return a rabbit template configured with the given configurer and connection factory
     */
    @NonNull
    public static RabbitTemplate rabbitTemplate(@NonNull RabbitTemplateConfigurer configurer, @NonNull ConnectionFactory factory, @Nullable RabbitTemplateCustomizer customizer) {
        return new RabbitAutoConfiguration.RabbitTemplateConfiguration().rabbitTemplate(configurer, factory, SingletonObjectProvider.ofNullable(customizer));
    }

    /**
     * Returns a configurer that applies the given rabbit properties to a simple listener container factory
     *
     * @param properties the rabbit properties holding listener retry and acknowledgement settings
     * @param converter the message converter applied to delivered messages, may be {@code null}
     * @param recoverer the message recoverer invoked after a message fails all retries, may be {@code null}
     * @param retryCustomizer an optional customizer of the retry template used by the listener
     *
     * @return a configurer that applies the given rabbit properties to a simple listener container factory
     */
    @NonNull
    public static SimpleRabbitListenerContainerFactoryConfigurer simpleRabbitListenerContainerFactoryConfigurer(@NonNull RabbitProperties properties, @Nullable MessageConverter converter, @Nullable MessageRecoverer recoverer, @Nullable RabbitListenerRetrySettingsCustomizer retryCustomizer) {
        RabbitAnnotationDrivenConfiguration configuration = new RabbitAnnotationDrivenConfiguration(SingletonObjectProvider.ofNullable(converter), SingletonObjectProvider.ofNullable(recoverer), SingletonObjectProvider.ofNullable(retryCustomizer), SingletonObjectProvider.empty(), properties);
        return configuration.simpleRabbitListenerContainerFactoryConfigurer();
    }

    /**
     * Returns a simple listener container factory configured with the given connection factory
     *
     * @param properties the rabbit properties holding listener retry and acknowledgement settings
     * @param converter the message converter applied to delivered messages, may be {@code null}
     * @param recoverer the message recoverer invoked after a message fails all retries, may be {@code null}
     * @param retryCustomizer an optional customizer of the retry template used by the listener
     * @param factory the connection factory the listener containers connect with
     * @param containerCustomizer an optional customizer applied to each created listener container
     *
     * @return a simple listener container factory configured with the given connection factory
     */
    @NonNull
    public static SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(@NonNull RabbitProperties properties, @Nullable MessageConverter converter, @Nullable MessageRecoverer recoverer, @Nullable RabbitListenerRetrySettingsCustomizer retryCustomizer, @NonNull ConnectionFactory factory, @Nullable ContainerCustomizer<SimpleMessageListenerContainer> containerCustomizer) {
        RabbitAnnotationDrivenConfiguration configuration = new RabbitAnnotationDrivenConfiguration(SingletonObjectProvider.ofNullable(converter), SingletonObjectProvider.ofNullable(recoverer), SingletonObjectProvider.ofNullable(retryCustomizer), SingletonObjectProvider.empty(), properties);
        SimpleRabbitListenerContainerFactoryConfigurer configurer = configuration.simpleRabbitListenerContainerFactoryConfigurer();
        return configuration.simpleRabbitListenerContainerFactory(configurer, factory, SingletonObjectProvider.ofNullable(containerCustomizer));
    }

    /**
     * Returns a configurer that applies the given rabbit properties to a direct listener container factory
     *
     * @param properties the rabbit properties holding listener retry and acknowledgement settings
     * @param converter the message converter applied to delivered messages, may be {@code null}
     * @param recoverer the message recoverer invoked after a message fails all retries, may be {@code null}
     * @param customizer an optional customizer of the retry template used by the listener
     *
     * @return a configurer that applies the given rabbit properties to a direct listener container factory
     */
    @NonNull
    public static DirectRabbitListenerContainerFactoryConfigurer directRabbitListenerContainerFactoryConfigurer(@NonNull RabbitProperties properties, @Nullable MessageConverter converter, @Nullable MessageRecoverer recoverer, @Nullable RabbitListenerRetrySettingsCustomizer customizer) {
        RabbitAnnotationDrivenConfiguration configuration = new RabbitAnnotationDrivenConfiguration(SingletonObjectProvider.ofNullable(converter), SingletonObjectProvider.ofNullable(recoverer), SingletonObjectProvider.ofNullable(customizer), SingletonObjectProvider.empty(), properties);
        return configuration.directRabbitListenerContainerFactoryConfigurer();
    }

    /**
     * Returns a direct listener container factory configured with the given connection factory
     *
     * @param properties the rabbit properties holding listener retry and acknowledgement settings
     * @param converter the message converter applied to delivered messages, may be {@code null}
     * @param recoverer the message recoverer invoked after a message fails all retries, may be {@code null}
     * @param retryCustomizer an optional customizer of the retry template used by the listener
     * @param factory the connection factory the listener containers connect with
     * @param containerCustomizer an optional customizer applied to each created listener container
     *
     * @return a direct listener container factory configured with the given connection factory
     */
    @NonNull
    public static DirectRabbitListenerContainerFactory directRabbitListenerContainerFactory(@NonNull RabbitProperties properties, @Nullable MessageConverter converter, @Nullable MessageRecoverer recoverer, @Nullable RabbitListenerRetrySettingsCustomizer retryCustomizer, @NonNull ConnectionFactory factory, @Nullable ContainerCustomizer<DirectMessageListenerContainer> containerCustomizer) {
        RabbitAnnotationDrivenConfiguration configuration = new RabbitAnnotationDrivenConfiguration(SingletonObjectProvider.ofNullable(converter), SingletonObjectProvider.ofNullable(recoverer), SingletonObjectProvider.ofNullable(retryCustomizer), SingletonObjectProvider.empty(), properties);
        DirectRabbitListenerContainerFactoryConfigurer configurer = configuration.directRabbitListenerContainerFactoryConfigurer();
        return configuration.directRabbitListenerContainerFactory(configurer, factory, SingletonObjectProvider.ofNullable(containerCustomizer));
    }
}
