# 请求级别客户端 API
*Request-Level Client-Side API*

The request-level API is the recommended and most convenient way of using Akka HTTP's client-side functionality. It internally builds upon the
@ref[Host-Level Client-Side API](host-level.md) to provide you with a simple and easy-to-use way of retrieving HTTP responses from remote servers.
Depending on your preference you can pick the flow-based or the future-based variant.

请求级别 API 是使用 Akka HTTP 的客户端功能的推荐和最方便的方法。它内部建立在 @ref[主机级别客户端 API](host-level.md) 之上，
为你提供从远程服务器检索 HTTP 响应的简单易用的方式。

@@@ note
It is recommended to first read the @ref[Implications of the streaming nature of Request/Response Entities](../implications-of-streaming-http-entity.md) section,
as it explains the underlying full-stack streaming concepts, which may be unexpected when coming
from a background with non-"streaming first" HTTP Clients.

推荐阅读 @ref[请求/响应实体流的实质含义](../implications-of-streaming-http-entity.md) 部分，它阐明了（Akka HTTP）底层的全栈流的概念。
因为对于没有“流式优先” HTTP 客户端概念背景的人来说，也许会感到难以理解。
@@@

@@@ note
The request-level API is implemented on top of a connection pool that is shared inside the actor system. A consequence of
using a pool is that long-running requests block a connection while running and starve other requests. Make sure not to use
the request-level API for long-running requests like long-polling GET requests. Use the @ref[Connection-Level Client-Side API](connection-level.md)
or an extra pool just for the long-running connection instead.

请求级别 API 是在连接池顶部实现的，共享内部的 actor 系统。使用连接池的一个结果是长时间运行的请求在运行时阻塞连接，并饿死其它请求。
确保对于长时间运行的请求不要使用请求级别 API，如：长轮询 GET 请求。为长时间运行的连接改用 @ref[连接级别客户端 API](connection-level.md) 或额外的连接池。
@@@

## Future-Based Variant
**基于 Future 的变体**

Most often, your HTTP client needs are very basic. You simply need the HTTP response for a certain request and don't
want to bother with setting up a full-blown streaming infrastructure.

通常，你的 HTTP 客户端需要是很基础的。你只需要某个请求进行 HTTP 响应，不想费心设置一个完善的流处理基础设施。

For these cases Akka HTTP offers the @scala[`Http().singleRequest(...)`]@java[`Http.get(system).singleRequest(...)`] method, which simply turns an @apidoc[HttpRequest] instance
into @scala[`Future[HttpResponse]`]@java[`CompletionStage<HttpResponse>`]. Internally the request is dispatched across the (cached) host connection pool for the
request's effective URI.

对于这些情况，Akka HTTP 提供了 @scala[`Http().singleRequest(...)`]@java[`Http.get(system).singleRequest(...)`] 方法，
该方法简单地将 @apidoc[HttpRequest] 实例转换为 @scala[`Future[HttpResponse]`]@java[`CompletionStage<HttpResponse>`] 。
在内部，以请求的有效 URI 将请求分配到跨（缓存的）主机连接池。

Just like in the case of the super-pool flow described above the request must have either an absolute URI or a valid
`Host` header, otherwise the returned future will be completed with an error.

就像上面超级连接池的情况描述的，请求必须包含包含 URI 路径或者有一个有效的 `Host` 头域，否则返回的 Future 将以一个错误完成。

### Example
**示例**

Scala
:   @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #single-request-example }

Java
:   @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #single-request-example }

### Using the Future-Based API in Actors
**在 Actor 里使用基于 Future 的 API**

When using the @scala[`Future`]@java[`CompletionStage`] based API from inside an @apidoc[Actor], all the usual caveats apply to how one should deal
with the futures completion. For example you should not access the actor's state from within the @scala[`Future`]@java[`CompletionStage`]'s callbacks
(such as `map`, `onComplete`, ...) and instead you should use the @scala[`pipeTo`]@java[`pipe`] pattern to pipe the result back
to the actor as a message:

当从 @apidoc[Actor] 内部使用基于 @scala[`Future`]@java[`CompletionStage`] 的 API时，所有常见的警告都适用于如何处理 `Future` 完成。
如使：在 @scala[`Future`]@java[`CompletionStage`] 的回调（如：`map`、`onComplete`、……）里不应该访问 Actor 的状态，你应该使用 @scala[`pipeTo`]@java[`pipe`] 模式将（`Future` 的）结果通过管道作为消息发回 actor 。 

Scala
:   @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #single-request-in-actor-example }

Java
:   @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #single-request-in-actor-example }

@@@ warning

Always make sure you consume the response entity streams (of type @scala[@apidoc[Source[ByteString,Unit]]]@java[@apidoc[Source[ByteString, Object]]]) 
by for example connecting it to a @apidoc[Sink] or by calling @scala[`response.discardEntityBytes()`]@java[`response.discardEntityBytes(Materializer)`] 
if you don't care about the response entity. Otherwise Akka HTTP (and the underlying Streams infrastructure) will understand the
lack of entity consumption as a back-pressure signal and stop reading from the underlying TCP connection!

总是确保你消费了响应实体流（@scala[@apidoc[Source[ByteString,Unit]]]@java[@apidoc[Source[ByteString, Object]]] 类型），
例如将流连接到一个 @apidoc[Sink] 或者如果你不关心响应实体时通过调用 @scala[`response.discardEntityBytes()`]@java[`response.discardEntityBytes(Materializer)`] 消费它。
否则，Akka HTTP（及底层的流基础设施）将没有实体消耗理解为回压信号并停止从底层 TCP 连接读取数据！

This is a feature of Akka HTTP that allows consuming entities (and pulling them through the network) in
a streaming fashion, and only *on demand* when the client is ready to consume the bytes -
it may be a bit surprising at first though.

Akka HTTP 的这个特性允许以流处理的方式消费实体（并通过网络拉取它们），并且只在客户端准备消费字节时才 *按需* （拉取数据）- 该特性起初可能有点令人惊讶。

There are tickets open about automatically dropping entities if not consumed ([#183](https://github.com/akka/akka-http/issues/183) and [#117](https://github.com/akka/akka-http/issues/117)),
so these may be implemented in the near future.

关于如果没有消费则自动丢弃实体，有打开的提案（[#183](https://github.com/akka/akka-http/issues/183) 和 [#117](https://github.com/akka/akka-http/issues/117)），这些提供可能在不久的将来实现。
@@@

## Flow-Based Variant
**基于 Flow 的变体**

The flow-based variant of the request-level client-side API is presented by the @scala[`Http().superPool(...)`]@java[`Http.get(system).superPool(...)`] method.
It creates a new "super connection pool flow", which routes incoming requests to a (cached) host connection pool
depending on their respective effective URIs.

请求级别客户端 API 的基于 Flow 的变体由 @scala[`Http().superPool(...)`]@java[`Http.get(system).superPool(...)`] 方法表示。
它创建一个新的“超级连接池 flow”，将传入请求路由到一个（缓存的）主机连接池，（路由算法）具体取决于它们各自的有效 URI。

The @apidoc[Flow] returned by @scala[`Http().superPool(...)`]@java[`Http.get(system).superPool(...)`] is very similar to the one from the @ref[Host-Level Client-Side API](host-level.md), so the
@ref[Using a Host Connection Pool](host-level.md#using-a-host-connection-pool) section also applies here.

因为通过 @scala[`Http().superPool(...)`]@java[`Http.get(system).superPool(...)`] 返回的 @apidoc[Flow] 非常类似
@ref[主机级别客户端 API](host-level.md) 返回的 @apidoc[Flow] ，所以 @ref[使用一个主机连接池](host-level.md#using-a-host-connection-pool) 部分描述的内容也适用与这里。

However, there is one notable difference between a "host connection pool client flow" for the host-level API and a
"super-pool flow":
Since in the former case the flow has an implicit target host context the requests it takes don't need to have absolute
URIs or a valid `Host` header. The host connection pool will automatically add a `Host` header if required.

但是，主机级别 API 的“主机连接池客户端 flow”和“超级池”之间有一个值得注意的区别：
因为在前一种情况下，flow 具有一个请求的隐式目标主机上下文，所以 flow 接收的请求不需要有绝对 URI 或者有效的 `Host` 头域。
如果需要，主机连接池将自动添加一个 `Host` 头域。

For a super-pool flow this is not the case. All requests to a super-pool must either have an absolute URI or a valid
`Host` header, because otherwise it'd be impossible to find out which target endpoint to direct the request to.

而对于”超级池 flow“，情况并非如此。到超级池的所有请求都必须具有绝对 URI 或有效 `Host` 头域之一，
否则超级池将无法找到请求该定向到哪个目标端点。

## Collecting headers from a server response
**从服务器响应收集头域**

Sometimes we would like to get only headers of specific type which are sent from a server. In order to collect headers in a type safe way Akka HTTP API provides a type for each HTTP header. Here is an example for getting all cookies set by a server (`Set-Cookie` header):

有时我们只想获得从服务器发送的特定类型的头域。为了以类型安全的方式收集头域，Akka HTTP API 为每个 HTTP 头域提供了类型。
这里是一个通过服务器获得所有 cookie 设置的（`Set-Cookie` 头域）：

Scala
:   @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #collecting-headers-example }

Java
:   @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #collecting-headers-example }

*（译注：不要忘记调用 `response.discardEntityBytes()`）*
