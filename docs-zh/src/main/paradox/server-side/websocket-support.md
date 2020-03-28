# Server WebSocket Support
**Server 网页套接字 支持**

WebSocket is a protocol that provides a bi-directional channel between browser and webserver usually run over an
upgraded HTTP(S) connection. Data is exchanged in messages whereby a message can either be binary data or Unicode text.

WebSocket 是一个在浏览器和 WEB 服务器之间提供双向通道的协议，通常运行在升级的 HTTP(S) 连接上。数据在消息中交换，消息可以是二进制，也可以是 Unicode 文本。

Akka HTTP provides a stream-based implementation of the WebSocket protocol that hides the low-level details of the
underlying binary framing wire-protocol and provides a simple API to implement services using WebSocket.

Akka HTTP 提供了 WebSocket 协议的基于流的实现，它隐藏了底层二进制帧连接协议的低级信息，并提供了一个简单的 API 来使用 WebSocket 实现服务。 

## Model
**模型**

The basic unit of data exchange in the WebSocket protocol is a message. A message can either be binary message,
i.e. a sequence of octets or a text message, i.e. a sequence of Unicode code points.

WebSocket 协议里数据交换的基本单元是消息。一个消息可以是二进制消息，例如：八进制的序列；或者文本消息，例如：Unicode 代码点序列。

In the data model the two kinds of messages, binary and text messages, are represented by the two classes
@apidoc[BinaryMessage] and @apidoc[TextMessage] deriving from a common superclass
@scala[@scaladoc[Message](akka.http.scaladsl.model.ws.Message)]@java[@javadoc[Message](akka.http.javadsl.model.ws.Message)].
@scala[The subclasses @apidoc[BinaryMessage] and @apidoc[TextMessage] contain methods to access the data.]
@java[The superclass @javadoc[Message](akka.http.javadsl.model.ws.Message)
contains `isText` and `isBinary` methods to distinguish a message and `asBinaryMessage` and `asTextMessage` methods to cast a message.
Take the API of @apidoc[TextMessage] as an example (@apidoc[BinaryMessage] is very similar with `String` replaced by @apidoc[akka.util.ByteString])]:

在数据模型中有两类消息，二进制消息和文本消息，它们被表示为从超类 @scala[@scaladoc[Message](akka.http.scaladsl.model.ws.Message)]@java[@javadoc[Message](akka.http.javadsl.model.ws.Message)] 派生的 @apidoc[BinaryMessage] 和 @apidoc[TextMessage] 两个类。
@scala[子类 @apidoc[BinaryMessage] 和 @apidoc[TextMessage] 包含访问数据的方法。]
@java[超类 @javadoc[Message](akka.http.javadsl.model.ws.Message) 包含 `isText` 和 `isBinary` 方法区分消息类型，`asBinaryMessage` 和 `asTextMessage` 方法转换消息类型。
以 @apidoc[TextMessage] 的 API 为例（ @apidoc[BinaryMessage] 与此非常相似，`String` 被替换为 @apidoc[akka.util.ByteString]）]：

Scala
:  @@snip [Message.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/scaladsl/model/ws/Message.scala) { #message-model }

Java
:  @@snip [Message.scala]($akka-http$/akka-http-core/src/main/scala/akka/http/javadsl/model/ws/Message.scala) { #message-model }

The data of a message is provided as a stream because WebSocket messages do not have a predefined size and could
(in theory) be infinitely long. However, only one message can be open per direction of the WebSocket connection,
so that many application level protocols will want to make use of the delineation into (small) messages to transport
single application-level data units like "one event" or "one chat message".

因为 WebSocket 消息没有定义大小且可能（理论上）无限长，所以消息的数据被提供为一个流。但是，每个 WebSocket 连接方向只能打开一个消息，
因此，许多应用级协议希望利用划定（小）消息来传输单个应用级数据单元，如：“一个事件”或“一条聊天消息”。

Many messages are small enough to be sent or received in one go. As an opportunity for optimization, the model provides
the notion of a "strict" message to represent cases where a whole message was received in one go.
@scala[Strict messages are represented with the `Strict` subclass for each kind of message which contains data as a strict, i.e. non-streamed, @apidoc[akka.util.ByteString] or `String`.]
@java[If `TextMessage.isStrict` returns true, the complete data is already available and can be accessed with `TextMessage.getStrictText` (analogously for @apidoc[BinaryMessage]).]

许多消息都很少，可以一次发送或接收。作为一个优化的机会，模型提供“严格”消息的概念，表示一次接收整个消息的情况。
@scala[严格消息由每种消息的 `Strict` 子类表示，这些消息包含的数据是严格的、非流式的 @apidoc[akka.util.ByteString] 或 `String`。]
@java[如果 `TextMessage.isStrict` 返回真，刚全部数据已经可用并且可以使用 `TextMessage.getStrictText` 访问（ @apidoc[BinaryMessage] 也类似）。]

When receiving data from the network connection the WebSocket implementation tries to create a strict message whenever
possible, i.e. when the complete data was received in one chunk. However, the actual chunking of messages over a network
connection and through the various streaming abstraction layers is not deterministic from the perspective of the
application. Therefore, application code must be able to handle both streamed and strict messages and not expect
certain messages to be strict. (Particularly, note that tests against `localhost` will behave differently than tests
against remote peers where data is received over a physical network connection.)

当从网络连接收数据时，WebSocket 实现尝试尽可能创建一个严格消息，既，当完整的数据以一个分块的形式收到时。
但是，从从应用程序的角度看，通过网络连接和各种流抽象层上的消息的实际分块是不确定。因此，应用代码必须能够处理流式和严格两种消息，不能期待消息是严格的。
（特别，注意针对 `localhost` 的测试与针对通过物理网络连接接收数据的远端的测试是不同的。）

For sending data, you can use @scala[`TextMessage.apply(text: String)`]@java[`TextMessage.create(String)`] to create a strict message if the
complete message has already been assembled. Otherwise, use @scala[`TextMessage.apply(textStream: Source[String, \_])`]@java[`TextMessage.create(Source<String, ?>)`]
to create a streaming message from an Akka Stream source.

发送数据，如果完整消息已经组装好，你可以使用 @scala[`TextMessage.apply(text: String)`]@java[`TextMessage.create(String)`] 创建一个严格消息。
否则，使用 @scala[`TextMessage.apply(textStream: Source[String, \_])`]@java[`TextMessage.create(Source<String, ?>)`] 从 Akka Stream 源创建一个流式消息。

## Server API
**服务器 API**

The entrypoint for the WebSocket API is the synthetic @apidoc[UpgradeToWebSocket] header which is added to a request
if Akka HTTP encounters a WebSocket upgrade request.

The WebSocket specification mandates that details of the WebSocket connection are negotiated by placing special-purpose
HTTP-headers into request and response of the HTTP upgrade. In Akka HTTP these HTTP-level details of the WebSocket
handshake are hidden from the application and don't need to be managed manually.

Instead, the synthetic @apidoc[UpgradeToWebSocket] represents a valid WebSocket upgrade request. An application can detect
a WebSocket upgrade request by looking for the @apidoc[UpgradeToWebSocket] header. It can choose to accept the upgrade and
start a WebSocket connection by responding to that request with an @apidoc[HttpResponse] generated by one of the
`UpgradeToWebSocket.handleMessagesWith` methods. In its most general form this method expects two arguments:
first, a handler @scala[@apidoc[Flow[Message, Message, Any]]]@java[@apidoc[Flow[Message, Message, ?]]] that will be used to handle WebSocket messages on this connection.
Second, the application can optionally choose one of the proposed application-level sub-protocols by inspecting the
values of @scala[`UpgradeToWebSocket.requestedProtocols`]@java[`UpgradeToWebSocket.getRequestedProtocols`] and pass the chosen protocol value to @scala[`handleMessages`]@java[`handleMessagesWith`].

### Handling Messages

A message handler is expected to be implemented as a @scala[@apidoc[Flow[Message, Message, Any]]]@java[@apidoc[Flow[Message, Message, ?]]]. For typical request-response
scenarios this fits very well and such a @apidoc[Flow] can be constructed from a simple function by using
@scala[`Flow[Message].map` or `Flow[Message].mapAsync`]@java[`Flow.<Message>create().map` or `Flow.<Message>create().mapAsync`].

There are other use-cases, e.g. in a server-push model, where a server message is sent spontaneously, or in a
true bi-directional scenario where input and output aren't logically connected. Providing the handler as a @apidoc[Flow] in
these cases may not fit. @scala[Another method named `UpgradeToWebSocket.handleMessagesWithSinkSource`]@java[An overload of `UpgradeToWebSocket.handleMessagesWith`] is provided, instead,
which allows to pass an output-generating @apidoc[Source[Message, \_]] and an input-receiving @apidoc[Sink[Message, \_]] independently.

Note that a handler is required to consume the data stream of each message to make place for new messages. Otherwise,
subsequent messages may be stuck and message traffic in this direction will stall.

### Example

Let's look at an @scala[@github[example](/docs/src/test/scala/docs/http/scaladsl/server/WebSocketExampleSpec.scala)]@java[@github[example](/docs/src/test/java/docs/http/javadsl/server/WebSocketCoreExample.java)].

WebSocket requests come in like any other requests. In the example, requests to `/greeter` are expected to be
WebSocket requests:

Scala
:  @@snip [WebSocketExampleSpec.scala]($test$/scala/docs/http/scaladsl/server/WebSocketExampleSpec.scala) { #websocket-request-handling }

Java
:  @@snip [WebSocketCoreExample.java]($test$/java/docs/http/javadsl/server/WebSocketCoreExample.java) { #websocket-handling }

@@@ div { .group-scala }
It uses pattern matching on the path and then inspects the request to query for the @apidoc[UpgradeToWebSocket] header. If
such a header is found, it is used to generate a response by passing a handler for WebSocket messages to the
`handleMessages` method. If no such header is found a `400 Bad Request` response is generated.
@@@

@@@ div { .group-java }
It uses a helper method `akka.http.javadsl.model.ws.WebSocket.handleWebSocketRequestWith` which can be used if
only WebSocket requests are expected. The method looks for the @apidoc[UpgradeToWebSocket] header and returns a response
that will install the passed WebSocket handler if the header is found. If the request is no WebSocket request it will
return a `400 Bad Request` error response.
@@@

In the example, the passed handler expects text messages where each message is expected to contain a (person's) name
and then responds with another text message that contains a greeting:

Scala
:  @@snip [WebSocketExampleSpec.scala]($test$/scala/docs/http/scaladsl/server/WebSocketExampleSpec.scala) { #websocket-handler }

Java
:  @@snip [WebSocketCoreExample.java]($test$/java/docs/http/javadsl/server/WebSocketCoreExample.java) { #websocket-handler }

@@@ note
Inactive WebSocket connections will be dropped according to the @ref[idle-timeout settings](../common/timeouts.md#idle-timeouts).
In case you need to keep inactive connections alive, you can either tweak your idle-timeout or inject
'keep-alive' messages regularly.
@@@

## Routing support

The routing DSL provides the @ref[handleWebSocketMessages](../routing-dsl/directives/websocket-directives/handleWebSocketMessages.md) directive to install a WebSocket handler if a request
is a WebSocket request. Otherwise, the directive rejects the request.

Let's look at how the above example can be rewritten using the high-level routing DSL.

Instead of writing the request handler manually, the routing behavior of the app is defined by a route that
uses the `handleWebSocketRequests` directive in place of the `WebSocket.handleWebSocketRequestWith`:

Scala
:  @@snip [WebSocketDirectivesExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/directives/WebSocketDirectivesExamplesSpec.scala) { #greeter-service }

Java
:  @@snip [WebSocketRoutingExample.java]($test$/java/docs/http/javadsl/server/WebSocketRoutingExample.java) { #websocket-route }

The handling code itself will be the same as with using the low-level API.

@@@ div { .group-scala }
The example also includes code demonstrating the testkit support for WebSocket services. It allows to create WebSocket
requests to run against a route using *WS* which can be used to provide a mock WebSocket probe that allows manual
testing of the WebSocket handler's behavior if the request was accepted.
@@@

See the @github[full routing example](/docs/src/test/java/docs/http/javadsl/server/WebSocketCoreExample.java).

<a id="keep-alive-ping"></a>

## Automatic keep-alive Ping support

For long running websocket connections it may be beneficial to enable automatic heartbeat using `Ping` frames.
Those are often used as a way to keep otherwise idle connections from being closed and also a way of ensuring the 
connection remains usable even after no data frames are communicated over a longer period of time. Such heartbeat may be 
initiated by either side of the connection, and the choice which side performs the heart beating is use-case dependent. 

This is supported in a transparent way via configuration in Akka HTTP, and you can enable it by setting the: 
`akka.http.server.websocket.periodic-keep-alive-max-idle = 1 second` to a specified max idle timeout. The keep alive triggers
when no other messages are in-flight during the such configured period. Akka HTTP will then automatically send
a [`Ping` frame](https://tools.ietf.org/html/rfc6455#section-5.5.2) for each of such idle intervals.

By default, the automatic keep-alive feature is disabled.

### Custom keep-alive data payloads

By default, pings do not carry any payload, as it is often enough to simply push *any* frame over the connection
to ensure the connection stays healthy (or detect if it was severed), however you may configure them to carry a custom 
payload, to do this you can provide a function that will be asked to emit the payload for each of the ping messages generated:

Scala
:  @@snip [WebSocketExampleSpec.scala]($test$/scala/docs/http/scaladsl/server/WebSocketExampleSpec.scala) { #websocket-ping-payload-server }

Java
:  @@snip [WebSocketCoreExample.java]($test$/java/docs/http/javadsl/server/WebSocketCoreExample.java) { #websocket-ping-payload-server }

### Uni-directional Pong keep-alive


A Ping response will always be replied to by the client-side with an appropriate `Pong` reply, carrying the same payload.
It is also possible to configure the keep-alive mechanism to send `Pong` frames instead of `Ping` frames, 
which enables an [uni-directional heartbeat](https://tools.ietf.org/html/rfc6455#section-5.5.3) mechanism (in which case 
the client side will *not* reply to such heartbeat). You can configure this mode by setting: 
`akka.http.server.websocket.periodic-keep-alive-mode = pong`.
