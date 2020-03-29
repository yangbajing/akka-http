# 核心服务器 API
*Core Server API*

The core Server API is scoped with a clear focus on the essential functionality of an HTTP/1.1 server:

核心服务器 API 的范围很明确，专注于 HTTP/1.1 的基本功能：

 * Connection management
 * Parsing and rendering of messages and headers
 * Timeout management (for requests and connections)
 * Response ordering (for transparent pipelining support)
 
 - 连接管理
 - 消息和头域的解析与渲染
 - 超时管理（用于请求和连接）
 - 响应顺序（用于透明的管道线支持，*译注：在同一个连接上不用等待响应到达既可发起下一个请求*）

All non-core features of typical HTTP servers (like request routing, file serving, compression, etc.) are left to
the @ref[higher layers](../routing-dsl/index.md), they are not implemented by the `akka-http-core`-level server itself.
Apart from general focus this design keeps the server core small and light-weight as well as easy to understand and
maintain.

HTTP 服务器的所有非核发特性（比如：请求路由、文件服务、压缩，等等）都移到 @ref[高级层](../routing-dsl/index.md) ，它们没有在 `akka-http-core` 层实现。
除了一般的关注点，这种设计保持了服务器核心更小、轻量，以及更易于理解和维护。

@@@ note
It is recommended to read the @ref[Implications of the streaming nature of Request/Response Entities](../implications-of-streaming-http-entity.md) section,
as it explains the underlying full-stack streaming concepts, which may be unexpected when coming
from a background with non-"streaming first" HTTP Servers.

推荐阅读 @ref[请求/响应实体流的实质含义](../implications-of-streaming-http-entity.md) 部分，它阐明了底层的全栈流的概念。
因为一个来自没有“流式优化” HTTP 服务器概念背景的人可能会感到难以理解。
@@@

## Streams and HTTP
**流和 HTTP**

The Akka HTTP server is implemented on top of @scala[@extref[Streams](akka-docs:scala/stream/index.html)]@java[@extref[Streams](akka-docs:java/stream/index.html)] and makes heavy use of it - in its
implementation as well as on all levels of its API.

Akka HTTP 服务器在 @scala[@extref[流](akka-docs:scala/stream/index.html)]@java[@extref[流](akka-docs:java/stream/index.html)] 之上实现，并且大量使用流 - 在低级 API 的实现以及它之上的所有层级。

On the connection level Akka HTTP offers basically the same kind of interface as @scala[@extref[Working with streaming IO](akka-docs:scala/stream/stream-io.html)]@java[@extref[Working with streaming IO](akka-docs:java/stream/stream-io.html)]:
A socket binding is represented as a stream of incoming connections. The application pulls connections from this stream
source and, for each of them, provides a @apidoc[Flow[HttpRequest, HttpResponse, \_]] to "translate" requests into responses.

在连接层，Akka HTTP 提供了与 @scala[@extref[处理流式 IO](akka-docs:scala/stream/stream-io.html)]@java[@extref[处理流式 IO](akka-docs:java/stream/stream-io.html)] 基本一样的接口：套接字绑定被表示进入连接的流。
应用程序从这个 Source 流中获取连接，并为每一个连接提供 @apidoc[Flow[HttpRequest, HttpResponse, \_]] 来“转译”请求到响应。

Apart from regarding a socket bound on the server-side as a @apidoc[Source[IncomingConnection, \_]] and each connection as a
@apidoc[Source[HttpRequest, \_]] with a @apidoc[Sink[HttpResponse, \_]] the stream abstraction is also present inside a single HTTP
message: The entities of HTTP requests and responses are generally modeled as a @apidoc[Source[ByteString, \_]]. See also
the @ref[HTTP Model](../common/http-model.md) for more information on how HTTP messages are represented in Akka HTTP.

此外，服务器端的套接字绑定成为 @apidoc[Source[IncomingConnection, \_]] ，
每个连接的 @apidoc[Source[HttpRequest, \_]] 、 @apidoc[Sink[HttpResponse, \_]] 流抽象也在单个 HTTP 消息里：
HTTP 请求和响应的实体建模为一个 @apidoc[Source[ByteString, \_]] 。
对于在 Akka HTTP 里怎样表示 HTTP 消息的更多信息，请见 @ref[HTTP Model](../common/http-model.md) 。

## Starting and Stopping
**启动和停止**

On the most basic level an Akka HTTP server is bound by invoking the `bind` method of the @scala[@scaladoc[akka.http.scaladsl.Http](akka.http.scaladsl.Http$)]@java[@javadoc[akka.http.javadsl.Http](akka.http.javadsl.Http)]
extension:

在基本层面上，Akka HTTP 服务器通过 @scala[@scaladoc[akka.http.scaladsl.Http](akka.http.scaladsl.Http$)]@java[@javadoc[akka.http.javadsl.Http](akka.http.javadsl.Http)] 扩展上的 `bind` 方法绑定：

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #binding-example }

Java
:   @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #binding-example }

Arguments to the `Http().bind` method specify the interface and port to bind to and register interest in handling
incoming HTTP connections. Additionally, the method also allows for the definition of socket options as well as a larger
number of settings for configuring the server according to your needs.

`Http().bind` 方法的参数指定要绑定的 interface 和 port ，及注册感兴趣的传入 HTTP 连接处理。此外，该方法也允许定义套接字选项以及根据需要配置服务器的大量设置。

The result of the `bind` method is a @apidoc[Source[Http.IncomingConnection]] which must be drained by the application in
order to accept incoming connections.
The actual binding is not performed before this source is materialized as part of a processing pipeline. In
case the bind fails (e.g. because the port is already busy) the materialized stream will immediately be terminated with
a respective exception.
The binding is released (i.e. the underlying socket unbound) when the subscriber of the incoming
connection source has cancelled its subscription. Alternatively one can use the `unbind()` method of the
`Http.ServerBinding` instance that is created as part of the connection source's materialization process.
The `Http.ServerBinding` also provides a way to get a hold of the actual local address of the bound socket, which is
useful for example when binding to port zero (and thus letting the OS pick an available port).

`bind` 方法的结果是一个 @apidoc[Source[Http.IncomingConnection]] ，为了接受（accept）传入的连接，应用程序必须排空这个 Source 。
在这个 Source 作为处理管道的一部分被物化（实例化）之前，实际的绑定不会执行。在这里，绑定失败（例如：因为端口繁忙），物化流将以相应的异常立即终止。
当传入连接源的订阅者取消订阅时，绑定被释放（例如：底层的套接子已解绑）。
或者，用户使用 `Http.ServerBinding` 实例的 `unbind` 方法，该实例是作为连接源的物化过程的一部分创建的。
`Http.ServerBinding` 也提供了方法获得已绑定套接字的真实的本地地址，这是有用的，比如当绑定到端口 0 时（让系统挑选一个可用的端口）。

<a id="http-low-level-server-side-example"></a>
## Request-Response Cycle
**请求-响应周期**

When a new connection has been accepted it will be published as an `Http.IncomingConnection` which consists
of the remote address and methods to provide a @apidoc[Flow[HttpRequest, HttpResponse, \_]] to handle requests coming in over
this connection.

当一个新的连接被接受，它将被发布为一个 `Http.IncomingConnection`。`Http.IncomingConnection` 包含远程地址，并且提供了在这个连接上处理 @apidoc[Flow[HttpRequest, HttpResponse, \_]] 的方法。

Requests are handled by calling one of the `handleWithXXX` methods with a handler, which can either be

>
 * a @apidoc[Flow[HttpRequest, HttpResponse, \_]] for `handleWith`,
 * a function @scala[`HttpRequest => HttpResponse`]@java[`Function<HttpRequest, HttpResponse>`] for `handleWithSyncHandler`,
 * a function @scala[`HttpRequest => Future[HttpResponse]`]@java[`Function<HttpRequest, CompletionStage<HttpResponse>>`] for `handleWithAsyncHandler`.

通过调用 `handleWithXXX` 方法的其中一个来处理请求，该方法可以是

>
 * @apidoc[Flow[HttpRequest, HttpResponse, \_]] 用于 `handleWith` 方法，
 * 函数 @scala[`HttpRequest => HttpResponse`]@java[`Function<HttpRequest, HttpResponse>`] 用于 `handleWithSyncHandler` 方法，
 * 函数 @scala[`HttpRequest => Future[HttpResponse]`]@java[`Function<HttpRequest, CompletionStage<HttpResponse>>`] 用于 `handleWithAsyncHandler` 方法。

Here is a complete example:

完整示例在这里：

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #full-server-example }

Java
:   @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #full-server-example }

In this example, a request is handled by transforming the request stream with a function @scala[`HttpRequest => HttpResponse`]@java[`Function<HttpRequest, HttpResponse>`]
using `handleWithSyncHandler` (or equivalently, Akka Stream's `map` operator). Depending on the use case many
other ways of providing a request handler are conceivable using Akka Stream's combinators.
If the application provides a @apidoc[Flow] it is also the responsibility of the application to generate exactly one response
for every request and that the ordering of responses matches the ordering of the associated requests (which is relevant
if HTTP pipelining is enabled where processing of multiple incoming requests may overlap). When relying on
`handleWithSyncHandler` or `handleWithAsyncHandler`, or the `map` or `mapAsync` stream operators, this
requirement will be automatically fulfilled.

在这个示例，使用 `handleWithSyncHandler` 方法（等价于 Akka 流的 `map` 操作符）和函数 @scala[`HttpRequest => HttpResponse`]@java[`Function<HttpRequest, HttpResponse>`] 来转换请求流。
根据使用情况，请求处理程序可以使用 Akka 流的组合子提供的许多其它方法。
如果应用程序提供了一个 @apidoc[Flow]，那么应用程序也有责任为每个请求生成响应，并且响应的顺序与相关请求的顺序匹配
（如果 HTTP 管道线被启用，这是有意义的。否则多个传入请求的处理可能重叠）。
当依赖 `handleWithSyncHandler` 或 `handleWithAsyncHandler` 又或者 `map` 或 `mapAsync` 流操作符时，这个要求将自动满足（*译注：指响应的顺序与相关请求的顺序匹配*）。

See @ref[Routing DSL Overview](../routing-dsl/overview.md) for a more convenient high-level DSL to create request handlers.

有关创建请求处理程序的便捷高级 DSL，见 @ref[路由 DSL 概述](../routing-dsl/overview.md) 。

### Streaming Request/Response Entities
**流式请求/响应实体**

Streaming of HTTP message entities is supported through subclasses of @apidoc[HttpEntity]. The application needs to be able
to deal with streamed entities when receiving a request as well as, in many cases, when constructing responses.
See @ref[HttpEntity](../common/http-model.md#httpentity) for a description of the alternatives.

通过 @apidoc[HttpEntity] 的子类支持HTTP 消息实体的流。当收到一个请求时，应用程序需要能够处理流式的实体。许多情况下，构造响应时也需要。
有关替代方案的说明，见 @ref[HttpEntity](../common/http-model.md#httpentity) 。 

If you rely on the @ref[Marshalling](../common/marshalling.md) and/or @ref[Unmarshalling](../common/unmarshalling.md) facilities provided by
Akka HTTP then the conversion of custom types to and from streamed entities can be quite convenient.

如果你依赖 Akka HTTP 提供的 @ref[编组](../common/marshalling.md) 和/或 @ref[解组](../common/unmarshalling.md) 设施。
那么自定义类型转换到流式实体或相反可能会很方便。

<a id="http-closing-connection-low-level"></a>
### Closing a connection
**关闭一个连接**

The HTTP connection will be closed when the handling @apidoc[Flow] cancels its upstream subscription or the peer closes the
connection. An often times more convenient alternative is to explicitly add a `Connection: close` header to an
@apidoc[HttpResponse]. This response will then be the last one on the connection and the server will actively close the
connection when it has been sent out.

当处理 @apidoc[Flow] 取消它的上游订阅或对端关闭连接（这里指客户端主动关闭连接）时，HTTP 连接将被关闭。
通常，更便捷的选择是显示添加一个 `Connection: close` 头到 @apidoc[HttpResponse] 。
响应将会是连接的最后一个，当响应已发出后服务器将主动关闭连接。

Connection will also be closed if request entity has been cancelled (e.g. by attaching it to `Sink.cancelled()`
or consumed only partially (e.g. by using `take` combinator). In order to prevent this behaviour entity should be
explicitly drained by attaching it to `Sink.ignore()`.

如果请求实体已被取消（例如：请求被连接到 `Sink.cancelled()` 或者只消费了部分数据，比如通过 `take` 组合子），连接也将关闭。

## Configuring Server-side HTTPS
**配置服务器端 HTTPS**

For detailed documentation about configuring and using HTTPS on the server-side refer to @ref[Server-Side HTTPS Support](server-https-support.md).

服务器端配置和使用 HTTPS 的详细文档请参考 @ref[服务器端 HTTPS 支持](server-https-support.md) 。

<a id="http-server-layer"></a>
## Stand-Alone HTTP Layer Usage
**独立 HTTP 层的使用方法**

Due to its Reactive-Streams-based nature the Akka HTTP layer is fully detachable from the underlying TCP
interface. While in most applications this "feature" will not be crucial it can be useful in certain cases to be able
to "run" the HTTP layer (and, potentially, higher-layers) against data that do not come from the network but rather
some other source. Potential scenarios where this might be useful include tests, debugging or low-level event-sourcing
(e.g by replaying network traffic).

由于基于反应式流，Akka HTTP 层与底层 TCP 接口是分离的。
虽然在大多数应用程序中这个“特性”并不重要，但在某些情况下，能够针对不是来自网络而是其他来源的数据“运行”HTTP层（以及更高的层）可能很有用。 
潜在场景可用于包括测试、调度或者底级事件法（重放网络流量）。

@@@ div { .group-scala }
On the server-side the stand-alone HTTP layer forms a @apidoc[BidiFlow] that is defined like this:

在服务器端，标准 HTTP 层结构 @apidoc[BidiFlow] 定义像这样：

@@snip [Http.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/scaladsl/Http.scala) { #server-layer }

You create an instance of `Http.ServerLayer` by calling one of the two overloads of the `Http().serverLayer` method,
which also allows for varying degrees of configuration.

通过调用 `Http().serverLayer` 的两个重载方法之一创建一个 `Http.ServerLayer` 实例，并对其进行不同程度的配置。
@@@
@@@ div { .group-java }
On the server-side the stand-alone HTTP layer forms a @apidoc[BidiFlow[HttpResponse, SslTlsOutbound, SslTlsInbound, HttpRequest, NotUsed]],
that is a stage that "upgrades" a potentially encrypted raw connection to the HTTP level.

You create an instance of the layer by calling one of the two overloads of the `Http.get(system).serverLayer` method,
which also allows for varying degrees of configuration. Note, that the returned instance is not reusable and can only
be materialized once.
@@@

## Controlling server parallelism
**控制服务器并行性**

Request handling can be parallelized on two axes, by handling several connections in parallel and by
relying on HTTP pipelining to send several requests on one connection without waiting for a response first. In both
cases the client controls the number of ongoing requests. To prevent being overloaded by too many requests, Akka HTTP
can limit the number of requests it handles in parallel.

请求处理可以在两个轴并行化，并行处理大量连接和依靠 HTTP 管道线在一个连接上发送多个请求（不用等待第一个响应）。这两种情况下，由客户端控制正在进行的请求数量。
为防止太多的请求超过负载，Akka HTTP 可以限制并行处理的请求数。

To limit the number of simultaneously open connections, use the `akka.http.server.max-connections` setting. This setting
applies to all of `Http.bindAndHandle*` methods. If you use `Http.bind`, incoming connections are represented by
a @apidoc[Source[IncomingConnection, ...]]. Use Akka Stream's combinators to apply backpressure to control the flow of
incoming connections, e.g. by using `throttle` or `mapAsync`.

要限制同时打开的连接数，使用 `akka.http.server.max-connections` 设置。该设置应用到所有 `Http.bindAndHandle*` 方法。
如果你使用 `Http.bind`，进入连接表示为 @apidoc[Source[IncomingConnection, ...]] 。使用 Akka 流的组合子应用回压来控制进入连接的流量，
例如：使用 `throttle` 或 `mapAsync` 。

HTTP pipelining is generally discouraged (and [disabled by most browsers](https://en.wikipedia.org/w/index.php?title=HTTP_pipelining&oldid=700966692#Implementation_in_web_browsers)) but
is nevertheless fully supported in Akka HTTP. The limit is applied on two levels. First, there's the
`akka.http.server.pipelining-limit` config setting which prevents that more than the given number of outstanding requests
is ever given to the user-supplied handler-flow. On the other hand, the handler flow itself can apply any kind of throttling
itself. If you use the `Http.bindAndHandleAsync`
entry-point, you can specify the `parallelism` argument (which defaults to `1`, which means that pipelining is disabled) to control the
number of concurrent requests per connection. If you use `Http.bindAndHandle` or `Http.bind`, the user-supplied handler
flow has full control over how many request it accepts simultaneously by applying backpressure. In this case, you can
e.g. use Akka Stream's `mapAsync` combinator with a given parallelism to limit the number of concurrently handled requests.
Effectively, the more constraining one of these two measures, config setting and manual flow shaping, will determine
how parallel requests on one connection are handled.

HTTP 管道线通常不鼓励使用（且 [多数浏览器禁用](https://en.wikipedia.org/w/index.php?title=HTTP_pipelining&oldid=700966692#Implementation_in_web_browsers)），但是 Akka HTTP 完整支持这个功能。限制被应用到两层。
第一，`akka.http.server.pipelining-limit` 配置设置，防止超过指定数量的未完成请求到用户提供的处理流。
另一方面，处理流程可以向自己应用任何一种节流。
如果你使用 `Http.bindAndHandleAsync` 为进入点，可以指定 `parallelism` 参数（默认为 `1`，这意味着管道线被禁用）控制每个连接的并发请求数。
如果你使用 `Http.bindAndHandle` 或 `Http.bind`，用户提供的处理流程通过应用回压完全控制它同时接受多少请求。
这种情况下，你可以使用 Akka 流的 `mapAsync` 组合子和给定的并行性来限制并发处理请求的数量。
事实上，这两种机制（配置和手动流程）里更多约束的那个，将决定怎样在一个连接上处理并行请求。 

<a id="handling-http-server-failures-low-level"></a>
## Handling HTTP Server failures in the Low-Level API
**在纸级 API 里处理 HTTP 服务器失败**

There are various situations when failure may occur while initialising or running an Akka HTTP server.
Akka by default will log all these failures, however sometimes one may want to react to failures in addition to them
just being logged, for example by shutting down the actor system, or notifying some external monitoring end-point explicitly.

当初始化和运行 Akka HTTP 服务器时，各种失败都可能发行。Akka 默认将记录所有失败，但是，有时也想在记录日志这外对失败作出反应。
比如关闭 actor 系统，或者显示的通知外部监视器端点。

There are multiple things that can fail when creating and materializing an HTTP Server (similarly, the same applied to
a plain streaming `Tcp()` server). The types of failures that can happen on different layers of the stack, starting
from being unable to start the server, and ending with failing to unmarshal an HttpRequest, examples of failures include
(from outer-most, to inner-most):

当创建并物化一个 HTTP 服务器后有很多事情可能失败（类似地，普通流式的 `Tcp()` 服务器也一样）。失败类型可能发生在栈的不同层上，
从不能启动服务器开始，到解组一个 `HttpRequest` 失败为止，失败的示例包括（从外部，到内部）：

 * Failure to `bind` to the specified address/port,
 * Failure while accepting new `IncomingConnection`s, for example when the OS has run out of file descriptors or memory,
 * Failure while handling a connection, for example if the incoming @apidoc[HttpRequest] is malformed.
 
 - `bind` 到指定地址/端口失败，
 - 接受新的 `IncomingConnection` 失败，比如当系统没有文件描述符或内存耗尽时，
 - 处理一个连接失败，比如传入的 @apidoc[HttpRequest] 是畸形的（无效）。

This section describes how to handle each failure situation, and in which situations these failures may occur.

这个部分描述了每种失败情况怎样处理，以及何种情况下这些失败可能发生。

#### Bind failures
**绑定失败**

The first type of failure is when the server is unable to bind to the given port. For example when the port
is already taken by another application, or if the port is privileged (i.e. only usable by `root`).
In this case the "binding future" will fail immediately, and we can react to it by listening on the @scala[Future's]@java[CompletionStage’s] completion:

第一类失败是服务器无法绑定到指定的端口。比如当端口已经用于另一个应用程序，或者端口是特权端口（例如：只能 `root` 使用）。
这种情况下，“绑定 Future”会立即失败，我们可以通过监听 @scala[Future]@java[CompletionStage] 的完成作出反应： 

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #binding-failure-handling }

Java
:   @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #binding-failure-handling }

Once the server has successfully bound to a port, the @apidoc[Source[IncomingConnection, \_]] starts running and emitting
new incoming connections. This source technically can signal a failure as well, however this should only happen in very
dramatic situations such as running out of file descriptors or memory available to the system, such that it's not able
to accept a new incoming connection. Handling failures in Akka Streams is pretty straight forward, as failures are signaled
through the stream starting from the stage which failed, all the way downstream to the final stages.

一旦服务器成功绑定到一个端口， @apidoc[Source[IncomingConnection, \_]] 开始运行并发出新的进入连接。这个 Source，从技术上也可能表示一个失败，
但是，这应该只发生在非常戏剧性的情况下，比如耗尽了系统可用的文件描述符或内存，以至于它无法接受新进入连接。
在 Akka 流中处理失败非常直接，因为失败通过流发出信号，从失败发生的阶段开始，一直到下游，直到最终阶段。

#### Connections Source failures
**连接 Source 失败**

In the example below we add a custom @apidoc[GraphStage] in order to react to the
stream's failure. See @scala[@extref[Custom stream processing](akka-docs:scala/stream/stream-customize.html)]@java[@extref[Custom stream processing](akka-docs:java/stream/stream-customize.html)]) for more on custom stages. We signal a `failureMonitor` actor with the cause why the stream is going down, and let the Actor
handle the rest – maybe it'll decide to restart the server or shutdown the ActorSystem, that however is not our concern anymore.

在下面的例子里，为了对流要失败作出反应我们添加了一个自定义 @apidoc[GraphStage] 。关于自定义 stage 的更多信息，见 @scala[@extref[自定义流处理](akka-docs:scala/stream/stream-customize.html)]@java[@extref[Custom stream processing](akka-docs:java/stream/stream-customize.html)]) 。
我们发信息给一个 `failureMonitor` actor，告诉流为什么将停止，并允许 Actor 处理剩余问题 - 也许 actor 会决定重启服务器或者关闭 ActorSystem，但这不是我们关心的。 

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #incoming-connections-source-failure-handling }

Java
:   @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #incoming-connections-source-failure-handling }

#### Connection failures
**连接失败**

The third type of failure that can occur is when the connection has been properly established,
however afterwards is terminated abruptly – for example by the client aborting the underlying TCP connection.

第三种可能发生的失败类型是当连接已经建立后，但是后来却突然终止 - 比如客户端中止了底层 TCP 连接。

To handle this failure we can use the same pattern as in the previous snippet, however apply it to the connection's Flow:

要处理这个失败，我们可以使用与前面代码片段相同的模式，但是，将它连接到 Flow：

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #connection-stream-failure-handling }

Java
:   @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #connection-stream-failure-handling }


Note that this is when the TCP connection is closed correctly, if the client just goes away, for example because of
a network failure, it will not be seen as this kind of stream failure. It will instead be detected through the
@ref[idle timeout](../common/timeouts.md#timeouts)).

注意，这是 TCP 连接正确关闭时的情况。如果客户端只是离开（比如因为网络故障），则不会被视为流失败。它通过 @ref[空闲超时](../common/timeouts.md#timeouts) 检测。

These failures can be described more or less infrastructure related, they are failing bindings or connections.
Most of the time you won't need to dive into those very deeply, as Akka will simply log errors of this kind
anyway, which is a reasonable default for such problems.

这些失败或多或少都与基础设施有关，它们是失败的绑定或连接。大多时候你不需要深入研究这些问题，因为 Akka 会记录这类错误，这是此类问题的合理默认行为。

In order to learn more about handling exceptions in the actual routing layer, which is where your application code
comes into the picture, refer to @ref[Exception Handling](../routing-dsl/exception-handling.md) which focuses explicitly on explaining how exceptions
thrown in routes can be handled and transformed into @apidoc[HttpResponse] s with appropriate error codes and human-readable failure descriptions.

为了了解更多在实际的路由层怎样处理异常，请参考 @ref[异常处理](../routing-dsl/exception-handling.md)。它关注显示地解释如何处理路由里抛出的异常，
并将其转换为具有合适错误代码和人类可读失败描述的 @apidoc[HttpResponse] 。
