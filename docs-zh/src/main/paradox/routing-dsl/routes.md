<a id="routes"></a>
# 路由
*Routes*

The "Route" is the central concept of Akka HTTP's Routing DSL. All the structures you build with the DSL, no matter
whether they consists of a single line or span several hundred lines, are @scala[`type`]@java[`function`] turning a 
@apidoc[RequestContext] into a @scala[`Future[RouteResult]`]@java[`CompletionStage<RouteResult>`].

“Route”是 Akka HTTP 路由 DSL 的中心概念。使用 DSL 构建的所有结构，不管是否包含在单行或跨几百行，都是将
@apidoc[RequestContext] 转换为 @scala[`Future[RouteResult]`]@java[`CompletionStage<RouteResult>`] @scala[类型]@java[函数]。

@@@ div { .group-scala }

```scala
type Route = RequestContext => Future[RouteResult]
```
It's a simple alias for a function turning a @apidoc[RequestContext] into a `Future[RouteResult]`.

将 @apidoc[RequestContext] 转换为 `Future[RouteResult]` 的函数的简单别名。
@@@

@@@ div { .group-java }

A @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] itself is a function that operates on a @apidoc[RequestContext] and returns a @apidoc[RouteResult]. The
@apidoc[RequestContext] is a data structure that contains the current request and auxiliary data like the so far unmatched
path of the request URI that gets passed through the route structure. It also contains the current `ExecutionContext`
and `akka.stream.Materializer`, so that these don't have to be passed around manually.

@scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 本向是一个函数，它操作一个 @apidoc[RequestContext] 并返回一个 @apidoc[RouteResult] 。
@apidoc[RequestContext] 是一个数据结果，包含当前请求和辅助数据，例如：到目前为止通过路由结构传递的请求 URI 的未匹配路径。
也包含当前的 `ExecutionContext` 和 `akka.stream.Materializer`，因此，这些不需要手动传递。
@@@

Generally when a route receives a request (or rather a @apidoc[RequestContext] for it) it can do one of these things:

通常当路由收到一个请求（更确切的说是 @apidoc[RequestContext] ），它可以做这些事情之一：

 * Complete the request by returning the value of `requestContext.complete(...)`
 * Reject the request by returning the value of `requestContext.reject(...)` (see @ref[Rejections](rejections.md#rejections))
 * Fail the request by returning the value of `requestContext.fail(...)` or by just throwing an exception (see @ref[Exception Handling](exception-handling.md#exception-handling))
 * Do any kind of asynchronous processing and instantly return a @scala[`Future[RouteResult]`]@java[`CompletionStage<RouteResult>`] to be eventually completed later

 - 通过返回 `requestContext.complete(...)` 的值完成请求
 - 通过返回 `requestContext.reject(...)` 的值拒绝请求（见 @ref[Rejections](rejections.md#rejections) ）
 - 通过返回 `requestContext.fail(...)` 的值或通过抛出一个异常使请求失败（见 @ref[Exception Handling](exception-handling.md#exception-handling)）
 - 进行任何类型的异步处理，并即刻返回 @scala[`Future[RouteResult]`]@java[`CompletionStage<RouteResult>`]，以便最终在以后完成

The first case is pretty clear, by calling `complete` a given response is sent to the client as reaction to the
request. In the second case "reject" means that the route does not want to handle the request. You'll see further down
in the section about route composition what this is good for.

第一种情况很清楚，通过调用 `complete` 将指定响应作为对请求的响应发送到客户端。
在第二种情况里，“reject”意味着路由不愿处理该请求。
你将进一步在关于路由组合部分了解这样做的好处。

A @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] can be "sealed" using `Route.seal`, which relies on the in-scope `RejectionHandler` and @apidoc[ExceptionHandler]
instances to convert rejections and exceptions into appropriate HTTP responses for the client.
@ref[Sealing a Route](#sealing-a-route) is described more in detail later. 

可以使用 `Route.seal` “密封” @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] ，路由依赖范围内的
`RejectionHandler` 和 @apidoc[ExceptionHandler] 实例将拒绝和异常转换为对客户端合适的 HTTP 响应。
后面 [密封路由](#sealing-a-route) 更详细地介绍。

Using `Route.handlerFlow` or `Route.asyncHandler` a @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] can be lifted into a handler @apidoc[Flow] or async handler
function to be used with a `bindAndHandleXXX` call from the @ref[Core Server API](../server-side/low-level-api.md).

使用 `Route.handlerFlow` 或 `Route.asyncHandler`，可以将 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 提升为 @apidoc[Flow] 或异步处理函数，它们可用于 @[核心服务器 API](../server-side/low-level-api.md) 的 `bindAndHandleXXX` 调用。

Note: There is also an implicit conversion from @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] to @apidoc[Flow[HttpRequest, HttpResponse, Unit]] defined in the
@apidoc[RouteResult] companion, which relies on `Route.handlerFlow`.

注意：也有从 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 到 @apidoc[Flow[HttpRequest, HttpResponse, Unit]] 的隐式转换定义在 @apidoc[RouteResult] 伴身对象，它依赖 `Route.handlerFlow`。

<a id="requestcontext"></a>
## RequestContext
**请求上下文**

The request context wraps an @apidoc[HttpRequest] instance to enrich it with additional information that are typically
required by the routing logic, like an `ExecutionContext`, @apidoc[Materializer], @apidoc[LoggingAdapter] and the configured
@apidoc[RoutingSettings]. It also contains the `unmatchedPath`, a value that describes how much of the request URI has not
yet been matched by a @ref[Path Directive](directives/path-directives/index.md#pathdirectives).

请求上下文包装了 @apidoc[HttpRequest] 实例，通过路由逻辑通常需要的使用附加信息来丰富它，例如：`ExecutionContext`、 @apidoc[Materializer] 、
@apidoc[LoggingAdapter] 和 已配置的 @apidoc[RoutingSettings] 。它还包含 `unmatchedPath`，该值描述 @ref[Path 指令](directives/path-directives/index.md#pathdirectives) 还未匹配多少请求 URI。 

The @apidoc[RequestContext] itself is immutable but contains several helper methods which allow for convenient creation of
modified copies.

@apidoc[RequestContext] 本身是不可变的，但是它包含一些辅助方法来方便创建修改后的复本。

<a id="routeresult"></a>
## RouteResult
**路由结果**

@apidoc[RouteResult] is a simple algebraic data type (ADT) that models the possible non-error results of a @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]].
It is defined as such:

@apidoc[RouteResult] 是一种简单代数数据类型（ADT），它对 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 可能的非错误结果建模。

它是这样定义的：

@@@ div { .group-scala }

```scala
sealed trait RouteResult

object RouteResult {
  final case class Complete(response: HttpResponse) extends RouteResult
  final case class Rejected(rejections: immutable.Seq[Rejection]) extends RouteResult
}
```

@@@

Usually you don't create any @apidoc[RouteResult] instances yourself, but rather rely on the pre-defined @ref[RouteDirectives](directives/route-directives/index.md#routedirectives)
(like @ref[complete](directives/route-directives/complete.md#complete), @ref[reject](directives/route-directives/reject.md#reject) or @ref[redirect](directives/route-directives/redirect.md#redirect)) or the respective methods on the [RequestContext](#requestcontext)
instead.

通常你自己不会创建任何 @apidoc[RouteResult] 实例，面是依赖预定义的 @ref[RouteDirectives](directives/route-directives/index.md#routedirectives)
（例如： @ref[complete](directives/route-directives/complete.md#complete) 、 @ref[reject](directives/route-directives/reject.md#reject) 或 @ref[redirect](directives/route-directives/redirect.md#redirect) ）或者 [RequestContext](#requestcontext) 上的相应方法。

## Composing Routes
**组合路由**

There are three basic operations we need for building more complex routes from simpler ones:

我们需要三个基本操作符来从简单路由构建更复杂的路由

 * Route transformation, which delegates processing to another, "inner" route but in the process changes some properties
of either the incoming request, the outgoing response or both
 * Route filtering, which only lets requests satisfying a given filter condition pass and rejects all others
 * Route chaining, which tries a second route if a given first one was rejected

 - 路由转换，将处理委托给另一个“内部”路由，但在此过程中改变传入请求或外出响应或者两者的一些属性
 - 路由过滤，将只让满足指定过滤条件的通过，并拒绝所有其它的
 - 路由链，如果指定的第一个路由被拒绝，将尝试第二个路由

The last point is achieved with the concatenation operator `~`, which is an extension method that becomes available
when you `import akka.http.scaladsl.server.Directives._`.
The first two points are provided by so-called @ref[Directives](directives/index.md#directives) of which a large number is already predefined by Akka
HTTP and which you can also easily create yourself.
@ref[Directives](directives/index.md#directives) deliver most of Akka HTTP's power and flexibility.

最后一点是使用连接操作符 `~` 达成的，操作符是一个扩展方法，在 `import akka.http.scaladsl.server.Directives._` 语句可用。
第一、二两点由 @ref[Directives](directives/index.md#directives) 提供，Akka HTTP 已经预定义了大量的指令，你也可以很容易的定义自己的指令。
@ref[Directives](directives/index.md#directives)  提供了 Akka HTTP 的大部分强大功能和灵活性。

<a id="the-routing-tree"></a>
## The Routing Tree
**路由树**

Essentially, when you combine directives and custom routes via the `concat` method, you build a routing
structure that forms a tree. When a request comes in it is injected into this tree at the root and flows down through
all the branches in a depth-first manner until either some node completes it or it is fully rejected.

实质上，当你通过 `concat` 方法结合指令和自定义路由时，构建的路由建构形成一颗树。
当一个请求进来时，它在根被注入，并以深度优先的方式向下流径所有分支，直到某个节点完成或者被完全拒绝。

Consider this schematic example:

考虑这个示意图例子：

@@@ div { .group-scala }

```scala
val route =
  a {
    concat(
      b {
        concat(
          c {
            ... // route 1
          },
          d {
            ... // route 2
          },
          ... // route 3
        )
      },
      e {
        ... // route 4
      }
    )
  }
```

@@@

@@@ div { .group-java }

```java
import static akka.http.javadsl.server.Directives.*;

Route route =
  directiveA(concat(() ->
    directiveB(concat(() ->
      directiveC(
        ... // route 1
      ),
      directiveD(
        ... // route 2
      ),
      ... // route 3
    )),
    directiveE(
      ... // route 4
    )
  ));
```

@@@

Here five directives form a routing tree.

这里五个指令形成一颗路由树。

 * Route 1 will only be reached if directives `a`, `b` and `c` all let the request pass through.
 * Route 2 will run if `a` and `b` pass, `c` rejects and `d` passes.
 * Route 3 will run if `a` and `b` pass, but `c` and `d` reject.

 - 如果指令 `a`、`b` 和 `c` 都让请求通过，路由 1 才能到达。
 - 如果 `a` 和 `b` 通过，`c` 拒绝且 `d` 通过，路由 2 将运行。
 - 如果 `a` 和 `b` 通过，但 `c` 和 `d` 拒绝，路由 3 将运行。

Route 3 can therefore be seen as a "catch-all" route that only kicks in, if routes chained into preceding positions
reject. This mechanism can make complex filtering logic quite easy to implement: simply put the most
specific cases up front and the most general cases in the back.

因此如果路由进入前面的路径被拒绝，则路由 3 可以看作“捕获-所有”路由。这种机制可以使复杂过滤逻辑非常容易实现：简单地把最特殊情况放在前面，而把最一般的情况放在后面。

## Sealing a Route
**密封路由**

As described in @ref[Rejections](rejections.md) and @ref[Exception Handling](exception-handling.md),
there are generally two ways to handle rejections and exceptions.

如 @ref[拒绝](rejections.md) 和 @ref[异常处理](exception-handling.md) 所述，通常有两种方法来处理拒绝和异常。 

 * Bring rejection/exception handlers @scala[`into implicit scope at the top-level`]@java[`seal()` method of the @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]]]
 * Supply handlers as arguments to @ref[handleRejections](directives/execution-directives/handleRejections.md#handlerejections) and @ref[handleExceptions](directives/execution-directives/handleExceptions.md#handleexceptions) directives 

 - 提供拒绝/异常处理程序 @scala[`到顶层的隐式范围里`]@java[给 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 的 `seal()` 方法]
 - 提供处理程序作为 @ref[handleRejections](directives/execution-directives/handleRejections.md#handlerejections) 和 @ref[handleExceptions](directives/execution-directives/handleExceptions.md#handleexceptions) 指令的参数

In the first case your handlers will be "sealed", (which means that it will receive the default handler as a fallback for all cases your handler doesn't handle itself) 
and used for all rejections/exceptions that are not handled within the route structure itself.

在第一种情况，你的处理程序将被“密封”（这意味着它将接收默认处理程序，作为对于你的处理程序本身不处理的所有情况的后备），
并且用于路由结构本身不处理的所有拒绝/异常。

### Route.seal() method to modify HttpResponse
**Route.seal() 方法修改 HttpResponse**

In application code, unlike @ref[test code](testkit.md#testing-sealed-routes), you don't need to use the `Route.seal()` method to seal a route.
As long as you bring implicit rejection and/or exception handlers to the top-level scope, your route is sealed. 

在应用程序代码中，不像 @ref[测试代码](testkit.md#testing-sealed-routes) 那样，你不需要使用 `Route.seal()` 方法密封路由。
只要在顶层提供了隐式拒绝和/或异常处理器，你的路由将被密闭。

However, you can use `Route.seal()` to perform modification on HttpResponse from the route.
For example, if you want to add a special header, but still use the default rejection handler, then you can do the following.
In the below case, the special header is added to rejected responses which did not match the route, as well as successful responses which matched the route.

但是，你可以使用 `Route.seal()` 从路由对 HttpResponse 进行修改。
例如，你想添加一个特殊的头域，但仍然想使用默认拒绝处理程序，那么你可以向下面这样做。
特殊的头域被添加到不匹配路由的拒绝响应以及匹配路由的成功响应里。

Scala
:   @@snip [RouteSealExampleSpec.scala]($root$/src/test/scala/docs/http/scaladsl/RouteSealExampleSpec.scala) { #route-seal-example }

Java
:   @@snip [RouteSealExample.java]($root$/src/test/java/docs/http/javadsl/RouteSealExample.java) { #route-seal-example }

### Converting routes between Java and Scala DSLs
**在 Java 和 Scala DSL 之间转换路由**

In some cases when building reusable libraries that expose routes, it may be useful to be able to convert routes between
their Java and Scala DSL representations. You can do so using the `asScala` method on a Java DSL route, or by using an
`RouteAdapter` to wrap an Scala DSL route. 

在某些情况中，在构建路由的可复用库时，能够在 Java 和 Scala DSL 之间转换路由可能是有用的。
你可以通过在 Java DSL 路由上使用 `asScala` 方法，或者使用 `RouteAdapter` 包装一个 Scala DSL 来做到。

Converting Scala DSL routes to Java DSL:

转换 Scala DSL 路由到 Java DSL：

Scala
:   @@snip [RouteJavaScalaDslConversionSpec.scala]($akka-http$//akka-http-tests/src/test/scala/akka/http/scaladsl/RouteJavaScalaDslConversionSpec.scala) { #scala-to-java }

Java
:   @@snip [RouteSealExample.java]($akka-http$/akka-http-tests/src/test/java/docs/http/javadsl/server/RouteJavaScalaDslConversionTest.java) { #scala-to-java }

Converting Java DSL routes to Scala DSL:

转换 Java DSL 路由到 Scala DSL：

Scala
:   @@snip [RouteJavaScalaDslConversionSpec.scala]($akka-http$//akka-http-tests/src/test/scala/akka/http/scaladsl/RouteJavaScalaDslConversionSpec.scala) { #java-to-scala }

Java
:   @@snip [RouteSealExample.java]($akka-http$/akka-http-tests/src/test/java/docs/http/javadsl/server/RouteJavaScalaDslConversionTest.java) { #java-to-scala }
