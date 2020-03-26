<a id="timeouts"></a>
# 超时
*Timeouts*

Akka HTTP comes with a variety of built-in timeout mechanisms to protect your servers from malicious attacks or
programming mistakes. Some of these are simply configuration options (which may be overridden in code) while others
are left to the streaming APIs and are easily implementable as patterns in user-code directly.

Akka HTTP 带有各种内建的超时机制从恶意攻击或编程错误中保护你的服务器。其中一些只是简单的配置选项（可以在代码里覆盖），而其它的则在流式 API 里，
以容易实现的模式在用户代码里直接处理。

## Common timeouts
**通用超时**

<a id="idle-timeouts"></a>
### Idle timeouts
**空闲超时**

The `idle-timeout` is a global setting which sets the maximum inactivity time of a given connection.
In other words, if a connection is open but no request/response is being written to it for over `idle-timeout` time,
the connection will be automatically closed.

`idle-timeout` 是一个全局设置，它设置连接的最大不活动（闲置）时间。
换句话说，如果一个连接打开但没有请求/响应在超过 `idle-timeout` 时间内写入，这个连接将被自动关闭。

The setting works the same way for all connections, be it server-side or client-side, and it's configurable
independently for each of those using the following keys:

对于服务器端或客户端，这个设置对于所有连接的工作方式都是一样的，而且每个独立的环境通过使用下面的键值配置：

```
akka.http.server.idle-timeout
akka.http.client.idle-timeout
akka.http.host-connection-pool.idle-timeout
akka.http.host-connection-pool.client.idle-timeout
```

@@@ note
For the client side connection pool, the idle period is counted only when the pool has no pending requests waiting.

对于客户端的连接池，空闲时段的判断起始点是当连接池中已经没有需要处理的请求在等待写入为准。
@@@

## Server timeouts
**服务器超时**

<a id="request-timeout"></a>
### Request timeout
**请求超时**

Request timeouts are a mechanism that limits the maximum time it may take to produce an @apidoc[HttpResponse] from a route.
If that deadline is not met the server will automatically inject a Service Unavailable HTTP response and close the connection
to prevent it from leaking and staying around indefinitely (for example if by programming error a Future would never complete,
never sending the real response otherwise).

请求超时是限制在一个路由上生成 @apidoc[HttpResponse] 的最大时长的一种机制。如果超期还没有生成相关响应，服务器将自动流入一个服务不可大字 HTTP 响应或关闭连接，以防止（资源）泄漏和（连接）无限期的存在（例如：如果编程错误造成 Future 永远不会完成，真正的响应将永远不会发送）。

The default @apidoc[HttpResponse] that is written when a request timeout is exceeded looks like this:

当一个请求超时，默认 @apidoc[HttpResponse] 写入连接，看起来就像这样： 

@@snip [HttpServerBluePrint.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/impl/engine/server/HttpServerBluePrint.scala) { #default-request-timeout-httpresponse }

A default request timeout is applied globally to all routes and can be configured using the
`akka.http.server.request-timeout` setting (which defaults to 20 seconds).

默认请求超时应用于所有路由，它可以使用 `akka.http.server.request-timeout` 来设置（默认为 20 秒）。

The request timeout can be configured at run-time for a given route using the any of the @ref[TimeoutDirectives](../routing-dsl/directives/timeout-directives/index.md).

请求超时可以在运行时配置，使用 @ref[TimeoutDirectives](../routing-dsl/directives/timeout-directives/index.md) 的任何指令为特定路由进行配置。

### Bind timeout
**绑定超时**

The bind timeout is the time period within which the TCP binding process must be completed (using any of the `Http().bind*` methods).
It can be configured using the `akka.http.server.bind-timeout` setting.

绑定超时是 TCP 绑定过程必须完成的时间段（使用任何 `Http().bind*` 方法）。
它可以使用 `akka.http.server.bind-timeout` 设置进行配置。

### Linger timeout
**Linger 超时**

The linger timeout is the time period the HTTP server implementation will keep a connection open after
all data has been delivered to the network layer. This setting is similar to the SO_LINGER socket option
but does not only include the OS-level socket but also covers the Akka IO / Akka Streams network stack.
The setting is an extra precaution that prevents clients from keeping open a connection that is
already considered completed from the server side.

linger 超时是 HTTP 服务器实现的在所有数据抵达网络层后保持连接打开的时间段。这个设置类似 SO_LINGER 套接字选项，但是它不只包含在 OS-level 的套接字上，而且也覆盖到 Akka IO / Akka Streams 网络层。该设置是一项客户的预防措施，防止客户端保持打开已经从服务器端完成的连接。 

If the network level buffers (including the Akka Stream / Akka IO networking stack buffers)
contains more data than can be transferred to the client in the given time when the server-side considers
to be finished with this connection, the client may encounter a connection reset.

如果网络层缓冲（包括 Akka Stream / Akka IO 网络栈缓冲）包含的数据比服务器端认为已完成这个连接时在给定时间里可以传输到客户端的更多，则客户端可能会遇到连接重置。 

Set to `infinite` to disable automatic connection closure (which will risk to leak connections).

设置为 `infinite` 来禁止自动连接关闭（这样将会有连接泄漏的风险）。

## Client timeouts
**客户端超时**

### Connecting timeout
**连接超时**

The connecting timeout is the time period within which the TCP connecting process must be completed.
Tweaking it should rarely be required, but it allows erroring out the connection in case a connection
is unable to be established for a given amount of time.

连接超时是 TCP 连接必须完成的时间段。很少需要调整它，但是它允许在给定的时间内连接无法建立时发出错误。

it can be configured using the `akka.http.client.connecting-timeout` setting.

它可以使用 `akka.http.client.connecting-timeout` 设置配置。

### Connection Lifetime timeout
**连接生存期超时**

This timeout configures a maximum amount of time, while the connection can be kept open. This is useful, when you reach
the server through a load balancer and client reconnecting helps the process of rebalancing between service instances.

这个超时配置连接能保持打开的最长时间。这很有用，当你通过负载均衡到达服务器时，客户端重新连接有助于在服务实例之间重新平衡。

It can be configured using the `akka.http.host-connection-pool.max-connection-lifetime` setting.

它可以通过使用 `akka.http.host-connection-pool.max-connection-lifetime` 设置配置。
