# 5. 客户端 API
*5. Client API*

All client-side functionality of Akka HTTP, for consuming HTTP-based services offered by other endpoints, is currently
provided by the `akka-http-core` module.

Akka HTTP 的所有客户端功能，用于消费通过其它端点提供的 HTTP 服务，当前都由 `akka-http-core` 模块提供。

It is recommended to first read the @ref[Implications of the streaming nature of Request/Response Entities](../implications-of-streaming-http-entity.md) section,
as it explains the underlying full-stack streaming concepts, which may be unexpected when coming
from a background with non-"streaming first" HTTP Clients.

推荐阅读 @ref[请求/响应实体流的实质含义](../implications-of-streaming-http-entity.md) 部分，它阐明了（Akka HTTP）底层的全栈流的概念。
因为对于没有“流式优先” HTTP 客户端概念背景的人来说，也许会感到难以理解。

All of the API's deal with @ref[`HttpRequest` and `HttpResponse`](request-and-response.md) objects.

所有 API 都处理 @ref[`HttpRequest` 和 `HttpResponse`](request-and-response.md) 对象。

Depending on your application's specific needs you can choose from three different API levels:

根据你的应用程序程序的特定需要，你可以从三种不同的 API 级别进行选择：

@ref[Request-Level Client-Side API](request-level.md)
: for letting Akka HTTP perform all connection management. Recommended for most usages.

@ref[请求级客户端 API](request-level.md)
: 用于让 Akka HTTP 执行所有连接管理。推荐使用。

@ref[Host-Level Client-Side API](host-level.md)
: for letting Akka HTTP manage a connection-pool to *one specific* host/port endpoint. Recommended when
  the user can supply a @apidoc[Source[HttpRequest, \_]] with requests to run against a single host
  over multiple pooled connections.

@ref[主机级客户端 API](host-level.md)
: 用于让 Akka HTTP 管理到 *一个特定* 主机/端口端点的连接池。当用户可以提供 @apidoc[Source[HttpRequest, \_]]，并在多个连接池上针对单个主机运行请求时，推荐使用。

@ref[Connection-Level Client-Side API](connection-level.md)
: for full control over when HTTP connections are opened/closed and how requests are scheduled across them. Only
  recommended for particular use cases.

@ref[连接级客户端 API](connection-level.md)
: 用于完全控制何时打开/关闭 HTTP 连接以及如何跨连接调度请求。仅推荐用于特殊用例。

You can interact with different API levels at the same time and, independently of which API level you choose,
Akka HTTP will happily handle many thousand concurrent connections to a single or many different hosts.

你可以同时与不同的 API 级别进行交互，并且独立于你选择的 API 级别，Akka HTTP 将愉快地处理到单个或多个不同主机的数千个并发连接。

@@toc { depth=3 }

@@@ index

* [request-and-response](request-and-response.md)
* [request-level](request-level.md)
* [host-level](host-level.md)
* [connection-level](connection-level.md)
* [pool-overflow](pool-overflow.md)
* [client-https-support](client-https-support.md)
* [client-transport](client-transport.md)
* [websocket-support](websocket-support.md)

@@@
