# 路由 DSL 概述
*Routing DSL Overview*

The Akka HTTP @ref[Core Server API](../server-side/low-level-api.md) provides a @apidoc[Flow]- or `Function`-level interface that allows
an application to respond to incoming HTTP requests by simply mapping requests to responses
(excerpt from @ref[Low-level server side example](../server-side/low-level-api.md#http-low-level-server-side-example)):

Akka HTTP @ref[核心服务器 API](../server-side/low-level-api.md) 提供 @apidoc[Flow]- 或 `Function`-级的接口，
允许应用程序通过简单的把请求映射到响应来回应传入的 HTTP 请求（节选自 @ref[低级服务器端示例](../server-side/low-level-api.md#http-low-level-server-side-example)）：

Scala
:  @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #low-level-server-example }

Java
:  @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #request-handler }

While it'd be perfectly possible to define a complete REST API service purely by @scala[pattern-matching against]@java[inspecting] the incoming
@apidoc[HttpRequest] @scala[(maybe with the help of a few extractors in the way of [Unfiltered](https://unfiltered.ws/))] this approach becomes somewhat
unwieldy for larger services due to the amount of syntax "ceremony" required. Also, it doesn't help in keeping your
service definition as [DRY](http://en.wikipedia.org/wiki/Don%27t_repeat_yourself) as you might like.

虽然完全可以通过 @scala[模式匹配]@java[检查] 传入的 @apidoc[HttpRequest]
@scala[(允许，在一此抽取器的帮助下以 [Unfiltered](https://unfiltered.ws/) 的方式)] 来定义一个完整、纯粹的 REST API 服务，
但由于所需语法“仪式”的数量，该方法对于大量服务显得笨重。
而且，它不能帮助服务定义保持 [DRY](http://en.wikipedia.org/wiki/Don%27t_repeat_yourself) 。

As an alternative Akka HTTP provides a flexible DSL for expressing your service behavior as a structure of
composable elements (called @ref[Directives](directives/index.md)) in a concise and readable way. Directives are assembled into a so called
*route structure* which, at its top-level, can be used to create a handler @apidoc[Flow] or async handler function that
can be directly supplied to a `bind` call. @scala[The conversion from @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] to flow can either be invoked explicitly
using `Route.handlerFlow` or, otherwise, the conversion is also provided implicitly by
`RouteResult.route2HandlerFlow` <a id="^1" href="#1">[1]</a>.]

作为替代，Akka HTTP 提供了一种灵活的 DSL 以简洁易读的方式表达服务行为表示为可组合元素（称为 @ref[指令](directives/index.md) ）的结构。
指令被组装成 *路由结构* ，在它的顶层可用于创建处理程序 @apidoc[Flow] 或异步处理函数，该处理函数可以直接提供给 `bind` 调用。
@scala[从 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult]) 转换]@java[@apidoc[Route]] 到 flow 可以显示调用 `Route.handlerFlow` ，或者也可以由 `RouteResult.route2HandlerFlow` 隐式提供。

Here's the complete example rewritten using the composable high-level API:

这里是使用可组合的高级 API 重写的完整示例：

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #high-level-server-example }

Java
:   @@snip [HighLevelServerExample.java]($test$/java/docs/http/javadsl/server/HighLevelServerExample.java) { #high-level-server-example }

The core of the Routing DSL becomes available with a single import:

路由 DSL 的核心可用单个导入语句：

Scala
:   ```scala
import akka.http.scaladsl.server.Directives._
```

Java
:   ```java
import static akka.http.javadsl.server.Directives.*;
```

@@@ div { .group-java }

Or by extending the `akka.http.javadsl.server.AllDirectives` class which brings together all directives into a single class
for easier access:

或者继承 `akka.http.javadsl.server.AllDirectives` 类，它将所有指令一起放入单个类中，以便于访问：

```java
extends AllDirectives
```

Of course it is possible to directly import only the directives you need (i.e. @apidoc[WebSocketDirectives] etc).

当然，可以直接导入只需要的指令（例如： @apidoc[WebSocketDirectives] 等）。

@@@

@@@ div { .group-scala }

This example also relies on the pre-defined support for Scala XML with:

这个示例还依赖预定义的 Scala XML 支持：

```scala
import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._
```

@@@

The very short example shown here is certainly not the best for illustrating the savings in "ceremony" and improvements
in conciseness and readability that the Routing DSL promises. The @ref[Long Example](index.md#longer-example) might do a better job in this
regard.

这里显示的非常简短的例子当然不是路由 DSL 所承诺节省“仪式”、提高简洁性和可读性的最好的说明。
@ref[长的示例](index.md#longer-example) 在这方面也放做的更好。

For learning how to work with the Routing DSL you should first understand the concept of @ref[Routes](routes.md).

要学习如何使用路由 DSL，你应该首先了解 @ref[路由](routes.md) 的概念。

@@@ div { .group-scala }

> <a id="1" href="#^1">[1]</a> To be picked up automatically, the implicit conversion needs to be provided in the companion object of the source
type. However, as @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] is just a type alias for `RequestContext => Future[RouteResult]`, there's no
companion object for @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]]. Fortunately, the [implicit scope](https://www.scala-lang.org/files/archive/spec/2.11/07-implicits.html#implicit-parameters) for finding an implicit conversion also
includes all types that are "associated with any part" of the source type which in this case means that the
implicit conversion will also be picked up from `RouteResult.route2HandlerFlow` automatically.

为了自动获取，隐式转换需要在源类型的伴身对象里提供。但是，因为 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidocRoute] 只是 `RequestContext => Future[RouteResult]` 的类型别名，所以不存在 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidocRoute] 伴身对象。
好在，查找隐式转换的 [隐式范围](https://www.scala-lang.org/files/archive/spec/2.11/07-implicits.html#implicit-parameters) 也包含在与源类型“任何部分相关”所有类型，在这种情况下，这意味着隐式转换将自动从 `RouteResult.route2HandlerFlow` 获取。
@@@
