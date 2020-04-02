# 路由测试包
*Route TestKit*

One of Akka HTTP's design goals is good testability of the created services.
For services built with the Routing DSL Akka HTTP provides a dedicated testkit that makes efficient testing of
route logic easy and convenient. This "route test DSL" is made available with the *akka-http-testkit* module.

Akka HTTP 的设计目标之一是创建的服务具有良好的可测试性。
对于使用路由 DSL 构建的服务，Akka HTTP 提供一个专用测试包，使得有效的路由逻辑测试容易且方便。
该“路由测试 DSL”与 *akka-http-testkit* 模块一起提供的。

## Dependency
**依赖**

To use Akka HTTP TestKit, add the module to your project:

要使用 Akka HTTP 测试包，添加模块到你的项目：

@@dependency [sbt,Gradle,Maven] {
  group="com.typesafe.akka" artifact="akka-stream-testkit_$scala.binary.version$" version="$akka.version$"
  group2="com.typesafe.akka" artifact2="akka-http-testkit_$scala.binary.version$" version2="$project.version$"
}

@@@ note
Since version `10.1.6`, `akka-stream-testkit` is a provided dependency, please remember to add it to your build dependencies.

从 `10.1.6` 版本开始，`akka-stream-testkit` 是可提供依赖，请记得将它添加到你的构建依赖里。
@@@

## Usage
**使用方法**

@@@ div { .group-scala }

Here is an example of what a simple test with the routing testkit might look like using the built-in support for
[scalatest](http://www.scalatest.org) and [specs2](http://etorreborre.github.io/specs2/):

这里是一个使用路由测试包的简单测试示例，使用了 [scalatest](http://www.scalatest.org) 和 [specs2](http://etorreborre.github.io/specs2/) 的内置支持：

ScalaTest
:  @@snip [FullTestKitExampleSpec.scala]($test$/scala/docs/http/scaladsl/server/FullTestKitExampleSpec.scala) { #source-quote }

specs2
:  @@snip [FullSpecs2TestKitExampleSpec.scala]($test$/scala/docs/http/scaladsl/server/FullSpecs2TestKitExampleSpec.scala) { #source-quote }

The basic structure of a test built with the testkit is this (expression placeholder in all-caps):

使用测试包构建的测试的基本结构是这样的（表达式点位符全部大写）：

```
REQUEST ~> ROUTE ~> check {
  ASSERTIONS
}
```

In this template *REQUEST* is an expression evaluating to an @apidoc[HttpRequest] instance.
In most cases your test will, in one way or another, extend from @apidoc[RouteTest] which itself mixes in the
`akka.http.scaladsl.client.RequestBuilding` trait, which gives you a concise and convenient way of constructing
test requests. <a id="^1" href="#1">[1]</a>

在这个模板中 *REQUEST* 是一个求值为 @apidoc[HttpRequest] 实例的表达式。
大多数情况下，你的测试将以某种方式从 @apidoc[RouteTest] 扩展，`RouteTest` 自身混入了 `akka.http.scaladsl.client.RequestBuilding` trait，
这提供了一个简洁方便的方式来构造测试请求。

*ROUTE* is an expression evaluating to a @ref[Route](routes.md). You can specify one inline or simply refer to the
route structure defined in your service.

*ROUTE* 是一个求值为 @ref[Route](routes.md) 的表达式。你可以指定一个内联或简单引用到服务里中定义的路由结构。

The final element of the `~>` chain is a `check` call, which takes a block of assertions as parameter. In this block
you define your requirements onto the result produced by your route after having processed the given request. Typically
you use one of the defined "inspectors" to retrieve a particular element of the routes response and express assertions
against it using the test DSL provided by your test framework. For example, with [scalatest](http://www.scalatest.org), in order to verify that
your route responds to the request with a status 200 response, you'd use the `status` inspector and express an
assertion like this:

用 `~>` 连接起来的最后个元素是 `check` 调用，它以断言块作为参数。在这个块中，你在处理给定请求后，将需求定义到路由生成的结果上。
通常，你使用一个定义的“检查器”来检索路由响应的特定元素，并且使用测试框架提供的测试 DSL 来表达对它的断言。例如：使用 [scalatest](http://www.scalatest.org) ，
为了验证路由以状态码 200 响应回复请求，可以使用 `status` 检查器并表示如下断言：

```scala
status shouldEqual 200
```

The following inspectors are defined:

定义了下列检查器：

### Table of Inspectors

|Inspector                                    | Description                                                                                                                                                         |
|---------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|`charset: HttpCharset`                       | Identical to `contentType.charset`                                                                                                                                  |
|`chunks: Seq[HttpEntity.ChunkStreamPart]`    | Returns the entity chunks produced by the route. If the entity is not `chunked` returns `Nil`.                                                                      |
|`closingExtension: String`                   | Returns chunk extensions the route produced with its last response chunk. If the response entity is unchunked returns the empty string.                             |
|`contentType: ContentType`                   | Identical to `responseEntity.contentType`                                                                                                                           |
|`definedCharset: Option[HttpCharset]`        | Identical to `contentType.definedCharset`                                                                                                                           |
|`entityAs[T :FromEntityUnmarshaller]: T`     | Unmarshals the response entity using the in-scope `FromEntityUnmarshaller` for the given type. Any errors in the process trigger a test failure.                    |
|`handled: Boolean`                           | Indicates whether the route produced an @apidoc[HttpResponse] for the request. If the route rejected the request `handled` evaluates to `false`.                           |
|`header(name: String): Option[HttpHeader]`   | Returns the response header with the given name or `None` if no such header is present in the response.                                                             |
|`header[T <: HttpHeader]: Option[T]`         | Identical to `response.header[T]`                                                                                                                                   |
|`headers: Seq[HttpHeader]`                   | Identical to `response.headers`                                                                                                                                     |
|`mediaType: MediaType`                       | Identical to `contentType.mediaType`                                                                                                                                |
|`rejection: Rejection`                       | The rejection produced by the route. If the route did not produce exactly one rejection a test failure is triggered.                                                |
|`rejections: Seq[Rejection]`                 | The rejections produced by the route. If the route did not reject the request a test failure is triggered.                                                          |
|`response: HttpResponse`                     | The @apidoc[HttpResponse] returned by the route. If the route did not return an @apidoc[HttpResponse] instance (e.g. because it rejected the request) a test failure is triggered.|
|`responseAs[T: FromResponseUnmarshaller]: T` | Unmarshals the response entity using the in-scope `FromResponseUnmarshaller` for the given type. Any errors in the process trigger a test failure.                  |
|`responseEntity: HttpEntity`                 | Returns the response entity.                                                                                                                                        |
|`status: StatusCode`                         | Identical to `response.status`                                                                                                                                      |
|`trailer: Seq[HttpHeader]`                   | Returns the list of trailer headers the route produced with its last chunk. If the response entity is unchunked returns `Nil`.                                      |

> <a id="1" href="#^1">[1]</a> If the request URI is relative it will be made absolute using an implicitly available instance of
`DefaultHostInfo` whose value is "[http://example.com](http://example.com)" by default. This mirrors the behavior of akka-http-core
which always produces absolute URIs for incoming request based on the request URI and the `Host`-header of
the request. You can customize this behavior by bringing a custom instance of `DefaultHostInfo` into scope.

> 如果请求 URI 是相对模式，那它将使用可用的 `DefaultHostInfo` 隐式实例组装为完全的，`DefaultHostInfo` 默认值是“[http://example.com](http://example.com)”。
这是 akka-http-core 行为的写照，它总是基于请求 URI 和请求的 `Host`-头为传入请求生成绝对 URI 。
你可以提供一个自定义 `DefaultHostInfo` 实例到作用域范围内定制该行为。 

@@@

@@@ div { .group-java }

To use the testkit you need to take these steps:

 * add a dependency to the `akka-http-testkit` module
 * derive the test class from `JUnitRouteTest`
 * wrap the route under test with `RouteTest.testRoute` to create a `TestRoute`
 * run requests against the route using `TestRoute.run(request)` which will return
a `TestResponse`
 * use the methods of `TestResponse` to assert on properties of the response

## Example
**示例**

To see the testkit in action consider the following simple calculator app service:

Java
:   @@snip [MyAppService.java]($test$/java/docs/http/javadsl/server/testkit/MyAppService.java) { #simple-app }

`MyAppService` extends from `AllDirectives` which brings all of the directives into scope. We define a method called `createRoute`
that provides the routes to serve to `bindAndHandle`.

Here's how you would test that service:

Java
:   @@snip [TestkitExampleTest.java]($test$/java/docs/http/javadsl/server/testkit/TestkitExampleTest.java) { #simple-app-testing }

## Writing Asserting against the HttpResponse

The testkit supports a fluent DSL to write compact assertions on the response by chaining assertions
using "dot-syntax". To simplify working with streamed responses the entity of the response is first "strictified", i.e.
entity data is collected into a single @apidoc[akka.util.ByteString] and provided the entity is supplied as an `HttpEntityStrict`. This
allows to write several assertions against the same entity data which wouldn't (necessarily) be possible for the
streamed version.

All of the defined assertions provide HTTP specific error messages aiding in diagnosing problems.

Currently, these methods are defined on `TestResponse` to assert on the response:

|Inspector                                                           | Description                                                                                                                         |
|--------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|
|`assertStatusCode(int expectedCode)`                                | Asserts that the numeric response status code equals the expected one                                                               |
|`assertStatusCode(StatusCode expectedCode)`                         | Asserts that the response @apidoc[StatusCode] equals the expected one                                                                      |
|`assertMediaType(String expectedType)`                              | Asserts that the media type part of the response's content type matches the given String                                            |
|`assertMediaType(MediaType expectedType)`                           | Asserts that the media type part of the response's content type matches the given @apidoc[MediaType]                                       |
|`assertEntity(String expectedStringContent)`                        | Asserts that the entity data interpreted as UTF8 equals the expected String                                                         |
|`assertEntityBytes(ByteString expectedBytes)`                       | Asserts that the entity data bytes equal the expected ones                                                                          |
|`assertEntityAs(Unmarshaller<T> unmarshaller, expectedValue: T)`    | Asserts that the entity data if unmarshalled with the given marshaller equals the given value                                       |
|`assertHeaderExists(HttpHeader expectedHeader)`                     | Asserts that the response contains an HttpHeader instance equal to the expected one                                                 |
|`assertHeaderKindExists(String expectedHeaderName)`                 | Asserts that the response contains a header with the expected name                                                                  |
|`assertHeader(String name, String expectedValue)`                   | Asserts that the response contains a header with the given name and value.                                                          |

It's, of course, possible to use any other means of writing assertions by inspecting the properties the response
manually. As written above, `TestResponse.entity` and `TestResponse.response` return strict versions of the
entity data.

## Supporting Custom Test Frameworks

Adding support for a custom test framework is achieved by creating new superclass analogous to
`JUnitRouteTest` for writing tests with the custom test framework deriving from `akka.http.javadsl.testkit.RouteTest`
and implementing its abstract methods. This will allow users of the test framework to use `testRoute` and
to write assertions using the assertion methods defined on `TestResponse`.


@@@

## Testing sealed Routes
**测试密封的路由**

The section above describes how to test a "regular" branch of your route structure, which reacts to incoming requests
with HTTP response parts or rejections. Sometimes, however, you will want to verify that your service also translates
@ref[Rejections](rejections.md) to HTTP responses in the way you expect.

上面部分阐述了怎样测试路由结构的“规则”分支，它用 HTTP 响应部分或拒绝来对传入请求作出反应。
但是，有时你想验证你的服务也以预期的方式转换 @ref[拒绝](rejections.md) 到 HTTP 响应。 

You do this by calling the `Route.seal()` method. The `Route.seal()` method applies the logic of the @scala[in-scope]
@ref[ExceptionHandler](exception-handling.md) and @ref[RejectionHandler](rejections.md#the-rejectionhandler)
@java[passed as method arguments] to all exceptions and rejections coming back from the
route, and translates them to the respective @apidoc[HttpResponse].

你通过调用 `Route.seal()` 方法。`Route.seal()` 方法将 @scala[作用域内] 
@ref[ExceptionHandler](exception-handling.md) 和 @ref[RejectionHandler](rejections.md#the-rejectionhandler) 的逻辑
应用到从路由返回的所有异常和拒绝，并且转换它们为各种的 @apidoc[HttpResponse] 。

Note that explicit call on the `Route.seal` method is needed in test code, but in your application code it is not necessary.
As described in @ref[Sealing a Route](routes.md#sealing-a-route), your application code only needs to bring
implicit rejection and exception handlers in scope.

注意，在测试代码里需要明确调用 `Route.seal` 方法，但是在你的应用程序代码中没有必要这么做。如 @ref[密封路由](routes.md#sealing-a-route) 里的描述，
应用程序代码只需要在作用域范围内提供隐式的拒绝和异常处理器即可。

## Testing Route fragments
**测试路由片段**

Since the testkit is request-based, you cannot test requests that are illegal or impossible in HTTP. One
such instance is testing a route that begins with the `pathEnd` directive, such as `routeFragment` here:

因为测试包是基于请求的，你不能测试 HTTP 里非法或不可能的请求。一个类似实例是以 `pathEnd` 指令开始的测试路由，如 `routeFragment`：

Scala
:   @@snip [TestKitFragmentSpec.scala]($test$/scala/docs/http/scaladsl/server/TestKitFragmentSpec.scala) { #fragment }

Java
:   @@snip [MyAppFragment.java]($test$/java/docs/http/javadsl/server/testkit/MyAppFragment.java) { #fragment}

You might create a route such as this to be able to compose it into another route such as:

你可以创建这样的，以便能够将它组合成另一个路由，如：

Scala
:   @@snip [FragmentExample.scala]($test$/scala/docs/http/scaladsl/server/FragmentExample.scala) { #source-quote }

Java
:   @@snip [MyAppFragment.java]($test$/java/docs/http/javadsl/server/testkit/MyAppFragment.java) { #source-quote }


However, it is impossible to unit test this Route directly using testkit, since it is impossible to create an
empty HTTP request. To test this type of route, embed it in a synthetic route in your test, such as `testRoute` in the example above.

但是，不可能直接使用测试包对这个路由进行单元测试，因为创建一个空的 HTTP 请求是不可能的。要测试这种类型的路由，将它嵌入到你的测试中的合成路由，
如示例上面所示的 `testRoute`：

This is what the full working test looks like:

这是完成的测试：

Scala
:   @@snip [TestKitFragmentSpec.scala]($test$/scala/docs/http/scaladsl/server/TestKitFragmentSpec.scala) { #source-quote }

Java
:   @@snip [TestKitFragmentTest.java]($test$/java/docs/http/javadsl/server/testkit/TestKitFragmentTest.java) { #source-quote }

## Accounting for Slow Test Systems
**考虑缓慢的测试系统**

The timeouts you consciously defined on your lightning fast development environment might be too tight for your, most
probably, high-loaded Continuous Integration server, invariably causing spurious failures. To account for such
situations, timeout durations can be scaled by a given factor on such environments. Check the
@scala[@extref[Akka Docs](akka-docs:scala/testing.html#accounting-for-slow-test-systems)]@java[@extref[Akka Docs](akka-docs:java/testing.html#accounting-for-slow-test-systems)]
for further information.

在闪电般的快速开发环境中有意识地定义的超时对于高负载的持续集成服务器而言很可能太严了，这总是会导致虚假故障。
考虑到这种情况，在此类环境中按给定的因子来缩放超时持续时间。检查 @scala[@extref[Akka 文档](akka-docs:scala/testing.html#accounting-for-slow-test-systems)]@java[@extref[Akka 文档](akka-docs:java/testing.html#accounting-for-slow-test-systems)] 获取进一步的信息。

## 增加超时
**Increase Timeout**

The default timeout when testing your routes using the testkit is @scala[1 second]@java[3 seconds] second. Sometimes, though, this might not be enough.
In order to extend this default timeout, to say 5 seconds, just add the following implicit in scope:

使用测试包测试你的路由时默认设置设置为 @scala[1 second]@java[3 seconds] 。但有时候这可能还不够。
为了延长这个默认超时（例如：5秒），只需要在作用域范围内添加以下隐式值：

Scala
:   @@snip [TestKitFragmentSpec.scala]($test$/scala/docs/http/scaladsl/server/TestKitFragmentSpec.scala) { #timeout-setting }

Java
:   @@snip [WithTimeoutTest.java]($test$/java/docs/http/javadsl/server/testkit/WithTimeoutTest.java) { #timeout-setting }

Remember to configure the timeout using `dilated` if you want to account for slow test systems.

如果希望考虑慢速测试系统，记得使用 `dilated` 配置超时。

## Examples
**示例**

A great pool of examples are the tests for all the predefined directives in Akka HTTP.
They can be found @scala[@github[here](/akka-http-tests/src/test/scala/akka/http/scaladsl/server/directives/)]@java[@github[here](/akka-http-tests/src/test/java/akka/http/javadsl/server/directives/)].

大量的例子是对 Akka HTTP 中所有预定义指令的测试。可以在这里找到 @scala[@github[这里](/akka-http-tests/src/test/scala/akka/http/scaladsl/server/directives/)]@java[@github[这里](/akka-http-tests/src/test/java/akka/http/javadsl/server/directives/)] 。
