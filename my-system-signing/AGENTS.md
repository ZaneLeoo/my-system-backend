# 可迁移签署模块约定

- 公共代码和本模块测试必须兼容 Java 8；保留 `release=8`。构建及 Spotless 可使用 JDK 17。
- 不依赖若依、Spring、Servlet、MyBatis、Jackson 或业务实体。Lombok 仅 provided；测试依赖不得进入运行时。
- 不使用 Java 8 后新增的语法或 JDK API，包括 List.of、Stream.toList、String.isBlank、java.net.http。
- 平台字段及数字状态在 provider 中转换；业务服务使用公共模型。扩展能力通过 spi 注入。
- 网络请求必须在本地事务外；创建结果未知不得盲目重试。恢复和回调行为必须有测试。
- 保留显式类型、所有控制语句花括号、中文字段注释及方法 Javadoc。
