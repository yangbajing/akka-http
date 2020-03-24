# Unmarshalling/解组

"Unmarshalling" is the process of converting some kind of a lower-level representation, often a "wire format", into a
higher-level (object) structure. Other popular names for it are "Deserialization" or "Unpickling".

“解组”是将某种低级别的表示（通过是”线上格式“）转换到高级别（对象）结构的过程。其它流行的叫法有“反序列化”（Deserialization）或“解开”（Unpickling）。

In Akka HTTP "Unmarshalling" means the conversion of a lower-level source object, e.g. a `MessageEntity`
(which forms the "entity body" of an HTTP request or response) or a full @apidoc[HttpRequest] or @apidoc[HttpResponse],
into an instance of type `T`.

Akka HTTP 里的“解组”意味着转换一个低级别的源对象，例如：`MessageEntity`（构成一个 HTTP 请求或响应的“实体正文”）或者 @apidoc[HttpRequest] 或者 @apidoc[HttpResponse] 到类型 `T` 的实例。

## Basic Design
**基础设计**

Unmarshalling of instances of type `A` into instances of type `B` is performed by an @apidoc[Unmarshaller[A, B]].

解码类型 `A` 实例到类型 `B` 实例由 @apidoc[Unmarshaller[A, B]] 执行。

@@@ div { .group-scala }
Akka HTTP also predefines a number of helpful aliases for the types of unmarshallers that you'll likely work with most:

Akka HTTP 也为你可能用到的大多数解组也定义了一些有用的别名：

@@snip [package.scala]($akka-http$/akka-http/src/main/scala/akka/http/scaladsl/unmarshalling/package.scala) { #unmarshaller-aliases }

@@@

At its core an @apidoc[Unmarshaller[A, B]] is very similar to a @scala[function `A => Future[B]`]@java[`Function<A, CompletionStage<B>>`] and as such quite a bit simpler
than its @ref[marshalling](marshalling.md) counterpart. The process of unmarshalling does not have to support
content negotiation which saves two additional layers of indirection that are required on the marshalling side.

@apidoc[Unmarshaller[A, B]] 的核心非常类似 @scala[函数 `A => Future[B]`]@java[`Function<A, CompletionStage<B>>`]，
因此比 @ref[编组](marshalling.md) 对应的要简单得多。解组过程不需要内容协商，这样可以节省编组方需要的两个间接层。

## Using unmarshallers
**使用解组器**

For an example on how to use an unmarshaller on the server side, see for example the @ref[Dynamic Routing Example](../routing-dsl/index.md#dynamic-routing-example).
For the client side, see @ref[Processing Responses](../client-side/request-and-response.md#processing-responses)

在服务器端怎样使用解组的示例请见 @ref[动态路由示例](../routing-dsl/index.md#dynamic-routing-example) 。
客户端示例请见 @ref[处理响应](../client-side/request-and-response.md#processing-responses) 。

## Predefined Unmarshallers
**预定义解组器**

Akka HTTP already predefines a number of unmarshallers for the most common types.
Specifically these are:

Akka HTTP 对于大多数类型已经预定义了一些解组器。具体来说，这些是：

 * @scala[@scaladoc[PredefinedFromStringUnmarshallers](akka.http.scaladsl.unmarshalling.PredefinedFromStringUnmarshallers)]
   @java[@javadoc[StringUnmarshallers](akka.http.javadsl.unmarshalling.StringUnmarshallers)]
    * `Byte`
    * `Short`
    * @scala[`Int`]@java[`Integer`]
    * `Long`
    * `Float`
    * `Double`
    * `Boolean`
 * @scala[@scaladoc[PredefinedFromEntityUnmarshallers](akka.http.scaladsl.unmarshalling.PredefinedFromEntityUnmarshallers)]
   @java[@apidoc[Unmarshaller]]
    * @scala[`Array[Byte]`]@java[`byte[]`]
    * @apidoc[akka.util.ByteString]
    * @scala[`Array[Char]`]@java[`char[]`]
    * `String`
    * @scala[`akka.http.scaladsl.model.FormData`]@java[`akka.http.javadsl.model.FormData`]

@@@ div { .group-scala }
 * @scaladoc[GenericUnmarshallers](akka.http.scaladsl.unmarshalling.GenericUnmarshallers)
    * @apidoc[Unmarshaller[T, T]](Unmarshaller) (identity unmarshaller)
    * @apidoc[Unmarshaller[Option[A], B]], if an @apidoc[Unmarshaller[A, B]] is available
    * @apidoc[Unmarshaller[A, Option[B]]], if an @apidoc[Unmarshaller[A, B]] is available
@@@

Additional unmarshallers are available in separate modules for specific content types, such as
@ref[JSON](json-support.md)@scala[ and @ref[XML](xml-support.md)].

@@@ div { .group-scala }

## Implicit Resolution
**隐式处理**

The unmarshalling infrastructure of Akka HTTP relies on a type-class based approach, which means that @apidoc[Unmarshaller]
instances from a certain type `A` to a certain type `B` have to be available implicitly.

Akka HTTP 解组的基础设施实现基于（Scala）类型类方法，这意味着从 `A` 类型到 `B` 类型的 @apidoc[Unmarshaller] 实例必需作为一个隐式参数/函数存在。 

The implicits for most of the predefined unmarshallers in Akka HTTP are provided through the companion object of the
@apidoc[Unmarshaller] trait. This means that they are always available and never need to be explicitly imported.
Additionally, you can simply "override" them by bringing your own custom version into local scope.

Akka HTTP 中大部分预定义的解组器的隐式工具都是通过 @apidoc[Unmarshaller] trait 的伴身对象提供的。这意味着它们总是可用，而不需要显示导入。
另外，你可以在局部可视范围里用自己的版本覆盖原有版本。
@@@

## Custom Unmarshallers
**自定义解组器**

Akka HTTP gives you a few convenience tools for constructing unmarshallers for your own types.
Usually you won't have to "manually" implement the @apidoc[Unmarshaller] @scala[trait]@java[class] directly.
Rather, it should be possible to use one of the convenience construction helpers defined on
@scala[the @apidoc[Unmarshaller] companion]@java[@apidoc[Unmarshaller]]:

Akka HTTP 为构造你自己类型的解组器提供了一些便利工具。通常不需要直接“手动”实现 @apidoc[Unmarshaller] @scala[trait]@java[类]。
相反，应该使用在 @scala[@apidoc[Unmarshaller] 伴身对象]@java[@apidoc[Unmarshaller]] 中定义的便利构造助手。

Scala
:  @@snip [Unmarshaller.scala]($akka-http$/akka-http/src/main/scala/akka/http/scaladsl/unmarshalling/Unmarshaller.scala) { #unmarshaller-creation }

Java
:  @@snip [Unmarshallers.scala]($akka-http$/akka-http/src/main/java/akka/http/javadsl/unmarshalling/Unmarshallers.java) { #unmarshaller-creation }

@@@ note
To avoid unnecessary memory pressure, unmarshallers should make sure to either fully consume the incoming entity data stream, or make sure it is properly cancelled on error.
Failure to do so might keep the remaining part of the stream in memory for longer than necessary.

为避免不必要的内存压力，解组器应确保完全消耗传入的实体数据流，或确保在出错时将其正确取消。否则，可能会将流的其余部分在内存中保留的时间比必要长。
@@@

## Deriving Unmarshallers
**衍生解组器**

Sometimes you can save yourself some work by reusing existing unmarshallers for your custom ones.
The idea is to "wrap" an existing unmarshaller with some logic to "re-target" it to your type.

有时，可通过复用已存在的解组器为自定义解组器节省一些工作。思路上就是以某种逻辑“包装”已存在的解组器以便“调整下目标”来对应你自己的类型。

Usually what you want to do is to transform the output of some existing unmarshaller and convert it to your type.
For this type of unmarshaller transformation Akka HTTP defines these methods:

通常，想转换已存在的解组器上的输出，并将其转换到你的类型。对于此类解组器转换，Akka HTTP 定义了这些方法：

@@@ div { .group-scala }
 * `baseUnmarshaller.transform`
 * `baseUnmarshaller.map`
 * `baseUnmarshaller.mapWithInput`
 * `baseUnmarshaller.flatMap`
 * `baseUnmarshaller.flatMapWithInput`
 * `baseUnmarshaller.recover`
 * `baseUnmarshaller.withDefaultValue`
 * `baseUnmarshaller.mapWithCharset` (仅适用于 FromEntityUnmarshallers)
 * `baseUnmarshaller.forContentTypes` (仅适用于 FromEntityUnmarshallers)
@@@

@@@ div { .group-java }
 * `baseMarshaller.thenApply`
 * `baseMarshaller.flatMap`
 * `Unmarshaller.forMediaType` (从 @apidoc[HttpEntity] 解组器衍生)
 * `Unmarshaller.forMediaTypes` (从 @apidoc[HttpEntity] 解组器衍生)
@@@

The method signatures should make their semantics relatively clear.

方法签名应使其语言相对清晰。

## Using Unmarshallers
**使用解组**

In many places throughout Akka HTTP unmarshallers are used implicitly, e.g. when you want to access the @ref[entity](../routing-dsl/directives/marshalling-directives/entity.md)
of a request using the @ref[Routing DSL](../routing-dsl/index.md).

Akka HTTP 的很多地方，解组器被隐式使用，例如：当你想使用 @ref[路由 DSL](../routing-dsl/index.md) 访问一个请求的 @ref[entity](../routing-dsl/directives/marshalling-directives/entity.md) 时。

However, you can also use the unmarshalling infrastructure directly if you wish, which can be useful for example in tests.
The best entry point for this is the @scala[`akka.http.scaladsl.unmarshalling.Unmarshal` object]@java[`akka.http.javadsl.unmarshalling.StringUnmarshallers` class], which you can use like this:

但是，如果你希望，也可以直接使用编组器基础设施，例如在测试中可能很有用。@scala[`akka.http.scaladsl.unmarshalling.Unmarshal` object]@java[`akka.http.javadsl.unmarshalling.StringUnmarshallers` 类] 是最好的切入点，你可以这样使用：

Scala
:  @@snip [UnmarshalSpec.scala]($test$/scala/docs/http/scaladsl/UnmarshalSpec.scala) { #use-unmarshal }

Java
:  @@snip [UnmarshalTest.scala]($test$/java/docs/http/javadsl/UnmarshalTest.java) { #imports #use-unmarshal }
