<a id="directives"></a>
# 指令
*Directives*

A "Directive" is a small building block used for creating arbitrarily complex @ref[route structures](../routes.md).
Akka HTTP already pre-defines a large number of directives and you can easily construct your own:

“指令”是用于创建任意复杂 @ref[路由结构](../routes.md) 的一个小的构建块。
Akka HTTP 已经预定义大量指令，你也可以很容易的构造你自己的指令：

@@toc { depth=1 }

@@@ index

* [alphabetically](alphabetically.md)
* [by-trait](by-trait.md)
* [custom-directives](custom-directives.md)

@@@

## Basics
**基础**

Directives create @ref[Routes](../routes.md). To understand how directives work it is helpful to contrast them with the "primitive"
way of creating routes.

指令创建 @ref[路由](../routes.md) 。要理解指令是怎样工作的，把它们与创建路由的“原始”方法进行对比是有帮助的。

@ref[Routes](../routes.md) effectively are simply highly specialised functions that take a @apidoc[RequestContext] and eventually `complete` it, 
which could (and often should) happen asynchronously.

有效 @ref[路由](../routes.md)  是调度专门化的函数，它拿到一个 @apidoc[RequestContext] 并最后 `complete` 它，
可以（而且通常应该）异步发生。

@@@ div { .group-java }

The @ref[complete](route-directives/complete.md) directive simply completes the request with a response:

@ref[complete](route-directives/complete.md) 指令仅通过响应完成请求：

@@@

@@@ div { .group-scala }


Since @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] is just a type alias for a function type @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] instances can be written in any way in which function
instances can be written, e.g. as a function literal:

由于 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 只是函数类型的类型别名， @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 实例可以被重写为任何函数实例，例如：作为函数字面量：

```scala
val route: Route = { ctx => ctx.complete("yeah") }
```

or shorter:

短的：

```scala
val route: Route = _.complete("yeah")
```

With the @ref[complete](route-directives/complete.md) directive this becomes even shorter:

使用 @ref[complete](route-directives/complete.md) 指令，变得更短：

@@@

Scala
:  ```scala
val route = complete("yeah")
```

Java
:  ```java
Route route = complete("yeah");
```

@@@ div { .group-scala }

These three ways of writing this @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] are fully equivalent, the created `route` will behave identically in all
cases.

这3种写 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 方式是完全等效的，创建的 `route` 在所有情况里下的行为都一样。

Let's look at a slightly more complicated example to highlight one important point in particular.
Consider these two routes:

让我们看下稍等复杂点的例子，特别强调一点。考虑这两个路由：

```scala
val a: Route = {
  println("MARK")
  ctx => ctx.complete("yeah")
}

val b: Route = { ctx =>
  println("MARK")
  ctx.complete("yeah")
}
```

The difference between `a` and `b` is when the `println` statement is executed.
In the case of `a` it is executed *once*, when the route is constructed, whereas in the case of `b` it is executed
every time the route is *run*.

`a` 和 `b` 的不同是执行 `println` 语句的时间。
`a` 的情况下当路由被构造时执行 *一次* ，而在 `b` 的情况下在路由每次 *运行* 时执行。

Using the @ref[complete](route-directives/complete.md) directive the same effects are achieved like this:

使用 @ref[complete](route-directives/complete.md) 指令相同效果像这样：

```scala
val a = {
  println("MARK")
  complete("yeah")
}

val b = complete {
  println("MARK")
  "yeah"
}
```

This works because the argument to the @ref[complete](route-directives/complete.md) directive is evaluated *by-name*, i.e. it is re-evaluated
every time the produced route is run.

因为 @ref[complete](route-directives/complete.md) 指令的参数是 *按名称* 计算的，比如：每次生成路由运行时都要重新计算。

Let's take things one step further:

让我们更进一步：

```scala
val route: Route = { ctx =>
  if (ctx.request.method == HttpMethods.GET)
    ctx.complete("Received GET")
  else
    ctx.complete("Received something else")
}
```

Using the @ref[get](method-directives/get.md) and @ref[complete](route-directives/complete.md) directives we can write this route like this:

使用 @ref[get](method-directives/get.md) 和 @ref[complete](route-directives/complete.md) 指令，我们可以像这样重写这个路由：

```scala
val route =
  concat(
    get {
      complete("Received GET")
    },
    complete("Received something else")
  )
```

Again, the produced routes will behave identically in all cases.

再次，生成路由的行为所有情况下是一样的。

Note that, if you wish, you can also mix the two styles of route creation:

注意：如果你愿意，还可以混合路由创建的两种风格：

```scala
val route =
  concat(
    get { ctx =>
      ctx.complete("Received GET")
    },
    complete("Received something else")
  )
```

Here, the inner route of the @ref[get](method-directives/get.md) directive is written as an explicit function literal.

这里，内部 @ref[get](method-directives/get.md) 指令的内部路由被写为显示的函数字面量。

However, as you can see from these examples, building routes with directives rather than "manually" results in code that
is a lot more concise and as such more readable and maintainable. In addition it provides for better composability (as
you will see in the coming sections). So, when using Akka HTTP's Routing DSL you should almost never have to fall back
to creating routes via @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] function literals that directly manipulate the @ref[RequestContext](../routes.md#requestcontext).

但是，正如你从这些示例看到的，用指令而不是“手动的”构建路由生成的代码更加简洁，因此可读性和可维护性更好。
此外，提供了更好的可组合性（你将在接下来的部分看到）。因此，在使用 Akka HTTP 的路由 DSL 时，你几乎不需要回退到通过直接操作 @ref[RequestContext](../routes.md#requestcontext) 的 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 函数字面量来创建路由。

@@@

@@@ div { .group-java }

Writing multiple routes that are tried as alternatives (in-order of definition), is as simple as using the `concat(route1, route2)`,
method:

编写多个路由（按顺序定义），就像使用 `concat(route1, route2)` 方法一样简单：

```java
Route routes = concat(
  pathSingleSlash(() ->
    getFromResource("web/calculator.html")
  ),
  path("hello", () -> complete("World!))
);
```

You could also simply define a "catch all" completion by providing it as the last route to attempt to match.
In the example below we use the `get()` (one of the @ref[MethodDirectives](method-directives/index.md)) to match all incoming `GET`
requests for that route, and all other requests will be routed towards the other "catch all" route, that completes the route:

你还可以简单的定义一个“捕获所有”完成，作为最后一个尝试匹配的路由。在下面的例子里使用 `get`（ @ref[MethodDirectives](method-directives/index.md) 的其中一个）方法匹配所以传入的 `GET` 请求，所有其它请求被路由到另一个“捕获所有”路由，从而完成这个路由：

```java
Route route =
  get(
    () -> complete("Received GET")
  ).orElse(
    () -> complete("Received something else")
  )
```

@@@

If no route matches a given request, a default `404 Not Found` response will be returned as response.

如果没有路由匹配指定请求，一个默认的 `404 Not Found` 响应将作为响应返回。

## Structure
**结构**

The general anatomy of a directive is as follows:

指令的一般解剖如下：

Scala
:  ```scala
name(arguments) { extractions =>
  ... // inner route
}
```

Java
:  ```java
directiveName(arguments [, ...], (extractions [, ...]) -> {
  ... // inner route
})
```

It has a name, zero or more arguments and optionally an inner route (The @ref[RouteDirectives](route-directives/index.md) are special in that they
are always used at the leaf-level and as such cannot have inner routes).

它有一个名字，零或多个参数，以及可选的内部路由（ @ref[RouteDirectives](route-directives/index.md) 是特殊的，
因为它们总是用于叶子级别，因此不能用于内部路由）。

Additionally directives can "extract" a number of values and make them available to their inner routes as function
arguments. When seen "from the outside" a directive with its inner route form an expression of type @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]].

另外，指令可以“提取”一些值并使它们作为函数参数对期内部路由可用。当”从部“看时，一个带有内部路由的指令形成一个 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 类型的表达式。

## What Directives do
**指令做什么**

A directive can do one or more of the following:

指令可以做以下一样或更多：

 * Transform the incoming @apidoc[RequestContext] before passing it on to its inner route (i.e. modify the request)
 * Filter the @apidoc[RequestContext] according to some logic, i.e. only pass on certain requests and reject others
 * Extract values from the @apidoc[RequestContext] and make them available to its inner route as "extractions"
 * Chain some logic into the @ref[RouteResult](../routes.md#routeresult) future transformation chain (i.e. modify the response or rejection)
 * Complete the request

 - 在传递到内部路由之前转换进入的 @apidoc[RequestContext]（例如：修改请求） 
 - 根据一些逻辑过滤 @apidoc[RequestContext]，例如：只通过某些请求而拒绝其它的
 - 从 @apidoc[RequestContext] 提取值并将期作为”提取“（的值）对内部路由可用

This means a `Directive` completely wraps the functionality of its inner route and can apply arbitrarily complex
transformations, both (or either) on the request and on the response side.

这意味着 `Directive` 完全封装了内部路由的功能，并且可以在请求和响应两端（或一端）应用任意复杂的转换。

## Composing Directives
**组合指令**

As you have seen from the examples presented so far the "normal" way of composing directives is nesting.
Let's take a look at this concrete example:

像你目前为止从示例看到的，组合指令的“一般”方法是嵌套。让我们看看这个具体的例子：

Scala
:  @@snip [DirectiveExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/DirectiveExamplesSpec.scala) { #example-1 }

Java
:  @@snip [DirectiveExamplesTest.java]($test$/java/docs/http/javadsl/server/DirectiveExamplesTest.java) { #example1 }

Here the `get` and `put` directives are chained together @scala[with the `concat` combinator]@java[using the `orElse` method] to form a higher-level route that
serves as the inner route of the `path` directive. Let's rewrite it in the following way:

这里 `get` 和 `put` 指令 @scala[使用 `concat` 组合器]@java[使用 `orElse` 方法] 被链接到一起形成高级路由，该路由用途 `path` 指令的内部路由。
让我们按下面的方式重写它：

Scala
:  @@snip [DirectiveExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/DirectiveExamplesSpec.scala) { #getOrPut }

Java
:  @@snip [DirectiveExamplesTest.java]($test$/java/docs/http/javadsl/server/DirectiveExamplesTest.java) { #getOrPut }

@@@ div { .group-java }

In this previous example, we combined the `get` and `put` directives into one composed directive and extracted it to its own method, which could be reused anywhere else in our code.

在前端的例子里，我们合并 `get` 和 `put` 指令到一个组合指令并提取它到一个方法，该方法可以在我们代码里的任何地方复用。

Instead of extracting the composed directives to its own method, we can also use the available `anyOf` combinator. The following code is equivalent to the previous one:

我们也可以使用可用的 `anyOf` 组合器，而不是将组合指令提取到它自己的方法中。以下代码与前一代码相同：
@@@

@@@ div { .group-scala }

What you can't see from this snippet is that directives are not implemented as simple methods but rather as stand-alone
objects of type `Directive`. This gives you more flexibility when composing directives. For example you can
also use the `|` operator on directives. Here is yet another way to write the example:

从这个代码片段你不能看到的是，指令不是作为简单方法实现的，而是作为 `Directive` 类型的独立对象。当组合指令时，这给予你更多的灵活性。
例如，你也可以使用指令上的 `|` 操作符。这是编写示例的另一种方法：
@@@

Scala
:  @@snip [DirectiveExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/DirectiveExamplesSpec.scala) { #getOrPutUsingPipe }

Java
:  @@snip [DirectiveExamplesTest.java]($test$/java/docs/http/javadsl/server/DirectiveExamplesTest.java) { #getOrPutUsingAnyOf }

@@@ div { .group-scala }

Or better (without dropping down to writing an explicit @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] function manually):

或者更好（不用手动编写的 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]] 函数）：

@@snip [DirectiveExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/DirectiveExamplesSpec.scala) { #getOrPutUsingPipeAndExtractMethod }

If you have a larger route structure where the `(get | put)` snippet appears several times you could also factor it
out like this:

如果你有一个较大的路由结构，其中 `(get | put)` 代码版段多次出现，你也可以将其按如下方式分解：

@@snip [DirectiveExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/DirectiveExamplesSpec.scala) { #example-5 }

Note that, because `getOrPut` doesn't take any parameters, it can be a `val` here.

注意，因为 `getOrPut` 没有任何参数，这里可以是 `val`。

As an alternative to nesting you can also use the `&` operator:

除了嵌套，你还可以使用 `&` 操作符：

@@snip [DirectiveExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/DirectiveExamplesSpec.scala) { #example-6 }

Here you can see that, when directives producing extractions are combined with `&`, the resulting "super-directive"
simply extracts the concatenation of its sub-extractions.

这里你可以看到，当生成提取的指令与 `&` 组合时，生成的“超级-指令”只提取其子提取的串联起来的值。

And once again, you can factor things out if you want, thereby pushing the "factoring out" of directive configurations
to its extreme:

再次，你可以分解你想要的，从而把指令配置的“因式分解”推到极致：

@@snip [DirectiveExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/DirectiveExamplesSpec.scala) { #example-7 }

This type of combining directives with the `|` and `&` operators as well as "saving" more complex directive
configurations as a `val` works across the board, with all directives taking inner routes.

TODO translate to chinese

@@@

@@@ div { .group-java }

The previous example, tries to complete the route first with a `GET` or with a `PUT` if the first one was rejected. 

In case you are constantly nesting the same directives several times in you code, you could factor them out in their own method and use it everywhere:

@@snip [DirectiveExamplesTest.java]($test$/java/docs/http/javadsl/server/DirectiveExamplesTest.java) { #composeNesting }

Here we simple created our own combined directive that accepts `GET` requests, then extracts the method and completes it with an inner route that takes this HTTP method as a parameter.

Again, instead of extracting own combined directives to its own method, we can make use of the `allOf` combinator. The following code is equivalent to the previous one:

@@snip [DirectiveExamplesTest.java]($test$/java/docs/http/javadsl/server/DirectiveExamplesTest.java) { #composeNestingAllOf }

In this previous example, the the inner route function provided to `allOf` will be called when the request is a `GET` and with the extracted client IP obtained from the second directive.

As you have already seen in the previous section, you can also use the `concat` method defined in @apidoc[RouteDirectives] as an alternative to `orElse` chaining. Here you can see the first example again, rewritten using `concat`:

@@snip [DirectiveExamplesTest.java]($test$/java/docs/http/javadsl/server/DirectiveExamplesTest.java) { #usingConcat }

The `concat` combinator comes handy when you want to avoid nesting. Here you can see an illustrative example:
 
@@snip [DirectiveExamplesTest.java]($test$/java/docs/http/javadsl/server/DirectiveExamplesTest.java) { #usingConcatBig }

Notice how you could adjust the indentation in these last two examples to have a more readable code.

@@@

Note that going too far with "compressing" several directives into a single one probably doesn't result in the most
readable and therefore maintainable routing code. It might even be that the very first of this series of examples
is in fact the most readable one.

注意，过多的“压缩”多个指令到单个指令可能不是产生可读性、可维护性最好的路由代码。这系列示例的第一个代码实际上可能是可读性最好的。

Still, the purpose of the exercise presented here is to show you how flexible directives can be and how you can
use their power to define your web service behavior at the level of abstraction that is right for **your** application.

不过，这里的练习的目的向你展示指令的灵活性，以及如何使用它们的能力在适合 **你的** 应用程序的抽象级别上定义你的 Web 服务行为。

@@@ div { .group-scala }

### Composing Directives with `~` Operator
**使用 `~` 操作符组合指令**

@@@ 

@@@ note { .group-scala }
Gotcha: forgetting the `~` (tilde) character in between directives can result in perfectly valid
Scala code that compiles but does not work as expected. What would be intended as a single expression would actually be multiple expressions, and only the final one would be used as the result of the parent directive. Because of this, the recommended way to compose routes is with the the `concat` combinator.

注意：忘记指令之间的 `~`（波浪符号）字符可能导致完全有效的 Scala 代码编码，但是不能按预期的工作。
单个表达式实际上是多个表达，只有最后一个表达式用作父指令的结果。因为这个原因，组合路由的推荐方式是使用 `concat` 组合器。 
@@@

@@@ div { .group-scala }

Alternatively we can combine directives using the `~` operator where we chain them together instead of passing each directive as a separate argument. Let's take a look at the usage of this combinator:

另外，我们可以使用 `~` 操作符将指令组合到一起，用于替代将每个指令作为单独的参数进行传递。让我们看看这个组合器的用法：

@@snip [DirectiveExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/DirectiveExamplesSpec.scala) { #example-8 }

@@@

## Type Safety of Directives
**指令的类型安全**

When you combine directives with the @scala[`|` and `&` operators]@java[`anyOf` and `allOf` methods] the routing DSL makes sure that all extractions work as
expected and logical constraints are enforced at compile-time.

当你将指令与 @scala[`|` 和 `&` 操作符]@java[`anyOf` 笔 `allOf` 方法] 组合时，路由 DSL 确保所有的提取都按预期工作，
并在编译时强制执行逻辑约束。

For example you cannot @scala[`|`]@java[`anyOf`] a directive producing an extraction with one that doesn't:

例如你不能 @scala[`|`]@java[`anyOf`] 一个指令生成提取而另一个不生成：

Scala
:   ```scala
val route = path("order" / IntNumber) | get // doesn't compile
```

Java
:  ```java
anyOf(this::get, this::extractClientIP, routeProvider) // doesn't compile
```

Also the number of extractions and their types have to match up:

还有提取器的数据和它们的类型也要匹配上：

Scala
:  ```scala
val route = path("order" / IntNumber) | path("order" / DoubleNumber)   // doesn't compile
val route = path("order" / IntNumber) | parameter('order.as[Int])      // ok
```

Java
:  ```java
anyOf(this::extractClientIP, this::extractMethod, routeProvider) // doesn't compile
anyOf(bindParameter(this::parameter, "foo"), bindParameter(this::parameter, "bar"), routeProvider) // ok
```
In this previous example we make use of the `bindParameter` function located in `akka-http/akka.http.javadsl.common.PartialApplication`.
In order to be able to call `anyOf`, we need to convert our directive that takes 2 parameters to a function that takes only 1.
In this particular case we want to use the `parameter` directive that takes a `String` and a function from `String` to @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]@java[@apidoc[Route]],
so to be able to use it in combination with `anyOf`, we need to bind the first parameter to `foo` and to `bar` in the second one. `bindParameter(this::parameter, "foo")` is equivalent 
to define your own function like this:
```java
Route parameterFoo(Function<String, Route> inner) {
  return parameter("foo", inner);
}
```

When you combine directives producing extractions with the @scala[`&` operator]@java[`allOf` method] all extractions will be properly gathered up:

当你结合指令与 @scala[`&` 操作符]@java[`allOf` 方法] 生成提取时，所有的提取将正确的收集：

Scala
:  ```scala
val order = path("order" / IntNumber) & parameters('oem, 'expired ?)
val route =
  order { (orderId, oem, expired) =>
    ...
  }
```

Java
:  ```java
allOf(this::extractScheme, this::extractMethod, (scheme, method) -> ...) 
```

Directives offer a great way of constructing your web service logic from small building blocks in a plug and play
fashion while maintaining DRYness and full type-safety. If the large range of @ref[Predefined Directives](alphabetically.md) does not
fully satisfy your needs you can also easily create @ref[Custom Directives](custom-directives.md).

指令提供了很好的方式以既插即用的方式从小的构建块构造你的 Web 服务逻辑，同时操持不要 DRY 原则且完全类型安全。
如果大量的 @ref[预定义指令](alphabetically.md) 不能完全满足你的需要，你可以轻松的创建 @ref[自定义指令](custom-directives.md) 。

@@@ div { .group-scala }

## Automatic Tuple extraction (flattening)
**自动元组提取（压扁）**

Convenient Scala DSL syntax described in @ref[Basics](#basics), and @ref[Composing Directives](#composing-directives) 
are made possible by Tuple extraction internally. Let's see how this works with examples.

在 @ref[基础](#basics) 中描述的便利 Scala DSL 语法，以及通过内部的元组提取使 @ref[组合指令](#composing-directives) 成为可能。
让我们通过例子看看这是怎么工作的。

```scala
val futureOfInt: Future[Int] = Future.successful(1)
val route =
  path("success") {
    onSuccess(futureOfInt) { //: Directive[Tuple1[Int]]
      i => complete("Future was completed.")
    }
  }
```
Looking at the above code, `onSuccess(futureOfInt)` returns a `Directive1[Int] = Directive[Tuple1[Int]]`.

看上面的代码，`onSuccess(futureOfInt)` 返回一个 `Directive1[Int] = Directive[Tuple1[Int]]` 。

```scala
val futureOfTuple2: Future[Tuple2[Int,Int]] = Future.successful( (1,2) )
val route =
  path("success") {
    onSuccess(futureOfTuple2) { //: Directive[Tuple2[Int,Int]]
      (i, j) => complete("Future was completed.")
    }
  }
```

Similarly, `onSuccess(futureOfTuple2)` returns a `Directive1[Tuple2[Int,Int]] = Directive[Tuple1[Tuple2[Int,Int]]]`,
but this will be automatically converted to `Directive[Tuple2[Int,Int]]` to avoid nested Tuples.

类似的，`onSuccess(futureOfTuple2)` 返回一个 `Directive1[Tuple2[Int,Int]] = Directive[Tuple1[Tuple2[Int,Int]]]`，
但是，这将会自动转换为 `Directve[Tuple2[Int, Int]]` 以避免嵌套的元组。

```scala
val futureOfUnit: Future[Unit] = Future.successful( () )
val route =
  path("success") {
    onSuccess(futureOfUnit) { //: Directive0
        complete("Future was completed.")
    }
  }
```
If the future returns `Future[Unit]`, it is a bit special case as it results in `Directive0`.
Looking at the above code, `onSuccess(futureOfUnit)` returns a `Directive1[Unit] = Directive[Tuple1[Unit]]`.
However, the DSL interprets `Unit` as `Tuple0`, and automatically converts the result to `Directive[Unit] = Directive0`,

如果进一步返回 `Future[Unit]`，这是一个有点特殊的情况，它的结果为 `Directive0`。
看上面的代码，`onSuccess(futureOfUnit)` 返回一个 `Directive1[Unit] = Directive[Tuple1[Unit]]`。
但是，DSL 解释 `Unit` 为 `Tuple0`，并自动转换结果为 `Directive[Unit] = Directive0`。
@@@
