# 服务器 HTTPS 支持
**Server HTTPS Support**

Akka HTTP supports TLS encryption on the server-side as well as on the @ref[client-side](../client-side/client-https-support.md).

Akka HTTP 支持在服务器端以及 @ref[客户端](../client-side/client-https-support.md)  上的 TLS 加密。

The central vehicle for configuring encryption is the @apidoc[HttpsConnectionContext], which can be created using
the static method `ConnectionContext.https` which is defined like this:

配置加密的主要工具是 @apidoc[HttpsConnectionContext] ，它通过使用静态方法 `ConnectionContext.https` 创建，定义看起来像这样：

Scala
:  @@snip [ConnectionContext.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/scaladsl/ConnectionContext.scala) { #https-context-creation }

Java
:  @@snip [ConnectionContext.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/javadsl/ConnectionContext.scala) { #https-context-creation }

On the server-side the `bind`, and `bindAndHandleXXX` methods of the @scala[@scaladoc[Http](akka.http.scaladsl.Http$)]@java[@javadoc[Http](akka.http.javadsl.Http)] extension define an
optional `httpsContext` parameter, which can receive the HTTPS configuration in the form of an `HttpsContext`
instance.
If defined encryption is enabled on all accepted connections. Otherwise it is disabled (which is the default).

在 @scala[@scaladoc[Http](akka.http.scaladsl.Http$)]@java[@javadoc[Http](akka.http.javadsl.Http)] 扩展上的 `bind` 和 `bindAndHandleXXX` 方法定义了一个可选 `httpsContext` 参数，它可以以一个 `HttpsContext` 实例的形式接收 HTTPS 配置。

For detailed documentation for client-side HTTPS support refer to @ref[Client-Side HTTPS Support](../client-side/client-https-support.md).

对于客户端 HTTPS 支持的详细文件参考 @ref[客户端 HTTPS 支持](../client-side/client-https-support.md) 。

## Obtaining SSL/TLS Certificates
**获得 SSL/TLS 证书**

In order to run an HTTPS server a certificate has to be provided, which usually is either obtained from a signing
authority or created by yourself for local or staging environment purposes.

为了运行 HTTPS 服务器，必须提供证书。证书通常从签名机构获得，或者用于本地或临时环境目的自己创建。

Signing authorities often provide instructions on how to create a Java keystore (typically with reference to Tomcat
configuration). If you want to generate your own certificates, the official Oracle documentation on how to generate
keystores using the JDK keytool utility can be found [here](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html).

签名机构通常提供怎样创建一个 Java 密钥库的说明（通常参考 Tomcat 配置）。
如果你想生成自己的证书，怎样使用 JDK keytool 工具生成密钥库可以在 [这里](https://docs.oracle.com/javase/8/docs/technotes/tools/unix/keytool.html) 找到 Oracle 官方文档。

SSL-Config provides a more targeted guide on generating certificates, so we recommend you start with the guide
titled [Generating X.509 Certificates](https://lightbend.github.io/ssl-config/CertificateGeneration.html).

因为 SSL-Config 提供生成证书的更多目录指南，所以我们推荐从指南 [生成 X.509 证书](https://lightbend.github.io/ssl-config/CertificateGeneration.html) 开始。

<a id="using-https"></a>
## Using HTTPS
**使用 HTTPS**

Once you have obtained the server certificate, using it is as simple as preparing an @apidoc[HttpsConnectionContext]
and either setting it as the default one to be used by all servers started by the given @scala[@scaladoc[Http](akka.http.scaladsl.Http$)]@java[@javadoc[Http](akka.http.javadsl.Http)] extension
or passing it in explicitly when binding the server.

一旦你获得了服务器证书，使用它就像 @apidoc[HttpsConnectionContext] 一样简单。设置它作为默认 HTTPS，被用于通过 @scala[@scaladoc[Http](akka.http.scaladsl.Http$)]@java[@javadoc[Http](akka.http.javadsl.Http)] 扩展启动的所有服务器，或者在绑定服务器时显示传递。

The below example shows how setting up HTTPS works.
First, you create and configure an instance of @apidoc[HttpsConnectionContext] :

下面示例演示怎样设置 HTTPS 。首先，创建并配置一个 @apidoc[HttpsConnectionContext] 的实例：

Scala
:  @@snip [HttpsServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/server/HttpsServerExampleSpec.scala) { #imports #low-level-default }

Java
:  @@snip [SimpleServerApp.java]($akka-http$/akka-http-tests/src/main/java/akka/http/javadsl/server/examples/simple/SimpleServerApp.java) { #https-http-config }

@scala[Once you configured the HTTPS context, you can set it as default:]
@java[Then pass it to the `akka.http.javadsl.Http` class's `setDefaultServerHttpContext` method, like in the below `main` method.]

@scala[一旦你配置了 HTTPS 上下文，你可以设置它为默认的：]
@java[把它传到 `akka.http.javadsl.Http` 类的 `setDefaultServerHttpContext` 方法，像下面 `main` 方法里面做的一样。]

Scala
:  @@snip [HttpsServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/server/HttpsServerExampleSpec.scala) { #set-low-level-context-default }

Java
: @@snip [SimpleServerApp.java]($akka-http$/akka-http-tests/src/main/java/akka/http/javadsl/server/examples/simple/SimpleServerApp.java) { #https-http-app }

@@@ div { .group-scala }

It is also possible to pass in the context to specific `bind...` (or client) calls, like displayed below:

也可以在上下文中传递到特定的 `bind...` （或客户端）调用，像下面显示的一样：

@@snip [HttpsServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/server/HttpsServerExampleSpec.scala) { #bind-low-level-context }

@@@

## Running both HTTP and HTTPS
**同时运行 HTTP 和 HTTPS**

If you want to run HTTP and HTTPS servers in a single application, you can call `bind...` methods twice,
one for HTTPS, and the other for HTTP.

如果你想在单个程序里同时运行 HTTP 和 HTTPS 服务器，你可以调用 `bind...` 方法两次，一个用于 HTTPS，另一个用于 HTTP。

When configuring HTTPS, you can do it up like explained in the above [Using HTTPS](#using-https) section,

配置 HTTPS 时，你可以像上面 [使用 HTTPS](#using-https) 部分描述的那样进行配置，

Scala
:  @@snip [HttpsServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/server/HttpsServerExampleSpec.scala) { #low-level-default }

Java
:  @@snip [SimpleServerApp.java]($akka-http$/akka-http-tests/src/main/java/akka/http/javadsl/server/examples/simple/SimpleServerApp.java) { #https-http-config }

Then, call `bind...` methods twice like below.
@scala[The passed `https` context is from the above code snippet.]
@java[`SimpleServerApp.useHttps(system)` is calling the above defined `public static HttpsConnectionContext useHttps(ActorSystem system)` method.]

然后，启用 `bind...` 方法两次，如下所示。
@scala[传递的 `https` 来自上面的代码片段。]
@java[`SimpleServerApp.useHttps(system)` 调用上面定义的 `public static HttpsConnectionContext useHttps(ActorSystem system)` 方法。]

Scala
:  @@snip [HttpsServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/server/HttpsServerExampleSpec.scala) { #both-https-and-http }

Java
:  @@snip [SimpleServerHttpHttpsApp.java]($akka-http$/akka-http-tests/src/main/java/akka/http/javadsl/server/examples/simple/SimpleServerHttpHttpsApp.java) { #both-https-and-http }

## Mutual authentication
**相互认证**

To require clients to authenticate themselves when connecting, pass in @scala[`Some(TLSClientAuth.Need)`]@java[`Optional.of(TLSClientAuth.need)`] as the `clientAuth` parameter of the
@apidoc[HttpsConnectionContext]
and make sure the truststore is populated accordingly. For further (custom) certificate checks you can use the
@scala[@scaladoc[`Tls-Session-Info`](akka.http.scaladsl.model.headers.Tls$minusSession$minusInfo)]@java[@javadoc[`TlsSessionInfo`](akka.http.javadsl.model.headers.TlsSessionInfo)] synthetic header.

在进行连接时要求客户端认证身份，传递 @scala[`Some(TLSClientAuth.Need)`]@java[`Optional.of(TLSClientAuth.need)`] 作为 @apidoc[HttpsConnectionContext] 的 `clientAuth` 参数，并确保信任库被相应地填充。
为了进一步（自定义）证书检查，你可以使用 @scala[@scaladoc[`Tls-Session-Info`](akka.http.scaladsl.model.headers.Tls$minusSession$minusInfo)]@java[@javadoc[`TlsSessionInfo`](akka.http.javadsl.model.headers.TlsSessionInfo)] 合成头。

At this point dynamic renegotiation of the certificates to be used is not implemented. For details see [issue #18351](https://github.com/akka/akka/issues/18351)
and some preliminary work in [PR #19787](https://github.com/akka/akka/pull/19787).

此时，证书的动态重新协商还没有实现。有关详细信息见 [问题 #18351](https://github.com/akka/akka/issues/18351)
和 [PR #19787](https://github.com/akka/akka/pull/19787) 里的一先初步工作。

## Further reading
**进一步阅读**

The topic of properly configuring HTTPS for your web server is an always changing one,
thus we recommend staying up to date with various security breach news and of course
keep your JVM at the latest version possible, as the default settings are often updated by
Oracle in reaction to various security updates and known issues.

为你的 WEB 服务器正确配置 HTTPS 是一个不断变化的主题，因此我们建议随时了解各种安全漏洞的最新消息，当然还应尽可能保持你的 JVM 为最新版本，
作为默认设置，因为默认设置经常被 Oracle 更新，以对大量安全更新和已经问题作出反应。 

We also recommend having a look at the [Play documentation about securing your app](https://www.playframework.com/documentation/2.5.x/ConfiguringHttps#ssl-certificates),
as well as the techniques described in the Play documentation about setting up a [reverse proxy to terminate TLS in
front of your application](https://www.playframework.com/documentation/2.5.x/HTTPServer) instead of terminating TLS inside the JVM, and therefore Akka HTTP, itself.

我们还建议看一看 [关于保护你的应用程序的 Play 文档](https://www.playframework.com/documentation/2.5.x/ConfiguringHttps#ssl-certificates)，
以及在 Play 文档里关于设置 [在你的应用程序前面反向代理终止 TLS](https://www.playframework.com/documentation/2.5.x/HTTPServer) 描述的技术来替代在 JVM 内部终止 TLS。

*译注：意思就是使用专门的 HTTP 服务器（如 Nginx、Apache 等）来管理 TLS（HTTPS）。*

Other excellent articles on the subject:

其它优秀文章：

 * [Oracle Java SE 8: Creating a Keystore using JSSE](https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#CreateKeystore)
 * [Java PKI Programmer's Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/security/certpath/CertPathProgGuide.html)
 * [Fixing X.509 Certificates](https://tersesystems.com/2014/03/20/fixing-x509-certificates/)
