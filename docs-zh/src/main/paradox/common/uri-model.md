## URI 模型

Akka HTTP offers its own specialised @apidoc[Uri] model class which is tuned for both performance and idiomatic usage within
other types of the HTTP model. For example, an @apidoc[HttpRequest]'s target URI is parsed into this type, where all character
escaping and other URI specific semantics are applied.

Akka HTTP 提供了自己的一套特制的 @apidoc[Uri] 模型，为了性能以及更好的于 HTTP 模型里的其它类型进行互动。例如，
@apidoc[HttpRequest] 的目标 URI 会解析成这个类型，并在解析过程中应用到所有字符转码和 URI 特殊语义等处理。

### Parsing a URI string
**一个 URI 字符串的语法分析**

We follow [RFC 3986](https://tools.ietf.org/html/rfc3986#section-1.1.2) to implement the URI parsing rules.
When you try to parse a URI string, Akka HTTP internally creates an instance of the @apidoc[Uri] class, which holds the modeled URI components inside.

我们按 [RFC 3986](https://tools.ietf.org/html/rfc3986#section-1.1.2) 实现了 URI 的语法分析规则。当你尝试解析一个 URI 字符串时，
Akka HTTP 内部分创建一个 @apidoc[Uri] 类的实例，其中保存建立的 URI 组件。

For example, the following creates an instance of a simple valid URI:

例如，下面创建了一个简单有效的 URI 实例：

Scala
:   
    ```
    Uri("http://localhost")
    ```
    
Java
:   
    ```
    Uri.create("http://localhost");
    ```


Below are some more examples of valid URI strings, and how you can construct a @apidoc[Uri] model class instances
@scala[,using `Uri.from()` method by passing `scheme`, `host`, `path` and `query` parameters].

以下再给出几个怎样构造 @apidoc[Uri] 模型实例的例子，它们都是有效 URI 字符串@scala[，使用 `Uri.from()` 方法和相关参数 `scheme`、`host`、`path` 和 `query`]。

Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #valid-uri-examples }

Java
:   @@snip [UriTest.scala]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #valid-uri-examples }

For exact definitions of the parts of a URI, like `scheme`, `path` and `query` refer to [RFC 3986](https://tools.ietf.org/html/rfc3986#section-1.1.2).
Here's a little overview:

对于一个 URI 的精确定义，例如 `scheme`、`path` 和 `query` ，参考 [RFC 3986](https://tools.ietf.org/html/rfc3986#section-1.1.2) 。
这是一个简短描述：

```
  foo://example.com:8042/over/there?name=ferret#nose
  \_/   \______________/\_________/ \_________/ \__/
   |           |            |            |        |
scheme     authority       path        query   fragment
   |   _____________________|__
  / \ /                        \
  urn:example:animal:ferret:nose
```

For "special" characters in URI, you typically use percent encoding like below.
Percent encoding is discussed in more detail in the @ref[Query String in URI](#query-string-in-uri) section.

对于 URI 里的“特殊”字符，一般使用如下的百分号编码。有关编码细节的更多讨论在 @ref[URI 里的查询字符串](#query-string-in-uri) 部分。

Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #dont-double-decode }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #dont-double-decode }


#### Invalid URI strings and IllegalUriException
**无效 URI 字符串以及 IllegalUriException**

When an invalid URI string is passed to `Uri()` as below, an `IllegalUriException` is thrown.

当如下一个无效的 URI 字符串被传给 `Uri()` ，一个 `IllegalUriException` 异常会抛出。

Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #illegal-cases-immediate-exception }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #illegal-scheme #illegal-userinfo #illegal-percent-encoding #illegal-path #illegal-path-with-control-char }

#### Directives to extract URI components
**抽取 URI 组件的指令**

To extract URI components with directives, see following references:
要使用指令抽取 URI 组件，可参考下列资料：

* @ref:[extractUri](../routing-dsl/directives/basic-directives/extractUri.md)
* @ref:[extractScheme](../routing-dsl/directives/scheme-directives/extractScheme.md)
* @ref:[scheme](../routing-dsl/directives/scheme-directives/scheme.md)
* @ref:[PathDirectives](../routing-dsl/directives/path-directives/index.md)
* @ref:[ParameterDirectives](../routing-dsl/directives/parameter-directives/index.md)

### Obtaining the raw request URI
**获取原始请求 URI**

Sometimes it may be needed to obtain the "raw" value of an incoming URI, without applying any escaping or parsing to it.
While this use case is rare, it comes up every once in a while. It is possible to obtain the "raw" request URI in Akka
HTTP Server side by turning on the `akka.http.server.raw-request-uri-header` flag.
When enabled, a `Raw-Request-URI` header will be added to each request. This header will hold the original raw request's
URI that was used. For an example check the reference configuration.

有时需要获取“原始”的 URI 值，原始 URI 不做转码和解析。虽然这种情况比较少，但也偶尔会遇到。在 Akka HTTP 服务器端打开 `akka.http.server.raw-request-uri-header` 标记就可获取”原始“请求URI。当标记被打开，一个 `Raw-Request-URI` 头将被添加到每个请求。这个头域将保存外部的原始请求 URI 以供使用。

### Query string in URI
**URI 里的查询字符串**

Although any part of URI can have special characters, it is more common for the query string in URI to have special characters,
which are typically [percent encoded](https://en.wikipedia.org/wiki/Percent-encoding).

虽然 URI 的任何部分都可能有特殊字符，但 URI 的查询字符串里有特殊字符更常见。

@scala[@apidoc[Uri] class's `query()` method]@java[The method `Uri::query()`] returns the query string of the URI, which is modeled in an instance of the `Query` class.
When you instantiate a @apidoc[Uri] class by passing a URI string, the query string is stored in its raw string form.
Then, when you call the `query()` method, the query string is parsed from the raw string.

@scala[@apidoc[Uri] 类的 `query()` 方法]@java[方法 `Uri::query()`] 返回一个 `Query` 类实例的 URI 查询字符串。
当你传一个 URI 字符串生成一个 @apidoc[Uri] 类实例时，查询字符串将以原始字符串的形式保存。直到你调用 `query` 方法时才解析。 

The below code illustrates how valid query strings are parsed.
Especially, you can check how percent encoding is used and how special characters like `+` and `;` are parsed.

下面的代码展示了怎样解析有效的查询字符串。特别是，可以留意下百分比编码的应用和怎样解析特殊字符串，如：`+` 和 `;` 。

@@@ note
The `mode` parameter to `Query()` and `Uri.query()` is discussed in @ref[Strict and Relaxed Mode](#strict-and-relaxed-mode).

@ref[严格和宽松模式](#strict-and-relaxed-mode) 讨论了 `Query` 和 `Uri.query()` 的 `mode` 参数。
@@@

Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #query-strict-definition }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #query-strict-definition }


Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #query-strict-mode }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #query-strict-mode }

Note that:

注意：

```
  Uri("http://localhost?a=b").query()
```

is equivalent to:

等价于：

```
  Query("a=b")
```

As in the [section 3.4 of RFC 3986](https://tools.ietf.org/html/rfc3986#section-3.4),
some special characters like "/" and "?" are allowed inside a query string, without escaping them using ("%") signs.

在 [RFC 3986 的 3.4 部分](https://tools.ietf.org/html/rfc3986#section-3.4) 里，一些特殊字符被允许在查询字符串里，例如：`'` 和 `?`，
它们不需要使用（"%"）标记。

> The characters slash ("/") and question mark ("?") may represent data within the query component.

> 字符斜杠（`/`）和问号（`?`）可以表示查询组件中的数据。

"/" and "?" are commonly used when you have a URI whose query parameter has another URI.

当 URI 中的查询参数有另一个 URI 时，`/` 和 `?` 常被用的。

Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #query-strict-without-percent-encode }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #query-strict-without-percent-encode }

However, some other special characters can cause `IllegalUriException` without percent encoding as follows.

但是，有些其它的特殊字符不使用百分比编码的话会抛出 `IllegalUriException` 异常，如下。

Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #query-strict-mode-exception-1 }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #query-strict-mode-exception-1 }


Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #query-strict-mode-exception-2 }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #query-strict-mode-exception-2 }

#### Strict and Relaxed Mode
**严格和宽松模式**

The `Uri.query()` method and `Query()` take a parameter `mode`, which is either `Uri.ParsingMode.Strict` or `Uri.ParsingMode.Relaxed`.
Switching the mode gives different behavior on parsing some special characters in URI.

`Uri.query()` 函数和 `Query()` 构造函数都可以读入一个参数 `mode`， 这个参数的类型可以是 `Uri.ParsingMode.Strict` 或 `Uri.ParsingMode.Relaxed`。模式不同的选择也会带来不同的字符串解析行为。

Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #query-relaxed-mode }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #query-relaxed-definition }

The below two cases threw `IllegalUriException` when you specified the `Strict` mode,

当你指定 `Strict` 模式时，以下两种情况抛出 `IllegalUriException` 异常，

Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #query-strict-mode-exception-1 }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #query-strict-mode-exception-1 #query-strict-mode-exception-2 }

but the `Relaxed` mode parses them as they are.

但 `Relaxed` 模式下解析正常。

Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #query-relaxed-mode-success }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #query-relaxed-mode-success }

However, even with the `Relaxed` mode, there are still invalid special characters which require percent encoding.

然而，即使在 `Relaxed` 模式，也存在一些无效的特殊字符需要进行百分比编码。

Scala
:   @@snip [UriSpec.scala]($akka-http$/akka-http-core/src/test/scala/akka/http/scaladsl/model/UriSpec.scala) { #query-relaxed-mode-exception }

Java
:   @@snip [UriTest.java]($akka-http$/akka-http-core/src/test/java/akka/http/javadsl/model/UriTest.java) { #query-relaxed-mode-exception-1 }

Other than specifying the `mode` in the parameters, like when using directives, you can specify the `mode` in your configuration as follows.

`mode` 除了可以作为一个参数设定，像使用指令时，你还可以在配置里设置，如下：

```
    # Sets the strictness mode for parsing request target URIs.
    # The following values are defined:
    #
    # `strict`: RFC3986-compliant URIs are required,
    #     a 400 response is triggered on violations
    #
    # `relaxed`: all visible 7-Bit ASCII chars are allowed
    #
    uri-parsing-mode = strict
```

To access the raw, unparsed representation of the query part of a URI use the `rawQueryString` member of the @apidoc[Uri] class.

要一个 URI 的访问原始的、未解析过的查询部分，使用 @apidoc[Uri] 的 `rawQueryString` 成员。

#### Directives to extract query parameters
**抽取查询参数的指令**

If you want to use directives to extract query parameters, see below pages.

如果你想使用指令抽取查询参数，见以下页面：

* @ref:[parameters](../routing-dsl/directives/parameter-directives/parameters.md)
* @ref:[parameter](../routing-dsl/directives/parameter-directives/parameter.md)
