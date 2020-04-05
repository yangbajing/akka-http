# 可插拔的客户端传输 / HTTP(S) 代理支持
*Pluggable Client Transports / HTTP(S) proxy Support*

The client side infrastructure has support to plug different transport mechanisms underneath (the API may still change in the future). A client side
transport is represented by an instance of
@scala[@scaladoc[akka.http.scaladsl.ClientTransport](akka.http.scaladsl.ClientTransport)]@java[@javadoc[akka.http.javadsl.ClientTransport](akka.http.javadsl.ClientTransport)]:

客户端基础设施支持在下面插入的不同传输机制（该 API 在未来仍有可能改变）。客户端传输由
@scala[@scaladoc[akka.http.scaladsl.ClientTransport](akka.http.scaladsl.ClientTransport)]@java[@javadoc[akka.http.javadsl.ClientTransport](akka.http.javadsl.ClientTransport)] 的实例表示。

Scala
:  @@snip [ClientTransport.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/scaladsl/ClientTransport.scala) { #client-transport-definition }

Java
:  @@snip [ClientTransport.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/javadsl/ClientTransport.scala) { #client-transport-definition }

A transport implementation defines how the client infrastructure should communicate with a given host.

传输实现定义了客户端基础设施应该如何与指定主机通信。

@@@note

In our model, SSL/TLS runs on top of the client transport, even if you could theoretically see it as part of the
transport layer itself.

在我们的模型中，SSL/TLS 运行在客户端传输之上，即使理论上你可以将它看作为传输层本身的一部分。
@@@

## Configuring Client Transports
**配置客户端传输**

A @apidoc[ClientTransport] can be configured in the @apidoc[ClientConnectionSettings]. Right now, this is not possible
through config files but only by code. First, use `ClientConnectionSettings.withTransport` to configure a transport,
then use `ConnectionPoolSettings.withConnectionSettings`. @apidoc[ClientConnectionSettings] can be passed to all
client-side entry points in @scala[@apidoc[Http$]]@java[@apidoc[Http]].

可以在 @apidoc[ClientConnectionSettings] 中配置 @apidoc[ClientTransport] 。现在，不能通过配置文件而只能通过代码实现。首先，使用
`ClientConnectionSettings.withTransport` 来配置一个传输，然后使用 `ConnectionPoolSettings.withConnectionSettings`。
@apidoc[ClientConnectionSettings] 可以被传递到 @scala[@apidoc[Http$]]@java[@apidoc[Http]] 中的所有客户端入口点。 

## Predefined Transports
**预定义传输**

### TCP

The default transport is `ClientTransport.TCP` which simply opens a TCP connection to the target host.

默认传输是 `ClientTransport.TCP`，它简单打开到目标的 TCP 连接。

### HTTP(S) Proxy
**HTTP(S) 代理**

A transport that connects to target servers via an HTTP(S) proxy. An HTTP(S) proxy uses the HTTP `CONNECT` method (as
specified in [RFC 7231 Section 4.3.6](https://tools.ietf.org/html/rfc7231#section-4.3.6)) to create tunnels to target
servers. The proxy itself should transparently forward data to the target servers so that end-to-end encryption should
still work (if TLS breaks, then the proxy might be fussing with your data).

一个传输通过 HTTP(S) 代理连接到目标服务器。HTTP(S) 代理使用 HTTP `CONNECT` 方法
（在 [RFC 7231，4.3.6 节](https://tools.ietf.org/html/rfc7231#section-4.3.6) 中指定）创建到目标服务器的隧道。
代理本身应该透明的转发数据到目标服务器，因此端到端的加密应该仍然工作（如果 TLS 中断，那么代理可能干扰你的数据）。

This approach is commonly used to securely proxy requests to HTTPS endpoints. In theory it could also be used to proxy
requests targeting HTTP endpoints, but we have not yet found a proxy that in fact allows this.

这个方法常见用于安全的代理请求到 HTTPS 端点。理论上它也可用于代理针对 HTTP 端点的请求，但我们还没有找到一个（这样的）代理，实际上允许这样做。

Instantiate the HTTP(S) proxy transport using `ClientTransport.httpsProxy(proxyAddress)`.

使用 `ClientTransport.httpsProxy(proxyAddress)` 实例化 HTTP(S) 代理传输。

The proxy transport can also be setup using `ClientTransport.httpsProxy()` or `ClientTransport.httpsProxy(basicHttpCredentials)`
In order to defined the transport as such, you will need to set the proxy host / port in your `conf` file like the following.

可以使用 `ClientTransport.httpsProxy()` 或 `ClientTransport.httpsProxy(basicHttpCredentials)` 来设置代理传输。
为了像这样定义代理，你将需要在 `配置` 文件中设置代理主机/端口，就像下面：

```
akka.http.client.proxy {
 https {
   host = ""
   port = 443
 }
}
```

If host is left as `""` and you attempt to setup a httpsProxy transport, an exception will be thrown.

如果主机被保留为 `""` 并且尝试设置一个 httpsProxy 代理，将抛出一个异常。

<a id="use-https-proxy-with-http-singlerequest"></a>
### Use HTTP(S) proxy with @scala[`Http().singleRequest`]@java[`Http.get(...).singleRequest`]
**与 @scala[`Http().singleRequest`]@java[`Http.get(...).singleRequest`] 一起使用 HTTP(S) 代理**

To make use of an HTTP proxy when using the `singleRequest` API you simply need to configure the proxy and pass
the appropriate settings object when calling the single request method.

要在使用 `singleRequest` API 时使用 HTTP 代理，你只需要配置代理并在调用单请求方法时传递合适的设置对象。 

Scala
:  @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #https-proxy-example-single-request }

Java
:  @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #https-proxy-example-single-request }

### Use HTTP(S) proxy that requires authentication
**使用需要身份验证的 HTTP(S) 代理**

In order to use a HTTP(S) proxy that requires authentication, you need to provide @apidoc[HttpCredentials] that will be used
when making the CONNECT request to the proxy:

为了使用需要身份验证的 HTTP(S) 代理，你需要提供在向代理发起 CONNECT 请求时将使用的 @apidoc[HttpCredentials] 。

Scala
:  @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #auth-https-proxy-example-single-request }

Java
:  @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #auth-https-proxy-example-single-request }

### Use HTTP(S) proxy with @scala[Http().singleWebSocketRequest]@java[Http.get(...).singleWebSocketRequest]
**与 @scala[Http().singleWebSocketRequest]@java[Http.get(...).singleWebSocketRequest] 一起使用 HTTP(S) 代理**

Making use of an HTTP proxy when using the `singleWebSocketRequest` is done like using `singleRequest`, except you set `ClientConnectionSettings`
instead of `ConnectionPoolSettings`:

当使用 `singleWebSocketRequest` 时使用 HTTP 代理类似于使用 `singleRequest`，除了设置 `ClientConnectionSettings` 来替代 `ConnectionPoolSettings`：

Scala
:  @@snip [WebSocketClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/WebSocketClientExampleSpec.scala) { #https-proxy-singleWebSocket-request-example }

Java
:  @@snip [WebSocketClientExampleTest.java]($test$/java/docs/http/javadsl/WebSocketClientExampleTest.java) { #https-proxy-singleWebSocket-request-example }

### Use HTTP(S) proxy that requires authentication for Web Sockets
**为 Web Sockets 使用需要身份验证的 HTTP(S) 代理**

Here is an example for Web Socket:

这是一个 Web Socket 的例子：

Scala
:  @@snip [WebSocketClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/WebSocketClientExampleSpec.scala) { #auth-https-proxy-singleWebSocket-request-example }

Java
:  @@snip [WebSocketClientExampleTest.java]($test$/java/docs/http/javadsl/WebSocketClientExampleTest.java) { #auth-https-proxy-singleWebSocket-request-example }


## Implementing Custom Transports
**实现自定义传输**

Implement `ClientTransport.connectTo` to implement a custom client transport.

实现 `ClientTransport.connectTo` 来实现一个自定义客户端传输。

Here are some ideas for custom (or future predefined) transports:

 * SSH tunnel transport: connects to the target host through an SSH tunnel
 * Per-host configurable transport: allows choosing transports per target host

这是自定义（或者未来预制）传输的一些语音：

 - SSH 隧道传输：通过 SSH 隧道连接到目标主机
 - 每主机可配置传输：允许选择每个目标主机的传输