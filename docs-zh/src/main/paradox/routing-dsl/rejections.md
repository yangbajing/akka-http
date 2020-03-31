<a id="rejections"></a>
# 拒绝
*Rejections*

In the chapter about constructing @ref[Routes](routes.md) the @scala[`~` operator]@java[`RouteDirectives.route()` method] was introduced, which connects two or more routes in a way
that allows the next specified route to get a go at a request if the first route "rejected" it. The concept of "rejections" is
used by Akka HTTP for maintaining a more functional overall architecture and in order to be able to properly
handle all kinds of error scenarios.

在关于构建 @ref[路由](routes.md) 的章节中介绍了 @scala[`~` 操作符]@java[`RouteDirectives.route()` 方法] ，
它们是连接两个或更多路由的方法，这样允许请求在第一个路由“拒绝”它时到达下一个指定的路由。
Akka HTTP 使用“拒绝”的概念来维护更具有功能性的整体架构，并能够正确的处理各种错误场景。

When a filtering directive, like the @ref[get](directives/method-directives/get.md) directive, cannot let the request pass through to its inner route because
the filter condition is not satisfied (e.g. because the incoming request is not a GET request) the directive doesn't
immediately complete the request with an error response. Doing so would make it impossible for other routes chained in
after the failing filter to get a chance to handle the request.
Rather, failing filters "reject" the request in the same way as by explicitly calling `requestContext.reject(...)`.

当过滤指令（就像 @ref[get](directives/method-directives/get.md) 指令）不能让请求通过它的内部路由时，
因为不满足过滤条件（例如：因为传入请求不是一个 GET 请求），该指令不会使用一个错误响应立即完成请求。
这样做将使链接的其它路由在过滤失败后无法获得处理请求的机会 *（译注：这里指如果直接以错误响应回复请求的话）*。
某种程度上，“reject”请求与显示调用 `requestContext.reject(...)` 的方式一样。

After having been rejected by a route the request will continue to flow through the routing structure and possibly find
another route that can complete it. If there are more rejections all of them will be picked up and collected.

被一个路由拒绝后，请求将继续流径路由结构，并可能找到另一个可以完成它的路由。如果有更多的拒绝，它们所有都将被收集起来。

If the request cannot be completed by (a branch of) the route structure an enclosing @ref[handleRejections](directives/execution-directives/handleRejections.md) directive
can be used to convert a set of rejections into an @apidoc[HttpResponse] - which typically would be an error response.
`Route.seal()` internally wraps its argument route with the @ref[handleRejections](directives/execution-directives/handleRejections.md) directive in order to "catch"
and handle any rejection.

如果通过路由结构（分支）无法完成请求，则一个（把所有路由）围起来的 @ref[handleRejections](directives/execution-directives/handleRejections.md)
指令可用于将一系列的拒绝转换为 @apidoc[HttpResponse] - 这通常是一个错误响应。

## Predefined Rejections
**预定义的拒绝**

A rejection encapsulates a specific reason why a route was not able to handle a request. It is modeled as an object of
type @apidoc[Rejection]. Akka HTTP comes with a set of @scala[@scaladoc[predefined rejections](akka.http.scaladsl.server.Rejection)]@java[@javadoc[predefined rejections](akka.http.javadsl.server.Rejections)], which are used by the many
@ref[predefined directives](directives/alphabetically.md).

拒绝封装了路由为什么不能处理请求的具体原因。它被建模为 @apidoc[Rejection] 类型的对像。Akka HTTP 附带有一系列 @scala[@scaladoc[预定义的拒绝](akka.http.scaladsl.server.Rejection)]@java[@javadoc[预定义拒绝](akka.http.javadsl.server.Rejections)] ，它们被用于许多的 @ref[预定义指令](directives/alphabetically.md) 。

Rejections are gathered up over the course of a Route evaluation and finally converted to @apidoc[HttpResponse] replies by
the @ref[handleRejections](directives/execution-directives/handleRejections.md) directive if there was no way for the request to be completed.

拒绝在路由计算过程中收集，如果无法完成请求，最终通过 @ref[handleRejections](directives/execution-directives/handleRejections.md) 指令转换为 @apidoc[HttpResponse] 回复。

<a id="the-rejectionhandler"></a>
## The RejectionHandler

The @ref[handleRejections](directives/execution-directives/handleRejections.md) directive delegates the actual job of converting a list of rejections to the provided @scala[@scaladoc[RejectionHandler](akka.http.scaladsl.server.RejectionHandler)]@java[@javadoc[RejectionHandler](akka.http.javadsl.server.RejectionHandler)],
so it can choose whether it would like to handle the current set of rejections or not.
Unhandled rejections will simply continue to flow through the route structure.

@ref[handleRejections](directives/execution-directives/handleRejections.md) 指令将转换拒绝列表的实际工作委托给提供的 @scala[@scaladoc[RejectionHandler](akka.http.scaladsl.server.RejectionHandler)]@java[@javadoc[RejectionHandler](akka.http.javadsl.server.RejectionHandler)] ，
因此，它可以选择是否要处理当前的拒绝集。未处理的拒绝将继续流径路由结构。

The default `RejectionHandler` applied by the top-level glue code that turns a @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] into a
@apidoc[Flow] or async handler function for the @ref[low-level API](../server-side/low-level-api.md)
@scala[(via `Route.handlerFlow` or `Route.asyncHandler`)]
will handle *all* rejections that reach it.

由顶层胶水代码应用的默认 `RejectionHandler`，通过 `Route.handlerFlow` 或 `Route.asyncHandler` 把 
@scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 转换为用于 @ref[底级 API](../server-side/low-level-api.md) 的 @apidoc[Flow] 或异步处理函数，
它们将处理到达的 *所有* 拒绝。

@@@ note
Please note that since version `10.1.2`, the default `RejectionHandler` will also discard the entity bytes automatically. If you want to change this behavior,
please refer to @ref[Customising rejection HTTP Responses](rejections.md#customising-rejections); however, might cause connections to stall
if the entity is not properly rejected or cancelled on the client side.

请注意，从 `10.1.2` 版本开始，默认 `RejectionHandler` 也将自动丢弃（请求的）实体字节流。
如果你想改变这个行为，请参考 @ref[自定义拒绝 HTTP 响应](rejections.md#customising-rejections) ；但是，如果客户端上的实体没有正确地拒绝或取消，可能会导致连接挂住。
@@@


## Rejection Cancellation
**拒绝取消**

As mentioned above, the `RejectionHandler` doesn't handle single rejections but a whole list of
them. This is because some route structure produce several "reasons" why a request could not be handled.

如上面提到的，`RejectionHandler` 不是处理单个拒绝而是一大堆。这是因为路由结构生成各种“原因”说明请求为什么无法被处理。

Take this route structure for example:

以这个路由结构为例：

Scala
:  @@snip [RejectionHandlerExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/RejectionHandlerExamplesSpec.scala) { #example-1 }

Java
:  @@snip [RejectionHandlerExamplesTest.java]($test$/java/docs/http/javadsl/server/RejectionHandlerExamplesTest.java) { #example1 }

For uncompressed POST requests this route structure would initially yield two rejections:

对于未压缩的 POST 请求，这个路由结构最初产生两个拒绝：

 * a @apidoc[MethodRejection] produced by the @ref[get](directives/method-directives/get.md) directive (which rejected because the request is not a GET request)
 * an @apidoc[UnsupportedRequestEncodingRejection] produced by the @ref[decodeRequestWith](directives/coding-directives/decodeRequestWith.md) directive (which only accepts
gzip-compressed requests here)

 - 由 @ref[get](directives/method-directives/get.md) 指令生成的 @apidoc[MethodRejection] （因为请求不是一个 GET 请求而被拒绝）
 - 由 @ref[decodeRequestWith](directives/coding-directives/decodeRequestWith.md) 指令生成的 @apidoc[UnsupportedRequestEncodingRejection] （这里只接受 gzip-压缩的请求）

In reality the route even generates one more rejection, a @apidoc[TransformationRejection] produced by the @ref[post](directives/method-directives/post.md)
directive. It "cancels" all other potentially existing *MethodRejections*, since they are invalid after the
@ref[post](directives/method-directives/post.md) directive allowed the request to pass (after all, the route structure *can* deal with POST requests).
These types of rejection cancellations are resolved *before* a `RejectionHandler` is called with any rejection.
So, for the example above the `RejectionHandler` will be presented with only one single rejection, the @apidoc[UnsupportedRequestEncodingRejection].

实际上，路由甚至会产生一个拒绝，由 @ref[post](directives/method-directives/post.md) 指令生成的 @apidoc[TransformationRejection] 。
它“取消”所有其它潜在存在的 *MethodRejections*，因为在 @ref[post](directives/method-directives/post.md) 指令允许请求通过以后其它 *MethodRejections* 是无效的（毕竟，路由结构 *可以* 处理 POST 请求）。
在使用任何拒绝调用 `RejectionHandler` 之前，这些类型的拒绝消息已被解决 *（译注：`post` 指令将从存在的拒绝列表里过滤掉所有其它 `MethodRejection`）*。
这样，对于上面的示例，`RejectionHandler` 将只呈现出一个拒绝，既 @apidoc[UnsupportedRequestEncodingRejection] 。

<a id="empty-rejections"></a>
## Empty Rejections
**空拒绝**

Internally rejections are stored in an immutable list, so you might ask yourself what the semantics of
an empty rejection list are. In fact, empty rejection lists have well defined semantics. They signal that a request was
not handled because the respective resource could not be found. Akka HTTP reserves the special status of "empty
rejection" to this most common failure a service is likely to produce.

内部拒绝存储在一个不可变列表中，因此你可能会问空拒绝列表的语意是什么？实际上，空拒绝列表有明确的语义。它们表示由于无法找到相应的资源，从而没有处理请求。
Akka HTTP 给服务可能产生的这种最常见失败预留“空拒绝”的特殊状态。

So, for example, if the @ref[path](directives/path-directives/path.md) directive rejects a request it does so with an empty rejection list. The
@ref[host](directives/host-directives/host.md) directive behaves in the same way.

比如：如果 @ref[path](directives/path-directives/path.md) 指令使用一个空拒绝列表拒绝请求。@ref[host](directives/host-directives/host.md) 指令表现相同的（行为）方式。

## Customizing Rejection Handling
**自定义拒绝处理**

If you'd like to customize the way certain rejections are handled you'll have to write a custom
[RejectionHandler](#the-rejectionhandler). Here is an example:

如果你想自定义处理某些拒绝的方式，你需要写一个自定义的 [RejectionHandler](#the-rejectionhandler) 。这里是一个示例：

Scala
:  @@snip [RejectionHandlerExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/RejectionHandlerExamplesSpec.scala) { #custom-handler-example }

Java
:  @@snip [RejectionHandlerExamplesTest.java]($test$/java/docs/http/javadsl/server/RejectionHandlerExamplesTest.java) { #custom-handler-example-java }

The easiest way to construct a `RejectionHandler` is with `RejectionHandler.newBuilder()` that Akka HTTP provides.
After having created a new `Builder` instance
you can attach handling logic for certain types of rejections through three helper methods:

构造 `RejectionHandler` 的最简单方法是使用 Akka HTTP 提供的 `RejectionHandler.newBuilder()`。
创建新的 `Builder` 实例后，你可以通过三个辅助方法为某些拒绝类型附加处理逻辑：

@scala[handle(PartialFunction[Rejection, Route])]@java[handle(Class<T>, Function<T, Route>)]
: Handles the provided type of rejections with the given function. The provided function simply produces a @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] which is
run when the rejection is "caught". This makes the full power of the Routing DSL available for defining rejection
handlers and even allows for recursing back into the main route structure if required.

: 使用给定的函数处理提供的拒绝类型。提供的函数只是生成一个 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] ，当拒绝被“捕获”时运行该路由。
这使 路由 DSL 的全部功能都可用于定义拒绝处理程序，甚至可以在需要的时候递归地回到主路由结构中。 

@scala[handleAll[T <: Rejection: ClassTag](f: immutable.Seq[T] => Route)]@java[handleAll<T extends Rejection>(Class<T>, Function<List<T>, Route>)]
: Handles all rejections of a certain type at the same time. This is useful for cases where your need access to more
than the first rejection of a certain type, e.g. for producing the error message to an unsupported request method.

: 同时处理某些类型的所有拒绝。如果你需要访问某些类型的拒绝多次，这是有用的，例如：为不支持的请求方法生成错误消息。

handleNotFound(Route)
: As described [above](#empty-rejections) "Resource Not Found" is special as it is represented with an empty
rejection set. The `handleNotFound` helper let's you specify the "recovery route" for this case.

: 如 [上](#empty-rejections) 所述，“资源未找到”是特殊的，它是用空拒绝集表示的。
`handleNotFound` 助手让我们为这种情况指定“恢复路由”。

Even though you could handle several different rejection types in a single partial function supplied to `handle`@java[ by "listening" to the `Rejection.class`],
it is recommended to split these up into distinct `handle` attachments instead.
This way the priority between rejections is properly defined via the order of your `handle` clauses rather than the
(sometimes hard to predict or control) order of rejections in the rejection set.

尽管，你可以在 @scala[提供给 `handle` 的单个部分函数]@java[通过 "listening" `Rejection.class`] 里处理各种不同的拒绝类型，
建议拆分这些到不同的 `handle` 附件。这样通过你的 `handle` 子句的顺序，而不是（有时很难预测或控制）拒绝集里的拒绝顺序来正确定义（处理）拒绝之间的优行级。

Once you have defined your custom `RejectionHandler` you have two options for "activating" it:

一旦你定义好了自定义 `RejectionHandler`，有两种选项来“激活”它：

 1. @scala[Bring it into implicit scope at the top-level]@java[Pass it to the `seal()` method of the @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] class]
 2. Supply it as an argument to the @ref[handleRejections](directives/execution-directives/handleRejections.md) directive

 - @scala[提供它到隐式作用域的顶层]@java[把它传递给 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 类的 `seal()` 方法]
 - 提供它作为 @ref[handleRejections](directives/execution-directives/handleRejections.md) 指令的参数

In the first case your handler will be "sealed" (which means that it will receive the default handler as a fallback for
all cases your handler doesn't handle itself) and used for all rejections that are not handled within the route structure
itself.

在第一种情况，你的处理程序将被“密封”（这意味着它将接收默认处理程序，作为你的处理程序不能处理的所有情况的回退），并且用于路由结构自身没有处理的所有拒绝。

The second case allows you to restrict the applicability of your handler to certain branches of your route structure.

第二种情况，允许你将处理程序的适用性限制在路由结构的某些分支。

<a id="customising-rejections"></a>
### Customising rejection HTTP Responses
**自定义拒绝 HTTP 响应**

It is also possible to customise just the responses that are returned by a defined rejection handler.
This can be useful for example if you like the rejection messages and status codes of the default handler,
however you'd like to wrap those responses in JSON or some other content type.

也可以只定制通过默认拒绝处理程序返回的响应。例如，如果你想使用默认处理程序的拒绝消息和状态码，但是想包装成 JSON 或其它内容类型，那么这可能很有用。

Please note that since those are not 200 responses, a different content type than the one that was sent in
a client's @apidoc[Accept] header *is* legal. Thus the default handler renders such rejections as `text/plain`.

请注意，因为这些不是 200 响应，所以发送与客户端的 @apidoc[Accept] 头域不同的内容类型 *是* 合法的。因此，默认处理程序渲染这样的拒绝为 `text/plain`。

In order to customise the HTTP Responses of an existing handler you can call the
`mapRejectionResponse` method on such handler as shown in the example below:

为了在已有的处理程序上自定义 HTTP 响应，你可以这样的处理程序上调用 `mapRejectionResponse` 方法，如下例所示：

Scala
:  @@snip [RejectionHandlerExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/RejectionHandlerExamplesSpec.scala) { #example-json }

Java
:  @@snip [RejectionHandlerExamplesTest.java]($test$/java/docs/http/javadsl/server/RejectionHandlerExamplesTest.java) { #example-json }

#### Adding the unmatched route in handleNotFound
**在 handleNotFound 里添加不匹配的路由**

Since rejection handlers are routes themselves, it is possible to do anything you could possibly want inside such handler.
For example you may want to include the path which was not found in the response to the client, this is as simple as
using the `extractUnmatchedPath` and completing the route with it.

由于拒绝处理程序本身就是路由，所以在这样的处理程序里做任何希望做的事是可能的。
例如：你可能希望在给客户端的响应里包含未找到的路径，这与使用 `extractUnmatchedPath` 并使用它完成路由一样简单。

Scala
:  @@snip [RejectionHandlerExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/RejectionHandlerExamplesSpec.scala) { #not-found-with-path }

Java
:  @@snip [ExecutionDirectivesExamplesTest.java]($test$/java/docs/http/javadsl/server/directives/ExecutionDirectivesExamplesTest.java) { #handleNotFoundWithDefails }

If you want to add even more information you can obtain the full request by using `extractRequest` as well.

如果你想要添加更多信息，也可以通过使用 `extractRequest` 获得完整请求。
