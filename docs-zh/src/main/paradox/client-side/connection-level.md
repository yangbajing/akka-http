# 连接级别客户端 API
*Connection-Level Client-Side API*

The connection-level API is the lowest-level client-side API Akka HTTP provides. It gives you full control over when
HTTP connections are opened and closed and how requests are to be send across which connection. As such it offers the
highest flexibility at the cost of providing the least convenience.

连接级别 API 是 Akka HTTP 提供的最低级别客户端 API。给你完全控制何时打开和关闭 HTTP 连接，以及如何通过哪个连接发送请求。
因此，它以提供最少的便利性为代价，提供了最高的灵活性。

@@@ note
It is recommended to first read the @ref[Implications of the streaming nature of Request/Response Entities](../implications-of-streaming-http-entity.md) section,
as it explains the underlying full-stack streaming concepts, which may be unexpected when coming
from a background with non-"streaming first" HTTP Clients.

推荐阅读 @ref[请求/响应实体流的实质含义](../implications-of-streaming-http-entity.md) 部分，它阐明了（Akka HTTP）底层的全栈流的概念。
因为对于没有“流式优先” HTTP 客户端概念背景的人来说，也许会感到难以理解。
@@@

## Opening HTTP Connections
**打开 HTTP 连接**

With the connection-level API you open a new HTTP connection to a target endpoint by materializing a @apidoc[Flow]
returned by the @scala[`Http().outgoingConnection(...)`]@java[`Http.get(system).outgoingConnection(...)`] method.
Here is an example:

使用连接级别 API，通过具体化 @scala[`Http().outgoingConnection(...)`]@java[`Http.get(system).outgoingConnection(...)`] 方法返回
的 @apidoc[Flow] 来打开到目标端点的新 HTTP 连接。这里是一个例子：

Scala
:  @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #outgoing-connection-example }

Java
:  @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #outgoing-connection-example }

Apart from the host name and port the @scala[`Http().outgoingConnection(...)`]@java[`Http.get(system).outgoingConnection(...)`]
method also allows you to specify socket options and a number of configuration settings for the connection.

除了主机名和端口， @scala[`Http().outgoingConnection(...)`]@java[`Http.get(system).outgoingConnection(...)`] 方法也允许对连接指定套接字选项和许多配置设置。

Note that no connection is attempted until the returned flow is actually materialized! If the flow is materialized
several times then several independent connections will be opened (one per materialization).
If the connection attempt fails, for whatever reason, the materialized flow will be immediately terminated with a
respective exception.

注意，直到返回的 flow 实际具体化之前，不会尝试连接！如果多次具体化 flow，那么多个独立的连接将被打开（每次具体化打开一个连接）。 
如果连接尝试，不管是什么原因，已具体化的流将以相应异常立即终止．

## Request-Response Cycle
**请求-响应周期**

Once the connection flow has been materialized it is ready to consume @apidoc[HttpRequest] instances from the source it is
attached to. Each request is sent across the connection and incoming responses dispatched to the downstream pipeline.
Of course and as always, back-pressure is adequately maintained across all parts of the
connection. This means that, if the downstream pipeline consuming the HTTP responses is slow, the request source will
eventually be slowed down in sending requests.

一时连接 flow 具体化完成，它准备好附着到源上从源消费 @apidoc[HttpRequest] 实例。每个请求通过连接发送，且传入响应被分发到下游管道线。
当然和往常一样，回压在连接的所有部分得到了充分保持。这意味着，如果下游管理线消费 HTTP 响应很慢，请求源将最终让发送请求减慢速度。

Any errors occurring on the underlying connection are surfaced as exceptions terminating the response stream (and
canceling the request source).

在底层连接上发生的任何错误都会作为终止响应流的异常（且取消请求源）。

## Closing Connections
**关闭连接**

Akka HTTP actively closes an established connection upon reception of a response containing `Connection: close` header.
The connection can also be closed by the server.

在收到的响应包含 `Connection: close` 头域时 Akka HTTP 主动关闭已建立连接。连接也可以被服务器关闭。

An application can actively trigger the closing of the connection by completing the request stream. In this case the
underlying TCP connection will be closed when the last pending response has been received.

应用程序可以通过完成请求流主动触发连接关闭。在这种情况下，当收到最后一个挂起的响应时，底层 TCP 连接将被关闭。

The connection will also be closed if the response entity is cancelled (e.g. by attaching it to `Sink.cancelled()`)
or consumed only partially (e.g. by using `take` combinator). In order to prevent this behaviour the entity should be
explicitly drained by attaching it to `Sink.ignore()`.

如果响应实体被取消，连接也将被关闭（例如：将实体附着到 `Sink.cancelled()`）或者只消费了部分（例如：通过使用 `take` 操作符）。
为了附上这种行为 *（译注：指忘记消费这种行为）*，实体应通过附着到 `Sink.ignore` 来明确地丢弃。 

## Timeouts
**超时**

Currently Akka HTTP doesn't implement client-side request timeout checking itself as this functionality can be regarded
as a more general purpose streaming infrastructure feature.

当前 Akka HTTP 自身未实例客户端请求超时检查，因为该功能可以看作是一个更通用的流基础设施功能。

It should be noted that Akka Streams provide various timeout functionality so any API that uses streams can benefit
from the stream stages such as `idleTimeout`, `backpressureTimeout`, `completionTimeout`, `initialTimeout`
and `throttle`. To learn more about these refer to their documentation in Akka Streams.

应注意到，Akka Streams 提供各种超时功能，因此，使用 Streams 的任何 API 都可以从流阶段受益，如：`idleTimeout`、`backpressureTimeout`、
`completionTimeout`、`initialTimeout` 和 `throttle`。

For more details about timeout support in Akka HTTP in general refer to @ref[Akka HTTP Timeouts](../common/timeouts.md).

关于 Akka HTTP 中超时支持的更多详细信息，参考 @ref[Akka HTTP 超时](../common/timeouts.md) 。

<a id="http-client-layer"></a>
## Stand-Alone HTTP Layer Usage
**独立的 HTTP 层使用方法**

Due to its Reactive-Streams-based nature the Akka HTTP layer is fully detachable from the underlying TCP
interface. While in most applications this "feature" will not be crucial it can be useful in certain cases to be able
to "run" the HTTP layer (and, potentially, higher-layers) against data that do not come from the network but rather
some other source. Potential scenarios where this might be useful include tests, debugging or low-level event-sourcing
(e.g by replaying network traffic).

由于基于反应式流，Akka HTTP 层与底层 TCP 接口是完全分离的。
虽然在大多数应用程序中这个“特性”并不重要，但在某些情况下，能够针对不是来自网络而是其他来源的数据“运行”HTTP层（以及更高的层）可能很有用。 
潜在场景可用于包括测试、调试或者低级事件源（例如，重放网络流量）。

On the client-side the stand-alone HTTP layer forms a `BidiStage` stage that "upgrades" a potentially encrypted raw connection to the HTTP level.
It is defined like this:

在客户端，独立的 HTTP 层形成一个 `BidiStage` 阶段，“升级”一个可能加密的原始连接到 HTTP 级别。它是这样定义的：

@@@ div { .group-scala }
@@snip [Http.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/scaladsl/Http.scala) { #client-layer }
@@@
@@@ div { .group-java }
```java
BidiFlow<HttpRequest, SslTlsOutbound, SslTlsInbound, HttpResponse, NotUsed>
```
@@@

You create an instance of @scala[`Http.ClientLayer`]@java[the layer] by calling one of the two overloads
of the @scala[`Http().clientLayer`]@java[`Http.get(system).clientLayer`] method,
which also allows for varying degrees of configuration.

通过调用 @scala[`Http().clientLayer`]@java[`Http.get(system).clientLayer`] 方法的两个重载之一，创建 `Http.ClientLayer` 实例。
该方法还允许不同程序的配置。
