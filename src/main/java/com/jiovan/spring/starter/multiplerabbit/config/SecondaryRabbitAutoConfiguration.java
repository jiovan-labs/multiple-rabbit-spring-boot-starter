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

package com.jiovan.spring.starter.multiplerabbit.config;


import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitMessagingTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.amqp.autoconfigure.CachingConnectionFactoryConfigurer;
import org.springframework.boot.amqp.autoconfigure.ConnectionFactoryCustomizer;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.amqp.autoconfigure.RabbitConfigurationUtils;
import org.springframework.boot.amqp.autoconfigure.RabbitConnectionDetails;
import org.springframework.boot.amqp.autoconfigure.RabbitConnectionFactoryBeanConfigurer;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateRetrySettingsCustomizer;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateConfigurer;
import org.springframework.boot.amqp.autoconfigure.RabbitTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.impl.CredentialsProvider;
import com.rabbitmq.client.impl.CredentialsRefreshService;
import com.jiovan.spring.starter.multiplerabbit.property.MultipleRabbitProperties;
import com.jiovan.spring.starter.multiplerabbit.util.CustomExchangeUtils;
import lombok.NonNull;


/**
 * Secondary configuration for rabbit
 *
 * @author David Hsing
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(prefix = "jiovan.multiple-rabbit", name = "enabled", matchIfMissing = true)
@ConditionalOnProperty(prefix = SecondaryRabbitAutoConfiguration.PROPERTIES_PREFIX, name = "host")
@ConditionalOnClass(value = {RabbitTemplate.class, Channel.class})
@AutoConfigureAfter(value = PrimaryRabbitAutoConfiguration.class)
@AutoConfigureBefore(value = RabbitAutoConfiguration.class)
public class SecondaryRabbitAutoConfiguration {
    public static final String PROPERTIES_PREFIX = "jiovan.multiple-rabbit.secondary";    // $NON-NLS-1$
    public static final String RABBIT_PROPERTIES = "secondaryRabbitProperties";    // $NON-NLS-1$
    public static final String CONNECTION_DETAILS = "secondaryRabbitConnectionDetails";    // $NON-NLS-1$
    public static final String CREDENTIALS_PROVIDER = "secondaryRabbitCredentialsProvider";    // $NON-NLS-1$
    public static final String CREDENTIALS_REFRESH_SERVICE = "secondaryRabbitCredentialsRefreshService";    // $NON-NLS-1$   // $NON-NLS-1$
    public static final String SSL_BUNDLES = "secondaryRabbitSslBundles";    // $NON-NLS-1$
    public static final String CONNECTION_FACTORY_BEAN_CONFIGURER = "secondaryRabbitConnectionFactoryBeanConfigurer";
    public static final String CONNECTION_NAME_STRATEGY = "secondaryRabbitConnectionNameStrategy";    // $NON-NLS-1$
    public static final String CACHING_CONNECTION_FACTORY_CONFIGURER = "secondaryRabbitCachingConnectionFactoryConfigurer";    // $NON-NLS-1$
    public static final String CONNECTION_FACTORY_CUSTOMIZER = "secondaryRabbitConnectionFactoryCustomizer";    // $NON-NLS-1$
    public static final String CONNECTION_FACTORY = "secondaryRabbitConnectionFactory";    // $NON-NLS-1$
    public static final String MESSAGE_CONVERTER = "secondaryRabbitMessageConverter";    // $NON-NLS-1$
    public static final String RETRY_TEMPLATE_CUSTOMIZER = "secondaryRabbitRetryTemplateCustomizer";    // $NON-NLS-1$
    public static final String RABBIT_TEMPLATE_CONFIGURER = "secondaryRabbitTemplateConfigurer";    // $NON-NLS-1$
    public static final String RABBIT_TEMPLATE_CUSTOMIZER = "primaryRabbitTemplateCustomizer";    // $NON-NLS-1$
    public static final String RABBIT_TEMPLATE = "secondaryRabbitTemplate";    // $NON-NLS-1$
    public static final String RABBIT_MESSAGING_TEMPLATE = "secondaryRabbitMessagingTemplate";    // $NON-NLS-1$
    public static final String AMQP_ADMIN = "secondaryRabbitAmqpAdmin";    // $NON-NLS-1$
    public static final String DELAYED_EXCHANGE = "secondaryRabbitDelayedExchange";    // $NON-NLS-1$

    /**
     * Returns a rabbit properties bean bound from the {@code PROPERTIES_PREFIX} configuration properties
     *
     * @return a rabbit properties bean bound from the {@code PROPERTIES_PREFIX} configuration properties
     */
    @Bean(name = RABBIT_PROPERTIES)
    @ConditionalOnMissingBean(name = RABBIT_PROPERTIES)
    @ConfigurationProperties(prefix = PROPERTIES_PREFIX)
    public MultipleRabbitProperties rabbitProperties() {
        return new MultipleRabbitProperties();
    }

    /**
     * Returns rabbit connection details derived from the secondary rabbit properties and SSL bundles
     *
     * @param properties the secondary rabbit properties qualified by {@code RABBIT_PROPERTIES}
     * @param bundles the secondary SSL bundles qualified by {@code SSL_BUNDLES}, may be {@code null}
     *
     * @return rabbit connection details derived from the secondary rabbit properties and SSL bundles
     */
    @Bean(name = CONNECTION_DETAILS)
    @ConditionalOnBean(name = RABBIT_PROPERTIES)
    @ConditionalOnMissingBean(name = CONNECTION_DETAILS)
    public RabbitConnectionDetails rabbitConnectionDetails(@Qualifier(value = RABBIT_PROPERTIES) @NonNull MultipleRabbitProperties properties, @Autowired(required = false) @Qualifier(value = SSL_BUNDLES) @Nullable SslBundles bundles) {
        return RabbitConfigurationUtils.rabbitConnectionDetails(properties, bundles);
    }

    /**
     * Returns a configurer bean that builds the secondary rabbit connection factory bean
     *
     * @param properties the secondary rabbit properties qualified by {@code RABBIT_PROPERTIES}
     * @param loader the resource loader used to resolve SSL material
     * @param details the connection details qualified by {@code CONNECTION_DETAILS}, may be {@code null} to derive them from the properties
     * @param credentials the credentials provider qualified by {@code CREDENTIALS_PROVIDER}, may be {@code null}
     * @param refresh the credentials refresh service qualified by {@code CREDENTIALS_REFRESH_SERVICE}, may be {@code null}
     * @param bundles the secondary SSL bundles qualified by {@code SSL_BUNDLES}, may be {@code null}
     *
     * @return a configurer bean that builds the secondary rabbit connection factory bean
     */
    @Bean(name = CONNECTION_FACTORY_BEAN_CONFIGURER)
    @ConditionalOnBean(name = RABBIT_PROPERTIES)
    @ConditionalOnMissingBean(name = CONNECTION_FACTORY_BEAN_CONFIGURER)
    public RabbitConnectionFactoryBeanConfigurer rabbitConnectionFactoryBeanConfigurer(@Qualifier(value = RABBIT_PROPERTIES) @NonNull MultipleRabbitProperties properties, @NonNull ResourceLoader loader,
        @Autowired(required = false) @Qualifier(value = CONNECTION_DETAILS) @Nullable RabbitConnectionDetails details,
        @Autowired(required = false) @Qualifier(value = CREDENTIALS_PROVIDER) @Nullable CredentialsProvider credentials,
        @Autowired(required = false) @Qualifier(value = CREDENTIALS_REFRESH_SERVICE) @Nullable CredentialsRefreshService refresh,
        @Autowired(required = false) @Qualifier(value = SSL_BUNDLES) @Nullable SslBundles bundles) {
        return RabbitConfigurationUtils.rabbitConnectionFactoryBeanConfigurer(properties, loader, details, credentials, refresh, bundles);
    }

    /**
     * Returns a configurer bean that applies cache and connection settings to the secondary caching connection factory
     *
     * @param properties the secondary rabbit properties qualified by {@code RABBIT_PROPERTIES}
     * @param details the connection details qualified by {@code CONNECTION_DETAILS}, may be {@code null} to derive them from the properties
     * @param strategy the connection name strategy qualified by {@code CONNECTION_NAME_STRATEGY}, may be {@code null}
     * @param bundles the secondary SSL bundles qualified by {@code SSL_BUNDLES}, may be {@code null}
     *
     * @return a configurer bean that applies cache and connection settings to the secondary caching connection factory
     */
    @Bean(name = CACHING_CONNECTION_FACTORY_CONFIGURER)
    @ConditionalOnBean(name = RABBIT_PROPERTIES)
    @ConditionalOnMissingBean(name = CACHING_CONNECTION_FACTORY_CONFIGURER)
    public CachingConnectionFactoryConfigurer cachingConnectionFactoryConfigurer(@Qualifier(value = RABBIT_PROPERTIES) @NonNull MultipleRabbitProperties properties,
        @Autowired(required = false) @Qualifier(value = CONNECTION_DETAILS) @Nullable RabbitConnectionDetails details,
        @Autowired(required = false) @Qualifier(value = CONNECTION_NAME_STRATEGY) @Nullable ConnectionNameStrategy strategy,
        @Autowired(required = false) @Qualifier(value = SSL_BUNDLES) @Nullable SslBundles bundles) {
        return RabbitConfigurationUtils.cachingConnectionFactoryConfigurer(properties, details, strategy, bundles);
    }

    /**
     * Returns a caching connection factory bean configured from the secondary rabbit configurers
     *
     * @param properties the secondary rabbit properties qualified by {@code RABBIT_PROPERTIES}
     * @param rabbitConfigurer the connection factory bean configurer qualified by {@code CONNECTION_FACTORY_BEAN_CONFIGURER}
     * @param cachingConfigurer the caching configurer qualified by {@code CACHING_CONNECTION_FACTORY_CONFIGURER}
     * @param customizer the connection factory customizer qualified by {@code CONNECTION_FACTORY_CUSTOMIZER}, may be {@code null}
     *
     * @return a caching connection factory bean configured from the secondary rabbit configurers
     *
     * @throws Exception if the connection factory cannot be created
     */
    @Bean(name = CONNECTION_FACTORY)
    @ConditionalOnBean(name = RABBIT_PROPERTIES)
    @ConditionalOnMissingBean(name = CONNECTION_FACTORY)
    public CachingConnectionFactory cachingConnectionFactory(@Qualifier(value = RABBIT_PROPERTIES) @NonNull MultipleRabbitProperties properties,
        @Qualifier(value = CONNECTION_FACTORY_BEAN_CONFIGURER) @NonNull RabbitConnectionFactoryBeanConfigurer rabbitConfigurer,
        @Qualifier(value = CACHING_CONNECTION_FACTORY_CONFIGURER) @NonNull CachingConnectionFactoryConfigurer cachingConfigurer,
        @Autowired(required = false) @Qualifier(value = CONNECTION_FACTORY_CUSTOMIZER) @Nullable ConnectionFactoryCustomizer customizer) throws Exception {
        return RabbitConfigurationUtils.cachingConnectionFactory(properties, rabbitConfigurer, cachingConfigurer, customizer);
    }

    /**
     * Returns a configurer bean that applies the secondary rabbit properties to a rabbit template
     *
     * @param properties the secondary rabbit properties qualified by {@code RABBIT_PROPERTIES}
     * @param converter the message converter qualified by {@code MESSAGE_CONVERTER}, may be {@code null}
     * @param customizer the retry template customizer qualified by {@code RETRY_TEMPLATE_CUSTOMIZER}, may be {@code null}
     *
     * @return a configurer bean that applies the secondary rabbit properties to a rabbit template
     */
    @Bean(name = RABBIT_TEMPLATE_CONFIGURER)
    @ConditionalOnBean(name = RABBIT_PROPERTIES)
    @ConditionalOnMissingBean(name = RABBIT_TEMPLATE_CONFIGURER)
    public RabbitTemplateConfigurer rabbitTemplateConfigurer(@Qualifier(value = RABBIT_PROPERTIES) @NonNull MultipleRabbitProperties properties,
        @Autowired(required = false) @Qualifier(value = MESSAGE_CONVERTER) @Nullable MessageConverter converter,
        @Autowired(required = false) @Qualifier(value = RETRY_TEMPLATE_CUSTOMIZER) @Nullable RabbitTemplateRetrySettingsCustomizer customizer) {
        return RabbitConfigurationUtils.rabbitTemplateConfigurer(properties, converter, customizer);
    }

    /**
     * Returns a rabbit template bean configured with the secondary configurer and connection factory
     *
     * @param configurer the rabbit template configurer qualified by {@code RABBIT_TEMPLATE_CONFIGURER}
     * @param factory the secondary connection factory qualified by {@code CONNECTION_FACTORY}
     * @param customizer the rabbit template customizer qualified by {@code RABBIT_TEMPLATE_CUSTOMIZER}
     *
     * @return a rabbit template bean configured with the secondary configurer and connection factory
     */
    @Bean(name = RABBIT_TEMPLATE)
    @ConditionalOnBean(name = {RABBIT_TEMPLATE_CONFIGURER, CONNECTION_FACTORY})
    @ConditionalOnMissingBean(name = RABBIT_TEMPLATE)
    public RabbitTemplate rabbitTemplate(@Qualifier(value = RABBIT_TEMPLATE_CONFIGURER) @NonNull RabbitTemplateConfigurer configurer,
        @Qualifier(value = CONNECTION_FACTORY) @NonNull ConnectionFactory factory,
        @Autowired(required = false) @Qualifier(value = RABBIT_TEMPLATE_CUSTOMIZER) @NonNull RabbitTemplateCustomizer customizer) {
        return RabbitConfigurationUtils.rabbitTemplate(configurer, factory, customizer);
    }

    /**
     * Returns a rabbit messaging template bean backed by the secondary rabbit template
     *
     * @param template the secondary rabbit template qualified by {@code RABBIT_TEMPLATE}
     *
     * @return a rabbit messaging template bean backed by the secondary rabbit template
     */
    @Bean(name = RABBIT_MESSAGING_TEMPLATE)
    @ConditionalOnBean(name = RABBIT_TEMPLATE, value = RabbitTemplate.class)
    @ConditionalOnMissingBean(name = RABBIT_MESSAGING_TEMPLATE)
    public RabbitMessagingTemplate rabbitMessagingTemplate(@Qualifier(value = RABBIT_TEMPLATE) @NonNull RabbitTemplate template) {
        return new RabbitMessagingTemplate(template);
    }

    /**
     * Returns an AMQP admin bean that manages broker resources on the secondary connection factory
     *
     * @param factory the secondary connection factory qualified by {@code CONNECTION_FACTORY}
     *
     * @return an AMQP admin bean that manages broker resources on the secondary connection factory
     */
    @Bean(name = AMQP_ADMIN)
    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX, name = "dynamic", matchIfMissing = true)
    @ConditionalOnBean(name = CONNECTION_FACTORY)
    @ConditionalOnMissingBean(name = AMQP_ADMIN)
    public AmqpAdmin amqpAdmin(@Qualifier(value = CONNECTION_FACTORY) @NonNull ConnectionFactory factory) {
        return new RabbitAdmin(factory);
    }

    /**
     * Returns a delayed message exchange bean declared from the secondary delayed exchange settings
     *
     * @param amqpAdmin the AMQP admin qualified by {@code AMQP_ADMIN} used to declare the exchange
     * @param properties the secondary rabbit properties qualified by {@code RABBIT_PROPERTIES} holding the delayed exchange settings
     *
     * @return a delayed message exchange bean declared from the secondary delayed exchange settings
     */
    @Bean(name = DELAYED_EXCHANGE)
    @ConditionalOnBooleanProperty(prefix = PROPERTIES_PREFIX + ".delayed-exchange", name = "enabled", matchIfMissing = true)
    @ConditionalOnProperty(prefix = PROPERTIES_PREFIX + ".delayed-exchange", name = "name")
    @ConditionalOnBean(name = AMQP_ADMIN)
    @ConditionalOnMissingBean(name = DELAYED_EXCHANGE)
    public CustomExchange delayedExchange(@Qualifier(value = AMQP_ADMIN) @Nonnull AmqpAdmin amqpAdmin, @Qualifier(value = RABBIT_PROPERTIES) @NonNull MultipleRabbitProperties properties) {
        MultipleRabbitProperties.DelayedExchange prop = properties.getDelayedExchange();
        return CustomExchangeUtils.delayedMessageExchange(amqpAdmin, prop.getName(), prop.getType(), BooleanUtils.isTrue(prop.getDurable()), BooleanUtils.isTrue(prop.getAutoDelete()));
    }
}
