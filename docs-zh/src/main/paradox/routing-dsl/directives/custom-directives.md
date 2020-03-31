# 自定义指令
*Custom Directives*

Part of the power of akka-http directives comes from the ease with which it’s possible to define
custom directives at differing levels of abstraction.

akka-http 指令的部分功能来自于轻松定义不同抽象级别的自定义指令的可能性。

There are essentially three ways of creating custom directives:

根本上，有三种方式创建自定义指令：

 1. By introducing new “labels” for configurations of existing directives
 2. By transforming existing directives
 3. By writing a directive “from scratch”

 - 为现有指令的配置引入新的“标签” *（译注：比如通过组合的方式）*
 - 转换现有指令
 - “从头开始”写一个指令

## Configuration Labeling
**配置标签**

The easiest way to create a custom directive is to simply assign a new name for a certain configuration
of one or more existing directives. In fact, most of the predefined akka-http directives can be considered
named configurations of more low-level directives.

创建自定义指令最简便的方法就是将一个或多个已有指令通过配置的方式分配一个新的名字来定义。事实上Akka HTTP预定义的大多数指令都由以较低级别指令命名配置的方式来定义的。

The basic technique is explained in the chapter about Composing Directives, where, for example, a new directive
`getOrPut` is defined like this:

基本技术就是本章里阐述的组合指令，使用：一个新的指令 `getOrPu` 像这样定义：

Scala
:   @@snip [CustomDirectivesExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/directives/CustomDirectivesExamplesSpec.scala) { #labeling }

Java
:   @@snip [CustomDirectivesExamplesTest.java]($test$/java/docs/http/javadsl/server/directives/CustomDirectivesExamplesTest.java) { #labeling-1 #labeling-2 }

@@@ div { .group-java }
Multiple directives can be nested to produce a single directive out of multiple like this:

可以嵌套多个指令，以便生成这样的一个单个指令：

@@snip [CustomDirectivesExamplesTest.java]($test$/java/docs/http/javadsl/server/directives/CustomDirectivesExamplesTest.java) { #composition-1 #composition-2 }

@@@

Another example is the @ref[MethodDirectives](method-directives/index.md) which are simply instances of a preconfigured @ref[method](method-directives/method.md) directive.
The low-level directives that most often form the basis of higher-level “named configuration” directives are grouped
together in the @ref[BasicDirectives](basic-directives/index.md) trait.

另一个例子是 @ref[MethodDirectives](method-directives/index.md)，它只是预配置 @ref[method](method-directives/method.md) 指令的实例。
底级指令通常构造高级“命名配置”指令的基础，它们被分组在 @ref[BasicDirectives](basic-directives/index.md) 中。

@@@ div { .group-scala }
## Transforming Directives
**转换指令**

The second option for creating new directives is to transform an existing one using one of the
“transformation methods”, which are defined on the @scaladoc[Directive](akka.http.scaladsl.server.Directive) class, the base class of all “regular” directives.

创建新指令的第二种选项是使用“转换方法”来转换现有指令，这些转换方法定义在 @scaladoc[Directive](akka.http.scaladsl.server.Directive) 类上，`Directive` 是所有“常规”指令的基类。

Apart from the combinator operators (`|` and `&`) and the case-class extractor (`as[T]`)
the following transformations are also defined on all `Directive` instances:

除了组合器操作符（`|` 和 `&`）和 case-class 提取器（`as[T]`），以下转换也定义在所有 `Directive` 实例上：

>
 * [map/tmap](#map-tmap)
 * [flatMap/tflatMap](#flatmap-tflatmap)
 * [require/trequire](#require-trequire)
 * [recover/recoverPF](#recover-recoverpf)

<a id="map-tmap"></a>
### map and tmap
**map 和 tmap**

If the Directive is a single-value `Directive`, the `map` method allows
for simple transformations:

如果指令是单值 `Directive`，`map` 方法允许用于简单转换：

@@snip [CustomDirectivesExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/directives/CustomDirectivesExamplesSpec.scala) { #map-0 }

One example of a predefined directive relying on `map` is the @github[optionalHeaderValue](/akka-http/src/main/scala/akka/http/scaladsl/server/directives/HeaderDirectives.scala) { #optionalHeaderValue } directive.

依赖 `map` 的其中一个预定义指令例子是 @github[optionalHeaderValue](/akka-http/src/main/scala/akka/http/scaladsl/server/directives/HeaderDirectives.scala) { #optionalHeaderValue } 指令。

The tmap modifier has this signature (somewhat simplified):

`tmap` 修改器有着这样的签名（进行了简化）：

```scala
def tmap[R](f: L => R): Directive[Out]
```

It can be used to transform the `Tuple` of extractions into another `Tuple`.
The number and/or types of the extractions can be changed arbitrarily. For example
if `R` is `Tuple2[A, B]` then the result will be a `Directive[(A, B)]`. Here is a
somewhat contrived example:

它可用于将提取的 `Tuple` 转换到另一个 `Tuple`。提取的数量和/或类型都可以任意改变。
例如，如果 `R` 是 `Tuple2[A, B]`，但结果是 `Directive[(A, B)]`。这里是一个有点做作的例子：

@@snip [CustomDirectivesExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/directives/CustomDirectivesExamplesSpec.scala) { #tmap-1 }

<a id="flatmap-tflatmap"></a>
### flatMap and tflatMap
**flatMap 和 tflatMap**

With map and tmap you can transform the values a directive extracts
but you cannot change the “extracting” nature of the directive.
For example, if you have a directive extracting an `Int` you can use map to turn
it into a directive that extracts that `Int` and doubles it, but you cannot transform
it into a directive, that doubles all positive `Int` values and rejects all others.

通过map、tmap可以将指令提取的值转换成其它值，但不能改变其“提取”指令的性质。
例如：如果你有一个提取 `Int` 值的指令，你可以使用 `map` 将其转换为一个提取 `Int` 并将它加倍的指令，
但是，你不能将它转换为将所有正数 `Int` 值加倍并拒绝其它的指令。

In order to do the latter you need `flatMap` or `tflatMap`. The `tflatMap`
modifier has this signature:

为了实现后者，你需要 `flatMap` 或 `tflatMap`。`tflatMap` 修改器有这样的签名：

```scala
def tflatMap[R: Tuple](f: L => Directive[R]): Directive[R]
```

The given function produces a new directive depending on the Tuple of extractions
of the underlying one. As in the case of [map/tmap](#map-tmap) there is also a single-value
variant called `flatMap`, which simplifies the operation for Directives only extracting one single value.

给定的函数根据底层函数的提取元组（值）生成一个新的指令。与 [map/tmap](#map-tmap) 的情况一个，也有一个名为 `flatMap` 的单值变体，
它简化了指令只提取一个单值的操作。

Here is the (contrived) example from above, which doubles positive Int values and rejects all others:

这里是上面的（做作的）例子，它加倍正数 Int 值并拒绝其它：

@@snip [CustomDirectivesExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/directives/CustomDirectivesExamplesSpec.scala) { #flatMap-0 }

A common pattern that relies on flatMap is to first extract a value
from the RequestContext with the extract directive and then flatMap with
some kind of filtering logic. For example, this is the implementation
of the method directive:

依赖 `flatMap` 的一种常用模式是，先使用提取指令从 `RequestContext` 提取一个值，然后使用某种过滤逻辑在 `flatMap` 中提取值。
例如：这是 `method` 指令的实现：

@@snip [MethodDirectives.scala]($akka-http$/akka-http/src/main/scala/akka/http/scaladsl/server/directives/MethodDirectives.scala) { #method }

The explicit type parameter `[Unit]` on the flatMap is needed in this case
because the result of the flatMap is directly concatenated with the
`cancelAllRejections` directive, thereby preventing “outside-in”
inference of the type parameter value.

`flatMap` 上的显示类型参数 `[Unit]` 在这种情况下是需要的，因为 `flatMap` 的结果直接连接到 `cancelAllRejections` 指令，这样能防止类型参数值的“从外向里”推理。*（译注：避免自动推导出错误的类型）*

<a id="require-trequire"></a>
### require and trequire
**require 和 trequire**

The require modifier transforms a single-extraction directive into a directive
without extractions, which filters the requests according the a predicate function.
All requests, for which the predicate is false are rejected, all others pass unchanged.

`require` 修改器将单提取指令转换为没有提取的指令，该指令根据谓词函数过滤请求。谓词为false的请求都被拒绝，其它请求保持不变。

The signature of require is this:

`require` 的签名是这样：

```scala
def require(predicate: T => Boolean, rejections: Rejection*): Directive0
```

One example of a predefined directive relying on require is the first overload of the host directive:

依赖 `require` 的预定义指令的一个例子是 `host` 指令的第一个重载版本：

@@snip[HostDirectives.scala]($akka-http$/akka-http/src/main/scala/akka/http/scaladsl/server/directives/HostDirectives.scala) { #require-host }

You can only call require on single-extraction directives. The trequire modifier is the
more general variant, which takes a predicate of type `Tuple => Boolean`.
It can therefore also be used on directives with several extractions.

只能在单提取指令上调用 `require`。`trequire` 修改器是更通用的变体，它接受一个 `Tuple => Boolean` 类型的谓词。
因此，它可以用于具有多个提取（值）的指令。

<a id="recover-recoverpf"></a>
### recover and recoverPF
**recover 和 recoverPF**

The `recover` modifier allows you “catch” rejections produced by the underlying
directive and, instead of rejecting, produce an alternative directive with the same type(s) of extractions.

`recover` 修改器允许“捕获”由底层指令成生的拒绝，并生成具有相同提取类型的替代指令。

The signature of recover is this:

`recover` 的签名是这样：

```scala
def recover[R >: L: Tuple](recovery: Seq[Rejection] => Directive[R]): Directive[R] =
```

In many cases the very similar `recoverPF` modifier might be little bit
easier to use since it doesn’t require the handling of all rejections:

在许多情况下，非常类似的 `recoverPF` 修改器也许可易使用，因为它不需要处理所有拒绝：

```scala
def recoverPF[R >: L: Tuple](
  recovery: PartialFunction[Seq[Rejection], Directive[R]]): Directive[R]
```

One example of a predefined directive relying `recoverPF` is the `optionalHeaderValue` directive:

依赖 `recoverPF` 的预定义指令的一个例子是 `optionalHeaderValue` 指令：

@@signature [HeaderDirectives.scala]($akka-http$/akka-http/src/main/scala/akka/http/scaladsl/server/directives/HeaderDirectives.scala) { #optionalHeaderValue }

### collect and tcollect
**collect 和 tcollect**

With collect and tcollect you can filter and map in one go, it mimics the collect known from the regular Scala collections.

使用 `collect` 和 `tcollect` 可以（对提取）一次性过虑和映射，它模拟了常规 Scala 集合中已知 `collect`（的行为）。

Here is an example, first via map and filter and finally using collect:

这里是一个示例，首先通过 `filter` 和 `map`，最后使用 `collect`：

```
parameter("a".as[Int]).filter(x => x != 0, MissingQueryParamRejection("a")).map(x => 42 / x)

parameter("a".as[Int]).collect({ case x if x != 0 => 42 / x }, MissingQueryParamRejection("a"))
```

## Directives from Scratch
**从头开始实现指令**

The third option for creating custom directives is to do it “from scratch”,
either by using `Directive.apply` or by subclassing `Directive` class directly. The `Directive` is defined like this
(leaving away operators and modifiers):

创建自定义指令的第三个选项是“从头开始”做起，直接使用 `Directive.apply` 或 `Directive` 的子类。`Directive` 的定义像这样（去掉了操作符和修改器）：

@@snip [Directive.scala]($akka-http$/akka-http/src/main/scala/akka/http/scaladsl/server/Directive.scala) { #basic }

It only has one abstract member that you need to implement, the `tapply` method, which creates
the Route the directives presents to the outside from its inner Route building function
(taking the extractions as parameters).

`Directive` 只有一个抽象成员 `tapply` 方法需要实现，该方法从内部路由构建函数（将提取的值作为参数）创建指令呈现给外部的路由。

Extractions are kept as a Tuple. Here are a few examples:

提取（值）保持为元组。这里是一些示例：

A `Directive[Unit]` extracts nothing (like the get directive).
Because this type is used quite frequently akka-http defines a type alias for it:

`Directive[Unit]` 什么也不提取（就像 `get` 指令）。因为该类型使用太频繁，akka-http 为它定义了一个类型别名：

```scala
type Directive0 = Directive[Unit]
```

A `Directive[(String)]` extracts one String value (like the hostName directive). The type alias for it is:

`Directive[(String)]` 提取一个字符串值（就像 `hostName` 指令）。它的类型别名是：

```scala
type Directive1[T] = Directive[Tuple1[T]]
```

A `Directive[(String, Int)]` extracts a `String` value and an `Int` value
(like a `parameters('a.as[String], 'b.as[Int])` directive). Such a directive can be defined to extract the
hostname and port of a request:

`Directive[(String, Int)]` 提取一个 `String` 值和一个 `Int` 值（就像 `parameters('a.as[String], 'b.as[Int])` 指令）。
因此，指令可用于定义提取请求的主机名和端口。

@@snip [CustomDirectivesExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/directives/CustomDirectivesExamplesSpec.scala) { #scratch-1 }

Beside using `Directive.apply`, you can also extending `Directive` directly(This is actually uncommon and the first is preferable for common use cases):

除了使用 `Directive.apply`，你也可以直接扩展 `Directive`（这实际上不常见，对于常见用例第一种更为可取）。

@@snip [CustomDirectivesExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/directives/CustomDirectivesExamplesSpec.scala) { #scratch-2 }

Keeping extractions as `Tuples` has a lot of advantages, mainly great flexibility
while upholding full type safety and “inferability”. However, the number of times
where you’ll really have to fall back to defining a directive from scratch should
be very small. In fact, if you find yourself in a position where a “from scratch”
directive is your only option, we’d like to hear about it,
so we can provide a higher-level “something” for other users.

保持提取值为 `Tuples` 具有很多优点，主要是在支持完整类型安全性和“可推断性”的同时具有很大的灵活性。
但是，真正有回退到从头开始定义自定义指令的次数是很少的。事实上，如果你发现自己处于一个“从头开始”定义指令是唯一选项的地步，我们想听到它，
所以我们可以为其他用户提供更高级的”东西“（指令） *（译注：希望大家能提出想要的或贡献自己的指令）*。

@@@
