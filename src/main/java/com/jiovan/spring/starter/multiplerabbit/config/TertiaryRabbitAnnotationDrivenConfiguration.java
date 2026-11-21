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


import jakarta.annotation.Nullable;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.DirectRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.amqp.autoconfigure.DirectRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.amqp.autoconfigure.RabbitAutoConfiguration;
import org.springframework.boot.amqp.autoconfigure.RabbitConfigurationUtils;
import org.springframework.boot.amqp.autoconfigure.RabbitProperties;
import org.springframework.boot.amqp.autoconfigure.RabbitListenerRetrySettingsCustomizer;
import org.springframework.boot.amqp.autoconfigure.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.NonNull;


/**
 * Tertiary configuration for annotation driven rabbit
 *
 * @author David Hsing
 *
 * @see org.springframework.boot.amqp.autoconfigure.RabbitAnnotationDrivenConfiguration
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(prefix = "jiovan.multiple-rabbit", name = "enabled", matchIfMissing = true)
@ConditionalOnProperty(prefix = TertiaryRabbitAutoConfiguration.PROPERTIES_PREFIX, name = "host")
@ConditionalOnClass(value = EnableRabbit.class)
@AutoConfigureAfter(value = {SecondaryRabbitAnnotationDrivenConfiguration.class, TertiaryRabbitAutoConfiguration.class})
@AutoConfigureBefore(value = RabbitAutoConfiguration.class)
@SuppressWarnings("JavadocReference")
public class TertiaryRabbitAnnotationDrivenConfiguration {
    public static final String MESSAGE_RECOVERER = "tertiaryRabbitMessageRecoverer";    // $NON-NLS-1$
    public static final String SIMPLE_CONTAINER_FACTORY_CONFIGURER = "tertiaryRabbitSimpleListenerContainerFactoryConfigurer";    // $NON-NLS-1$
    public static final String DIRECT_CONTAINER_FACTORY_CONFIGURER = "tertiaryRabbitDirectListenerContainerFactoryConfigurer";    // $NON-NLS-1$
    public static final String CONTAINER_FACTORY = "tertiaryRabbitListenerContainerFactory";    // $NON-NLS-1$

    /**
     * Returns a configurer that applies the tertiary rabbit properties to a simple listener container factory
     *
     * @param properties the rabbit properties qualified by {@code RABBIT_PROPERTIES} that hold listener settings
     * @param converter the message converter qualified by {@code MESSAGE_CONVERTER}, may be {@code null}
     * @param recoverer the message recoverer qualified by {@code MESSAGE_RECOVERER}, may be {@code null}
     * @param customizer the retry template customizer qualified by {@code RETRY_TEMPLATE_CUSTOMIZER}, may be {@code null}
     *
     * @return a configurer that applies the tertiary rabbit properties to a simple listener container factory
     */
    @Bean(name = SIMPLE_CONTAINER_FACTORY_CONFIGURER)
    @ConditionalOnProperty(prefix = TertiaryRabbitAutoConfiguration.PROPERTIES_PREFIX, name = "listener.type", havingValue = "simple", matchIfMissing = true)
    @ConditionalOnBean(name = TertiaryRabbitAutoConfiguration.RABBIT_PROPERTIES, value = RabbitProperties.class)
    @ConditionalOnMissingBean(name = SIMPLE_CONTAINER_FACTORY_CONFIGURER)
    public SimpleRabbitListenerContainerFactoryConfigurer simpleRabbitListenerContainerFactoryConfigurer(
        @Qualifier(value = TertiaryRabbitAutoConfiguration.RABBIT_PROPERTIES) @NonNull RabbitProperties properties,
        @Autowired(required = false) @Qualifier(value = TertiaryRabbitAutoConfiguration.MESSAGE_CONVERTER) @Nullable MessageConverter converter,
        @Autowired(required = false) @Qualifier(value = MESSAGE_RECOVERER) @Nullable MessageRecoverer recoverer,
        @Autowired(required = false) @Qualifier(value = TertiaryRabbitAutoConfiguration.RETRY_TEMPLATE_CUSTOMIZER) @Nullable RabbitListenerRetrySettingsCustomizer customizer) {
        return RabbitConfigurationUtils.simpleRabbitListenerContainerFactoryConfigurer(properties, converter, recoverer, customizer);
    }

    /**
     * Returns a simple listener container factory bean configured with the tertiary connection factory
     *
     * @param configurer the simple container factory configurer qualified by {@code SIMPLE_CONTAINER_FACTORY_CONFIGURER}
     * @param factory the tertiary connection factory the listener containers connect with
     *
     * @return a simple listener container factory bean configured with the tertiary connection factory
     */
    @Bean(name = CONTAINER_FACTORY)
    @ConditionalOnProperty(prefix = TertiaryRabbitAutoConfiguration.PROPERTIES_PREFIX, name = "listener.type", havingValue = "simple", matchIfMissing = true)
    @ConditionalOnBean(name = {SIMPLE_CONTAINER_FACTORY_CONFIGURER, TertiaryRabbitAutoConfiguration.CONNECTION_FACTORY})
    @ConditionalOnMissingBean(name = CONTAINER_FACTORY)
    public SimpleRabbitListenerContainerFactory simpleRabbitListenerContainerFactory(
        @Qualifier(value = SIMPLE_CONTAINER_FACTORY_CONFIGURER) @NonNull SimpleRabbitListenerContainerFactoryConfigurer configurer,
        @Qualifier(value = TertiaryRabbitAutoConfiguration.CONNECTION_FACTORY) @NonNull ConnectionFactory factory) {
        SimpleRabbitListenerContainerFactory containerFactory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(containerFactory, factory);
        return containerFactory;
    }

    /**
     * Returns a configurer that applies the tertiary rabbit properties to a direct listener container factory
     *
     * @param properties the rabbit properties qualified by {@code RABBIT_PROPERTIES} that hold listener settings
     * @param converter the message converter qualified by {@code MESSAGE_CONVERTER}, may be {@code null}
     * @param recoverer the message recoverer qualified by {@code MESSAGE_RECOVERER}, may be {@code null}
     * @param customizer the retry template customizer qualified by {@code RETRY_TEMPLATE_CUSTOMIZER}, may be {@code null}
     *
     * @return a configurer that applies the tertiary rabbit properties to a direct listener container factory
     */
    @Bean(name = DIRECT_CONTAINER_FACTORY_CONFIGURER)
    @ConditionalOnProperty(prefix = TertiaryRabbitAutoConfiguration.PROPERTIES_PREFIX, name = "listener.type", havingValue = "direct")
    @ConditionalOnBean(name = TertiaryRabbitAutoConfiguration.RABBIT_PROPERTIES, value = RabbitProperties.class)
    @ConditionalOnMissingBean(name = DIRECT_CONTAINER_FACTORY_CONFIGURER)
    public DirectRabbitListenerContainerFactoryConfigurer directRabbitListenerContainerFactoryConfigurer(
        @Qualifier(value = TertiaryRabbitAutoConfiguration.RABBIT_PROPERTIES) @NonNull RabbitProperties properties,
        @Autowired(required = false) @Qualifier(value = TertiaryRabbitAutoConfiguration.MESSAGE_CONVERTER) @Nullable MessageConverter converter,
        @Autowired(required = false) @Qualifier(value = MESSAGE_RECOVERER) @Nullable MessageRecoverer recoverer,
        @Autowired(required = false) @Qualifier(value = TertiaryRabbitAutoConfiguration.RETRY_TEMPLATE_CUSTOMIZER) @Nullable RabbitListenerRetrySettingsCustomizer customizer) {
        return RabbitConfigurationUtils.directRabbitListenerContainerFactoryConfigurer(properties, converter, recoverer, customizer);
    }

    /**
     * Returns a direct listener container factory bean configured with the tertiary connection factory
     *
     * @param configurer the direct container factory configurer qualified by {@code DIRECT_CONTAINER_FACTORY_CONFIGURER}
     * @param factory the tertiary connection factory the listener containers connect with
     *
     * @return a direct listener container factory bean configured with the tertiary connection factory
     */
    @Bean(name = CONTAINER_FACTORY)
    @ConditionalOnProperty(prefix = TertiaryRabbitAutoConfiguration.PROPERTIES_PREFIX, name = "listener.type", havingValue = "direct")
    @ConditionalOnBean(name = {DIRECT_CONTAINER_FACTORY_CONFIGURER, TertiaryRabbitAutoConfiguration.CONNECTION_FACTORY})
    @ConditionalOnMissingBean(name = CONTAINER_FACTORY)
    public DirectRabbitListenerContainerFactory directRabbitListenerContainerFactory(
        @Qualifier(value = DIRECT_CONTAINER_FACTORY_CONFIGURER) @NonNull DirectRabbitListenerContainerFactoryConfigurer configurer,
        @Qualifier(value = TertiaryRabbitAutoConfiguration.CONNECTION_FACTORY) @NonNull ConnectionFactory factory) {
        DirectRabbitListenerContainerFactory containerFactory = new DirectRabbitListenerContainerFactory();
        configurer.configure(containerFactory, factory);
        return containerFactory;
    }
}
