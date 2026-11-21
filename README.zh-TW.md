[English](README.md) | [簡體中文](README.zh-CN.md) | [繁體中文](README.zh-TW.md)

# 多 RabbitMQ Spring Boot Starter

讓 Spring Boot 應用快速整合多個 `RabbitMQ`。

## 快速開始

- 匯入相依性

```xml
    <dependency>
        <groupId>com.jiovan.spring.starter</groupId>
        <artifactId>multiple-rabbit-spring-boot-starter</artifactId>
        <version>LATEST</version>
    </dependency>
```

> 預設情況下，此 starter 會自動生效，你可以透過 `jiovan.multiple-rabbit.enabled = false` 關閉它。

- 使用字首 `jiovan.multiple-rabbit` 設定 Spring Boot 的 `application.yml`

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

> 此 starter 最多支援 3 個 RabbitMQ `ConnectionFactory`。（三振出局）

- **可選功能**：如果要使用 RabbitMQ 延遲訊息交換機，需要先下載 `rabbitmq_delayed_message_exchange` 外掛並按如下方式安裝：
```
rabbitmq-plugins enable rabbitmq_delayed_message_exchange
```

> 外掛下載地址：https://github.com/rabbitmq/rabbitmq-delayed-message-exchange

- 透過 `@Autowired`/`@Resource` 註解並配合 `@Qualifier` 註解注入以下 Bean（以 `primary` 為例）

| Bean 型別 | 限定符 |
|--------------------------|----------------------------------------------------------|
| CachingConnectionFactory | PrimaryRabbitAutoConfiguration.CONNECTION_FACTORY |
| RabbitTemplate | PrimaryRabbitAutoConfiguration.RABBIT_TEMPLATE |
| RabbitMessagingTemplate | PrimaryRabbitAutoConfiguration.RABBIT_MESSAGING_TEMPLATE |
| AmqpAdmin | PrimaryRabbitAutoConfiguration.AMQP_ADMIN |
| CustomExchange | PrimaryRabbitAutoConfiguration.DELAYED_EXCHANGE |

## 文件

- GitHub：https://github.com/jiovan-labs/multiple-rabbit-spring-boot-starter
- RabbitMQ GitHub：https://github.com/rabbitmq/rabbitmq-java-client
- RabbitMQ 主頁：https://rabbitmq.com

## 環境要求

- jdk 17+

## 許可證

本專案採用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 許可證。

請參閱 `NOTICE.txt` 檔案瞭解所需的宣告與歸屬資訊。

## 捐贈

喜歡這個工具包嗎？歡迎[向我們捐贈](https://jiovan.com/donation)以支援開發。

## 版權

北京玖漫科技有限公司

## 網站

- 玖漫：https://jiovan.com
