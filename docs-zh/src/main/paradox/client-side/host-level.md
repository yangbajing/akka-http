# 主机级别客户端 API
*Host-Level Client-Side API*

As opposed to the @ref[Connection-Level Client-Side API](connection-level.md) the host-level API relieves you from manually managing individual HTTP
connections. It autonomously manages a configurable pool of connections to *one particular target endpoint* (i.e.
host/port combination).

与 @ref[连接级别客户端 API](connection-level.md) 相反，主机级别的 API 使你不必手动管理单个 HTTP 连接。
它自主管理连接到 *一个特定目标端点* （例如：主机/端口 组合）的可配置池。

@@@ note
It is recommended to first read the @ref[Implications of the streaming nature of Request/Response Entities](../implications-of-streaming-http-entity.md) section,
as it explains the underlying full-stack streaming concepts, which may be unexpected when coming
from a background with non-"streaming first" HTTP Clients.

推荐阅读 @ref[请求/响应实体流的实质含义](../implications-of-streaming-http-entity.md) 部分，它阐明了（Akka HTTP）底层的全栈流的概念。
因为对于没有“流式优先” HTTP 客户端概念背景的人来说，也许会感到难以理解。
@@@

## Requesting a Host Connection Pool
**请求主机连接池**

The best way to get a hold of a connection pool to a given target endpoint is the @scala[`Http().cachedHostConnectionPool(...)`]@java[`Http.get(system).cachedHostConnectionPool(...)`]
method, which returns a @apidoc[Flow] that can be "baked" into an application-level stream setup. This flow is also called
a "pool client flow".

获得一个持有到指定目标端点的连接池的最好方式是使用 @scala[`Http().cachedHostConnectionPool(...)`]@java[`Http.get(system).cachedHostConnectionPool(...)`] 方法，该方法返回一个可以“烘焙到”应用程序级别流设置中的 @apidoc[Flow] 。
这个 flow 也被称为“池客户端 flow”。

The connection pool underlying a pool client flow is cached. For every @apidoc[akka.actor.ActorSystem], target endpoint and pool
configuration there will never be more than a single pool live at any time.

构成连接池的池客户端 flow 被缓存。对于每个 @apidoc[akka.actor.ActorSystem]，目标端点和池配置任何时候都不会有超过一个池活动。

Also, the HTTP layer transparently manages idle shutdown and restarting of connection pools as configured.
The client flow instances therefore remain valid throughout the lifetime of the application, i.e. they can be
materialized as often as required and the time between individual materialization is of no importance.

而且，HTTP 层按配置透明地管理连接池的空闲关闭和重启。

When you request a pool client flow with @scala[`Http().cachedHostConnectionPool(...)`]@java[`Http.get(system).cachedHostConnectionPool(...)`], Akka HTTP will immediately start
the pool, even before the first client flow materialization. However, this running pool will not actually open the
first connection to the target endpoint until the first request has arrived.

当你使用 @scala[`Http().cachedHostConnectionPool(...)`]@java[`Http.get(system).cachedHostConnectionPool(...)`]
请求“池客户端 flow”时，Akka HTTP 将立即启动池，甚至在第一个客户端 flow 具体化之前。
但是，在第一个请求到达前，该运行池实际上不会打开到目标端点的第一个连接。

## Configuring a Host Connection Pool
**配置主机连接池**

Apart from the connection-level config settings and socket options there are a number of settings that allow you to
influence the behavior of the connection pool logic itself.
Check out the `akka.http.host-connection-pool` section of the Akka HTTP @ref[Configuration](../configuration.md) for
more information about which settings are available and what they mean.

除了连接级别配置设置和套接字选项之外，还有许多设置允许你影响连接池逻辑自身的行为。
有关哪些设置可用和它们的含义的更多信息，查看 Akka HTTP @ref[配置](../configuration.md) 的 `akka.http.host-connection-pool` 部分。

Note that, if you request pools with different configurations for the same target host you will get *independent* pools.
This means that, in total, your application might open more concurrent HTTP connections to the target endpoint than any
of the individual pool's `max-connections` settings allow!

注意，如果你为相同目标主机请求具有不同的配置连接池，那么你将获得 *独立的* 连接池。
这意味着，你的应用程序打开到目标端点的并发 HTTP 连接可能比任何单个连接池的 `max-connections` 设置允许的更多。

There is one setting that likely deserves a bit deeper explanation: `max-open-requests`.
This setting limits the maximum number of requests that can be in-flight at any time for a single connection pool.
If an application calls @scala[`Http().cachedHostConnectionPool(...)`]@java[`Http.get(system).cachedHostConnectionPool(...)`]
3 times (with the same endpoint and settings) it will get
back `3` different client flow instances for the same pool. If each of these client flows is then materialized `4` times
(concurrently) the application will have 12 concurrently running client flow materializations.
All of these share the resources of the single pool.

有一个设置可能值得深入解释：`max-open-requests`。此设置限制单个连接池在任何时间可以运行的最大请求数量。
如果应用程序调用 @scala[`Http().cachedHostConnectionPool(...)`]@java[`Http.get(system).cachedHostConnectionPool(...)`] 3次
（使用相同端点和设置），对于相同连接池它将取回 `3` 个不同的客户端 flow 实例。如果这些客户端 flow 每一个都被具体化 `4` 次（并发），
应用程序将有 12 个并发运行的客户端 flow 物化值。所有这些（flow、连接等）都共享单个池的资源。

This means that no more than 12 requests can be open at any time.

这意味着任何时候都不可能打开多于 12 个请求。

The `max-open-requests` config setting allows for applying a hard limit which serves mainly as a protection against
erroneous connection pool use, e.g. because the application is materializing too many client flows that all compete for
the same pooled connections.

`max-open-requests` 配置设置允许应用硬限制，该限制主要是为了防止错误的连接池使用，例如：因为应用程序实现了太多的客户端 flow，导致它们都在争用相同的连接池。

<a id="using-a-host-connection-pool"></a>
## Using a Host Connection Pool
**使用主机连接池**

The "pool client flow" returned by @scala[`Http().cachedHostConnectionPool(...)`]@java[`Http.get(system).cachedHostConnectionPool(...)`] has the following type:

通过 @scala[`Http().cachedHostConnectionPool(...)`]@java[`Http.get(system).cachedHostConnectionPool(...)`] 返回的“池客户端 flow”具有下面的类型： 

@@@ div { .group-scala }
```scala
Flow[(HttpRequest, T), (Try[HttpResponse], T), HostConnectionPool]
```
@@@
@@@ div { .group-java }
```java
Flow<Pair<HttpRequest, T>, Pair<Try<HttpResponse>, T>, HostConnectionPool>
```
@@@

This means it consumes pairs of type @scala[`(HttpRequest, T)`]@java[@apidoc[Pair[HttpRequest, T]]] and produces pairs of type @scala[`(Try[HttpResponse], T)`]@java[`Pair<Try<HttpResponse>, T>`]
which might appear more complicated than necessary on first sight.
The reason why the pool API includes objects of custom type `T` on both ends lies in the fact that the underlying
transport usually comprises more than a single connection and as such the pool client flow often generates responses in
an order that doesn't directly match the consumed requests.
We could have built the pool logic in a way that reorders responses according to their requests before dispatching them
to the application, but this would have meant that a single slow response could block the delivery of potentially many
responses that would otherwise be ready for consumption by the application.

这意味着它消费 @scala[`(HttpRequest, T)`]@java[@apidoc[Pair[HttpRequest, T]]] 类型的元组并且产生
@scala[`(Try[HttpResponse], T)`]@java[`Pair<Try<HttpResponse>, T>`]　类型的元组，该类型第一眼看上去可能比较复杂。
池 API 在两端都包含自定义类型 `T` 的原因是，事实上底层传输通常包含多个连接，此类池客户端 flow 通常按所用请求不直接匹配的顺序生成响应。
我们本来可以用响应发送到应用程序之前根据它们的请求重新排序响应的方式来构建池逻辑，但是这将意味着单个慢速响应可能阻止潜在的多个响应的传递，
否则这些响应准备好被应用程序消费。 

In order to prevent unnecessary head-of-line blocking the pool client-flow is allowed to dispatch responses as soon as
they arrive, independently of the request order. Of course this means that there needs to be another way to associate a
response with its respective request. The way that this is done is by allowing the application to pass along a custom
"context" object with the request, which is then passed back to the application with the respective response.
This context object of type `T` is completely opaque to Akka HTTP, i.e. you can pick whatever works best for your
particular application scenario.

为了防止不必要的行首（head-of-line）阻塞，池客户端-flow 被允许在响应到达时方式立刻分发它们，与请求顺序无关。
这样做的方式是允许应用程序随请求传递一个自定义“上下文”对象，然后以相应的响应将其传递回应用程序。
`T` 类型的上下文对象对 Akka HTTP 完全不透明，例如：你可以选择最适合你的特定应用程序的场景。

@@@ note

A consequence of using a pool is that long-running requests block a connection while running and may starve other
requests. Make sure not to use a connection pool for long-running requests like long-polling GET requests.
Use the @ref[Connection-Level Client-Side API](connection-level.md) instead.

使用池的一种结果是长时间运行的请求在运行时阻塞连接，并可能饿死其它请求。确保对于长时间运行的请求不要使用连接池，例如长轮询 GET 请求。
使用 @ref[连接级别客户端 API](connection-level.md) 替代。
@@@

## 连接分配逻辑
**Connection Allocation Logic**

This is how Akka HTTP allocates incoming requests to the available connection "slots":

 1. If there is a connection alive and currently idle then schedule the request across this connection.
 2. If no connection is idle and there is still an unconnected slot then establish a new connection.
 3. If all connections are already established and "loaded" with other requests then pick the connection with the least
open requests that only has requests with idempotent methods scheduled to it, if there is one.
 4. Otherwise apply back-pressure to the request source, i.e. stop accepting new requests.

这是 Akka HTTP 如何分配传入请求到可用连接“插槽”：

 1. 如果有一个连接处于活动状态并且当前处于空闲状态，那么就在这个连接上调度请求。
 2. 如果没有连接空闲并且仍有一个未连接的插槽，那么建立一个新连接。
 3. 如果所有连接都已经建立并且“加载”了其它请求，那么选择打开请求数最少的连接，这些请求只有调度了幂等方法的请求（如果有的话）。
 4. 否则，对请求源施加回压，例如：停止接受新的请求。

## Retrying a Request
**重试请求**

If the `max-retries` pool config setting is greater than zero the pool retries idempotent requests for which
a response could not be successfully retrieved. Idempotent requests are those whose HTTP method is defined to be
idempotent by the HTTP spec, which are all the ones currently modelled by Akka HTTP except for the `POST`, `PATCH`
and `CONNECT` methods.

如果 `max-retries` 池配置设置大于零，池将重试无法成功检索到响应的幂等请求。幂等请求是那些HTTP 方法被 HTTP 规范定义为幂等的的请求，
这些请求当前都由 Akka HTTP 建模的，除了 `POST`、`PATCH` 和 `CONNECT` 方法。

When a response could not be received for a certain request there are essentially three possible error scenarios:

 1. The request got lost on the way to the server.
 2. The server experiences a problem while processing the request.
 3. The response from the server got lost on the way back.

当不能收到某个请求的响应时，基本上有三种可能的错误场景：

 1. 请求在去服务器的路上丢失。
 2. 服务器在处理请求时遇到问题。
 3. 响应从服务器返回的路上丢失。

Since the host connector cannot know which one of these possible reasons caused the problem and therefore `PATCH` and
`POST` requests could have already triggered a non-idempotent action on the server these requests cannot be retried.

由于主机连接器不知道是哪种可能原因导致的问题，因此 `PATCH` 和 `POST` 请求在服务器上可能已经触发了非幂等动作，所以不能重试这些请求。

In these cases, as well as when all retries have not yielded a proper response, the pool produces a failed `Try`
(i.e. a `scala.util.Failure`) together with the custom request context.

在这些情况下，以及当所有重试都不能产生正确响应时，池将生成一个失败 `Try`（既：`scala.util.Failure`）和自定义请求上下文。

If a request fails during connecting to the server, for example, because the DNS name cannot be resolved or the server
is currently unavailable, retries are attempted with exponential backoff delay. See the documentation of the
`akka.http.host-connection-pool.base-connection-backoff` setting in the @ref[configuration](../configuration.md).

如果在连接到服务器期间请求失败，例如，因为不能解析 DNS 名称或者服务器当前不可达，尝试以指数回退延迟重试。
见 @ref[配置](../configuration.md) 里 `akka.http.host-connection-pool.base-connection-backoff` 设置的文档。

## Pool Shutdown
**池关闭**

Completing a pool client flow will simply detach the flow from the pool. The connection pool itself will continue to run
as it may be serving other client flows concurrently or in the future. Only after the configured `idle-timeout` for
the pool has expired will Akka HTTP automatically terminate the pool and free all its resources.

完成的池客户端 flow 将从池简单地分离 flow。连接池自身将继续运行，因为它可能同时或在将来服务其它客户端 flow。
只有在池配置的 `idle-timeout` 过期以后，Akka HTTP 才将自动终止池并释放其所有资源。

If a new client flow is requested with @scala[`Http().cachedHostConnectionPool(...)`]@java[`Http.get(system).cachedHostConnectionPool(...)`] or if an already existing client flow is
re-materialized the respective pool is automatically and transparently restarted.

如果使用 @scala[`Http().cachedHostConnectionPool(...)`]@java[`Http.get(system).cachedHostConnectionPool(...)`] 请求一个新的客户端 flow 或者
一个已存在的客户端 flow 重新具体化，则自动并透明的重启相应池。

In addition to the automatic shutdown via the configured idle timeouts it's also possible to trigger the immediate
shutdown of a specific pool by calling `shutdown()` on the `HostConnectionPool` instance that the pool client
flow materializes into. This `shutdown()` call produces a @scala[`Future[Unit]`]@java[`CompletionStage<Done>`] which is fulfilled when the pool
termination has been completed.

除了通过配置的空闲超时自动关闭以外，还可以调用池客户端 flow 具体化到的 `HostConnectionPool` 实例上的 `shutdown()` 方法来触发特定池的立即关闭。
`shutdown()` 调用产生一个 @scala[`Future[Unit]`]@java[`CompletionStage<Done>`] ，当池终止完成时 @scala[`Future[Unit]`]@java[`CompletionStage<Done>`] 也完成。

It's also possible to trigger the immediate termination of *all* connection pools in the @apidoc[akka.actor.ActorSystem] at the same
time by calling @scala[`Http().shutdownAllConnectionPools()`]@java[`Http.get(system).shutdownAllConnectionPools()`].
This call too produces a @scala[`Future[Unit]`]@java[`CompletionStage<Done>`] which is fulfilled when all pools have terminated.

还可以调用 @scala[`Http().shutdownAllConnectionPools()`]@java[`Http.get(system).shutdownAllConnectionPools()`] 同时触发
@apidoc[akka.actor.ActorSystem] 里的 *所有* 连接池立即终止。
该调用产生一个 @scala[`Future[Unit]`]@java[`CompletionStage<Done>`] ，当池终止完成时 @scala[`Future[Unit]`]@java[`CompletionStage<Done>`] 也完成。

@@@ note
When encountering unexpected `akka.stream.AbruptTerminationException` exceptions during @apidoc[akka.actor.ActorSystem] **shutdown**
please make sure that active connections are shut down before shutting down the entire system, this can be done by
calling the @scala[`Http().shutdownAllConnectionPools()`]@java[`Http.get(system).shutdownAllConnectionPools()`] method,
and only once its @scala[`Future`]@java[`CompletionStage`] completes, shutting down the actor system.

当在 @apidoc[akka.actor.ActorSystem] **关闭** 期间遇到意外的 `akka.stream.AbruptTerminationException` 异常时，
请确保在关闭整个系统之前关闭活动连接，这可以通过调用 @scala[`Http().shutdownAllConnectionPools()`]@java[`Http.get(system).shutdownAllConnectionPools()`]
方法来完成，并且只在 @scala[`Future`]@java[`CompletionStage`] 完成后，再关闭 actor 系统。
@@@

## Examples
**示例**

@@@ note { .group-scala }
At this place we previously showed an example that used the `Source.single(request).via(pool).runWith(Sink.head)`. In
fact, this is an anti-pattern that doesn't perform well. Please either supply requests using a queue or in a streamed fashion as
shown below.

在这个地方，我们之前展示了一个使用 `Source.single(request).via(pool).runWith(Sink.head)` 的例子。事实上，这是一种表现不好的反模式。
请使用队列或流的方式提供请求，如下所示。
@@@

@@@ div { .group-scala }

### Using the host-level API with a queue
**使用带有队列的主机级别 API**

In many cases, you just want to issue requests to a pool and receive responses when they are available. In most cases,
you should use the @ref[Request-Level Client-Side API](request-level.md) for this purpose. If you want to use a similar Future-based API
with the host-level API, here's how to do it.

在许多情况下，你只想向池发出请求，并在可用时接收响应。大多情况下，为此目的你应使用 @ref[请求级别客户端 API](request-level.md) 。
如果你想将类似基于 Future 的 API 与主机级别 API 一起使用，这里是它们如何做到的说明。

As explained above, Akka HTTP prevents to build up an unbounded buffer of requests and an unlimited number of connections.
Therefore, it guards itself a) by applying backpressure to all request streams connected to the cached pool and b)
by failing requests with a @apidoc[BufferOverflowException] when the internal buffer overflows when too many materializations
exist or too many requests have been issued to the pool.

如上面所述，Akka HTTP 防止构建无界缓冲请求和无限制数量的连接。因此，它保护自己 a) 对连接到缓存池的所有请求流施加回压，
b) 当内部缓冲溢出、当存在太多具体化值 *(译注：指池客户端-flow)* 或者向池发出太多请求时，使用 @apidoc[BufferOverflowException] 让请求失败。

To mimic the request-level API we can put an explicit queue in front of the pool and decide ourselves what to do when
this explicit queue overflows. This example shows how to do this. (Thanks go to [kazuhiro's blog for the initial idea](https://kazuhiro.github.io/scala/akka/akka-http/akka-streams/2016/01/31/connection-pooling-with-akka-http-and-source-queue.html).)

要模仿请求级别 API，我们可以在池的前面放置一个显示队列，并且当这个显示队列溢出时我们自己决定要做什么。这个例子显示这是如何做的。
（谢谢 [有关最被想法的 kazuhiro 的博客](https://kazuhiro.github.io/scala/akka/akka-http/akka-streams/2016/01/31/connection-pooling-with-akka-http-and-source-queue.html)）。

You can tweak the `QueueSize` setting according to your memory constraints. In any case, you need to think about a strategy
about what to do when requests fail because the queue overflowed (e.g. try again later or just fail).

你可以根据内存限制调整 `QueueSize` 设置。在任何情况下，你需要思考一个策略，既当因为队列溢出而请求失败时（例如：稍候重试或只是失败）。 

@@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #host-level-queue-example }

### Using the host-level API in a streaming fashion
**以流方式使用主机级别 API**

Even better is it to use the streaming API directly. This will mostly prevent intermediate buffers as data can be
generated "on-the-fly" while streaming the requests. You supply the requests as a stream, i.e. as a @apidoc[Source[(HttpRequest, ...)]], and
the pool will "pull out" single requests when capacity is available on one of the connections to the host.

直接使用流式 API 甚至更好。这将主要防止中间缓冲区，因为数据可以在流请求时“动态”生成。以流的形式提供抗日请求，例如：作为 @apidoc[Source[(HttpRequest, ...)]] ，
当到主机的连接上容量可用时，池将“拉出”单个请求。

@@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #host-level-streamed-example }
@@@

@@@ div { .group-java }
For now, please see the Scala examples in [Scala Host-Level Client API](../client-side/host-level.html#examples?language=scala).
If you want to help with converting the examples see issue [#836](https://github.com/akka/akka-http/issues/836).

现在，请见 [主机级别客户端 API](../client-side/host-level.html#examples?language=scala) 里的 Scala 例子。
如果你想帮忙转换例子到 Java，见问题 [#836](https://github.com/akka/akka-http/issues/836) 。
@@@
