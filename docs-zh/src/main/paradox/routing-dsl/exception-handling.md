<a id="exception-handling"></a>
# 异常处理
*Exception Handling*

Exceptions thrown during route execution bubble up through the route structure to the next enclosing
@ref[handleExceptions](directives/execution-directives/handleExceptions.md) directive or the top of your route structure.

在路由执行期间被抛出的异常通过路由结构向上冒泡到下一个围住的 @ref[handleExceptions](directives/execution-directives/handleExceptions.md) 指令或路由结构的顶部。

Similarly to the way that @ref[Rejections](rejections.md) are handled the @ref[handleExceptions](directives/execution-directives/handleExceptions.md) directive delegates the actual job
of converting an exception to its argument, an @apidoc[ExceptionHandler]@scala[, which is defined like this:]@java[.]

类似于 @ref[描绘](rejections.md) 的处理方式， @ref[handleExceptions](directives/execution-directives/handleRejections.md) 指令将转换异常的实际工作委托给它的参数，
一个 @apidoc[ExceptionHandler]@scala[，它的定义像这样：]@java[。]

@@@ div { .group-scala }
```scala
trait ExceptionHandler extends PartialFunction[Throwable, Route]
```
@@@

Since an @apidoc[ExceptionHandler] is a partial function, it can choose which exceptions it would like to handle and
which not. Unhandled exceptions will simply continue to bubble up in the route structure.
At the root of the route tree any still unhandled exception will be dealt with by the top-level handler which always
handles *all* exceptions.

由于 @apidoc[ExceptionHandler] 是一个部分函数，所以可以选择要处理哪些异常而不处理哪些异常。未处理异常将在路由结构中继续向上冒泡。
在路由树的根，任何仍未处理的异常都将由顶层处理程序处理，它始终处理 *所有* 异常。

`Route.seal` internally wraps its argument route with the @ref[handleExceptions](directives/execution-directives/handleExceptions.md) directive in order to "catch" and
handle any exception.

`Route.seal` 内部使用 @ref[handleExceptions](directives/execution-directives/handleExceptions.md) 指令包裹路由参数，以便“捕获”并处理任何异常。

So, if you'd like to customize the way certain exceptions are handled you need to write a custom @apidoc[ExceptionHandler].
Once you have defined your custom @apidoc[ExceptionHandler] you have two options for "activating" it:

因此，如果你想定制某些异常的处理方式，你需要写一个自定义 @apidoc[ExceptionHandler] 。
一旦你定义好了自定义 @apidoc[ExceptionHandler]，有两种选项来“激活”它：

 1. @scala[Bring it into implicit scope at the top-level.]@java[Pass it to the `seal()` method of the @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] class.]
 2. Supply it as argument to the @ref[handleExceptions](directives/execution-directives/handleExceptions.md) directive.

  - @scala[提供它到隐式作用域的顶层]@java[把它传递给 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 类的 `seal()` 方法]
  - 提供它作为 @ref[handleExceptions](directives/execution-directives/handleExceptions.md) 指令的参数


In the first case your handler will be "sealed" (which means that it will receive the default handler as a fallback for
all cases your handler doesn't handle itself) and used for all exceptions that are not handled within the route
structure itself.
Here you can see an example of it:

在第一种情况，你的处理程序将被“密封”（这意味着它将接收默认处理程序，作为你的处理程序不能处理的所有情况的回退），并且用于路由结构自身没有处理的所有异常。
在这里你可以看到一个例子：

Scala
:   @@snip [ExceptionHandlerExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/ExceptionHandlerExamplesSpec.scala) { #seal-handler-example }

Java
:   @@snip [ExceptionHandlerExamplesTest.java]($test$/java/docs/http/javadsl/ExceptionHandlerInSealExample.java) { #seal-handler-example }


The second case allows you to restrict the applicability of your handler to certain branches of your route structure.

第二种情况，允许你将处理程序的适用性限制在路由结构的某些分支。

Here is an example for wiring up a custom handler via @ref[handleExceptions](directives/execution-directives/handleExceptions.md):

这里是通过 @ref[handleExceptions](directives/execution-directives/handleExceptions.md) 连接自定义处理程序的示例：

Scala
:   @@snip [ExceptionHandlerExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/ExceptionHandlerExamplesSpec.scala) { #explicit-handler-example }

Java
:   @@snip [ExceptionHandlerExamplesTest.java]($test$/java/docs/http/javadsl/ExceptionHandlerExample.java) { #explicit-handler-example }

@@@ div { .group-scala }
And this is how to do it implicitly:

这是如何使用隐式参数的例子：

@@snip [ExceptionHandlerExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/ExceptionHandlerExamplesSpec.scala) { #implicit-handler-example }
@@@

## Default Exception Handler
**默认异常处理程序**

A default @apidoc[ExceptionHandler] is used if no custom instance is provided.

如果未提供自定义实例，则使用默认 @apidoc[ExceptionHandler]。 

It will handle every `NonFatal` throwable, write its stack trace and complete the request
with `InternalServerError` `(500)` status code.

它将处理每一个 `NonFatal` throwable，写它的堆栈跟踪 *（译注：记录异常栈）* 并使用 `InternalServerError` `(500)` 状态码完成请求。

The message body will contain a string obtained via `Throwable#getMessage` call on the exception caught.

消息正文将包含一个通过在捕获异常上的 `Throwable#getMessage` 调用获得的字符串。

In case `getMessage` returns `null` (which is true for e.g. `NullPointerException` instances),
the class name and a remark about the message being null are included in the response body.

在`getMessage` 返回 `null` 在情况下，类名和一句关于消息为 null 的注释被包含到响应正文。

Note that `IllegalRequestException`s' stack traces are not logged, since instances of this class
normally contain enough information to provide a useful error message.

注意，`IllegalRequestException` 的堆栈跟踪不被记录，因为该类的实例通常包含足够的信息来提供用有的错误消息。

@@@ note

Users are strongly encouraged not to rely on using the @apidoc[ExceptionHandler] as a means of handling errors. By errors, we mean things that are an expected part of normal operations: for example, issues discovered during input validation. The @apidoc[ExceptionHandler] is meant to be a means of handling failures. See [Failure vs Error](https://www.reactivemanifesto.org/glossary#Failure) in the glossary of the [Reactive Manifesto](https://www.reactivemanifesto.org).

强烈建议用户不要依赖使用 @apidoc[ExceptionHandler] 作为处理错误的方法。
所谓错误，我们的本意指它是正常操作中预期的部分：例如，输入检验期间发现的问题。 @apidoc[ExceptionHandler] 是处理失败的方法。
见 [反应式宣言](https://www.reactivemanifesto.org) 词汇表中的 [失败 vs 错误](https://www.reactivemanifesto.org/glossary#Failure) 。  

Distinguishing between errors and failures (i.e. thrown `Exceptions` handled via the @apidoc[ExceptionHandler]) provides a much better mental model but also leads to performance improvements.

区别错误和失败（例如，通过 @apidoc[ExceptionHandler] 处理抛出的 `Exceptions`）提供更好的心智模型，而且也会导致性能的改进。

This is because exceptions are known to have a negative performance impact for cases
when the depth of the call stack is significant (stack trace construction cost)
and when the handler is located far from the place of the throwable instantiation (stack unwinding costs).

这是因为当调用堆栈深度非常大（堆栈跟踪构建成本）并且处理程序远离可抛出实例化的地方（堆栈展开成本）时，已知异常会对性能产生负面影响。

In a typical Akka application both these conditions are frequently true,
so as a rule of thumb, you should try to minimize the number of `Throwable` instances
reaching the exception handler.

在典型 Akka 应用程序里这些条件通常都为真，因此根据经验，应该尽量减少到达异常处理程序的 `Throwable` 实例的数量

To understand the performance implications of (mis-)using exceptions,
have a read at this excellent post by A. Shipilёv: [The Exceptional Performance of Lil' Exception](https://shipilev.net/blog/2014/exceptional-performance).

要了解（错误-）使用异常对性能可能的影响，阅读 A. Shipilёv 的这篇优秀文章：[LilException 的异常性能](https://shipilev.net/blog/2014/exceptional-performance) 。
@@@


@@@ note
Please note that since version `10.1.6`, the default `ExceptionHandler` will also discard the entity bytes automatically. If you want to change this behavior,
please refer to @ref[the section above](exception-handling.md#exception-handling); however, might cause connections to stall
if the entity is not properly rejected or cancelled on the client side.

请注意，从 `10.1.6` 版本开始，默认 `ExceptionHandler` 也将自动丢弃（请求的）实体字节流。
如果你想改变这个行为，请参考 @ref[上面的部分](exception-handling.md#exception-handling) ；但是，如果客户端上的实体没有正确地拒绝或取消，可能会导致连接挂住。
@@@

## Including sensitive data in exceptions
**异常中包含敏感的数据**

To prevent certain types of attack, it is not recommended to include arbitrary invalid user input in the response.
However, sometimes it can be useful to include it in the exception and logging for diagnostic reasons.
In such cases, you can use exceptions that extend `ExceptionWithErrorInfo`, such as `IllegalHeaderException`:

要防止某些类型的攻击，在响应里包含任意无效的用户输入是不建议的。
但是，有时为了诊断原因在异常和日志里包含它是有用的。
在此类情况下，你可以使用扩展了 `ExceptionWithErrorInfo` 的异常，例如：`IllegalHeaderException`：

Scala
:   @@snip [ExceptionHandlerExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/ExceptionHandlerExamplesSpec.scala) { #no-exception-details-in-response }

Java
:   @@snip [ExceptionHandlerExamplesTest.java]($test$/java/docs/http/javadsl/RespondWithHeaderHandlerExampleTest.java) { #no-exception-details-in-response  }


## Respond with headers and Exception Handler
**使用头域和异常处理程序响应**

If you wrap an ExceptionHandler inside a different directive, then that directive will still apply. Example below shows
that wrapping an ExceptionHandler inside a respondWithHeader directive will still add the header to the response.   

如果把 `ExceptionHandler` 包裹在不同的指令里，那么这个指令仍然适用。下面的示例显示，把 `ExceptionHandler` 包裹在 `respondWithHeader` 指令里，仍将添加头域到响应。

Scala
:   @@snip [ExceptionHandlerExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/ExceptionHandlerExamplesSpec.scala) { #respond-with-header-exceptionhandler-example }

Java
:   @@snip [ExceptionHandlerExamplesTest.java]($test$/java/docs/http/javadsl/RespondWithHeaderHandlerExampleTest.java) { #respond-with-header-exceptionhandler-example  }
