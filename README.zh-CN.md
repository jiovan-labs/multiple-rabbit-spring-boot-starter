[English](README.md) | [简体中文](README.zh-CN.md) | [繁體中文](README.zh-TW.md)

# 多 RabbitMQ Spring Boot Starter

让 Spring Boot 应用快速集成多个 `RabbitMQ`。

## 快速开始

- 导入依赖

```xml
    <dependency>
        <groupId>com.jiovan.spring.starter</groupId>
        <artifactId>multiple-rabbit-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> 默认情况下，此 starter 会自动生效，你可以通过 `jiovan.multiple-rabbit.enabled = false` 关闭它。

- 使用前缀 `jiovan.multiple-rabbit` 配置 Spring Boot 的 `application.yml`

```yml
jiovan:
    multiple-rabbit:
        primary:
            host: '192.168.0.1'
            delayed-exchange:
                name: 'delayed-exchange'
                type: direct
                durable: true
                auto-delete: true
        secondary:
            host: '192.168.0.2'
        tertiary:
            host: '192.168.0.3'
```

> 此 starter 最多支持 3 个 RabbitMQ `ConnectionFactory`。（三振出局）

- **可选功能**：如果要使用 RabbitMQ 延迟消息交换机，需要先下载 `rabbitmq_delayed_message_exchange` 插件并按如下方式安装：
```
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

> 插件下载地址：https://github.com/rabbitmq/rabbitmq-delayed-message-exchange

- 通过 `@Autowired`/`@Resource` 注解并配合 `@Qualifier` 注解注入以下 Bean（以 `primary` 为例）

| Bean 类型 | 限定符 |
|--------------------------|----------------------------------------------------------|
| CachingConnectionFactory | PrimaryRabbitAutoConfiguration.CONNECTION_FACTORY |
| RabbitTemplate | PrimaryRabbitAutoConfiguration.RABBIT_TEMPLATE |
| RabbitMessagingTemplate | PrimaryRabbitAutoConfiguration.RABBIT_MESSAGING_TEMPLATE |
| AmqpAdmin | PrimaryRabbitAutoConfiguration.AMQP_ADMIN |
| CustomExchange | PrimaryRabbitAutoConfiguration.DELAYED_EXCHANGE |

## 文档

- GitHub：https://github.com/jiovan-labs/multiple-rabbit-spring-boot-starter
- RabbitMQ GitHub：https://github.com/rabbitmq/rabbitmq-java-client
- RabbitMQ 主页：https://rabbitmq.com

## 环境要求

- jdk 17+

## 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

请参阅 `NOTICE.txt` 文件了解所需的声明与归属信息。

## 捐赠

喜欢这个工具包吗？欢迎[向我们捐赠](https://jiovan.com/donation)以支持开发。

## 版权

北京玖漫科技有限公司

## 网站

- 玖漫：https://jiovan.com
