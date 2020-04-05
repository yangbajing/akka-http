# 池溢出和最大打开请求数设置
*Pool overflow and the max-open-requests setting*

@ref[Request-Level Client-Side API](request-level.md) and @ref[Host-Level Client-Side API](host-level.md)
use a connection pool underneath. The connection pool will open a limited number of concurrent connections to one host
(see the `akka.http.host-connection-pool.max-connections` setting). This will limit the rate of requests a pool
to a single host can handle.

@ref[请求级别客户端 API](request-level.md) 和 @ref[主机级别客户端 API](host-level.md) 在下面使用一个连接池。
连接池将打开到一个主机的有限数量的并发连接（见 `akka.http.host-connection-pool.max-connections` 设置）。
这将限制池对单个主机可以处理的请求的速率。 

When you use the @ref[stream-based host-level API](host-level.md#using-the-host-level-api-in-a-streaming-fashion)
stream semantics prevent that the pool is overloaded with requests. On the other side, when a new request is pushed either using
@scala[`Http().singleRequest()`]@java[`Http.get(system).singleRequest()`] or when materializing too many streams using the same
@scala[`Http().cachedHostConnectionPool`]@java[`Http.get(system).cachedHostConnectionPool`], requests may start to queue
up when the rate of new requests is greater than the rate at which the pool can process requests.

当你使用 @ref[基于流的主机级别 API](host-level.md#using-the-host-level-api-in-a-streaming-fashion) 时，流语义防止池请求过载。
在另一边，当使用 @scala[`Http().singleRequest()`]@java[`Http.get(system).singleRequest()`] 推送新请求，
或者使用相同 @scala[`Http().cachedHostConnectionPool`]@java[`Http.get(system).cachedHostConnectionPool`] 具体化太多流时，
当新请求的速率大于池可以处理请求的速率时，请求可能会开始排队。

In such a situation `max-open-requests` per host connection pool will be queued to buffer short-term peaks of requests.
Further requests will fail immediately with a @apidoc[BufferOverflowException] with a message like this:

在这样的情况下，每个主机连接池的 `max-open-requests` 将排队以缓冲短期请求高峰。更多的请求将以 @apidoc[BufferOverflowException] 立即失败，
消息像这样：

```
Exceeded configured max-open-requests value of ...
```

This will usually happen under high load or when the pool has been running for some time with the processing speed being
too slow to handle all the incoming requests.

这通常发生在高负载下，或者当池已经运行一段时间，处理速度太慢，无法处理所有传入请求时。

Note, that even if the pool can handle regular load, short-term hiccups (at the server, the network, or at the client) can make
the queue overflow, so you need to treat this as an expected condition. Your application should be able to deal with it. In many cases, it
makes sense to treat pool overflow the same as a `503` answer from the server which usually is used when the server is
overloaded. A common way to treat it would be to retry the request after some while (using a viable backoff strategy).

注意，即使池可以处理常规负载，短期故障（在服务器、网络或客户端）可能导致队列溢出，因此需要把这视为预期条件。你的应用程序应该可以处理它。
在许多情况下，把池溢出视为来自服务器的 `503` 应答（通常在服务器过载时使用）是有意义的。一种常见处理方式是在一段时间后重试请求（使用可行的后退策略）。

## Common causes of pool overload
**池过载的常见原因**

As explained above the general explanation for pool overload is that the incoming request rate is higher that the request
processing rate. This can have all kinds of causes (and hints for fixing them in parentheses):

 * The server is too slow (improve server performance)
 * The network is too slow (improve network performance)
 * The client issues requests too fast (slow down creation of requests if possible)
 * There's high latency between client and server (use more concurrent connections to hide latency with parallelism)
 * There are peaks in the request rate (prevent peaks by tuning the client application or increase `max-open-requests` to
   buffer short-term peaks)
 * Response entities were not read or discarded (see @ref[Implications of the streaming nature of Http entities](../implications-of-streaming-http-entity.md))
 * Some requests are slower than others blocking the connections of a pool for other requests (see below)

如上所述，对池过载的一般说明是传入请求速率高于请求处理速率。这可能有多种原因（以及在括号中修复它们的提示）：

 - 服务器太慢（改进服务器性能）
 - 网络太慢（改进网络性能）
 - 客户端发出请求太快（如果可能，减慢请求的创建速度）
 - 客户端和服务器这间有调整延迟（使用更多并发连接以并行方式隐藏延迟）
 - 请求速率峰值（通过优化客户端应用程序或者增加 `max-open-requests` 以缓冲短期峰值来防止峰值）
 - 响应实体不可读或已丢弃（见 @ref[请求/响应实体流的实质含义](../implications-of-streaming-http-entity.md)）
 - 一些请求比其它请求慢，其它请求会阻塞池的连接（见下面）

The last point may need a bit more explanation. If some requests are much slower than others, e.g. if the request is
a long-running Server Sent Events request than this will block one of the connections of the pool for a long time. If
there are multiple such requests going on at the same time it will lead to starvation and other requests cannot make any
progress any more. Make sure to run a long-running request on a dedicated connection (using the
@ref[Connection-Level Client-Side API](connection-level.md)) to prevent such a situation.

最后点可能需要更多解释。如果一些请求比其它请求慢得多，例如：如果请求是一个长时间运行的 SSE 请求，这将阻塞池里的一个连接很长时间。
如果同时有多个此类请求，它将导致饥饿和其它请求不能取得任何进展。确保在专用连接上运行长时间运行的请求（使用 @ref[连接级别客户端 API](connection-level.md)），以防止此类情况。

## Why does this happen only with Akka Http and not with [insert other client]
**为什么这种情况只在 Akka HTTP 上发生，而其它 HTTP 客户端没有？**

Many Java HTTP clients don't set limits by default for some of the resources used. E.g. some clients will never queue a
request but will just open another connection to the server if all the pooled connections are currently busy. However,
this might just move the problem from the client to the server. Also using an excessive number of connections will lead to
worse performance on the network as more connections will compete for bandwidth.

许多 Java HTTP 客户端默认对使用的某些资源不设置限制。例如：某些客户端从不对请求排队，但如果所有池连接当前都很忙，则只会打开到服务器的另一个连接。
但是，只可能只是将问题从客户端移动了服务器端。而且，使用过多的连接将导致网络性能下降，因为更多的连接将争夺带宽。