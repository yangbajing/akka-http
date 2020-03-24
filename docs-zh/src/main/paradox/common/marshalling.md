# Marshalling 编组
*编码、组合*

@java[TODO @github[overhaul for Java](#1367)]

Marshalling is the process of converting a higher-level (object) structure into some kind of lower-level
representation, often a "wire format". Other popular names for marshalling are "serialization" or "pickling".

编组是指转换一个高阶（对象）结构到某种低阶表示的过程。其它流行的叫法是“序列化”（serialization）或“腌制”（pickling）。

In Akka HTTP, marshalling means the conversion of an object of type `T` into a lower-level target type,
e.g. a `MessageEntity` (which forms the "entity body" of an HTTP request or response) or a full @apidoc[HttpRequest] or
@apidoc[HttpResponse].

在 Akka HTTP 里面，编组意味着转换一个 `T` 类型的对像到一个低级的目标类型，例如：`MessageEntity`（构成一个 HTTP 请求或响应的“实体正文”）
或者 @apidoc[HttpRequest] 或者 apidoc[HttpResponse]。

On the server-side, for example, marshalling is used to convert an application-domain object to a response entity. Requests can
contain an @apidoc[Accept] header that lists acceptable content types for the client, such as `application/json` and `application/xml`. A marshaller contains the logic to
negotiate the result content types based on the @apidoc[Accept] and the `AcceptCharset` headers.

在服务器端，编组被用于转换应用域对象到一个响应实体。请求可以包含一个 @apidoc[Accept] 头列出客户端可接收到内容类型，例如 `application/json`
和 `application/xml`。一个编组器包含根据 @apidoc[Accept] 和 `AcceptCharset` 头协商结果内容类型的逻辑。

## Basic Design
**基础设计**

Marshalling of instances of type `A` into instances of type `B` is performed by a @apidoc[Marshaller[A, B]].

编码 `A` 类型实例到 `B` 类型实例由 @apidoc[Marshaller[A, B]] 执行。

Contrary to what you might initially expect, @apidoc[Marshaller[A, B]] is not a plain function `A => B` but rather
essentially a function @scala[`A => Future[List[Marshalling[B]]]`]@java[`A => CompletionStage<List<Marshalling<B>>>`].
Let's dissect this rather complicated looking signature piece by piece to understand why marshallers are designed this
way.
Given an instance of type `A` a @apidoc[Marshaller[A, B]] produces:

与你最初的期望相反，@apidoc[Marshaller[A, B]] 不是一个普通的 `A => B` 函数，本质上是一个
@scala[`A => Future[List[Marshalling[B]]]`]@java[`A => CompletionStage<List<Marshalling<B>>>`] 函数。
让我们逐步剖析这个看起来相当复杂的签名，以理解设计人员为什么要这么设计。
给定一个类型 `A` 的实例，@apidoc[Marshaller[A, B]] 生成：

1. A @scala[`Future`]@java[`CompletionStage`]: This is probably quite clear. Marshallers are not required to synchronously produce a result, so instead
they return a future, which allows for asynchronicity in the marshalling process.
    @scala[`Future`]@java[`CompletionStage`]：这个可能相对比较清晰。编组器不一定要同步产生一个结果，所以返回一个 Future，以便编组过程可以异步完成。

2. of `List`: Rather than only a single target representation for `A` marshallers can offer several ones. Which
one will be rendered onto the wire in the end is decided by content negotiation.
For example, the @apidoc[Marshaller[OrderConfirmation, MessageEntity]] might offer a JSON as well as an XML representation.
The client can decide through the addition of an @apidoc[Accept] request header which one is preferred. If the client doesn't
express a preference the first representation is picked.
    有 `List`：与其返回一个单一的表达类型，类型 `A` 的编组器可以提供几个不同的目标类型。具体哪个类型作为最终渲染用到通讯渠道上，则依赖于双方的内容协商。

3. of @scala[`Marshalling[B]`]@java[`Marshalling<B>`]: Rather than returning an instance of `B` directly marshallers first produce a
@scala[`Marshalling[B]`]@java[`Marshalling<B>`]. This allows for querying the @apidoc[MediaType] and potentially the @apidoc[HttpCharset] that the marshaller
will produce before the actual marshalling is triggered. Apart from enabling content negotiation this design allows for
delaying the actual construction of the marshalling target instance to the very last moment when it is really needed.
    有 @scala[`Marshalling[B]`]@java[`Marshalling<B>`]：与其直接返回一个 `B` 目标类型的实例，编组器会先返回一个 `Marshalling[B]` 类型。这使编组器可以在进入编组过程前先查询确认 `MediaType` 或 `HttpCharset`。这样的设计既可以支持内容协商，又可以使具体的目标编组对象推迟到有需要的时候才被构建。

@@@ div { .group-scala }

This is how `Marshalling` is defined:

这是 `Marshalling` 的定义方式：

@@snip [Marshaller.scala]($akka-http$/akka-http/src/main/scala/akka/http/scaladsl/marshalling/Marshaller.scala) { #marshalling }

Akka HTTP also defines a number of helpful aliases for the types of marshallers that you'll likely work with most:

Akka HTTP 为你可能用到的大多数编组也定义了一些有用的别名：

@@snip [package.scala]($akka-http$/akka-http/src/main/scala/akka/http/scaladsl/marshalling/package.scala) { #marshaller-aliases }

@@@

## Predefined Marshallers
**预定义编组器**

Akka HTTP already predefines a number of marshallers for the most common types.
Specifically these are:

Akka HTTP 对多数常见类型预定了一些编组器。具体来说，这些是：

@@@ div { .group-scala }

 * @scaladoc[PredefinedToEntityMarshallers](akka.http.scaladsl.marshalling.PredefinedToEntityMarshallers)
    * `Array[Byte]`
    * @apidoc[akka.util.ByteString]
    * `Array[Char]`
    * `String`
    * `akka.http.scaladsl.model.FormData`
    * `akka.http.scaladsl.model.MessageEntity`
    * `T <: akka.http.scaladsl.model.Multipart`
 * @scaladoc[PredefinedToResponseMarshallers](akka.http.scaladsl.marshalling.PredefinedToResponseMarshallers)
    * `T`, if a `ToEntityMarshaller[T]` is available
    * @apidoc[HttpResponse]
    * @apidoc[StatusCode]
    * `(StatusCode, T)`, if a `ToEntityMarshaller[T]` is available
    * `(Int, T)`, if a `ToEntityMarshaller[T]` is available
    * `(StatusCode, immutable.Seq[HttpHeader], T)`, if a `ToEntityMarshaller[T]` is available
    * `(Int, immutable.Seq[HttpHeader], T)`, if a `ToEntityMarshaller[T]` is available
 * @scaladoc[PredefinedToRequestMarshallers](akka.http.scaladsl.marshalling.PredefinedToRequestMarshallers)
    * @apidoc[HttpRequest]
    * @apidoc[Uri]
    * `(HttpMethod, Uri, T)`, if a `ToEntityMarshaller[T]` is available
    * `(HttpMethod, Uri, immutable.Seq[HttpHeader], T)`, if a `ToEntityMarshaller[T]` is available
 * @scaladoc[GenericMarshallers](akka.http.scaladsl.marshalling.GenericMarshallers)
    * @apidoc[Marshaller[Throwable, T]]
    * @apidoc[Marshaller[Option[A], B]], if a @apidoc[Marshaller[A, B]] and an `EmptyValue[B]` is available
    * @apidoc[Marshaller[Either[A1, A2], B]], if a @apidoc[Marshaller[A1, B]] and a @apidoc[Marshaller[A2, B]] is available
    * @apidoc[Marshaller[Future[A], B]], if a @apidoc[Marshaller[A, B]] is available
    * @apidoc[Marshaller[Try[A], B]], if a @apidoc[Marshaller[A, B]] is available

@@@

@@@ div { .group-java }

 * Predefined @apidoc[RequestEntity] marshallers:
    * `byte[]`
    * @apidoc[akka.util.ByteString]
    * `char[]`
    * `String`
    * @apidoc[FormData]
    * `Optional<T>` using an existing @apidoc[RequestEntity] marshaller for `T`. An empty optional will yield an empty entity.
 * Predefined @apidoc[HttpResponse] marshallers:
    * `T` using an existing @apidoc[RequestEntity] marshaller for `T`
    * `T` and @apidoc[StatusCode] using an existing @apidoc[RequestEntity] marshaller for `T`
    * `T`, @apidoc[StatusCode] and `Iterable[HttpHeader]` using an existing @apidoc[RequestEntity] marshaller for `T`

All marshallers can be found in @apidoc[Marshaller].

所有编组器中在 @apidoc[Marshaller] 找到。

@@@

@@@ div { .group-scala }

## Implicit Resolution
**隐式处理**

The marshalling infrastructure of Akka HTTP relies on a type-class based approach, which means that @apidoc[Marshaller]
instances from a certain type `A` to a certain type `B` have to be available implicitly.

Akka HTTP 编组的基础设施实现基于（Scala）类型类方法，这意味着从 `A` 类型到 `B` 类型的 @apidoc[Marshaller] 实例必需作为一个函数参数/函数存在。 

The implicits for most of the predefined marshallers in Akka HTTP are provided through the companion object of the
@apidoc[Marshaller] trait. This means that they are always available and never need to be explicitly imported.
Additionally, you can simply "override" them by bringing your own custom version into local scope.

Akka HTTP 中大部分预定义的编组器的隐式工具都是通过 @apidoc[Marshaller] trait 的伴身对象提供的。这意味着它们总是可用，而不需要显示导入。
另外，你可以在本地可视范围里用自己的版本覆盖原有版本。
@@@

## Custom Marshallers
**自定义编组器**

Akka HTTP gives you a few convenience tools for constructing marshallers for your own types.
Before you do that you need to think about what kind of marshaller you want to create.
If all your marshaller needs to produce is a `MessageEntity` then you should probably provide a
@scala[`ToEntityMarshaller[T]`]@java[@apidoc[Marshaller[T, MessageEntity]]]. The advantage here is that it will work on both the client- as well as the server-side since
a @scala[`ToReponseMarshaller[T]`]@java[@apidoc[Marshaller[T, HttpResponse]]] as well as a @scala[`ToRequestMarshaller[T]`]@java[@apidoc[Marshaller[T, HttpRequest]]] can automatically be created if a
@scala[`ToEntityMarshaller[T]`]@java[@apidoc[Marshaller[T, MessageEntity]]] is available.

If, however, your marshaller also needs to set things like the response status code, the request method, the request URI
or any headers then a @scala[`ToEntityMarshaller[T]`]@java[@apidoc[Marshaller[T, MessageEntity]]] won't work. You'll need to fall down to providing a
@scala[`ToResponseMarshaller[T]`]@java[@apidoc[Marshaller[T, HttpResponse]]] or a @scala[`ToRequestMarshaller[T]]`]@java[@apidoc[Marshaller[T, HttpRequest]]] directly.

For writing your own marshallers you won't have to "manually" implement the @apidoc[Marshaller] @scala[trait]@java[class] directly.

@@@ div { .group-scala }

Rather, it should be possible to use one of the convenience construction helpers defined on the @apidoc[Marshaller]
companion:

@@snip [Marshaller.scala]($akka-http$/akka-http/src/main/scala/akka/http/scaladsl/marshalling/Marshaller.scala) { #marshaller-creation }

@@@

## Deriving Marshallers

Sometimes you can save yourself some work by reusing existing marshallers for your custom ones.
The idea is to "wrap" an existing marshaller with some logic to "re-target" it to your type.

In this regard wrapping a marshaller can mean one or both of the following two things:

 * Transform the input before it reaches the wrapped marshaller
 * Transform the output of the wrapped marshaller

For the latter (transforming the output) you can use `baseMarshaller.map`, which works exactly as it does for functions.
For the former (transforming the input) you have four alternatives:

 * `baseMarshaller.compose`
 * `baseMarshaller.composeWithEC`
 * `baseMarshaller.wrap`
 * `baseMarshaller.wrapWithEC`

`compose` works just like it does for functions.
`wrap` is a compose that allows you to also change the `ContentType` that the marshaller marshals to.
The `...WithEC` variants allow you to receive an `ExecutionContext` internally if you need one, without having to
depend on one being available implicitly at the usage site.

## Using Marshallers

In many places throughout Akka HTTP, marshallers are used implicitly, e.g. when you define how to @ref[complete](../routing-dsl/directives/route-directives/complete.md) a
request using the @ref[Routing DSL](../routing-dsl/index.md).

@@@ div { .group-scala }

However, you can also use the marshalling infrastructure directly if you wish, which can be useful for example in tests.
The best entry point for this is the @scaladoc[Marshal](akka.http.scaladsl.marshalling.Marshal) object, which you can use like this:

@@snip [MarshalSpec.scala]($test$/scala/docs/http/scaladsl/MarshalSpec.scala) { #use-marshal }

@@@

@@@ div { .group-java }

However, many directives dealing with @ref[marshalling](../routing-dsl/directives/marshalling-directives/index.md) also  require that you pass a marshaller explicitly. The following example shows how to marshal Java bean classes to JSON using the @ref:[Jackson JSON support](json-support.md#jackson-support):

@@snip [PetStoreExample.java]($akka-http$/akka-http-tests/src/main/java/akka/http/javadsl/server/examples/petstore/PetStoreExample.java) { #imports #marshall }

@@@
