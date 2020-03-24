# 配置

Before configuring Akka HTTP it must be added to your project as described in
@ref[the introduction](introduction.md#using-akka-http).

在配置 Akka HTTP 之前，必需按 @ref[导读](introduction.md#using-akka-http) 描述的将它添加到你的项目里。

Just like any other Akka module Akka HTTP is configured via [Typesafe Config](https://github.com/lightbend/config).
Usually this means that you provide an `application.conf` which contains all the application-specific settings that
differ from the default ones provided by the reference configuration files from the individual Akka modules.

就像其它 Akka 模块一样 Akka HTTP 是通过 [Typesafe Config](https://github.com/lightbend/config) 配置的。
通常，这意味着你应提供一个 `application.conf` 配置文件，其中包含所有应用程序特定相关的设置，这些设置与各 Akka 模块中的参考配置文件提供的默认值不同。 

These are the relevant default configuration values for the Akka HTTP modules.

这些是 Akka HTTP 模块的相关默认配置值。

akka-http-core
:  @@snip [reference.conf]($akka-http$/akka-http-core/src/main/resources/reference.conf)

akka-http
:  @@snip [reference.conf]($akka-http$/akka-http/src/main/resources/reference.conf)

akka-http-caching
:  @@snip [reference.conf]($akka-http$/akka-http-caching/src/main/resources/reference.conf)

The other Akka HTTP modules do not offer any configuration via [Typesafe Config](https://github.com/lightbend/config).

其它 Akka HTTP 模块不通过 [Typesafe Config](https://github.com/lightbend/config) 提供任何配置。
