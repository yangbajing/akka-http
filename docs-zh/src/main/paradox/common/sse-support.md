# 服务器-发送事件支持
*Server-Sent Events Support*

Server-Sent Events (SSE) is a lightweight and [standardized](https://www.w3.org/TR/eventsource)
protocol for pushing notifications from a HTTP server to a client. In contrast to WebSocket, which
offers bi-directional communication, SSE only allows for one-way communication from the server to
the client. If that's all you need, SSE has the advantages to be much simpler, to rely on HTTP only
and to offer retry semantics on broken connections by the browser.

服务器-发送事件（SSE）是一个轻量级、[标准化](https://www.w3.org/TR/eventsource) 协议，用于从 HTTP 服务器推送通知到客户端。
与 WebSocket 相反，它提供双向通信，而 SSE 只允许从服务器到客户端一种通信方式。如果这就是你需要的，SSE 的优势在于更简单，它只依赖 HTTP，
并且在浏览器断开的连接上提供重试语义。

According to the SSE specification clients can request an event stream from the server via HTTP. The
server responds with the media type `text/event-stream` which has the fixed character encoding UTF-8
and keeps the response open to send events to the client when available. Events are textual
structures which carry fields and are terminated by an empty line, e.g.

根据 SSE 规范，客户端可以通过 HTTP 从服务器请求一个事件流。
服务器用具有固定字符编码 UTF-8 的媒体类型 `text/event-stream` 应答，并且保持响应打开，在可用时发送事件到客户端。
事件是文本结构，携带字段并以以空行结尾。例如：

```
data: { "username": "John Doe" }
event: added
id: 42

data: another event
```

Clients can optionally signal the last seen event to the server via the @scala[`Last-Event-ID`]@java[`LastEventId`] header, e.g.
after a reconnect.

客户端可以选择通过 @scala[`Last-Event-ID`]@java[`LastEventId`] 头域最后看到的事件发送到服务器，例如：重新连接后。

*译注：客户端可以保存 @scala[`Last-Event-ID`]@java[`LastEventId`] 的值，在重连后以此恢复正常处理。*

## Model
**模型**

Akka HTTP represents event streams as @apidoc[Source[ServerSentEvent, \_]] where @apidoc[ServerSentEvent] is a
@scala[case] class with the following read-only properties:

Akka HTTP 维护事件流为 @apidoc[Source[ServerSentEvent, \_]]，其中 @apidoc[ServerSentEvent] 是一个 @scala[case] 类，具有下面的只读属性：

- @scala[`data: String`]@java[`String data`] – the actual payload, may span multiple lines
    - 真实的负荷，可能跨多行
- @scala[`eventType: Option[String]`]@java[`Optional<String> type`] – optional qualifier, e.g. "added", "removed", etc.
    - 可选的修饰，例如：`added`、`removed` 等
- @scala[`id: Option[String]`]@java[`Optional<String> id`] – optional identifier
    - 可选的标识符
- @scala[`retry: Option[Int]`]@java[`OptionalInt retry`] – optional reconnection delay in milliseconds
    - 可选的重新连接延迟（毫秒）

In accordance to the SSE specification Akka HTTP also provides the @scala[`Last-Event-ID`]@java[`LastEventId`] header and the
@scala[`text/event-stream`]@java[`TEXT_EVENT_STREAM`] media type.

## Server-side usage: marshalling
**服务器端使用方法：编组**

In order to respond to a HTTP request with an event stream, you have to
@scala[bring the implicit `ToResponseMarshaller[Source[ServerSentEvent, \_]]` defined by @apidoc[EventStreamMarshalling] into the scope defining the respective route]@java[use the `EventStreamMarshalling.toEventStream` marshaller]:

为了使用事件流应答一个 HTTP 请求，你需要 @scala[提供由 @apidoc[EventStreamMarshalling] 定义的隐式转换 `ToResponseMarshaller[Source[ServerSentEvent, \_]]` 到各自路由的作用域范围]@java[使用 `EventStreamMarshalling.toEventStream` 编组]:

Scala
:  @@snip [ServerSentEventsExampleSpec.scala]($test$/scala/docs/http/scaladsl/ServerSentEventsExampleSpec.scala) { #event-stream-marshalling-example }

Java
:  @@snip [EventStreamMarshallingTest.java]($akka-http$/akka-http-tests/src/test/java/akka/http/javadsl/marshalling/sse/EventStreamMarshallingTest.java) { #event-stream-marshalling-example }

## Client-side usage: unmarshalling
**客户端使用方法：解组**

In order to unmarshal an event stream as @apidoc[Source[ServerSentEvent, \_]], you have to @scala[bring the implicit `FromEntityUnmarshaller[Source[ServerSentEvent, _]]` defined by @apidoc[EventStreamUnmarshalling] into scope]@java[use the `EventStreamUnmarshalling.fromEventsStream` unmarshaller]:

为了解组一个事件流为 @apidoc[Source[ServerSentEvent, \_]] ，你需要 @scala[提供由 @apidoc[EventStreamUnmarshalling] 定义的隐式转换 `FromEntityUnmarshaller[Source[ServerSentEvent, _]]` 到作用域范围]@java[使用 `EventStreamUnmarshalling.fromEventsStream` 解组]:

Scala
:  @@snip [ServerSentEventsExampleSpec.scala]($test$/scala/docs/http/scaladsl/ServerSentEventsExampleSpec.scala) { #event-stream-unmarshalling-example }

Java
:  @@snip [EventStreamMarshallingTest.java]($akka-http$/akka-http-tests/src/test/java/akka/http/javadsl/unmarshalling/sse/EventStreamUnmarshallingTest.java) { #event-stream-unmarshalling-example }

Notice that if you are looking for a resilient way to permanently subscribe to an event stream,
Alpakka provides the [EventSource](https://developer.lightbend.com/docs/alpakka/current/sse.html)
connector which reconnects automatically with the id of the last seen event.

注意，如果你正在寻找永久订阅事件流的弹性方法，那么 Alpakka 提供的 [EventSource](https://developer.lightbend.com/docs/alpakka/current/sse.html) 连接器可以使用最后看到的事件 id 自动重连。
