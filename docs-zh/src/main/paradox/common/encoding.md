# 编码 / 解码
*Encoding / Decoding*

The [HTTP spec](https://tools.ietf.org/html/rfc7231#section-3.1.2.1) defines a `Content-Encoding` header, which signifies whether the entity body of an HTTP message is
"encoded" and, if so, by which algorithm. The only commonly used content encodings are compression algorithms.

[HTTP 规范](https://tools.ietf.org/html/rfc7231#section-3.1.2.1) 定义了一个 `Content-Encoding` 头域，表示一个 HTTP 消息的实体正文是否已“编码”，如果是的话，用的哪种算法。常用的内容编码是压缩算法。

Currently, Akka HTTP supports the compression and decompression of HTTP requests and responses with the `gzip` or
`deflate` encodings.
The core logic for this lives in the @scala[@scaladoc[akka.http.scaladsl.coding](akka.http.scaladsl.coding.index) package.]@java[@javadoc[akka.http.javadsl.coding.Coder](akka.http.javadsl.coding.Coder) enum class.]

当前，Akka HTTP 支持使用 `gzip` 或 `deflate` 编码对 HTTP 请求和响应进行压缩和解压缩。
核心逻辑在 @scala[@scaladoc[akka.http.scaladsl.coding](akka.http.scaladsl.coding.index) 包]@java[@javadoc[akka.http.javadsl.coding.Coder](akka.http.javadsl.coding.Coder) 枚举] 里。

## Server side
**服务器端**

The support is not enabled automatically, but must be explicitly requested.
For enabling message encoding/decoding with @ref[Routing DSL](../routing-dsl/index.md) see the @ref[CodingDirectives](../routing-dsl/directives/coding-directives/index.md).

该支持不是自动启用的，必须明确地请求。使用 @ref[Routing DSL](../routing-dsl/index.md) 启用消息编码/解码，请见 @ref[CodingDirectives](../routing-dsl/directives/coding-directives/index.md) 。

## Client side
**客户端**

There is currently no high-level or automatic support for decoding responses on the client-side.

当前，对于客户端响应解码没有高级或自动支持。

The following example shows how to decode responses manually based on the `Content-Encoding` header:

下面的例子显示了怎样基于 `Content-Encoding` 头域手动解码响应：

Scala
:   @@snip [HttpClientDecodingExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientDecodingExampleSpec.scala) { #single-request-decoding-example }

Java
:   @@snip [HttpClientDecodingExampleTest.java]($test$/java/docs/http/javadsl/HttpClientDecodingExampleTest.java) { #single-request-decoding-example }
