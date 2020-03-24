# 1. 导读

@@project-info{ projectId="akka-http" }

The Akka HTTP modules implement a full server- and client-side HTTP stack on top of *akka-actor* and *akka-stream*. It's
not a web-framework but rather a more general toolkit for providing and consuming HTTP-based services. While interaction
with a browser is of course also in scope it is not the primary focus of Akka HTTP.

Akka HTTP 模块组在 *akka-actor* 和 *akka-stream* 的基础上实现了全HTTP栈（ 服务器-和客户端 ）的功能。它并不是一个 web 框架，而是一个更通用的工具箱，以便生成提供或消费基于 HTTP 的网络服务。虽然与浏览器进行互动是其功能的组成部分，但这个并不是 Akka HTTP 的主要目的（译注：简单来说，别把 Akka HTTP 模组只当作页面服务器）

Akka HTTP follows a rather open design and many times offers several different API levels for "doing the same thing".
You get to pick the API level of abstraction that is most suitable for your application.
This means that, if you have trouble achieving something using a high-level API, there's a good chance that you can get
it done with a low-level API, which offers more flexibility but might require you to write more application code.

Akka HTTP 采用的是一个开放式的设计，很多时候会提供不同抽象层次的 API 完成同一件事。 使用者可以选择更合适相关应用所需抽象度的 API。这里的意思指，如果当采用一个比较高抽象度的 API 无法实现开发需求的时候，很有可能开发人员可以采用一个比较底抽象度的 API 达到目的，虽然这样会有更多的灵活型，但也意味着需要写更多的代码。

## Philosophy
**理念**

Akka HTTP has been driven with a clear focus on providing tools for building integration layers rather than application cores. As such it regards itself as a suite of libraries rather than a framework.

Akka HTTP 的开发一直有一个明确的焦点，它是为整合层提供工具，而不是针对应用程序的核心。因而，它把自己当作工具库而不是一个框架。

A framework, as we’d like to think of the term, gives you a “frame”, in which you build your application. It comes with a lot of decisions already pre-made and provides a foundation including support structures that lets you get started and deliver results quickly. In a way a framework is like a skeleton onto which you put the “flesh” of your application in order to have it come alive. As such frameworks work best if you choose them before you start application development and try to stick to the framework's “way of doing things” as you go along.

框架，我们从名称联想到，给你一个”框“，在这个框里面你构建你的应用程序。它有很多已经被预设的开发决定，同时也提供了一个基础，这个基础包括了一些架构能让开发者可以快速的投入以及交付结果。某种意义上框架就像一个骨架，你把应用的”血肉“放进去，就可以衍生应用程序出来。如果你开始应用程序开发前就选择这些框架，并坚持框架的”工作方式“，那么这样的框架是最佳的。

For example, if you are building a browser-facing web application it makes sense to choose a web framework and build your application on top of it because the “core” of the application is the interaction of a browser with your code on the web-server. The framework makers have chosen one “proven” way of designing such applications and let you “fill in the blanks” of a more or less flexible “application-template”. Being able to rely on best-practice architecture like this can be a great asset for getting things done quickly.

举个例子，如果你构建一个面向浏览器的 Web 应用，那么，使用一个 Web 框架是应当的。因为这个应用的“核心”是浏览器与开发者在服务器端的代码进行互动。框架的作者已经挑选了一个”被证明有效“的方式为相关的应用程序开发设计，并让开发者或多或少地在这个”应用模板“里面进行”填空”。对开发者而言，可以依赖于已知的最佳实践架构下进行快速开发有时是很大的资产。

However, if your application is not primarily a web application because its core is not browser-interaction but some specialized maybe complex business service and you are merely trying to connect it to the world via a REST/HTTP interface a web-framework might not be what you need. In this case the application architecture should be dictated by what makes sense for the core not the interface layer. Also, you probably won’t benefit from the possibly existing browser-specific framework components like view templating, asset management, JavaScript- and CSS generation/manipulation/minification, localization support, AJAX support, etc.

可是，如果你的应用并不只是提供一个 web 应用程序（因为这个程序的核心并不是浏览器互动），而是一些特别的，甚至是复杂的，业务相关的应用服务，而你只是需要通过 REST/HTTP 提供接口连接。那，一个完整的 web 框架就未必是你所需要的。在这种情景下，应用架构应该取决于什么对核心更合理而不是对接口层面。而且，这个时候你并未能从某些框架中针对浏览器端开发的组件中获益，例如，视图模版，静态资产管理，JS 和 CSS 的生成器／调解器／缩减器，本地化处理，AJAX 支持，等等。

Akka HTTP was designed specifically as “not-a-framework”, not because we don’t like frameworks, but for use cases where a framework is not the right choice. Akka HTTP is made for building integration layers based on HTTP and as such tries to “stay on the sidelines”. Therefore you normally don’t build your application “on top of” Akka HTTP, but you build your application on top of whatever makes sense and use Akka HTTP merely for the HTTP integration needs.

Akka HTTP 被特意设计为“不是一个框架”，并非我们不喜欢框架，而是为了某些当架构不是最优解的应用场景。Akka HTTP 是针对建立基于 HTTP 的整合层面，并会尽量“保持边缘化”。因此，你并不需要“基于” Akka HTTP 开发，而应该使用任何合理基础开发应用，并只当有 HTTP 整合需求时引入 Akka HTTP。

On the other hand, if you prefer to build your applications with the guidance of a framework, you should give [Play Framework](https://www.playframework.com/) or [Lagom](https://www.lagomframework.com/) a try, which both use Akka internally.

另一方面而言，如果开发者有意愿在一个框架下指导开发应用，可以试试用 Play framework 或者 Lagom，两者都在其内部使用了 Akka 。

（译注：不用理采 Lightbend 的纯广告，你完全可以直接基于 Akka HTTP 进行开发。当然也建议尝试下 Play 或 Lagom。）

## Using Akka HTTP
**使用 Akka HTTP**

Akka HTTP is provided as independent modules from Akka itself under its own release cycle. Akka HTTP is @ref[compatible](compatibility-guidelines.md)
with Akka 2.5 and any later 2.x versions released during the lifetime of Akka HTTP 10.1.x. The modules, however, do *not* depend on `akka-actor` or `akka-stream`, so the user is required to
choose an Akka version to run against and add a manual dependency to `akka-stream` of the chosen version.

Akka HTTP 在其自身的发布周期内作为 Akka 的独立模块提供。Akka HTTP 与 Akka 2.5 和在 Akka HTTP 10.1.x 生命周期内 2.x 的更高版本兼容。但是，这些模块 *不* 能依赖 `akka-actor` 或 `akka-stream`，因此用户需要选择运行的 Akka 版本并向所选版本的 `akka-stream` 手动添加依赖。

sbt
:   @@@vars
    ```
    "com.typesafe.akka" %% "akka-http"   % "$project.version$" $crossString$
    "com.typesafe.akka" %% "akka-stream" % "$akka.version$" // or whatever the latest version is
    ```
    @@@

Gradle
:   @@@vars
    ```
    compile group: 'com.typesafe.akka', name: 'akka-http_$scala.binary_version$',   version: '$project.version$'
    compile group: 'com.typesafe.akka', name: 'akka-stream_$scala.binary_version$', version: '$akka.version$'
    ```
    @@@

Maven
:   @@@vars
    ```
    <dependency>
      <groupId>com.typesafe.akka</groupId>
      <artifactId>akka-http_$scala.binary_version$</artifactId>
      <version>$project.version$</version>
    </dependency>
    <dependency>
      <groupId>com.typesafe.akka</groupId>
      <artifactId>akka-stream_$scala.binary_version$</artifactId>
      <version>$akka.version$</version> <!-- Or whatever the latest version is -->
    </dependency>
    ```
    @@@


Alternatively, you can bootstrap a new sbt project with Akka HTTP already
configured using the [Giter8](http://www.foundweekends.org/giter8/) template:

或者，你可以使用 [Giter8](http://www.foundweekends.org/giter8/) 已经配置好的 Akka HTTP 模板来引导新的 sbt 项目。

@@@ div { .group-scala }
```sh
sbt -Dsbt.version=0.13.15 new https://github.com/akka/akka-http-quickstart-scala.g8
```
@@@
@@@ div { .group-java }
```sh
sbt -Dsbt.version=0.13.15 new https://github.com/akka/akka-http-quickstart-java.g8
```
From there on the prepared project can be built using Gradle or Maven.

可以使用 Gradle 或 Maven 从构建准备好的项目开始。
@@@

More instructions can be found on the @scala[[template
project](https://github.com/akka/akka-http-quickstart-scala.g8)]@java[[template
project](https://github.com/akka/akka-http-quickstart-java.g8)].

更多说明在 @scala[[template
project](https://github.com/akka/akka-http-quickstart-scala.g8)]@java[[template
project](https://github.com/akka/akka-http-quickstart-java.g8)] 能找到。

## Routing DSL for HTTP servers
**HTTP 服务端的路由 DSL**

The high-level, routing API of Akka HTTP provides a DSL to describe HTTP "routes" and how they should be handled.
Each route is composed of one or more level of @apidoc[Directives] that narrows down to handling one specific type of
request.

Akka HTTP 在高抽像程度的 API 里提供了一个 DSL 来描述 HTTP “路由”和其相关处理。每个路由包含一或多级 @apidoc[Directives]，每个路由则专注于处理一个类型的请求。

For example one route might start with matching the `path` of the request, only matching if it is "/hello", then
narrowing it down to only handle HTTP `get` requests and then `complete` those with a string literal, which
will be sent back as a HTTP OK with the string as response body.

举个例子，一个路由通过 `path` 匹配请求（路径），只有当路径是 `/hello` 时，才处理相关的 HTTP `get` 请求并 `complete` （完成返回）返回字符串，
该文本将作为一个 HTTP OK 响应中的正文。

The
@scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]
@java[@javadoc[Route](akka.http.scaladsl.server.Route)]
created using the Route DSL is then "bound" to a port to start serving HTTP requests:

使用路由 DSL 生成的 @scala[@scaladoc[Route](akka.http.scaladsl.server.index#Route=akka.http.scaladsl.server.RequestContext=%3Escala.concurrent.Future[akka.http.scaladsl.server.RouteResult])]
@java[@javadoc[Route](akka.http.scaladsl.server.Route)] 对像 “绑定” 到端口以开始服务 HTTP 请求：

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #minimal-routing-example }

Java
:   @@snip [HttpServerMinimalExampleTest.java]($test$/java/docs/http/javadsl/HttpServerMinimalExampleTest.java) { #minimal-routing-example }

When you run this server, you can either open the page in a browser,
at the following url: [http://localhost:8080/hello](http://localhost:8080/hello), or call it in your terminal, via `curl http://localhost:8080/hello`.

当你运行这个服务时，你也可以在下面连接：[http://localhost:8080/hello](http://localhost:8080/hello) 打开浏览器页面，或则在你的终端里调用 `curl http://localhost:8080/hello` （访问）。

## Marshalling
**转换**

Transforming request and response bodies between over-the-wire formats and objects to be used in your application is
done separately from the route declarations, in marshallers, which are pulled in implicitly using the "magnet" pattern.
This means that you can `complete` a request with any kind of object as long as there is an implicit marshaller
available in scope.

在 marshaller 中，在线格式和对像之间转换请求和响应正文在你的应用程序里面是和路由声明分开的，这些是使用“magent”模式隐式引入的。
这意味着你能以任何对像 `complete` 一个请求，只要范围内有可用的隐式 marshaller。 

@@@ div { .group-scala }
Default marshallers are provided for simple objects like String or ByteString, and you can define your own for example
for JSON. An additional module provides JSON serialization using the spray-json library (see @ref[JSON Support](common/json-support.md)
for details):

默认 marshaller 提供了简单对像如 `String` 或 `ByteString` （的编组转换功能）。例如，你可以定义你自己的 JSON （编组转换）。附加模块使用 spary-json 库提供 JSON 序列化 (有关详细信息见 @ref[JSON 支持](common/json-support.md)):

@@dependency [sbt,Gradle,Maven] {
  group="com.typesafe.akka"
  artifact="akka-http-spray-json_$scala.binary.version$"
  version="$project.version$"
}

@@@
@@@ div { .group-java }
JSON support is possible in `akka-http` by the use of Jackson, an external artifact (see @ref[JSON Support](common/json-support.md#jackson-support)
for details):

@@dependency [sbt,Gradle,Maven] {
  group="com.typesafe.akka"
  artifact="akka-http-jackson_$scala.binary.version$"
  version="$project.version$"
}

@@@

A common use case is to reply to a request using a model object having the marshaller transform it into JSON. In
this case shown by two separate routes. The first route queries an asynchronous database and marshalls the
@scala[`Future[Option[Item]]`]@java[`CompletionStage<Optional<Item>>`] result into a JSON response. The second unmarshalls an `Order` from the incoming request,
saves it to the database and replies with an OK when done.

通常情况下用一个模型对像来回复请求，由 marshaller 将其转换为 JSON。这个用例展示了两个单独的路由。第一个路由查询一个异步数据库，并将 @scala[`Future[Option[Item]]`]@java[`CompletionStage<Optional<Item>>`] 结果编组为 JSON 响应。第二个将入站请求解组为 `Order`，再保存它到数据库里，并在完成后以 OK 回复。

Scala
:   @@snip [SprayJsonExampleSpec.scala]($test$/scala/docs/http/scaladsl/SprayJsonExampleSpec.scala) { #second-spray-json-example }

Java
:   @@snip [JacksonExampleTest.java]($test$/java/docs/http/javadsl/JacksonExampleTest.java) { #second-jackson-example }

When you run this server, you can update the inventory via `curl -H "Content-Type: application/json" -X POST -d '{"items":[{"name":"hhgtg","id":42}]}' http://localhost:8080/create-order` on your terminal - adding an item named `"hhgtg"` and having an `id=42`; and then view the inventory either in a browser, at a url like: [http://localhost:8080/item/42](http://localhost:8080/item/42) - or on the terminal,
via `curl http://localhost:8080/item/42`.

当你运行这个服务，你能通过你的终端 `curl -H "Content-Type: application/json" -X POST -d '{"items":[{"name":"hhgtg","id":42}]}' http://localhost:8080/create-order` 来更新库存 - 添加 `id=42` 并且名为 `"hhgtg"` 的项目；然后在浏览器中查看库存，像这样的连接：[http://localhost:8080/item/42](http://localhost:8080/item/42) - 或者通过终端 `curl http://localhost:8080/item/42` 。

The logic for the marshalling and unmarshalling JSON in this example is provided by the @scala["spray-json"]@java["Jackson"] library
(details on how to use that here: @scala[@ref[JSON Support](common/json-support.md))]@java[@ref[JSON Support](common/json-support.md#jackson-support))].

在这个示例里编组和解组 JSON 逻辑由 @scala["spray-json"]@java["Jackson"] 库提供 (怎样使用的详细信息在这里: @scala[@ref[JSON 支持](common/json-support.md))]@java[@ref[JSON 支持](common/json-support.md#jackson-support))]

## Streaming
**流处理**

One of the strengths of Akka HTTP is that streaming data is at its heart meaning that both request and response bodies
can be streamed through the server achieving constant memory usage even for very large requests or responses. Streaming
responses will be backpressured by the remote client so that the server will not push data faster than the client can
handle, streaming requests means that the server decides how fast the remote client can push the data of the request
body.

Akka HTTP 的优势之一是流数据是它的核心，这意味着请求和响应可通过服务器流式传输，即使很大的请求或响应也能获得恒定的内存使用。流式响应将由远程客户端施加回压，
以使服务器不会推送超过客户端可处理速度的数据，流式请求意味着服务器决定远程客户端可以多快的速度推送请求正文。

Example that streams random numbers as long as the client accepts them:

以下的例子是向客户端输出一个随机数的数据流，只有客户端能接受的话：

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #stream-random-numbers }

Java
:   @@snip [HttpServerStreamRandomNumbersTest.java]($test$/java/docs/http/javadsl/HttpServerStreamRandomNumbersTest.java) { #stream-random-numbers }

Connecting to this service with a slow HTTP client would backpressure so that the next random number is produced on
demand with constant memory usage on the server. This can be seen using curl and limiting the rate
`curl --limit-rate 50b 127.0.0.1:8080/random`

连接到服务的一个慢速 HTTP 客户端将产生回压，以便根据需要在服务器上以固定的内存使用来生成下一个随机数。使用 curl 和速率限制可以看到这种情况 `curl --limit-rate 50b 127.0.0.1:8080/random`

Akka HTTP routes easily interact with actors. In this example one route allows for placing bids in a fire-and-forget
style while the second route contains a request-response interaction with an actor. The resulting response is rendered
as json and returned when the response arrives from the actor.

Akka HTTP 路由很容易与 actor 互动。在下面示例里，一个路由既可以用 fire-and-forget 风格，而同时第二个路由包含 request-response 方式与 actor 交互。
当响应从 actor 到达时，响应结果被渲染为 json 返回。

*（译注：fire-and-forget 和 request-response 都是 actor 模型的请求交互处理模式，有关 Akka actor 交互模式的更多内容见：[常用交互模式](https://www.yangbajing.me/akka-cookbook/actor/pattern/index.html)）*

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #actor-interaction }

Java
:   @@snip [HttpServerActorInteractionExample.java]($test$/java/docs/http/javadsl/HttpServerActorInteractionExample.java) { #actor-interaction }

When you run this server, you can add an auction bid via `curl -X PUT http://localhost:8080/auction?bid=22&user=MartinO` on the terminal; and then you can view the auction status either in a browser, at the url [http://localhost:8080/auction](http://localhost:8080/auction), or, on the terminal, via `curl http://localhost:8080/auction`.

当你运行这个服务，你可以在终端上通过 `curl -X PUT http://localhost:8080/auction?bid=22&user=MartinO` 添加拍卖出价；接着你可以通过浏览器在 url 地址 [http://localhost:8080/auction](http://localhost:8080/auction) ，或者通过终端 `curl http://localhost:8080/auction` 查看拍卖状态。 

More details on how JSON marshalling and unmarshalling works can be found in the @ref[JSON Support section](common/json-support.md).

有关 JSON 编组和解组工作的更多信息能在 @ref[JSON 支持](common/json-support.md) 找到。

Read more about the details of the high level APIs in the section @ref[High-level Server-Side API](routing-dsl/index.md).

阅读有关高级 API 的更多信息在 @ref[高级服务端 API](routing-dsl/index.md) 部分。

## Low-level HTTP server APIs
**低级 HTTP 服务端 API**

The low-level Akka HTTP server APIs allows for handling connections or individual requests by accepting
@apidoc[HttpRequest] s and answering them by producing @apidoc[HttpResponse] s. This is provided by the `akka-http-core` module,
which is included automatically when you depend on `akka-http` but can also be used on its own.
APIs for handling such request-responses as function calls and as a @apidoc[Flow[HttpRequest, HttpResponse, \_]] are available.

低级 Akka HTTP 服务器 API 允许通过接受 @apidoc[HttpRequest] 并生成 @apidoc[HttpResponse] 应答来连接或单个请求。这由 `akka-http-core` 模块提供，当你依赖 `akka-http` 但没有使用它（`akka-http-core`）时将自动包含它的依赖。提供了处理函数调用或 @apidoc[Flow[HttpRequest, HttpResponse, \_]] 形式的 request-response 的 API。

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #low-level-server-example }

Java
:   @@snip [HttpServerLowLevelExample.java]($test$/java/docs/http/javadsl/HttpServerLowLevelExample.java) { #low-level-server-example }

Read more details about the low level APIs in the section @ref[Core Server API](server-side/low-level-api.md).

阅读有关低级 API 的更多信息在 @ref[核心服务 API](server-side/low-level-api.md) 部分。

## HTTP client API
**HTTP 客户端 API**

The client APIs provide methods for calling a HTTP server using the same @apidoc[HttpRequest] and @apidoc[HttpResponse] abstractions
that Akka HTTP server uses but adds the concept of connection pools to allow multiple requests to the same server to be
handled more performantly by re-using TCP connections to the server.

客户端 API 使用相同的 @apidoc[HttpRequest] 和 @apidoc[HttpResponse] 抽像以提供方法来调用一个 HTTP 服务。Akka HTTP 服务器添加了连接池的概念，以允许到相同服务的多个请求复用 TCP 连接，从而使处理更多高效。 

Example simple request:

请求简单示例：

Scala
:   @@snip [HttpClientExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpClientExampleSpec.scala) { #single-request-example }

Java
:   @@snip [ClientSingleRequestExample.java]($test$/java/docs/http/javadsl/ClientSingleRequestExample.java) { #single-request-example }

Read more about the details of the client APIs in the section @ref[Consuming HTTP-based Services (Client-Side)](client-side/index.md).

阅读有关客户端 API 的更多信息在 @ref[Consuming HTTP-based Services (Client-Side)](client-side/index.md) 部分。 

## The modules that make up Akka HTTP
**组织 Akka HTTP 的更多模块**

Akka HTTP is structured into several modules:

Akka HTTP 由以下几个模块构成：

akka-http
: Higher-level functionality, like (un)marshalling, (de)compression as well as a powerful DSL
for defining HTTP-based APIs on the server-side, this is the recommended way to write HTTP servers
with Akka HTTP. Details can be found in the section @ref[High-level Server-Side API](routing-dsl/index.md)

: 高级功能，例如解组/编组（(un)marshalling）、解压/压缩（(de)compression）以及强大的DSL用于在服务器端定义基于 HTTP 的应用程序 API，使用这套 DSL 来编写 HTTP 服务是 Akka HTTP 的推荐方式。详细内容可在 @ref[高级服务端 API](routing-dsl/index.md) 部分找到。

akka-http-core
: A complete, mostly low-level, server- and client-side implementation of HTTP (incl. WebSockets)
Details can be found in sections @ref[Core Server API](server-side/low-level-api.md) and @ref[Consuming HTTP-based Services (Client-Side)](client-side/index.md)

: 一个完整的、大多数低级的、服务器-和客户端 HTTP 实现（包括 WebSocket）。详细内容能在 @ref[核心服务器 API](server-side/low-level-api.md) 和 @ref[消费基于 HTTP 的服务 (客户端)](client-side/index.md) 部分找到。

akka-http-testkit
: A test harness and set of utilities for verifying server-side service implementations

测试套件以及工具集，用以验证服务器端服务的实现

@@@ div { .group-scala }
akka-http-spray-json
: Predefined glue-code for (de)serializing custom types from/to JSON with [spray-json](https://github.com/spray/spray-json)
Details can be found here:  @ref[JSON Support](common/json-support.md)

预设好的胶水代码用于自定义类型与 JSON 之间的（反）序列化，使用 [spary-json](https://github.com/spray/spray-json)。详细内容能在这里找到： @ref[JSON Support](common/json-support.md) 
@@@

@@@ div { .group-scala }
akka-http-xml
: Predefined glue-code for (de)serializing custom types from/to XML with [scala-xml](https://github.com/scala/scala-xml)
Details can be found here: @ref[XML Support](common/xml-support.md)

预设好的胶水代码用于自定义类型与 XML 之间的（反）序列化，使用 [scala-xml](https://github.com/scala/scala-xml)。详细内容能在这里找到： @ref[XML Support](common/xml-support.md)
@@@
@@@ div { .group-java }
akka-http-jackson
: Predefined glue-code for (de)serializing custom types from/to JSON with [jackson](https://github.com/FasterXML/jackson)

预设好的胶水代码用户自定义类型与 JSON 之间的（反）序列化，使用 [jackson](https://github.com/FasterXML/jackson)
@@@
