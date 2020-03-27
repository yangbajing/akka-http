# 4. 服务器 API
**Server API**

Apart from the @ref[HTTP Client](../client-side/index.md) Akka HTTP also provides an embedded,
[Reactive-Streams](https://www.reactive-streams.org/)-based, fully asynchronous HTTP/1.1 server implemented on top of @scala[@extref[Streams](akka-docs:scala/stream/index.html)]@java[@extref[Streams](akka-docs:java/stream/index.html)].

除了 @ref[HTTP 客户端](../client-side/index.md)，Akka HTTP 也提供了嵌入式的、基于 [反应式-流](https://www.reactive-streams.org/)-based、
完全异步的 HTTP/1.1 服务器，它在 @scala[@extref[流](akka-docs:scala/stream/index.html)]@java[@extref[流](akka-docs:java/stream/index.html)] 之上实现。

It supports the following features:

支持下列特性：

 * Full support for [HTTP persistent connections](https://en.wikipedia.org/wiki/HTTP_persistent_connection)
 * Full support for [HTTP pipelining](https://en.wikipedia.org/wiki/HTTP_pipelining)
 * Full support for asynchronous HTTP streaming including "chunked" transfer encoding accessible through an idiomatic API
 * Optional SSL/TLS encryption
 * WebSocket support

 - 完全支持 [HTTP 持久化连接](https://en.wikipedia.org/wiki/HTTP_persistent_connection)
 - 完全支持 [HTTP 管线化](https://en.wikipedia.org/wiki/HTTP_pipelining)
 - 完全支持异步 HTTP 流，包括通过惯用 API 访问的 "分块" 传输编码
 - 可选的 SSL/TLS 加密
 - WebSocket 支持

The server-side components of Akka HTTP are split into two layers:

Akka HTTP 的服务器端组件分为两层：

@ref[Core Server API](low-level-api.md)
:  The basic low-level server implementation in the `akka-http-core` module.

@ref[核心服务器API](low-level-api.md)
:  基础的低级服务器，实现在 `akka-http-core` 模块。

@ref[High-level Server-Side API](../routing-dsl/index.md)
:  Higher-level functionality in the `akka-http` module which offers a very flexible "Routing DSL" for elegantly defining RESTful web services as well as
   functionality of typical web servers or frameworks, like deconstruction of URIs, content negotiation or
   static content serving.

@ref[高级服务器端 API](../routing-dsl/index.md)
:  `akka-http` 模块里的高级功能，提供了一个很灵活的“路由 DSL”来优雅地定义 RESTful Web 服务，以及实现典型的 Web 服务器或框架的功能，
    比如：解构 URI、内容协商或者静态内容服务。

Depending on your needs you can either use the low-level API directly or rely on the high-level
@ref[Routing DSL](../routing-dsl/index.md) which can make the definition of more complex service logic much
easier. You can also interact with different API levels at the same time and, independently of which API level you choose
Akka HTTP will happily serve many thousand concurrent connections to a single or many different clients.

根据你的需要，你可以选择直接使用低级 API 或者依赖高级 [路由 API](../routing-dsl/index.md) ，高级 API 可以使定义复杂的服务逻辑更容易。
你也可以同时使用不同级别的 API，依赖于这些你选择的 API，单个 Akka HTTP （服务）可以很好的服务成千上万的并发请求或者很多不同的客户端。

@@@ note
It is recommended to read the @ref[Implications of the streaming nature of Request/Response Entities](../implications-of-streaming-http-entity.md) section,
as it explains the underlying full-stack streaming concepts, which may be unexpected when coming
from a background with non-"streaming first" HTTP Servers.

推荐阅读 @ref[请求/响应实体流的实质含义](../implications-of-streaming-http-entity.md) 部分，它阐明了（Akka HTTP）底层的全栈流的概念。
因为对于没有“流式优先” HTTP 服务器概念背景的人来说，它们也许会感到难以理解。
@@@

@@toc { depth=3 }

@@@ index

* [low-level-api](low-level-api.md)
* [routing-dsl/index](../routing-dsl/index.md)
* [websocket-support](websocket-support.md)
* [server-https-support](server-https-support.md)
* [graceful-termination](graceful-termination.md)
* [http2](http2.md)

@@@
