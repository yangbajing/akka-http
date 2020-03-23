# HTTP Model
**HTTP 模型**

Akka HTTP model contains a deeply structured, fully immutable, case-class based model of all the major HTTP data
structures, like HTTP requests, responses and common headers.
It lives in the *akka-http-core* module and forms the basis for most of Akka HTTP's APIs.

Akka HTTP 模型包含一套，结构严密，全不可变，基于 case-class 模型对应主要的 HTTP 数据结构，例如 HTTP 请求、响应和通用的（HTTP）头。
这套模型在 *akka-http-core* 模块里，并构成了大部分 Akka HTTP API 基础。

## Overview
**概述**

Since akka-http-core provides the central HTTP data structures you will find the following import in quite a
few places around the code base (and probably your own code as well):

既然 akka-http-core 提供了主要的 HTTP 数据类型，你会发现以下的引入会经常出现在相当多的代码里（或许还有你自己的代码）：

Scala
:   @@snip [ModelSpec.scala]($test$/scala/docs/http/scaladsl/ModelSpec.scala) { #import-model }

Java
:   @@snip [ModelDocTest.java]($test$/java/docs/http/javadsl/ModelDocTest.java) { #import-model }

This brings all of the most relevant types in scope, mainly:

这个包基本上引入了包括全部的主要相关类型，比如：

 * @apidoc[HttpRequest] and @apidoc[HttpResponse], the central message model
 * `headers`, the package containing all the predefined HTTP header models and supporting types
 * Supporting types like @apidoc[Uri], @apidoc[HttpMethods$], @apidoc[MediaTypes$], @apidoc[StatusCodes$], etc.
 
 * @apidoc[HttpRequest] 和 @apidoc[HttpResponse]，主要的消息模型；
 * `headers`, 这个包包含了所有预定义的 HTTP 头模型和支持的辅助类型；
 * 支持的辅助类型如： @apidoc[Uri]、 @apidoc[HttpMethods$]、 @apidoc[MediaTypes$]、 @apidoc[StatusCodes$] 等等。
    

A common pattern is that the model of a certain entity is represented by an immutable type (class or trait),
while the actual instances of the entity defined by the HTTP spec live in an accompanying object carrying the name of
the type plus a trailing plural 's'.

在 Akka HTTP 的包定义规则里，一个比较通用的做法是，一个数据类型的抽象一般都是用一个不可变的类型（可以是 class 或者 trait）进行描述，而对应 HTTP 规范中具体的实例／值则由其对应的伴生对象生成并存放。伴生对象的命名规则为相关定义的名称复数，也就是在其对应规范类型名称后加‘s'

For example:

例如：

 * Defined @apidoc[HttpMethod] instances @scala[live in]@java[are defined as static fields of] the @apidoc[HttpMethods$] @scala[object]@java[class].
 * Defined @apidoc[HttpCharset] instances @scala[live in]@java[are defined as static fields of] the @apidoc[HttpCharsets$] @scala[object]@java[class].
 * Defined @apidoc[HttpEncoding] instances @scala[live in]@java[are defined as static fields of] the @apidoc[HttpEncodings$] @scala[object]@java[class].
 * Defined @apidoc[HttpProtocol] instances @scala[live in]@java[are defined as static fields of] the @apidoc[HttpProtocols$] @scala[object]@java[class].
 * Defined @apidoc[MediaType] instances @scala[live in]@java[are defined as static fields of] the @apidoc[MediaTypes$] @scala[object]@java[class].
 * Defined @apidoc[StatusCode] instances @scala[live in]@java[are defined as static fields of] the @apidoc[StatusCodes$] @scala[object]@java[class].

 * 定义 @apidoc[HttpMethod] 实例 @scala[置于]@java[成一个静态字段置于] @apidoc[HttpMethods$] @scala[object]@java[class].
 * 定义 @apidoc[HttpCharset] 实例 @scala[置于]@java[成一个静态字段置于] @apidoc[HttpCharsets$] @scala[object]@java[class].
 * 定义 @apidoc[HttpEncoding] 实例 @scala[置于]@java[成一个静态字段置于] @apidoc[HttpEncodings$] @scala[object]@java[class].
 * 定义 @apidoc[HttpProtocol] 实例 @scala[置于]@java[成一个静态字段置于] @apidoc[HttpProtocols$] @scala[object]@java[class].
 * 定义 @apidoc[MediaType] 实例 @scala[置于]@java[成一个静态字段置于] @apidoc[MediaTypes$] @scala[object]@java[class].
 * 定义 @apidoc[StatusCode] 实例 @scala[置于]@java[成一个静态字段置于] @apidoc[StatusCodes$] @scala[object]@java[class].

## HttpRequest

@apidoc[HttpRequest] and @apidoc[HttpResponse] are the basic @scala[case]@java[immutable] classes representing HTTP messages.

@apidoc[HttpRequest] 和 @apidoc[HttpResponse] 是表示 HTTP 消息的基本 @scala[case]@java[immutable] 类。

An @apidoc[HttpRequest] consists of

一个 @apidoc[HttpRequest] 包括

 * a method (GET, POST, etc.)
 * a URI (see @ref[URI model](uri-model.md) for more information)
 * a seq of headers
 * an entity (body data)
 * a protocol

 * 请求方法 (GET, POST, 等等)
 * URI 地址（有关更多信息见 @ref[URI 模型](uri-model.md)）
 * （HTTP）头列表
 * 实体（正文数据）
 * 协议

Here are some examples how to construct an @apidoc[HttpRequest]:

这是如何构造一个 @apidoc[HttpRequest] 的一些示例：

Scala
:   @@snip [ModelSpec.scala]($test$/scala/docs/http/scaladsl/ModelSpec.scala) { #construct-request }

Java
:   @@snip [ModelDocTest.java]($test$/java/docs/http/javadsl/ModelDocTest.java) { #construct-request }

@@@ div { .group-scala }
All parameters of `HttpRequest.apply` have default values set, so `headers` for example don't need to be specified
if there are none. Many of the parameters types (like @apidoc[HttpEntity] and @apidoc[Uri]) define implicit conversions
for common use cases to simplify the creation of request and response instances.

`HttpRequest.apply` 的所有参数都有设置默认值，因此如果 HTTP 请求头，则不需要指定 `headers` 参数。
许多参数类型（如： @apidoc[HttpEntity] 和 @apidoc[Uri]）为常见用例定义了隐式转换，以简化请求和响应实例的生成。
@@@
@@@ div { .group-java }
In its basic form `HttpRequest.create` creates an empty default GET request without headers which can then be
transformed using one of the `withX` methods, `addHeader`, or `addHeaders`. Each of those will create a
new immutable instance, so instances can be shared freely. There exist some overloads for `HttpRequest.create` that
simplify creating requests for common cases. Also, to aid readability, there are predefined alternatives for `create`
named after HTTP methods to create a request with a given method and URI directly.

`HttpRequest.create` 以基本形式创建一个没有请求头的空的默认 GET 请求，然后可以通过使用 `withX` 的其中一个方法对其修改。如 `addHeader` 或 `addHeaders`。
因为每个 `withX` 方法都将创建一个新的不可变实例，所以实例可以自由共享（译注：多个请求头实例之间可以共享相同的 header、uri、protocol等）。
`HttpRequest.create` 存在一些重载方法来简化常见情况下请求的创建。另外，为了提供可读性，预定义了以 `create` 名字后接 HTTP 方法名的替代方法，用以直接使用结合实际的方法和 URI 创建请求。
@@@

<a id="synthetic-headers"></a>
### Synthetic Headers
**合成 HTTP 头**

In some cases it may be necessary to deviate from fully RFC-Compliant behavior. For instance, Amazon S3 treats
the `+` character in the path part of the URL as a space, even though the RFC specifies that this behavior should
be limited exclusively to the query portion of the URI.

某些情况下可能有必要偏离完全符合 RFC 的行为。例如，Amazon S3 将 URL 路径部分中的 `+` 字符当作空格，即使 RFC 规定此行为仅限于 URI 的查询部分。  

In order to work around these types of edge cases, Akka HTTP provides for the ability to provide extra,
non-standard information to the request via synthetic headers. These headers are not passed to the client
but are instead consumed by the request engine and used to override default behavior.

为解决这些边缘情况，Akka HTTP 提供了通过合成头向请求提供非标准信息的能力。这些头不会传递给客户端，但由请求引擎消息，并且用于覆盖默认设置。

For instance, in order to provide a raw request uri, bypassing the default url normalization, you could do the
following:

例如：为了提供一个原始请求 uri，绕过默认的 url 规范化，你可按下面方式做：

Scala
:   @@snip [ModelSpec.scala]($test$/scala/docs/http/scaladsl/ModelSpec.scala) { #synthetic-header-s3 }

Java
:   @@snip [ModelDocTest.java]($test$/java/docs/http/javadsl/ModelDocTest.java) { #synthetic-header-s3 }

## HttpResponse

An @apidoc[HttpResponse] consists of

一个 @apidoc[HttpResponse] 包括

 * a status code
 * a @scala[`Seq`]@java[list] of headers
 * an entity (body data)
 * a protocol

 * 状态码
 * （HTTP）头 @scala[`Seq`]@java[list]
 * 实体 (正文数据)
 * 协议

Here are some examples how to construct an @apidoc[HttpResponse]:

这是如何构造一个 @apidoc[HttpResponse] 的一些示例：

Scala
:   @@snip [ModelSpec.scala]($test$/scala/docs/http/scaladsl/ModelSpec.scala) { #construct-response }

Java
:   @@snip [ModelDocTest.java]($test$/java/docs/http/javadsl/ModelDocTest.java) { #construct-response }

In addition to the simple @scala[@apidoc[HttpEntity] constructors]@java[`HttpEntities.create` methods] which create an entity from a fixed `String` or @apidoc[akka.util.ByteString]
as shown here the Akka HTTP model defines a number of subclasses of @apidoc[HttpEntity] which allow body data to be specified as a
stream of bytes. @java[All of these types can be created using the method on `HttpEntites`.]

除了从固定的 `String` 或 @apidoc[akka.util.ByteString] 创建实体的简单 @scala[@apidoc[HttpEntity] 构造器]@java[`HttpEntities.create` 方法]，
Akka HTTP 模型还定义了大量 @apidoc[HttpEntity] 的子类型处理字节学流式的正文数据。@java[所以这些类型都可以通过 `HttpEntites` 上的方法创建。]

<a id="httpentity"></a>
## HttpEntity

An @apidoc[HttpEntity] carries the data bytes of a message together with its Content-Type and, if known, its Content-Length.
In Akka HTTP there are five different kinds of entities which model the various ways that message content can be
received or sent:

一个 @apidoc[HttpEntity] 携带消息的数据字节以及它的 Content-Type 和 Content-Length（如果知道）。
在 Akka HTTP 里存在五种不同的实体，可以接收或发送各种方式的消息内容。

@scala[HttpEntity.Strict]@java[HttpEntityStrict]
: The simplest entity, which is used when all the entity are already available in memory.
It wraps a plain @apidoc[akka.util.ByteString] and  represents a standard, unchunked entity with a known `Content-Length`.

: 最简单的实体，用于当整个实体在内存里可用时。它把一个 @apidoc[akka.util.ByteString] 包装成一个标准的，非分块的，以及带有已知 `Content-Length`（内容长度）的实体。 

@scala[HttpEntity.Default]@java[HttpEntityDefault]
: The general, unchunked HTTP/1.1 message entity.
It has a known length and presents its data as a @apidoc[Source[ByteString, \_]] which can be only materialized once.
It is an error if the provided source doesn't produce exactly as many bytes as specified.
The distinction of @scala[`Strict`]@java[`HttpEntityStrict`] and @scala[`Default`]@java[`HttpEntityDefault`] is an API-only one. On the wire,
both kinds of entities look the same.

: 通用的，非分块的 HTTP/1.1 消息实体。它具有已知长度并其内容数据为一个 @apidoc[Source[ByteString, \_]] ，该实体只能被实例化一次。
如果提供的数据来源不能生成与指定长度完全相同的字节，则它是一个错误。@scala[`Strict`]@java[`HttpEntityStrict`] 和 @scala[`Default`]@java[`HttpEntityDefault`] 的区别只在 API 上。
在实际传输上，两者看起来一样的。

@scala[HttpEntity.Chunked]@java[HttpEntityChunked]
: The model for HTTP/1.1 [chunked content](https://tools.ietf.org/html/rfc7230#section-4.1) (i.e. sent with `Transfer-Encoding: chunked`).
The content length is unknown and the individual chunks are presented as a @scala[`Source[HttpEntity.ChunkStreamPart]`]@java[@apidoc[Source[ChunkStreamPart, ?]]].
A `ChunkStreamPart` is either a non-empty @scala[`Chunk`]@java[chunk] or @scala[a `LastChunk`]@java[the empty last chunk] containing optional trailer headers.
The stream consists of zero or more @scala[`Chunked`]@java[non-empty chunks] parts and can be terminated by an optional @scala[`LastChunk` part]@java[last chunk].

: 模型为 HTTP/1.1 [分块内容](https://tools.ietf.org/html/rfc7230#section-4.1)（既，发送时使用 `Transfer-Encoding: chunked` HTTP 头）。
内容长度是未知的且单独的块被表示为 @scala[`Source[HttpEntity.ChunkStreamPart]`]@java[@apidoc[Source[ChunkStreamPart, ?]]] 。
一个 `ChunkStreamPart` 是非空的 @scala[`Chunk`]@java[chunk] 或 包含可选头的 @scala[a `LastChunk`]@java[空的最后一块] 。
流包括零个或多个 @scala[`Chunked`]@java[非空块] 部分，且能被一个可选的 @scala[`LastChunk` 部分]@java[最后块] 终止。

@scala[HttpEntity.CloseDelimited]@java[HttpEntityCloseDelimited]
: An unchunked entity of unknown length that is implicitly delimited by closing the connection (`Connection: close`).
The content data are presented as a @apidoc[Source[ByteString, \_]].
Since the connection must be closed after sending an entity of this type it can only be used on the server-side for
sending a response.
Also, the main purpose of `CloseDelimited` entities is compatibility with HTTP/1.0 peers, which do not support
chunked transfer encoding. If you are building a new application and are not constrained by legacy requirements you
shouldn't rely on `CloseDelimited` entities, since implicit terminate-by-connection-close is not a robust way of
signaling response end, especially in the presence of proxies. Additionally this type of entity prevents connection
reuse which can seriously degrade performance. Use @scala[`HttpEntity.Chunked`]@java[`HttpEntityChunked`] instead!

: 未知长度的非块实体，它的长度由连接闭关隐式界定（`Connection: close`）。内容数据被表示为 @apidoc[Source[ByteString, \_]] 。
因此，这个类型的实体发送后连接必需被关闭，它只能用于服务端发送响应。
同时，设计 `CloseDelimited` 实体的主要原因是兼容 HTTP/1.0，因为旧的协议不支持分块传输编码。如果你正在构建新的应用程序，且没有受遗留需求的约束，
不应该使用 `CloseDelimited` ，因为隐式的连接关闭时终止（terminate-by-connection-close）是不可靠的信令响应结束方式，特别是在代理的情况下（现今的服务大量放置在代理服务器后）。
再加上这个类型的实体阻止连接复用，会严重影响性能。使用 @scala[`HttpEntity.Chunked`]@java[`HttpEntityChunked`] 替代它！

@scala[HttpEntity.IndefiniteLength]@java[HttpEntityIndefiniteLength]
: A streaming entity of unspecified length for use in a `Multipart.BodyPart`.

: 在 `Multipart.BodyPart` 中使用的未指定长度的流式实体。

Entity types @scala[`Strict`]@java[`HttpEntityStrict`], @scala[`Default`]@java[`HttpEntityDefault`], and @scala[`Chunked`]@java[`HttpEntityChunked`] are a subtype of @scala[`HttpEntity.Regular`]@java[@apidoc[RequestEntity]]
which allows to use them for requests and responses. In contrast, @scala[`HttpEntity.CloseDelimited`]@java[`HttpEntityCloseDelimited`] can only be used for responses.

实体类型 @scala[`Strict`]@java[`HttpEntityStrict`]、@scala[`Default`]@java[`HttpEntityDefault`] 和 @scala[`Chunked`]@java[`HttpEntityChunked`] 是 @scala[`HttpEntity.Regular`]@java[@apidoc[RequestEntity]] 的子类型。
它们适用于请求和响应。相反，@scala[`HttpEntity.CloseDelimited`]@java[`HttpEntityCloseDelimited`] 只能被用于响应。

Streaming entity types (i.e. all but @scala[`Strict`]@java[`HttpEntityStrict`]) cannot be shared or serialized. To create a strict, shareable copy of an
entity or message use `HttpEntity.toStrict` or `HttpMessage.toStrict` which returns a @scala[`Future`]@java[`CompletionStage`] of the object with
the body data collected into a @apidoc[akka.util.ByteString].

流式实体类型（@scala[`Strict`]@java[`HttpEntityStrict`]除外）不能被共享或序列化。要创建一个严格的（strict）、可共享的实体或消息复本，可使用 `HttpEntity.toStrict` 或 `HttpMessage.toStrict` 返回一个 @scala[`Future`]@java[`CompletionStage`] ，正文数据被收集到一个 @apidoc[akka.util.ByteString] 里面。

@@@note { title=译注 }
1. 为什么需要 `toStrict`？ TODO
2. `toStrict` 为什么需要使用 `Future` 包裹？ TODO
@@@

The @scala[@apidoc[HttpEntity] companion object]@java[class `HttpEntities`] contains @scala[several helper constructors]@java[static methods] to create entities from common types easily.

在 @scala[@apidoc[HttpEntity] 伴生对像]@java[`HttpEntities` 类] 包含 @scala[一些辅助构造函数]@java[静态方法] 使从常用类型创建实体更容易。

You can @scala[pattern match over]@java[use] the @scala[subtypes]@java[`isX` methods] of @apidoc[HttpEntity] @java[to find out of which subclass an entity is] if you want to provide
special handling for each of the subtypes. However, in many cases a recipient of an @apidoc[HttpEntity] doesn't care about
of which subtype an entity is (and how data is transported exactly on the HTTP layer). Therefore, the general method
@scala[`HttpEntity.dataBytes`]@java[`HttpEntity.getDataBytes()`] is provided which returns a @apidoc[Source[ByteString, \_]] that allows access to the data of an
entity regardless of its concrete subtype.

如果要对每个子类型进行特殊处理，可以 @scala[在]@java[使用] @apidoc[HttpEntity] @scala[的子类型上进行模式匹配]@java[上的 `isX` 方法找出实体属于哪个子类型]。
然而，在大部分情况下一个 @apidoc[HttpEntity] 的接收方并不关心实体的子类型（以及数据在 HTTP 层是如何传输的）。那么，通用方法 @scala[`HttpEntity.dataBytes`]@java[`HttpEntity.getDataBytes()`] 返回一个 @apidoc[Source[ByteString, \_]] ，在不管它的具体子类型的情况下允许访问实体的数据。

@@@ note { title='When to use which subtype?' }
*什么时候使用哪种子类型？*

 * Use @scala[`Strict`]@java[`HttpEntityStrict`] if the amount of data is "small" and already available in memory (e.g. as a `String` or @apidoc[akka.util.ByteString])
 * Use @scala[`Default`]@java[`HttpEntityDefault`] if the data is generated by a streaming data source and the size of the data is known
 * Use @scala[`Chunked`]@java[`HttpEntityChunked`] for an entity of unknown length
 * Use @scala[`CloseDelimited`]@java[`HttpEntityCloseDelimited`] for a response as a legacy alternative to @scala[`Chunked`]@java[`HttpEntityChunked`] if the client
doesn't support chunked transfer encoding. Otherwise use @scala[`Chunked`]@java[`HttpEntityChunked`]!
 * In a `Multipart.BodyPart` use @scala[`IndefiniteLength`]@java[`HttpEntityIndefiniteLength`] for content of unknown length.

 * @scala[`Strict`]@java[`HttpEntityStrict`]，如果数据里“小”并且已经在内存中可用（例如：`String` or @apidoc[akka.util.ByteString]）；
 * @scala[`Default`]@java[`HttpEntityDefault`]，如果数据由流数据源产生且数据的大小已知；
 * @scala[`Chunked`]@java[`HttpEntityChunked`]，数据长度未知的实体；
 * 如果客户端不支持分块传输编码，使用 @scala[`CloseDelimited`]@java[`HttpEntityCloseDelimited`] 作为响应替代 @scala[`Chunked`]@java[`HttpEntityChunked`] 。否则使用 @scala[`Chunked`]@java[`HttpEntityChunked`]！
 * 在 `Multipart.BodyPart` 中，@scala[`IndefiniteLength`]@java[`HttpEntityIndefiniteLength`] 用于未知长度的内容。
@@@

@@@ warning { title="Caution" }
*警告*

When you receive a non-strict message from a connection then additional data are only read from the network when you
request them by consuming the entity data stream. This means that, if you *don't* consume the entity stream then the
connection will effectively be stalled. In particular no subsequent message (request or response) will be read from
the connection as the entity of the current message "blocks" the stream.
Therefore you must make sure that you always consume the entity data, even in the case that you are not actually
interested in it!

当你从连接上收到一个非严格的消息时，只有当消费实体数据流时，才会继续中网络上读取额外的数据。这意味着，如果你 *停止* 消费实体流，则连接将停摆。
连接上后续的消息（请求或响应）将不会被读取，因为当前消息的实体“阻塞”了这个流。为此，你必需确认你总是消费了实体数据，即使你对它没有兴趣！
（译注：使用 `entity.discardBytes` 忽略实体数据）
@@@

### Limiting message entity length
**限制消息实体长度**

All message entities that Akka HTTP reads from the network automatically get a length verification check attached to
them. This check makes sure that the total entity size is less than or equal to the configured
`max-content-length` <a id="^1" href="#1">[1]</a>, which is an important defense against certain Denial-of-Service attacks.
However, a single global limit for all requests (or responses) is often too inflexible for applications that need to
allow large limits for *some* requests (or responses) but want to clamp down on all messages not belonging into that
group.

Akka HTTP 从网络上读取的所有消息实体都会自动获得附加的一个长度检查。该检查确认实体总大小小于等于配置的 `max-content-length` <a id="^1" href="#1">[1]</a>，
以便作为对抗某些拒绝服务攻击的重要防范手段。然而，对于需要允许 *某些* 请求（或响应）有较大的限制，而要限制不属于该组的所有消息的应用程序时，
对所有请求（或响应）的全局限制不够灵活。

In order to give you maximum flexibility in defining entity size limits according to your needs the @apidoc[HttpEntity]
features a `withSizeLimit` method, which lets you adjust the globally configured maximum size for this particular
entity, be it to increase or decrease any previously set value.
This means that your application will receive all requests (or responses) from the HTTP layer, even the ones whose
`Content-Length` exceeds the configured limit (because you might want to increase the limit yourself).
Only when the actual data stream @apidoc[Source] contained in the entity is materialized will the boundary checks be
actually applied. In case the length verification fails the respective stream will be terminated with an
`EntityStreamSizeException` either directly at materialization time (if the `Content-Length` is known) or whenever more
data bytes than allowed have been read.

为了给予你根据你的需要定义实体大小限制条件的最大灵活性， @apidoc[HttpEntity] 提供了 `withSizeLimit` 方法，
该方法让你调整此特定实体的全局配置最大大小，无论该大小是增加还是减少任何先前设置的值。
这意味着你的应用程序将从 HTTP 层收到所有请求（或响应），甚至 `Content-Length` 超过配置的限制的请求（或响应）（因为你可能希望自己增加限制）。
只有当实际数据流 @apidoc[Source] 具现化后，边界检查才会被执行。如果长度校验失效，对应的数据流就抛出 `EntityStreamSizeException` 并终结，
终止可以直接发生在具现化的时候（`Content-Length` 已知）或者再读入多几个超标字节之后。 

When called on `Strict` entities the `withSizeLimit` method will return the entity itself if the length is within
the bound, otherwise a `Default` entity with a single element data stream. This allows for potential refinement of the
entity size limit at a later point (before materialization of the data stream).

当在 `Strict` 实体上调用 `withSizeLimit` 方法时，如果长度（设置的）在（配置的）范围内，将返回它自己，否则返回带单一元素的数据流的 `Default` 实体。
这允许晚一点的时候（数据流具现化之前）再去做实体大小的限制

By default all message entities produced by the HTTP layer automatically carry the limit that is defined in the
application's `max-content-length` config setting. If the entity is transformed in a way that changes the
content-length and then another limit is applied then this new limit will be evaluated against the new
content-length. If the entity is transformed in a way that changes the content-length and no new limit is applied
then the previous limit will be applied against the previous content-length.
Generally this behavior should be in line with your expectations.

默认情况下，所有在 HTTP 层生成的消息实体自动带有由应用程序的 `max-content-length` 配置设置的长度限制。如果实体转换改变了内容长度并设置了新的限制，则这个新的限制将应用予新内容长度。
如果实体转换改变了内容长度并不设置新的限制，则之前的限制将适用于之前的内容长度。

> <a id="1" href="#^1">[1]</a> *akka.http.parsing.max-content-length* (applying to server- as well as client-side),
*akka.http.server.parsing.max-content-length* (server-side only),
*akka.http.client.parsing.max-content-length* (client-side only) or
*akka.http.host-connection-pool.client.parsing.max-content-length* (only host-connection-pools)

> <a id="1" href="#^1">[1]</a> *akka.http.parsing.max-content-length*（应用于服务端，也应用于客户端）、
*akka.http.server.parsing.max-content-length* (server-side only)、
*akka.http.client.parsing.max-content-length* (client-side only) 或
*akka.http.host-connection-pool.client.parsing.max-content-length* (只用 host-connection-pools)

### Special processing for HEAD requests
**HEAD 请求的特殊处理**

[RFC 7230](https://tools.ietf.org/html/rfc7230#section-3.3.3) defines very clear rules for the entity length of HTTP messages.

[RFC 7230](https://tools.ietf.org/html/rfc7230#section-3.3.3) 为 HTTP 消息的实体长度定义了很明确的规则。

Especially this rule requires special treatment in Akka HTTP:

特别是此规则需要在 Akka HTTP 中进行特别对待：

>
Any response to a HEAD request and any response with a 1xx
(Informational), 204 (No Content), or 304 (Not Modified) status
code is always terminated by the first empty line after the
header fields, regardless of the header fields present in the
message, and thus cannot contain a message body.

>
对 HEAD 请求的任何响应以及任何响应为 1xx（Informational）、204（No Content）或 304（Not Modified）状态码的信息都是使用 header 字段后的第一个空行作为终止，
不论在消息中的 header 字段有何表示，因为不能包含消息正文（译注：就算指定的 Content-Length，也不能包含消息正文）。

Responses to HEAD requests introduce the complexity that *Content-Length* or *Transfer-Encoding* headers
can be present but the entity is empty. This is modeled by allowing @scala[*HttpEntity.Default*]@java[*HttpEntityDefault*] and @scala[*HttpEntity.Chunked*]@java[*HttpEntityChunked*]
to be used for HEAD responses with an empty data stream.

针对 HEAD 请求的响应引入了可以存在 *Content-Length* 或 *Transfer-Encoding* 头，但实体内容为空的复杂性。这通过允许 @scala[*HttpEntity.Default*]@java[*HttpEntityDefault*] 和 @scala[*HttpEntity.Chunked*]@java[*HttpEntityChunked*] 使用具有空的数据流的 HEAD 响应来建模。

Also, when a HEAD response has an @scala[*HttpEntity.CloseDelimited*]@java[*HttpEntityCloseDelimited*] entity the Akka HTTP implementation will *not* close the
connection after the response has been sent. This allows the sending of HEAD responses without *Content-Length*
header across persistent HTTP connections.

并且，当 HEAD 响应是一个 @scala[*HttpEntity.CloseDelimited*]@java[*HttpEntityCloseDelimited*] 实体时，Akka HTTP 实现将 *不会* 在发送响应后关闭连接。
这允许在持久化的 HTTP 连接上发送不带 *Content-Length* 头的 HEAD 响应。

<a id="header-model"></a>
## Header Model
**头域模型**

Akka HTTP contains a rich model of the most common HTTP headers. Parsing and rendering is done automatically so that
applications don't need to care for the actual syntax of headers. Headers not modelled explicitly are represented
as a @apidoc[RawHeader], which is essentially a String/String name/value pair.

Akka HTTP 包含最常见 HTTP 头的丰富模型。解析和渲染是自动完成的，因此应用程序不需要关心头域的实际语法。
若头域未显示建模则表示为 @apidoc[RawHeader]，它本质上是一个 String/String name/value 形式的键值对。

See these examples of how to deal with headers:

查看这些示例如何处理头域：

Scala
:   @@snip [ModelSpec.scala]($test$/scala/docs/http/scaladsl/ModelSpec.scala) { #headers }

Java
:   @@snip [ModelDocTest.java]($test$/java/docs/http/javadsl/ModelDocTest.java) { #headers }

## HTTP Headers
**HTTP 头**

When the Akka HTTP server receives an HTTP request it tries to parse all its headers into their respective
model classes. Independently of whether this succeeds or not, the HTTP layer will
always pass on all received headers to the application. Unknown headers as well as ones with invalid syntax (according
to the header parser) will be made available as @apidoc[RawHeader] instances. For the ones exhibiting parsing errors a
warning message is logged depending on the value of the `illegal-header-warnings` config setting.

当 Akka HTTP 服务器收到一个 HTTP 请求，它将尝试解析所有头域到它们所表示的模型类。无论成功与否，HTTP 层始终将传递所有收到的头域给应用程序。
未知头域甚至一些使用了无效标记（基于头域语法分析）的信息都将做为 @apidoc[RawHeader] 的实例。出现解析错误的头域会有相应的警告信息记录在日志中
，是否记录取决于 `illegal-header-warnings` 这个配置的值。

Some headers have special status in HTTP and are therefore treated differently from "regular" headers:

某些头域在 HTTP 里有特殊的状态，因此它们与“普通”的头域的处理方式有所不同：

Content-Type
: The Content-Type of an HTTP message is modeled as the `contentType` field of the @apidoc[HttpEntity].
The `Content-Type` header therefore doesn't appear in the `headers` sequence of a message.
Also, a `Content-Type` header instance that is explicitly added to the `headers` of a request or response will
not be rendered onto the wire and trigger a warning being logged instead!

: HTTP 消息的 Content-Type 建模为 @apidoc[HttpEntity] 的 `contentType` 字段。`Content-Type` 头因此不会出现在消息的 `headers` 列表里。
即使一个 `Content-Type` 头实例被显示地添加到请求或响应的 `headers` 字段里，它也不会被生成到最终的通讯线路上，而是生成一个警告信息并记录日志。

Transfer-Encoding
: Messages with `Transfer-Encoding: chunked` are represented @scala[via the `HttpEntity.Chunked`]@java[as a `HttpEntityChunked`] entity.
As such chunked messages that do not have another deeper nested transfer encoding will not have a `Transfer-Encoding`
header in their `headers` @scala[sequence]@java[list].
Similarly, a `Transfer-Encoding` header instance that is explicitly added to the `headers` of a request or
response will not be rendered onto the wire and trigger a warning being logged instead!

Content-Length
: The content length of a message is modelled via its [HttpEntity](#httpentity). As such no `Content-Length` header will ever
be part of a message's `header` sequence.
Similarly, a `Content-Length` header instance that is explicitly added to the `headers` of a request or
response will not be rendered onto the wire and trigger a warning being logged instead!

Server
: A `Server` header is usually added automatically to any response and its value can be configured via the
`akka.http.server.server-header` setting. Additionally an application can override the configured header with a
custom one by adding it to the response's `header` sequence.

User-Agent
: A `User-Agent` header is usually added automatically to any request and its value can be configured via the
`akka.http.client.user-agent-header` setting. Additionally an application can override the configured header with a
custom one by adding it to the request's `header` sequence.

Date
: The @apidoc[Date] response header is added automatically but can be overridden by supplying it manually.

Connection
: On the server-side Akka HTTP watches for explicitly added `Connection: close` response headers and as such honors
the potential wish of the application to close the connection after the respective response has been sent out.
The actual logic for determining whether to close the connection is quite involved. It takes into account the
request's method, protocol and potential @apidoc[Connection] header as well as the response's protocol, entity and
potential @apidoc[Connection] header. See @github[this test](/akka-http-core/src/test/scala/akka/http/impl/engine/rendering/ResponseRendererSpec.scala) { #connection-header-table } for a full table of what happens when.

Strict-Transport-Security
: HTTP Strict Transport Security (HSTS) is a web security policy mechanism which is communicated by the
`Strict-Transport-Security` header. The most important security vulnerability that HSTS can fix is SSL-stripping
man-in-the-middle attacks. The SSL-stripping attack works by transparently converting a secure HTTPS connection into a
plain HTTP connection. The user can see that the connection is insecure, but crucially there is no way of knowing
whether the connection should be secure. HSTS addresses this problem by informing the browser that connections to the
site should always use TLS/SSL. See also [RFC 6797](https://tools.ietf.org/html/rfc6797).


<a id="custom-headers"></a>
## Custom Headers

Sometimes you may need to model a custom header type which is not part of HTTP and still be able to use it
as convenient as is possible with the built-in types.

Because of the number of ways one may interact with headers (i.e. try to @scala[match]@java[convert] a @apidoc[CustomHeader] @scala[against]@java[to] a @apidoc[RawHeader]
or the other way around etc), a helper @scala[trait]@java[classes] for custom Header types @scala[and their companions classes ]are provided by Akka HTTP.
Thanks to extending @apidoc[ModeledCustomHeader] instead of the plain @apidoc[CustomHeader] @scala[such header can be matched]@java[the following methods are at your disposal]:

Scala
:   @@snip [ModeledCustomHeaderSpec.scala]($akka-http$/akka-http-tests/src/test/scala/akka/http/scaladsl/server/ModeledCustomHeaderSpec.scala) { #modeled-api-key-custom-header }

Java
:   @@snip [CustomHeaderExampleTest.java]($test$/java/docs/http/javadsl/CustomHeaderExampleTest.java) { #modeled-api-key-custom-header }

Which allows this @scala[CustomHeader]@java[modeled custom header] to be used in the following scenarios:

Scala
:   @@snip [ModeledCustomHeaderSpec.scala]($akka-http$/akka-http-tests/src/test/scala/akka/http/scaladsl/server/ModeledCustomHeaderSpec.scala) { #matching-examples }

Java
:   @@snip [CustomHeaderExampleTest.java]($test$/java/docs/http/javadsl/CustomHeaderExampleTest.java) { #conversion-creation-custom-header }

Including usage within the header directives like in the following @ref[headerValuePF](../routing-dsl/directives/header-directives/headerValuePF.md) example:

Scala
:   @@snip [ModeledCustomHeaderSpec.scala]($akka-http$/akka-http-tests/src/test/scala/akka/http/scaladsl/server/ModeledCustomHeaderSpec.scala) { #matching-in-routes }

Java
:   @@snip [CustomHeaderExampleTest.java]($test$/java/docs/http/javadsl/CustomHeaderExampleTest.java) { #header-value-pf }

@@@ note { .group-scala }
When defining custom headers, it is better to extend @apidoc[ModeledCustomHeader] instead of its parent @apidoc[CustomHeader].
Custom headers that extend @apidoc[ModeledCustomHeader] automatically comply with the pattern matching semantics that usually apply to built-in
types (such as matching a custom header against a @apidoc[RawHeader] in routing layers of Akka HTTP applications).
@@@

@@@ note { .group-java }
Implement @apidoc[ModeledCustomHeader] and @java[@javadoc[ModeledCustomHeaderFactory](akka.http.javadsl.model.headers.ModeledCustomHeaderFactory)] instead of @apidoc[CustomHeader] to be
able to use the convenience methods that allow parsing the custom user-defined header from @apidoc[HttpHeader].
@@@

## Parsing / Rendering

Parsing and rendering of HTTP data structures is heavily optimized and for most types there's currently no public API
provided to parse (or render to) Strings or byte arrays.

@@@ note
Various parsing and rendering settings are available to tweak in the configuration under `akka.http.client[.parsing]`,
`akka.http.server[.parsing]` and `akka.http.host-connection-pool[.client.parsing]`, with defaults for all of these
being defined in the `akka.http.parsing` configuration section.

For example, if you want to change a parsing setting for all components, you can set the `akka.http.parsing.illegal-header-warnings = off`
value. However this setting can be still overridden by the more specific sections, like for example `akka.http.server.parsing.illegal-header-warnings = on`.

In this case both `client` and `host-connection-pool` APIs will see the setting `off`, however the server will see `on`.

In the case of `akka.http.host-connection-pool.client` settings, they default to settings set in `akka.http.client`,
and can override them if needed. This is useful, since both `client` and `host-connection-pool` APIs,
such as the Client API @scala[`Http().outgoingConnection`]@java[`Http.get(sys).outgoingConnection`] or the Host Connection Pool APIs @scala[`Http().singleRequest`]@java[`Http.get(sys).singleRequest`]
or @scala[`Http().superPool`]@java[`Http.get(sys).superPool`], usually need the same settings, however the `server` most likely has a very different set of settings.
@@@

<a id="registeringcustommediatypes"></a>
## Registering Custom Media Types

Akka HTTP @scala[@scaladoc[predefines](akka.http.scaladsl.model.MediaTypes$)]@java[@javadoc[predefines](akka.http.javadsl.model.MediaTypes)] most commonly encountered media types and emits them in their well-typed form while parsing http messages.
Sometimes you may want to define a custom media type and inform the parser infrastructure about how to handle these custom
media types, e.g. that `application/custom` is to be treated as `NonBinary` with `WithFixedCharset`. To achieve this you
need to register the custom media type in the server's settings by configuring @apidoc[ParserSettings] like this:

Scala
:   @@snip [CustomMediaTypesSpec.scala]($akka-http$/akka-http-tests/src/test/scala/akka/http/scaladsl/CustomMediaTypesSpec.scala) { #application-custom }

Java
:   @@snip [CustomMediaTypesExampleTest.java]($test$/java/docs/http/javadsl/CustomMediaTypesExampleTest.java) { #application-custom-java }

You may also want to read about MediaType [Registration trees](https://en.wikipedia.org/wiki/Media_type#Registration_trees), in order to register your vendor specific media types
in the right style / place.

<a id="registeringcustomstatuscodes"></a>
## Registering Custom Status Codes

Similarly to media types, Akka HTTP @scala[@scaladoc:[predefines](akka.http.scaladsl.model.StatusCodes$)]@java[@javadoc:[predefines](akka.http.javadsl.model.StatusCodes)]
well-known status codes, however sometimes you may need to use a custom one (or are forced to use an API which returns custom status codes).
Similarly to the media types registration, you can register custom status codes by configuring @apidoc[ParserSettings] like this:

Scala
:   @@snip [CustomStatusCodesSpec.scala]($akka-http$/akka-http-tests/src/test/scala/akka/http/scaladsl/CustomStatusCodesSpec.scala) { #application-custom }

Java
:   @@snip [CustomStatusCodesExampleTest.java]($test$/java/docs/http/javadsl/CustomStatusCodesExampleTest.java) { #application-custom-java }

<a id="registeringcustommethod"></a>
## Registering Custom HTTP Method

Akka HTTP also allows you to define custom HTTP methods, other than the well-known methods @scala[@scaladoc[predefined](akka.http.scaladsl.model.HttpMethods$)]@java[@javadoc[predefined](akka.http.javadsl.model.HttpMethods)] in Akka HTTP.
To use a custom HTTP method, you need to define it, and then add it to parser settings like below:

Scala
:   @@snip [CustomHttpMethodSpec.scala]($test$/scala/docs/http/scaladsl/server/directives/CustomHttpMethodSpec.scala) { #application-custom }

Java
:   @@snip [CustomHttpMethodsExampleTest.java]($test$/java/docs/http/javadsl/server/directives/CustomHttpMethodExamplesTest.java) { #customHttpMethod }
