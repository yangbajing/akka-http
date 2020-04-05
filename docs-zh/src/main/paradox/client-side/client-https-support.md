# 客户端 HTTPS 支持
*Client-Side HTTPS Support*

Akka HTTP supports TLS encryption on the client-side as well as on the @ref[server-side](../server-side/server-https-support.md).

Akka HTTP 在客户端以及 @ref[服务器端](../server-side/server-https-support.md) 都支持 TSL 加密。

The central vehicle for configuring encryption is the @apidoc[HttpsConnectionContext], which can be created using
the static method `ConnectionContext.https` which is defined like this:

配置加密的主要工具是 @apidoc[HttpsConnectionContext] ，它通过使用静态方法 `ConnectionContext.https` 创建，定义看起来像这样：

Scala
:  @@snip[ConnectionContext.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/scaladsl/ConnectionContext.scala) { #https-context-creation }

Java
:  @@snip [ConnectionContext.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/javadsl/ConnectionContext.scala) { #https-context-creation }

In addition to the `outgoingConnection`, `newHostConnectionPool` and `cachedHostConnectionPool` methods the
@scala[@scaladoc[akka.http.scaladsl.Http](akka.http.scaladsl.Http$)]@java[@javadoc[akka.http.javadsl.Http](akka.http.javadsl.Http)]
extension also defines `outgoingConnectionHttps`, `newHostConnectionPoolHttps` and
`cachedHostConnectionPoolHttps`. These methods work identically to their counterparts without the `-Https` suffix,
with the exception that all connections will always be encrypted.

除 `outgoingConnection`、`newHostConnectionPool` 和 `cachedHostConnectionPool` 方法之外，
@scala[@scaladoc[akka.http.scaladsl.Http](akka.http.scaladsl.Http$)]@java[@javadoc[akka.http.javadsl.Http](akka.http.javadsl.Http)]
扩展也定义了 `outgoingConnectionHttps`、`newHostConnectionPoolHttps` 和 `cachedHostConnectionPoolHttps`。
这些方法和它们没有 `-Https` 后缀的对应方法相同，区别是所有连接将始终加密。

The `singleRequest` and `superPool` methods determine the encryption state via the scheme of the incoming request,
i.e. requests to an "https" URI will be encrypted, while requests to an "http" URI won't.

`singleRequest` 和 `superPool` 方法通过传入请求的模式来决定加密状态，例如：将加密对 "https" URI 的请求，而对 "http" URI 的请求不会。

The encryption configuration for all HTTPS connections, i.e. the `HttpsContext` is determined according to the
following logic:

 1. If the optional `httpsContext` method parameter is defined it contains the configuration to be used (and thus
takes precedence over any potentially set default client-side `HttpsContext`).
 2. If the optional `httpsContext` method parameter is undefined (which is the default) the default client-side
`HttpsContext` is used, which can be set via the `setDefaultClientHttpsContext` on the @scala[@apidoc[Http$]]@java[@apidoc[Http]] extension.
 3. If no default client-side `HttpsContext` has been set via the `setDefaultClientHttpsContext` on the @scala[@apidoc[Http$]]@java[@apidoc[Http]]
extension the default system configuration is used.

对于所有 HTTPS 连接的加密配置，例如：`HttpsContext` 根据以下逻辑决定：

 1. 如果定义了可选的 `httpsContext` 方法参数，则使用它包含的配置（因此优先于任何可能设置的默认客户端 `HttpsContext`）。
 2. 如果未定义可选的 `httpsContext` 方法参数，则使用默认客户端 `HttpsContext`，默认值可以通过 @scala[@apidoc[Http$]]@java[@apidoc[Http]] 扩展上的 `setDefaultClientHttpsContext` 设置。
 3. 如果没有通过 @scala[@apidoc[Http$]]@java[@apidoc[Http]] 扩展上的 `setDefaultClientHttpsContext` 设置默认客户端 `HttpsContext`，则默认的系统配置被使用。

Usually the process is, if the default system TLS configuration is not good enough for your application's needs,
that you configure a custom `HttpsContext` instance and set it via
@scala[`Http().setDefaultClientHttpsContext`]@java[`Http.get(system).setDefaultClientHttpsContext`].
Afterwards you simply use `outgoingConnectionHttps`, `newHostConnectionPoolHttps`, `cachedHostConnectionPoolHttps`,
`superPool` or `singleRequest` without a specific `httpsContext` argument, which causes encrypted connections
to rely on the configured default client-side `HttpsContext`.

通常的过程是，如果默认的系统 TSL 配置不能满足你的应用程序的需要，那么配置一个自定义 `HttpsContext` 实例通过
@scala[`Http().setDefaultClientHttpsContext`]@java[`Http.get(system).setDefaultClientHttpsContext`] 设置它。
然后，你简单使用 `outgoingConnectionHttps`、`newHostConnectionPoolHttps`、`cachedHostConnectionPoolHttps`、`superPool` 或
`singleRequest` 而不用指定 `httpsContext` 参数，这将导致加密连接依赖于配置的默认客户端 `HttpsContext`。 

If no custom `HttpsContext` is defined the default context uses Java's default TLS settings. Customizing the
`HttpsContext` can make the Https client less secure. Understand what you are doing!

如果没有定义自定义的 `HttpsContext`，默认上下文使用 Java 的默认 TLS 设置。定制 `HttpsContext` 可能会降低 Https 客户端的安全性。
请了解你正在做什么！

## SSL-Config
**SSL-配置**

Akka HTTP heavily relies on, and delegates most configuration of any SSL/TLS related options to
[Lightbend SSL-Config](https://lightbend.github.io/ssl-config/), which is a library specialized in providing an secure-by-default SSLContext
and related options.

Akka HTTP 严重依赖并将大部分 SSL/TLS 相关选项的配置委托给 [Lightbend SSL-配置](https://lightbend.github.io/ssl-config/)，
这是一个专门提供默认安全 `SSLContext` 和相关选项的库。

Please refer to the [Lightbend SSL-Config](https://lightbend.github.io/ssl-config/) documentation for detailed documentation of all available settings.

有关所有可用设置的详细文档，请参考 [Lightbend SSL-配置](https://lightbend.github.io/ssl-config/) 文档。

SSL Config settings used by Akka HTTP (as well as Streaming TCP) are located under the *akka.ssl-config* namespace.

Akka HTTP（以及流式 TCP）使用的 SSL 配置设置位于 *akka.ssl-config* 名称空间。

## Detailed configuration and workarounds
**详细的配置和解决方案**

Akka HTTP relies on [Lightbend SSL-Config](https://lightbend.github.io/ssl-config) which is a library maintained by Lightbend that makes configuring
things related to SSL/TLS much simpler than using the raw SSL APIs provided by the JDK. Please refer to its
documentation to learn more about it.

Akka HTTP 依赖由 Lightbend 维护的 [Lightbend SSL-配置](https://lightbend.github.io/ssl-config/) 库，
它使对 SSL/TLS 相关的配置比使用 JDK 提供的原始 SSL API 简单得多。请参考它的文档学习更多。  

All configuration options available to this library may be set under the `akka.ssl-config` configuration for Akka HTTP applications.

此库可用的所有配置选项都可以在 Akka HTTP 应用程序的 `akka.ssl-config` 配置下设置。

@@@ note
When encountering problems connecting to HTTPS hosts we highly encourage to reading up on the excellent ssl-config
configuration. Especially the quick start sections about [adding certificates to the trust store](https://lightbend.github.io/ssl-config/WSQuickStart.html#connecting-to-a-remote-server-over-https) should prove
very useful, for example to easily trust a self-signed certificate that applications might use in development mode.

当遇到连接到 HTTPS 主机的问题时，我们强烈建议阅读优秀的 ss-config 配置。特别是有关
[添加证书到信任存储](https://lightbend.github.io/ssl-config/WSQuickStart.html#connecting-to-a-remote-server-over-https)
的快速入门部分非常有用，例如，轻松信任自签名证书，应用程序可能在使用开发模式。
@@@

@@@ warning

While it is possible to disable certain checks using the so called "loose" settings in SSL Config, we **strongly recommend**
to instead attempt to solve these issues by properly configuring TLS–for example by adding trusted keys to the keystore.

虽然可以使用 SSL 配置中所谓的“松散”设置禁用某些检查，但我们 **强烈建议** 尝试通过正确配置 TLS 来解决这些问题，例如向密钥库添加可信密钥。

If however certain checks really need to be disabled because of misconfigured (or legacy) servers that your
application has to speak to, instead of disabling the checks globally (i.e. in `application.conf`) we suggest
configuring the loose settings for *specific connections* that are known to need them disabled (and trusted for some other reason).
The pattern of doing so is documented in the following sub-sections.

但是，如果因为你的应用程序需要与错误配置（或遗留）的服务器对话，某些检查确实需要禁用，则建议为已知需要禁用这些检查的 *特定连接*
（以及某些其它原因而可信的连接）配置设置，而不是全局禁用这些检查（例如：在 `application.conf` 中）。
这样做的模式被记录在下面小节中。
@@@

### Hostname verification
**主机名验证**

Hostname verification proves that the Akka HTTP client is actually communicating with the server it intended to
communicate with. Without this check a man-in-the-middle attack is possible. In the attack scenario, an alternative
certificate would be presented which was issued for another host name. Checking the host name in the certificate
against the host name the connection was opened against is therefore vital.

主机名验证证明 Akka HTTP 客户端是真正的在与它要与之通信的服务器通信。没有这个检查，中间人攻击是可能的。在攻击场景中，
将提供为另一个主机名颁发的替代证书。因此，根据打开连接所针对的主机名检查证书中的主机名必不可少。

The default `HttpsContext` enables hostname verification. Akka HTTP relies on the [Lightbend SSL-Config](https://lightbend.github.io/ssl-config) library
to implement this and security options for SSL/TLS. Hostname verification is provided by the JDK
and used by Akka since Java 7, and on Java 6 the verification is implemented by ssl-config manually.

默认 `HttpsContext` 启用主机名验证。Akka HTTP 依赖 [Lightbend SSL-配置](https://lightbend.github.io/ssl-config) 库实现该功能和
SSL/TLS 的安全选项。Akka 从 Java 7 开始主机名验证由 JDK 提供，而在 Java 6 上验证由 ssl-config 手动实现。

For further recommended reading we would like to highlight the [fixing hostname verification blog post](https://tersesystems.com/2014/03/23/fixing-hostname-verification/) by blog post by Will Sargent.

有关进一步推荐阅读，我们想突出 Will Sargent 的这篇博客文章 [修复主机名验证的博客文章](https://tersesystems.com/2014/03/23/fixing-hostname-verification/)

### 服务器名字指示 (SNI)
**Server Name Indication (SNI)**

SNI is an TLS extension which aims to guard against man-in-the-middle attacks. It does so by having the client send the
name of the virtual domain it is expecting to talk to as part of the TLS handshake.

SNI 是一个 TLS 扩展，旨在防范中间人攻击。它通过让客户端发送它期望作为 TLS 握手的一部分与之通信的虚拟域的名字来做到。

It is specified as part of [RFC 6066](https://tools.ietf.org/html/rfc6066#page-6).

它被指定为 [RFC 6066](https://tools.ietf.org/html/rfc6066#page-6) 的一部分。

### Disabling TLS security features, at your own risk
**禁用 TLS 安全特性，风险自负**

@@@ warning

It is highly discouraged to disable any of the security features of TLS, however do acknowledge that workarounds may sometimes be needed.

禁用 TLS 的任何安全特性都是非常不鼓励的，但是请承认有时可能需要一些变通方法。

Before disabling any of the features one should consider if they may be solvable *within* the TLS world,
for example by [trusting a certificate](https://lightbend.github.io/ssl-config/WSQuickStart.html), or [configuring the trusted cipher suites](https://lightbend.github.io/ssl-config/CipherSuites.html).
There's also a very important section in the ssl-config docs titled [LooseSSL - Please read this before turning anything off!](https://lightbend.github.io/ssl-config/LooseSSL.html#please-read-this-before-turning-anything-off).

在禁用任何特性之前，应该考虑这些特性是否可以在 TLS 世界 *中* 解决，例如通过 [可信证书](https://lightbend.github.io/ssl-config/WSQuickStart.html)
或 [配置可信密码套件](https://lightbend.github.io/ssl-config/CipherSuites.html) 。
在 ssl-config 文档中还有非常重要的部分，标题为 [LooseSSL - 请在关闭任何东西之前阅读此内容](https://lightbend.github.io/ssl-config/LooseSSL.html#please-read-this-before-turning-anything-off)

If disabling features is indeed desired, we recommend doing so for *specific connections*,
instead of globally configuring it via `application.conf`.

如果确实需要禁用功能，建议对 *特定连接* 执行操作，而不是通过 `application.conf` 全局配置。
@@@

The following shows an example of disabling SNI for a given connection:

下面显示了为给定连接禁用 SNI 的例子：

Scala
:  @@snip [HttpsExamplesSpec.scala]($test$/scala/docs/http/scaladsl/HttpsExamplesSpec.scala) { #disable-sni-connection }

Java
:  @@snip [HttpsExamplesDocTest.java]($test$/java/docs/http/javadsl/HttpsExamplesDocTest.java) { #disable-sni-connection }

The `badSslConfig` is a copy of the default `AkkaSSLConfig` with the slightly changed configuration to disable SNI.
This value can be cached and used for connections which should indeed not use this feature.

`badSslConfig` 是默认 `AkkaSSLConfig` 的副本，它的配置略有更改以禁用 SNI。`badSslConfig` 可以被缓存，并用于不应用使用此特性的连接。