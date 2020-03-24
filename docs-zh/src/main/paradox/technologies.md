# 7. 支持的技术

This page gives an overview over the technologies that Akka HTTP implements, supports, and integrates with. The page is
still quite new. If you are looking for support of some technology and found information somewhere else, please help us fill
out this page using the link at the bottom.

这个页面概述了 Akka HTTP 实现、支持和集成的技术。该页面还很新。如果你正在寻找某种技术的支持并在其它地方找到了信息，请使用底部的链接来帮助我们完善这个页面。

## HTTP

Akka HTTP implements HTTP/1.1 including these features (non-exclusive list):

Akka HTTP 实现的 HTTP/1.1 包含这些功能（非完整性清单）

 * Persistent connections
 * HTTP Pipelining (currently not supported on the client-side)
 * 100-Continue
 * @ref[Client Connection Pooling](client-side/request-level.md)
 
 - 持久化连接
 - HTTP 管道（当前不支持客户端）
 - 100-Continue
 - @ref[Client 连接池](client-side/request-level.md)

## HTTPS

HTTPS is supported through the facilities that Java provides. See @ref[Server HTTPS Support](server-side/server-https-support.md)
and @ref[Client HTTPS Support](client-side/client-https-support.md) for more information.

通过 Java 提供的功能支持 HTTPS。有关更多信息见 @ref[服务器 HTTPS 支持](server-side/server-https-support.md) 和 @ref[客户端 HTTPS 支持](client-side/client-https-support.md) 。

## WebSocket

Akka HTTP implements WebSocket on both the server side and the client side. See @ref[Server Websocket Support](server-side/websocket-support.md)
and @ref[Client Websocket Support](client-side/websocket-support.md) for more information.

Akka HTTP 在服务器端和客户端都实现了 WebSocket。有关更多信息见 @ref[服务器 Websocket 支持](server-side/websocket-support.md) 和and @ref[客户端 Websocket 支持](client-side/websocket-support.md) 。

## HTTP/2

Akka HTTP provides server-side HTTP/2 support currently in a preview version. See @ref[Server HTTP/2 Support](server-side/http2.md)
for more information.

Akka HTTP 提供了服务器端 HTTP/2 支持，当前为预览版本。有关更多信息见 @ref[服务器 HTTP/2 支持](server-side/http2.md) 。

## Multipart

Akka HTTP has modeled multipart/* payloads. It provides streaming multipart parsers and renderers e.g. for parsing
file uploads and provides a typed model to access details of such a payload.

Akka HTTP 已对 multipart/* 负荷建模。提供流式 multipart 解析和渲染，例如：用于文件上传解析并提供类型化模型来访问此负荷的详细信息。

## Server-sent Events (SSE)

Server-sent Events (SSE) are supported through marshalling that will provide or consume an (Akka Stream based) stream of
events. See @ref[SSE Support](common/sse-support.md) for more information.

通过编组提供或消费事件流（基于 Akka Stream）支持服务器-发送事件（SSE）。有关更多信息见 @ref[SSE 支持](common/sse-support.md) 。

## JSON

Marshalling to and from JSON is supported out of the box for spray-json-based model in Scala and Jackson-based models in
Java. See @ref[JSON Support](common/json-support.md) for more information.

Scala 中基于 spary-json，Java 中基于 Jackson ，为编组到 JSON，从 JSON 解组提供了开箱即用的支持。
有送更多信息见 @ref[JSON Support](common/json-support.md) 。

## XML

Marshalling to and from XML is supported Scala XML literals. See @ref[XML Support](common/xml-support.md) for more information.

支持 Scala XML 字面量解组到 XML，从 XML 解组。有关更多信息见 @ref[XML Support](common/xml-support.md) 。

## Gzip and Deflate Content-Encoding

GZIP and Deflate content-encodings for automatic encoding / decoding of HTTP payloads are supported through directives.
See @ref[CodingDirectives](routing-dsl/directives/coding-directives/index.md) for more information.

通过指令支持用于 HTTP 负荷的 GZIP 和 Deflate 内容编码格式的自动编码 / 解码。有关更多信息见 @ref[CodingDirectives](routing-dsl/directives/coding-directives/index.md) 。
