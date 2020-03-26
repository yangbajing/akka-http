# JSON 支持
**JSON Support**

Akka HTTP's @ref[marshalling](marshalling.md) and @ref[unmarshalling](unmarshalling.md) infrastructure makes it rather easy to seamlessly convert application-domain objects from and to JSON.
Integration with @scala[[spray-json]]@java[[Jackson]] is provided out of the box through the @scala[`akka-http-spray-json`]@java[`akka-http-jackson`] module.
Integration with other JSON libraries are supported by the community.
See [the list of current community extensions for Akka HTTP](https://akka.io/community/#extensions-to-akka-http).

Akka HTTP 的 @ref[编组](marshalling.md) 和 @ref[解组](unmarshalling.md) 基础设施使应用域对象与 JSON 的无缝转换相当容易。
通过使用 @scala[[spray-json]]@java[[Jackson]] 提供的开箱即用  @scala[`akka-http-spray-json`]@java[`akka-http-jackson`] 模块集成。
集成其它 JSON 库由社区提供支持。见 [Akka HTTP 的当前社区扩展列表](https://akka.io/community/#extensions-to-akka-http) 。

@@@ div { .group-java }

## Jackson Support
**Jackson 支持**

To make use of the support module for (un)marshalling from and to JSON with [Jackson], add a library dependency onto:

要利用 [Jackson] 支持模块 (解)编组 JSON，添加库依赖到项目：

@@dependency [sbt,Gradle,Maven] {
  group="com.typesafe.akka"
  artifact="akka-http-jackson_$scala.binary.version$"
  version="$project.version$"
}

Use `akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller(T.class)` to create an @apidoc[Unmarshaller[HttpEntity,T]] which expects the request
body (HttpEntity) to be of type `application/json` and converts it to `T` using Jackson.

使用 `akka.http.javadsl.marshallers.jackson.Jackson.unmarshaller(T.class)` 创建一个 @apidoc[Unmarshaller[HttpEntity,T]] ，
它期望请求正文（HttpEntity）属于 `application/json` 类型并使用 Jackson 将其转换为 `T` 类型。

@@snip [PetStoreExample.java]($akka-http$/akka-http-tests/src/main/java/akka/http/javadsl/server/examples/petstore/PetStoreExample.java) { #imports #unmarshall }

Use `akka.http.javadsl.marshallers.jackson.Jackson.marshaller(T.class)` to create a @apidoc[Marshaller[T,RequestEntity]] which can be used with
`RequestContext.complete` or `RouteDirectives.complete` to convert a POJO to an HttpResponse.

使用 `akka.http.javadsl.marshallers.jackson.Jackson.marshaller(T.class)` 创建一个 @apidoc[Marshaller[T,RequestEntity]] ，
它可用于 `RequestContext.complete` 或 `RouteDirectives.complete` 转换一个 POJO 到 @apidoc[HttpResponse] 。

@@snip [PetStoreExample.java]($akka-http$/akka-http-tests/src/main/java/akka/http/javadsl/server/examples/petstore/PetStoreExample.java) { #imports #marshall }

Refer to @github[this file](/akka-http-tests/src/main/java/akka/http/javadsl/server/examples/petstore/PetStoreExample.java) in the sources for the complete example.

有关完整示例，请参考 @github[这个文件](/akka-http-tests/src/main/java/akka/http/javadsl/server/examples/petstore/PetStoreExample.java) 。

@@@


@@@ div { .group-scala }

## spray-json Support
**spray-json 支持**

The @scaladoc[SprayJsonSupport](akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport) trait provides a `FromEntityUnmarshaller[T]` and `ToEntityMarshaller[T]` for every type `T`
that an implicit `spray.json.RootJsonReader` and/or `spray.json.RootJsonWriter` (respectively) is available for.

@scaladoc[SprayJsonSupport](akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport) trait 为隐式转换 `spray.json.RootJsonReader` 和/或 `spray.json.RootJsonWriter` 可用的每个类型 `T` 分别提供 `FromEntityUnmarshaller[T]` 和 `ToEntityMarshaller[T]`。

*译注：若类型 `T` 在范围内有 `spray.json.RootJsonReader` 和/或 `spray.json.RootJsonWriter` 隐式转换，那 `SprayJsonSupport` 将自动生成 `FromEntityUnmarshaller[T]` 和 `ToEntityMarshaller[T]`*

To enable automatic support for (un)marshalling from and to JSON with [spray-json], add a library dependency onto:

要使用 [spray-json] 对(解)编组 JSON 启用自动支持，添加库依赖到项目：

@@dependency [sbt,Gradle,Maven] {
  group="com.typesafe.akka"
  artifact="akka-http-spray-json_$scala.binary.version$"
  version="$project.version$"
}

Next, provide a `RootJsonFormat[T]` for your type and bring it into scope. Check out the [spray-json] documentation for more info on how to do this.

接下来，为你的类型提供 `RootJsonFormat[T]` 并将其放入范围内。有关怎样执行此操作的更多信息，请查看 [spray-json] 文档。

Finally, import the `FromEntityUnmarshaller[T]` and `ToEntityMarshaller[T]` implicits directly from `SprayJsonSupport` as shown in the example below or mix the `akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport` trait into your JSON support module.

最后，如下面例子所示，直接从 `SprayJsonSupport` 导入 `FromEntityUnmarshaller[T]` 和 `ToEntityMarshaller[T]` 隐式转换，
或者将 `akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport` trait 混入你的 JSON 支持模块。

Once you have done this (un)marshalling between JSON and your type `T` should work nicely and transparently.

一旦完成了 JSON 和你的类型 `T` 之间的(解)编组功能，就可以很好地、透明的工作了。

@@snip [SprayJsonExampleSpec.scala]($test$/scala/docs/http/scaladsl/SprayJsonExampleSpec.scala) { #minimal-spray-json-example }

@@@ 

<a id="json-streaming-client-side"></a>
## Consuming JSON Streaming style APIs
**消费 JSON 流风格的 API**

A popular way of implementing streaming APIs is [JSON Streaming](https://en.wikipedia.org/wiki/JSON_Streaming) (see @ref[Source Streaming](../routing-dsl/source-streaming-support.md)
for documentation on building server-side of such API).

实现流式 API 的一种流行方式是 [JSON Streaming](https://en.wikipedia.org/wiki/JSON_Streaming)（有关构建此类 API 的服务器端文档，见 @ref[Source Streaming](../routing-dsl/source-streaming-support.md)）。

Depending on the way the API returns the streamed JSON (newline delimited, raw sequence of objects, or "infinite array") 
you may have to apply a different framing mechanism, but the general idea remains the same: consuming the infinite entity stream
and applying a framing to it, such that the single objects can be easily deserialized using the usual marshalling infrastructure:

根据 API 返回流式 JSON 的方式（换行分隔，对象的原始序列或者无限数组），你可能需要应用不同的分帧机制，但是思路是一样的：
消费无限实体流并向它应用分帧，这样，使用常规的编组基础设施，单个对象可以被容易地反序列化。 

Scala
:   @@snip [EntityStreamingSpec.scala]($akka-http$/akka-http-tests/src/test/scala/akka/http/scaladsl/server/EntityStreamingSpec.scala) { #json-streaming-client-example }
 
Java
:   @@snip [HttpClientExampleDocTest.java]($test$/java/docs/http/javadsl/server/JsonStreamingExamplesTest.java) { #json-streaming-client-example-raw }

@@@ div { .group-scala }

In the above example the marshalling is handled by the implicitly provided `JsonEntityStreamingSupport`, which is also used when building server-side streaming APIs.
You can also achieve the same more explicitly, by manually connecting the entity byte stream through a framing and then deserialization stage: 

在上面的示例中，编组由隐式提供的 `JsonEntityStreamingSupport` 处理，该支持也用于构建服务器端流式 API。
你也可以更明确的达到相同目的，通过分帧手动连接实体字节流，然后反序列化阶段：

Scala
:   @@snip [EntityStreamingSpec.scala]($akka-http$/akka-http-tests/src/test/scala/akka/http/scaladsl/server/EntityStreamingSpec.scala) { #json-streaming-client-example-raw }
 
@@@

@@@ div { .group-java }

In the above example the `JsonEntityStreamingSupport` class is used to obtain the proper framing, though you could also
pick the framing manually by using `akka.stream.javadsl.Framing` or `akka.stream.javadsl.JsonFraming`. 
Framing stages are used to "chunk up" the pieces of incoming bytes into appropriately sized pieces of valid JSON,
which then can be handled easily by a not-streaming JSON serializer such as jackson in the example. This technique is simpler to use
and often good enough rather than writing a fully streaming JSON parser (which also is possible). 

@@@ 


@@@ div { .group-scala }

## Pretty printing
**美化输出**

By default, spray-json marshals your types to compact printed JSON by implicit conversion using `CompactPrinter`, as defined in:

默认，spray-json 把你的类型使用隐式转换 `CompactPrinter` 以紧凑格式输出 JSON，如下：

@@snip [SprayJsonSupport.scala]($akka-http$/akka-http-marshallers-scala/akka-http-spray-json/src/main/scala/akka/http/scaladsl/marshallers/sprayjson/SprayJsonSupport.scala) { #sprayJsonMarshallerConverter }

Alternatively to marshal your types to pretty printed JSON, bring a `PrettyPrinter` in scope to perform implicit conversion.

或者，将你的类型编组为美化的 JSON，在作用域内提供 `PrettyPrinter` 执行隐式转换。

@@snip [SprayJsonPrettyMarshalSpec.scala]($test$/scala/docs/http/scaladsl/SprayJsonPrettyMarshalSpec.scala) { #example }

To learn more about how spray-json works please refer to its [documentation][spray-json].

要了解有关 spray-json 怎样工作的更多信息，请参考它的 [文档][spray-json]。

@@@

[spray-json]: https://github.com/spray/spray-json
[jackson]: https://github.com/FasterXML/jackson
