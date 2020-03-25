# 请求/响应实体流的实质含义
*Implications of the streaming nature of Request/Response Entities*

Akka HTTP is streaming *all the way through*, which means that the back-pressure mechanisms enabled by Akka Streams
are exposed through all layers–from the TCP layer, through the HTTP server, all the way up to the user-facing @apidoc[HttpRequest]
and @apidoc[HttpResponse] and their @apidoc[HttpEntity] APIs.

Akka HTTP 是 *完全* 流式传输的，这意味着 Akka Streams 启用的回压机制在所有层暴露 - 从 TCP 层到 HTTP 服务器，一直到面向用户的
@apidoc[HttpRequest] 和 @apidoc[HttpResponse] 以及它们的 @apidoc[HttpEntity] API。

This has surprising implications if you are used to non-streaming / not-reactive HTTP clients.
Specifically it means that: "*lack of consumption of the HTTP Entity, is signaled as back-pressure to the other
side of the connection*". This is a feature, as it allows one only to consume the entity, and back-pressure servers/clients
from overwhelming our application, possibly causing un-necessary buffering of the entity in memory.

如果你习惯了非流式的/非反应式的 HTTP 客户端，这将产生令人惊讶的影响。特别是，它意味着：“缺少对 HTTP 实体的消费，将向连接的另一端作用回压信号”。
这是一个特性，只允许一个人消费实体（实体只能被消费一次），以及来压倒我们应用的回压服务器/客户端，可能会导致对内存中的实体进行不必要的缓冲。

@@@ warning

Consuming (or discarding) the Entity of a request is mandatory!
If *accidentally* left neither consumed or discarded Akka HTTP will
assume the incoming data should remain back-pressured, and will stall the incoming data via TCP back-pressure mechanisms.
A client should consume the Entity regardless of the status of the @apidoc[HttpResponse].

消费（或丢弃）请求实体（里的数据）是强制性的！
如果 *意外地* 既不消费，也不丢弃实体，Akka HTTP 将假定传入的数据应保持回压状态，并将通过 TCP 回压机制暂停传入数据。
不管 @apidoc[HttpResponse] 的状态码为何，客户端都应该消费实体（译注：或者丢弃，服务对于传入的 @apidoc[HttpRequest] 也一样）。
@@@

## Client-Side handling of streaming HTTP Entities
**流式 HTTP 实体的客户端处理**

### Consuming the HTTP Response Entity (Client)
**消费 HTTP 响应实体（客户端）**

The most common use-case of course is consuming the response entity, which can be done via
running the underlying `dataBytes` Source (or on the server-side using directives such as `BasicDirectives.extractDataBytes`).

最常见的情况是消费响应实体，这可以通过运行底层的 `dataBytes` 源（Source）（或者在服务器端使用 `BasicDirectives.extractDataBytes` 指令）来做到。

It is encouraged to use various streaming techniques to utilise the underlying infrastructure to its fullest,
for example by framing the incoming chunks, parsing them line-by-line and then connecting the flow into another
destination Sink, such as a File or other Akka Streams connector:

这鼓励使用各种流式技术来充分利用底层基础设施。
例如，分帧进入的数据块，将它们按行解析，然后连接到其它目标 Sink，例如一个文件或其它 Akka Streams 连接器：

Scala
:   @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #manual-entity-consume-example-1 }

Java
:   @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #manual-entity-consume-example-1 }

however sometimes the need may arise to consume the entire entity as `Strict` entity (which means that it is
completely loaded into memory). Akka HTTP provides a special @scala[`toStrict(timeout)`]@java[`toStrict(timeout, materializer)`] method which can be used to
eagerly consume the entity and make it available in memory:

但是，有时需要能将整个实体作为 `Strict` 实体（这意味着实体正文将完全加载到内存中）。
Akka HTTP 提供了专门的 @scala[`toStrict(timeout)`]@java[`toStrict(timeout, materializer)`] 方法，用于立即消费实体并使其在内存中可用。

Scala
:   @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #manual-entity-consume-example-2 }

Java
:   @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #manual-entity-consume-example-2 }

### Integrating with Akka Streams
**集成 Akka Streams**

In some cases, it is necessary to process the results of a series of Akka HTTP calls as Akka Streams. In order
to ensure that the HTTP Response Entity is consumed in a timely manner, the Akka HTTP stream for each request must
be executed and completely consumed, then sent along for further processing.

在某些情况下，有必要将一系列 Akka HTTP 调用的结果处理为 Akka 流。为了保证 HTTP 响应实体被及时消费，必须执行每个请求的 Akka HTTP 流并完全消费，
然后将其发送给后续进行处理

*（译注：尽早将实体流消费完成，生成 `Future` 或将 `Future` 通过 `Source.mapAsync` 包装后再发送给后续处理，而不是将实体的 `dataBytes` 直接给到后续）。*

Failing to account for this behavior can result in seemingly non-deterministic failures due to complex interactions
between http and stream buffering. This manifests as errors such as the following:

由于 http 和流缓冲之间的复杂交互，若不考虑到这种行为，可能导致看起来不确定的失败。这表现为下面的错误：

```
Response entity was not subscribed after 1 second. Make sure to read the response entity body or call `discardBytes()` on it.
```

```
响应实体在1秒钟后未被订阅，请确保读取响应实体正文或对其调用 `discardBytes()` 。
```

This error indicates that the http response has been available for too long without being consumed. It can be 
partially worked around by increasing the subscription timeout, but you will still run the risk of running into network 
level timeouts and could still exceed the timeout under load so it's best to resolve the issue properly such as in 
the examples below:

此错误表示，http 响应可以长时间不被消费。这可以通过增加订阅超时来部分解决，但是仍将遇到网络层面超时的网络，并且在加载时仍可能遇到超时，
因此最好妥善解决此问题，如下面例子所示：

Scala
:   @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #manual-entity-consume-example-3 }

Java
:   @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #manual-entity-consume-example-3 }

### Discarding the HTTP Response Entity (Client)
**丢弃 HTTP 响应实体（客户端）**

Sometimes when calling HTTP services we do not care about their response payload (e.g. all we care about is the response code),
yet as explained above entity still has to be consumed in some way, otherwise we'll be exerting back-pressure on the
underlying TCP connection.

有时，当调用 HTTP 服务时我们并不关心它的响应负荷（例如，我们只关心响应状态码），但如上面所述，实体仍然必须被某种方式消费，否则将对底层 TCP 连接施加回压。

The `discardEntityBytes` convenience method serves the purpose of easily discarding the entity if it has no purpose for us.
It does so by piping the incoming bytes directly into an `Sink.ignore`.

`discardEntityBytes` 便利方法的目的是，如果实体对于我们没有意义，可以很容易的丢弃它。它通过将传入的字节直接导入到一个 `Sink.ignore` 来做到。

The two snippets below are equivalent, and work the same way on the server-side for incoming HTTP Requests:

下面的两个代码片段是等效的，在服务端方式处理传入 HTTP 请求的方式相同：

Scala
:   @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #manual-entity-discard-example-1 }

Java
:   @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #manual-entity-discard-example-1 }

Or the equivalent low-level code achieving the same result:

或实现相同效果的等效底级代码:

Scala
:   @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #manual-entity-discard-example-2 }

Java
:   @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/HttpClientExampleDocTest.java) { #manual-entity-discard-example-2 }

## Server-Side handling of streaming HTTP Entities
**流式 HTTP 实体的服务器端处理**

Similarly as with the Client-side, HTTP Entities are directly linked to Streams which are fed by the underlying
TCP connection. Thus, if request entities remain not consumed, the server will back-pressure the connection, expecting
that the user-code will eventually decide what to do with the incoming data.

与客户端类似，HTTP 实体直接链接到由流，这个流是由底层 TCP 连接提供的。因此，如果请求实体保持未消费，服务器将对连接施加回压，期待用户代码最终决定如何处理传入的数据。

Note that some directives force an implicit `toStrict` operation, such as @scala[`entity(as[String])`]@java[`entity(exampleUnmarshaller, example -> {})`] and similar ones.

注意，有些指令强制（执行）一个隐式的 `toStrict` 操作，例如：@scala[`entity(as[String])`]@java[`entity(exampleUnmarshaller, example -> {})`] 和类似的指令。

### Consuming the HTTP Request Entity (Server)
**消费 HTTP 请求实体（服务器端）**

The simplest way of consuming the incoming request entity is to simply transform it into an actual domain object,
for example by using the @ref[entity](routing-dsl/directives/marshalling-directives/entity.md) directive:

消费传入请求实体的简单方式是将其转换为一个实际的域对象，例如通过使用 @ref[entity](routing-dsl/directives/marshalling-directives/entity.md) 指令：

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #consume-entity-directive }

Java
:   @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #consume-entity-directive }

Of course you can access the raw dataBytes as well and run the underlying stream, for example piping it into an
FileIO Sink, that signals completion via a @scala[`Future[IoResult]`]@java[`CompletionStage<IoResult>`] once all the data has been written into the file:

当然，你能访问原始的 `dataBytes` 并运行底层的流，例如将它导入到一个 `FileIO` Sink，一旦所有数据都写入文件，通过 @scala[`Future[IoResult]`]@java[`CompletionStage<IoResult>`] 发出完成信号。

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #consume-raw-dataBytes }

Java
:   @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #consume-raw-dataBytes }

### Discarding the HTTP Request Entity (Server)
**丢弃 HTTP 请求实体（服务器端）**

Sometimes, depending on some validation (e.g. checking if given user is allowed to perform uploads or not)
you may want to decide to discard the uploaded entity.

有时，取决于某些校验（例如：检查特定用户是否允许执行上传），你可能想决定丢弃上传的实体。

Please note that discarding means that the entire upload will proceed, even though you are not interested in the data
being streamed to the server - this may be useful if you are simply not interested in the given entity, however
you don't want to abort the entire connection (which we'll demonstrate as well), since there may be more requests
pending on the same connection still.

请注意，丢弃意味着整个上传将继续，即使你对传到服务器的数据不感兴趣 - 这可能有用，如果你只是对特定实体不感兴趣，但是你不想终止整个连接（就像我们之前演示的那样），
因为在同一个连接上可能还有更多请求等待处理。

In order to discard the databytes explicitly you can invoke the `discardEntityBytes` bytes of the incoming `HTTPRequest`:

为了显式丢弃数据字节，你可以在传入的 `HttpRequest` 上调用 `discardEntityBytes`：

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #discard-discardEntityBytes }

Java
:   @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #discard-discardEntityBytes }

A related concept is *cancelling* the incoming @scala[`entity.dataBytes`]@java[`entity.getDataBytes()`] stream, which results in Akka HTTP
*abruptly closing the connection from the Client*. This may be useful when you detect that the given user should not be allowed to make any
uploads at all, and you want to drop the connection (instead of reading and ignoring the incoming data).
This can be done by attaching the incoming @scala[`entity.dataBytes`]@java[`entity.getDataBytes()`] to a `Sink.cancelled()` which will cancel
the entity stream, which in turn will cause the underlying connection to be shut-down by the server –
effectively hard-aborting the incoming request:

一个相关的概念是 *取消* 传入的 @scala[`entity.dataBytes`]@java[`entity.getDataBytes()`] 流，在 Akka HTTP 中这样的结果为 *突然关闭来自客户端的连接*。
当你检测到不应允许特定用户进行任何上传，并且你想断开连接（而不是读取并忽略传入的数据）时这可能有用。
这可以通过将传入的 @scala[`entity.dataBytes`]@java[`entity.getDataBytes()`] 附着到一个 `Sink.cancelled()` 来做到，这将取消实体流，
反过来将导致底层连接被服务器关闭 - 有效地硬中止传入的请求：

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #discard-close-connections }

Java
:   @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #discard-close-connections }

Closing connections is also explained in depth in the @ref[Closing a connection](server-side/low-level-api.md#http-closing-connection-low-level)
section of the docs.

在文档的 @ref[关闭一个连接](server-side/low-level-api.md#http-closing-connection-low-level) 部分有深入的说明。

### Pending: Automatic discarding of not used entities
**待定：未使用实体的自动丢弃**

Under certain conditions it is possible to detect an entity is very unlikely to be used by the user for a given request,
and issue warnings or discard the entity automatically. This advanced feature has not been implemented yet, see the below
note and issues for further discussion and ideas.

在一定条件下，可能检测到一个实体不太可能被用户用于特定请求，并自动发出警告或丢弃实体。这个高级特性现在还未实现，对于进一步的讨论和想法见下面的说明和问题。

@@@ note

An advanced feature code named "auto draining" has been discussed and proposed for Akka HTTP, and we're hoping
to implement or help the community implement it.

已经讨论并提出了一个高级特性代码：“自动排空”用于 Akka HTTP，我们希望能够实现或帮助社区实现它。

You can read more about it in [issue #183](https://github.com/akka/akka-http/issues/183)
as well as [issue #117](https://github.com/akka/akka-http/issues/117) ; as always, contributions are very welcome!

你可以在 [问题 #183](https://github.com/akka/akka-http/issues/183) 以及 [问题 #117](https://github.com/akka/akka-http/issues/117) 阅读更多内容；
一如既往，非常欢迎贡献！
@@@
