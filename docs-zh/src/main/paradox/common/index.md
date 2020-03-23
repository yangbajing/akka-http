# 3. Data Types & Abstractions
**3. 数据类型 & 抽像**

HTTP and related specifications define a great number of concepts and functionality that is not specific to either
HTTP's client- or server-side since they are meaningful on both end of an HTTP connection.
The documentation for their counterparts in Akka HTTP lives in this section rather than in the ones for the
@ref[Client-Side API](../client-side/index.md), @ref[Core Server API](../server-side/low-level-api.md) or @ref[High-level Server-Side API](../routing-dsl/index.md),
which are specific to one side only.

HTTP 和相关规范定义了大量的概念和功能，这些都不是特定于 HTTP 客户或服务端的，因为它们对于 HTTP 连接的两端都很有意义。
Akka HTTP 中相应模型（数据类型 & 抽像）的文档都在本部分，而不是 @ref[客户端 API](../client-side/index.md)、@ref[核心服务器 API](../server-side/low-level-api.md) 及 @ref[高级服务端 API](../routing-dsl/index.md) ，它们在专门章节。

@@toc { depth=3 }

@@@ index

* [http-model](http-model.md)
* [uri-model](uri-model.md)
* [marshalling](marshalling.md)
* [unmarshalling](unmarshalling.md)
* [encoding](encoding.md)
* [json-support](json-support.md)
* [xml-support](xml-support.md)
* [xml-support](sse-support.md)
* [timeouts](timeouts.md)
* [caching](caching.md)

@@@
