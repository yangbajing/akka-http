# 路径匹配器 DSL
*The PathMatcher DSL*

The `PathMatcher` mini-DSL is used to match incoming URL's and extract values from them. It is used in the @ref[`path` directive](directives/path-directives/path.md).

`PathMatcher` 微-DSL 用于匹配传入的 URL 并提取它的值。它在 @ref[`path` 指令](directives/path-directives/path.md) 中使用。

Some simple examples of the DSL in action can be found [below](#examples).

实战里 DSL 的一些简单示例可以在 [下面](#examples) 找到。

## Overview
**概述**

When a request (or rather the respective @apidoc[RequestContext] instance) enters the route structure it has an
"unmatched path" that is identical to the `request.uri.path`. As it descends the routing tree and passes through one
or more @ref[pathPrefix](directives/path-directives/pathPrefix.md) or @ref[path](directives/path-directives/path.md) directives the "unmatched path" progressively gets "eaten into" from the
left until, in most cases, it eventually has been consumed completely.

当一个请求（或者确切地说一个 @apidoc[RequestContext] 实例）进入路由结构时，它有一个与 `request.uri.path` 相同（类型）的 `unmatchedPath`。
当 `RequestContext` 通过路由树向下传播，通过一个或多个 @ref[pathPrefix](directives/path-directives/pathPrefix.md) 或者 @ref[path](directives/path-directives/path.md) 路径时，`unmatchedPath` 的左边被逐渐“吃掉”，在大多数情况下，它最终会被完全消耗掉。

What exactly gets matched and consumed as well as extracted from the unmatched path in each directive is defined with
the path matching DSL, which is built around these types:

在每个指令中，都由路径匹配 DSL 来精确地进行匹配并从中提取需要的内容。路径匹配 DSL 围绕着以下类型构建：

Scala
:   ```scala
    trait PathMatcher[L: Tuple]
    type PathMatcher0 = PathMatcher[Unit]
    type PathMatcher1[T] = PathMatcher[Tuple1[T]]
    type PathMatcher2[T,U] = PathMatcher[Tuple2[T,U]]
    // .. etc
    ```

Java
:   ```java
    package akka.http.javadsl.server;
    class PathMatcher0
    class PathMatcher1<T1>
    class PathMatcher2<T1, T2>
    // .. etc
    ```

The number and types of the values extracted by a `PathMatcher` instance
@scala[is represented by the `L` type
parameter which needs to be one of Scala's TupleN types or `Unit` (which is designated by the `Tuple` context bound).]
@java[is determined by the class and its type parameters.]
@scala[The convenience alias `PathMatcher0` can be used for all matchers which don't extract anything while `PathMatcher1[T]`
defines a matcher which only extracts a single value of type `T`.]

通过 `PathMatcher` 实例提取的值的数量和类型 @scala[表示为 `L` 类型参数，`L` 需要是 Scala 的 `TupleN` 类型或 `Unit` 类型（由 `Tuple` 上下文绑定指定）。]
@java[是通过类和它的类型参数决定。]
@scala[便利别名 `PathMatcher0` 可以用于所以不提取任何内容的匹配器，而 `PathMatcher1[T]` 定义了 只提取类型为 `T` 的单个值的匹配器。]

Here is an example of a more complex `PathMatcher` expression:

这里是一个更复杂的 `PathMatcher` 表达式例子：

Scala
:  @@snip [PathDirectivesExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/directives/PathDirectivesExamplesSpec.scala) { #path-matcher }

Java
:  @@snip [PathDirectivesExamplesTest.java]($test$/java/docs/http/javadsl/server/directives/PathDirectivesExamplesTest.java) { #path-matcher }

This will match paths like `foo/bar/X42/edit` or @scala[`foo/bar/X/create`]@java[`foo/bar/X37/create`].

这将匹配路由，如：`foo/bar/X42/edit` 或 @scala[`foo/bar/X/create`]@java[`foo/bar/X37/create`] 。

@@@ note
The path matching DSL describes what paths to accept **after** URL decoding. This is why the path-separating
slashes have special status and cannot simply be specified as part of a string! **The string "foo/bar" would match
the raw URI path "foo%2Fbar"**, which is most likely not what you want!

路径匹配 DSL 描述了在 *URL* 解码之后接受什么路径。这就是为什么路径-分隔斜线具有特殊的状态，并且不能简单地指定为字符串的一部分！必需使用/函数来定义，
**字符串 "foo/bar" 将匹配原始的 URI 路径 "foo%2fbar"** ，这很可能不是你想要的。
@@@



## Basic PathMatchers
**基本的路径匹配器**

@@@ div { .group-scala }
A complex `PathMatcher` can be constructed by combining or modifying more basic ones. Here are the basic matchers
that Akka HTTP already provides for you:

复杂的 `PathMatcher` 可以组合和修改更多基本的路径匹配器来构造。Akka HTTP 已经提供了一些基本的匹配器：

String
: You can use a `String` instance as a `PathMatcher0`. Strings simply match themselves and extract no value.
Note that strings are interpreted as the decoded representation of the path, so if they include a '/' character
this character will match "%2F" in the encoded raw URI!

: 可以使用 `String` 实例作为 `PathMatcher0`。字符串只匹配自身，不提取任何值。需要注意的是字符串被解释为路径的解码表示（decode），
因此，如果它包含一个 `/` 字符时，这个字符将匹配编码的原始 URI 中的 ’%2F’ 。

Regex
: You can use a `Regex` instance as a `PathMatcher1[String]`, which matches whatever the regex matches and extracts
one `String` value. A `PathMatcher` created from a regular expression extracts either the complete match (if the
regex doesn't contain a capture group) or the capture group (if the regex contains exactly one capture group).
If the regex contains more than one capture group an `IllegalArgumentException` will be thrown.

: 可以使用 `Regex` 实例作为 `PathMatcher1[String]`，它匹配正则表达式匹配的任何内容并提取为一个 `String` 值。
从正则表达式创建的 `PathMatcher` 提取完整匹配（如果正则不包含捕获组）或者捕获的内容（如果正则精确包含一个捕获组）。
若正则表达式包含多个捕获组，则将引发 `IllegalArgumentException` 异常。

Map[String, T]
: You can use a `Map[String, T]` instance as a `PathMatcher1[T]`, which matches any of the keys and extracts the
respective map value for it.

: 可以使用 `Map[String, T]` 实体作为 `PathMatcher1[T]`，它匹配任何键并为其提取相应映射值。

Slash: PathMatcher0
: Matches exactly one path-separating slash (`/`) character and extracts nothing.

: 精确匹配一个路径分隔斜杆（`/`）字符并不提取任何内容。

Segment: PathMatcher1[String]
: Matches if the unmatched path starts with a path segment (i.e. not a slash).
If so the path segment is extracted as a `String` instance.

: 如果未匹配的路径以路径段（即，非斜线）开始，则匹配。如果是，则将路径段提取为 `String` 实例。

PathEnd: PathMatcher0
: Matches the very end of the path, similar to `$` in regular expressions and extracts nothing.

: 匹配路径的末尾，类似正则表达式里的 `$`，不提取任何内容。

Remaining: PathMatcher1[String]
: Matches and extracts the complete remaining unmatched part of the request's URI path as an (encoded!) String.
If you need access to the remaining *decoded* elements of the path use `RemainingPath` instead.

: 匹配并将请求的 URI 路径的剩余未匹配部分提取为（编码的）字符串。如果你需要访问路径的剩余 *解码* 元素，则使用 `RemainingPath` 替代。 

RemainingPath: PathMatcher1[Path]
: Matches and extracts the complete remaining, unmatched part of the request's URI path.

: 匹配并提取请求的 URI 路径的剩余未匹配部分。

IntNumber: PathMatcher1[Int]
: Efficiently matches a number of decimal digits (unsigned) and extracts their (non-negative) `Int` value. The matcher
will not match zero digits or a sequence of digits that would represent an `Int` value larger than `Int.MaxValue`.

: 高效地匹配多个十进制数字（无符号）并提取它们的（非负数）`Int` 值。匹配器将不匹配零位数字或表示的 `Int` 值大于 `Int.MaxValue` 的整数序列。

LongNumber: PathMatcher1[Long]
: Efficiently matches a number of decimal digits (unsigned) and extracts their (non-negative) `Long` value. The matcher
will not match zero digits or a sequence of digits that would represent an `Long` value larger than `Long.MaxValue`.

: 高效地匹配多个十进制数字（无符号）并提取它们的（非负数）`Long` 值。匹配器将不匹配零位数字或表示的 `Long` 值大于 `Long.MaxValue` 的整数序列。

HexIntNumber: PathMatcher1[Int]
: Efficiently matches a number of hex digits and extracts their (non-negative) `Int` value. The matcher will not match
zero digits or a sequence of digits that would represent an `Int` value larger than `Int.MaxValue`.

: 高效地匹配多个十六进制数字并提取它们的（非负数）`Int` 值。匹配器将不匹配零位数字或表示的 `Int` 值大于 `Int.MaxValue` 的整数序列。

HexLongNumber: PathMatcher1[Long]
: Efficiently matches a number of hex digits and extracts their (non-negative) `Long` value. The matcher will not
match zero digits or a sequence of digits that would represent an `Long` value larger than `Long.MaxValue`.

: 高效地匹配多个十六进制数字并提取它们的（非负数）`Long` 值。匹配器将不匹配零位数字或表示的 `Long` 值大于 `Long.MaxValue` 的整数序列。

DoubleNumber: PathMatcher1[Double]
: Matches and extracts a `Double` value. The matched string representation is the pure decimal,
optionally signed form of a double value, i.e. without exponent.

: 匹配并提取一个 `Double` 值。匹配的字符串表现形式是纯十进制，可选的有符号双精度值，例如：不带指数。

JavaUUID: PathMatcher1[UUID]
: Matches and extracts a `java.util.UUID` instance.

: 匹配并提取一个 `java.util.UUID` 实例。

Neutral: PathMatcher0
: A matcher that always matches, doesn't consume anything and extracts nothing.
Serves mainly as a neutral element in `PathMatcher` composition.

: 一个总会匹配的匹配器，不消耗任何东西也不提取任何内容。主要用作 `PathMatcher` 组合中的中性元素。

Segments: PathMatcher1[List[String]]
: Matches all remaining segments as a list of strings. Note that this can also be "no segments" resulting in the empty
list. If the path has a trailing slash this slash will *not* be matched, i.e. remain unmatched and to be consumed by
potentially nested directives.

: 匹配所有剩余（路径）段作为一个字符串列表。注意这也可以是“无段”导致空列表。如果路径有一个尾随斜杠，则此斜杠将不匹配，即，保持不匹配，并由潜在的嵌套指令使用。

separateOnSlashes(string: String): PathMatcher0
: Converts a path string containing slashes into a `PathMatcher0` that interprets slashes as
path segment separators. This means that a matcher matching "%2F" cannot be constructed with this helper.

: 将包含斜杠的路径字符串转换为 `PathMatcher0`，将斜杠解释为路径段分隔符。这意味着无法使用此助手构造匹配“%2F”的匹配程序。

provide[L: Tuple](extractions: L): PathMatcher[L]
: Always matches, consumes nothing and extracts the given `TupleX` of values.

: 总是匹配，不消耗任何东西并提取给定值的 `TupleX`。

PathMatcher[L: Tuple](prefix: Path, extractions: L): PathMatcher[L]
: Matches and consumes the given path prefix and extracts the given list of extractions.
If the given prefix is empty the returned matcher matches always and consumes nothing.

: 匹配并使用给定的路径前缀且提取给定的提取列表。如果给定的前缀为空，则返回的匹配器始终匹配并且不消耗任何内容。

@@@

@@@ div { .group-java }

A path matcher is a description of a part of a path to match. The simplest path matcher is `PathMatcher.segment` which
matches exactly one path segment against the supplied constant string.

Other path matchers defined in @apidoc[PathMatchers] match the end of the path (`PathMatchers.END`), a single slash
(`PathMatchers.SLASH`), or nothing at all (`PathMatchers.NEUTRAL`).

Many path matchers are hybrids that can both match (by using them with one of the PathDirectives) and extract values,
Extracting a path matcher value (i.e. using it with `handleWithX`) is only allowed if it nested inside a path
directive that uses that path matcher and so specifies at which position the value should be extracted from the path.

Predefined path matchers allow extraction of various types of values:

PathMatchers.segment(String)
: Strings simply match themselves and extract no value.
Note that strings are interpreted as the decoded representation of the path, so if they include a '/' character
this character will match "%2F" in the encoded raw URI!

PathMatchers.segment(java.util.regex.Pattern)
: You can use a regular expression instance as a path matcher, which matches whatever the regex matches and extracts
one `String` value. A `PathMatcher` created from a regular expression extracts either the complete match (if the
regex doesn't contain a capture group) or the capture group (if the regex contains exactly one capture group).
If the regex contains more than one capture group an `IllegalArgumentException` will be thrown.

PathMatchers.SLASH
: Matches exactly one path-separating slash (`/`) character.

PathMatchers.END
: Matches the very end of the path, similar to `$` in regular expressions.

PathMatchers.Segment
: Matches if the unmatched path starts with a path segment (i.e. not a slash).
If so the path segment is extracted as a `String` instance.

PathMatchers.Remaining
: Matches and extracts the complete remaining unmatched part of the request's URI path as an (encoded!) String.
If you need access to the remaining *decoded* elements of the path use `RemainingPath` instead.

PathMatchers.intValue
: Efficiently matches a number of decimal digits (unsigned) and extracts their (non-negative) `Int` value. The matcher
will not match zero digits or a sequence of digits that would represent an `Int` value larger than `Integer.MAX_VALUE`.

PathMatchers.longValue
: Efficiently matches a number of decimal digits (unsigned) and extracts their (non-negative) `Long` value. The matcher
will not match zero digits or a sequence of digits that would represent an `Long` value larger than `Long.MAX_VALUE`.

PathMatchers.hexIntValue
: Efficiently matches a number of hex digits and extracts their (non-negative) `Int` value. The matcher will not match
zero digits or a sequence of digits that would represent an `Int` value larger than `Integer.MAX_VALUE`.

PathMatchers.hexLongValue
: Efficiently matches a number of hex digits and extracts their (non-negative) `Long` value. The matcher will not
match zero digits or a sequence of digits that would represent an `Long` value larger than `Long.MAX_VALUE`.

PathMatchers.uuid
: Matches and extracts a `java.util.UUID` instance.

PathMatchers.NEUTRAL
: A matcher that always matches, doesn't consume anything and extracts nothing.
Serves mainly as a neutral element in `PathMatcher` composition.

PathMatchers.segments
: Matches all remaining segments as a list of strings. Note that this can also be "no segments" resulting in the empty
list. If the path has a trailing slash this slash will *not* be matched, i.e. remain unmatched and to be consumed by
potentially nested directives.

@@@

@@@ div { .group-scala }
## Combinators
**组合器**

Path matchers can be combined with these combinators to form higher-level constructs:

路径匹配器可以与这些组合器组合起来形成更高级的结构：

Tilde Operator (`~`)
: The tilde is the most basic combinator. It simply concatenates two matchers into one, i.e if the first one matched
(and consumed) the second one is tried. The extractions of both matchers are combined type-safely.
For example: `"foo" ~ "bar"` yields a matcher that is identical to `"foobar"`.

波浪操作符（`~`）
: 波浪符号是最基本的组合器。它简单地连接两个匹配为一个，例如：如果第一个匹配器匹配（并消耗）则第二个被尝试。两个匹配器的提取被安全地组合在一起。
例如：`"foo" ~ "bar"` 会产生一个与 `"foobar"` 相同的匹配器。

Slash Operator (`/`)
: This operator concatenates two matchers and inserts a `Slash` matcher in between them.
For example: `"foo" / "bar"` is identical to `"foo" ~ Slash ~ "bar"`.

斜杆操作符（`/`）
: 这个操作符连接两个匹配器并在中间插入一个 `Slash` 匹配器。例如：`"foo" / "bar"` 等同于 `"foo" ~ Slash ~ "bar"`.

Pipe Operator (`|`)
: This operator combines two matcher alternatives in that the second one is only tried if the first one did *not* match.
The two sub-matchers must have compatible types.
For example: `"foo" | "bar"` will match either "foo" *or* "bar".
When combining an alternative expressed using this operator with an `/` operator, make sure to surround the alternative with parentheses, like so: `("foo" | "bar") / "bom"`. Otherwise, the `/` operator takes precedence and would only apply to the right-hand side of the alternative.

管道操作符（`|`）
: 此操作符组合了两个匹配器备选项，因为只有在第一个匹配器不匹配时才尝试第二个匹配器。两个子匹配器必须具有兼容的类型。例如：`"foo" | "bar"` 将匹配 "foo" *或* "bar"。
当使用这个操作符和 `/` 操作符来表示一个可选项时，一定要用括号把它括起来，就像这样：`("foo" | "bar") / "bom"`。否则，`/` 运算符优先，只适用于替代项的右侧。

## Modifiers
**修改器**

Path matcher instances can be transformed with these modifier methods:

路径匹配器实例可以使用这些修改器方法进行转换：

/
: The slash operator cannot only be used as combinator for combining two matcher instances, it can also be used as
a postfix call. `matcher /` is identical to `matcher ~ Slash` but shorter and easier to read.

: 斜杆操作符不只用作合并两个匹配器实例的组合器，它还可以作用后缀调用。`matcher /` 等同于 `matcher ~ Slash`，但更短且更易读。

?
:
By postfixing a matcher with `?` you can turn any `PathMatcher` into one that always matches, optionally consumes
and potentially extracts an `Option` of the underlying matchers extraction. The result type depends on the type
of the underlying matcher:

: `?` 后缀匹配器，可以将任何 `PathMatcher` 转换为始终匹配的路径匹配器，可选地消耗并可能提取底层匹配器提取的 `Option`。结果类型取决于底层匹配器的类型：

|If a `matcher` is of type | then `matcher.?` is of type|
|--------------------------|----------------------------|
|`PathMatcher0`          | `PathMatcher0`           |
|`PathMatcher1[T]`       | `PathMatcher1[Option[T]]`|
|`PathMatcher[L: Tuple]` | `PathMatcher[Option[L]]` |

repeat(separator: PathMatcher0 = PathMatchers.Neutral)
:
By postfixing a matcher with `repeat(separator)` you can turn any `PathMatcher` into one that always matches,
consumes zero or more times (with the given separator) and potentially extracts a `List` of the underlying matcher's
extractions. The result type depends on the type of the underlying matcher:

: `repeat(separator)` 后缀匹配器，可以将任何 `PathMatcher` 转换为始终匹配的路径匹配器，消耗零或多次（使用指定分隔符）并可能提取底层匹配器提取的 `List`。结果类型取决于底层匹配器的类型：

|If a `matcher` is of type | then `matcher.repeat(...)` is of type|
|--------------------------|--------------------------------------|
|`PathMatcher0`          | `PathMatcher0`         |
|`PathMatcher1[T]`       | `PathMatcher1[List[T]]`|
|`PathMatcher[L: Tuple]` | `PathMatcher[List[L]]` |

unary_!
: By prefixing a matcher with `!` it can be turned into a `PathMatcher0` that only matches if the underlying matcher
does *not* match and vice versa.

: `!` 前缀匹配器，它可以变成一个 `PathMatcher0`，只有当底层匹配器 *不* 匹配时才匹配，反之亦然。

transform / (h)flatMap / (h)map
: These modifiers allow you to append your own "post-application" logic to another matcher in order to form a custom
one. You can map over the extraction(s), turn mismatches into matches or vice-versa or do anything else with the
results of the underlying matcher. Take a look at the method signatures and implementations for more guidance as to
how to use them.

: 这些修改器允许你将自己的“后应用程序”逻辑附加到另一个匹配器，以便形成自定义匹配器。你可以映射提取（值），将不匹配转换为匹配，或者将匹配转换为不匹配，或者对底层匹配器的结果执行其他操作。
有关如何使用它们的更多指导，参阅方法签名和实现。

@@@

## Examples
**示例**

Here's a collection of path matching examples:

这里是一组路由匹配示例：

Scala
:   @@snip [PathDirectivesExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/directives/PathDirectivesExamplesSpec.scala) { #path-dsl }

Java
:   @@snip [PathDirectiveExampleTest.java]($test$/java/docs/http/javadsl/server/PathDirectiveExampleTest.java) { #path-examples }

