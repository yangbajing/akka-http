# 路由 DSL
**Routing DSL**

In addition to the @ref[Core Server API](../server-side/low-level-api.md) Akka HTTP provides a very flexible "Routing DSL" for elegantly
defining RESTful web services. It picks up where the low-level API leaves off and offers much of the higher-level
functionality of typical web servers or frameworks, like deconstruction of URIs, content negotiation or
static content serving.

在 @ref[核心服务器 API](../server-side/low-level-api.md) 之外，Akka HTTP 提供了一个很灵活的 “路由 DSL” 来优雅的定义 RESTful Web 服务。
它改善低级 API 的不足，同时提供典型 Web 服务器或框架的很多高级功能，比如：解构 URI、内容协商或者静态内容服务。

@@@ note
It is recommended to read the @ref[Implications of the streaming nature of Request/Response Entities](../implications-of-streaming-http-entity.md) section,
as it explains the underlying full-stack streaming concepts, which may be unexpected when coming
from a background with non-"streaming first" HTTP Servers.

推荐阅读 @ref[请求/响应实体流的实质含义](../implications-of-streaming-http-entity.md) 部分，它阐明了（Akka HTTP）底层的全栈流的概念。
因为对于没有“流式优先” HTTP 服务器概念背景的人来说，也许会感到难以理解。
@@@

@@toc { depth=1 }

@@@ index

* [overview](overview.md)
* [routes](routes.md)
* [directives/index](directives/index.md)
* [rejections](rejections.md)
* [exception-handling](exception-handling.md)
* [path-matchers](path-matchers.md)
* [case-class-extraction](case-class-extraction.md)
* [source-streaming-support](source-streaming-support.md)
* [testkit](testkit.md)
* [http-app](HttpApp.md)

@@@

## Minimal Example
**小型示例**

This is a complete, very basic Akka HTTP application relying on the Routing DSL:

这是一个完整的、最基本的依靠路由 DSL 的 Akka HTTP 应用程序：

Scala
:  @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #minimal-routing-example }

Java
:  @@snip [HttpServerMinimalExampleTest.java]($test$/java/docs/http/javadsl/HttpServerMinimalExampleTest.java) { #minimal-routing-example }

It starts an HTTP Server on localhost and replies to GET requests to `/hello` with a simple response.

它在本地启动一个 HTTP 服务器，并使用简单响应回复到 `/hello` 的 GET 请求。

@@@ warning { title="API may change" }
The following example uses an experimental feature and its API is subjected to change in future releases of Akka HTTP.
For further information about this marker, see @extref:[The @DoNotInherit and @ApiMayChange markers](akka-docs:common/binary-compatibility-rules.html#the-donotinherit-and-apimaychange-markers)
in the Akka documentation.

下面示例使用实验特性，它的 API 在未来 Akka HTTP 发布时随时变化。
有关此标记的更多信息，见 Akka 文档里的 @extref:[@DoNotInherit 和 @ApiMayChange 标记](akka-docs:common/binary-compatibility-rules.html#the-donotinherit-and-apimaychange-markers) 。
@@@

To help start a server Akka HTTP provides an experimental helper class called @apidoc[HttpApp].
This is the same example as before rewritten using @apidoc[HttpApp]:

为了帮助启动一个服务，Akka HTTP 提供一个实验性辅助类 @apidoc[HttpApp] 。
这是使用 @apidoc[HttpApp] 重写之前的相同示例：

Scala
:  @@snip [HttpAppExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpAppExampleSpec.scala) { #minimal-routing-example }

Java
:  @@snip [HttpAppExampleTest.java]($test$/java/docs/http/javadsl/server/HttpAppExampleTest.java) { #minimal-routing-example }

See @ref[HttpApp Bootstrap](HttpApp.md) for more details about setting up a server using this approach.

有关使用这个方式设置服务器的更多详细信息，见 @ref[HttpApp 引导程序](HttpApp.md) 。

@@@ div { .group-scala }

## Longer Example
**较长的示例**

The following is an Akka HTTP route definition that tries to show off a few features. The resulting service does
not really do anything useful but its definition should give you a feel for what an actual API definition with
the Routing DSL will look like:

下面是一个 Akka HTTP 路由定义，试图展示一些功能。服务实际上没有做任何有用的事，但是，它的定义应该让你了解了路由 DSL 的实际 API 定义看起来像：

@@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #long-routing-example }

@@@

## Interaction with Akka Typed
**与 Akka Typed 的交互**

Since Akka version `2.5.22`, Akka typed became ready for production, Akka HTTP, however, is still using the
untyped `ActorSystem`. This following example will demonstrate how to use Akka HTTP and Akka Typed together
within the same application.

从 Akka `2.5.22` 版本开始，Akka typed 可用于生产，但是，Akka HTTP 将继续使用无类型的 `ActorSystem`。
下面的例子将演示在同一个程序里怎样一起使用 Akka HTTP 和 Akka Typed。

We will create a small web server responsible to record build jobs with its state and duration, query jobs by
id and status, and clear the job history.

我们将创建一个小型 Web 服务器，服务器负责记录使用状态和持续时间的构建作业、通过 id 和状态查询作业、和清理作业历史。

First let's start by defining the `Behavior` that will act as a repository for the build job information, this isn't 
strictly needed for our sample but just to have an actual actor to interact with:

首先让我们开始于通过定义的 `Behavior`，它将作为构建作业信息的存储库，对于我们的示例这不是必须的，仅仅是为了有一个实际的 actor 与之交互：

Scala
:  @@snip [HttpServerWithTypedSample.scala]($test$/scala-2.12+/docs/http/scaladsl/HttpServerWithTypedSample.scala) { #akka-typed-behavior }


Then, let's define the JSON marshaller and unmarshallers for the HTTP routes:

然后，让我们定义 HTTP 路由的 JSON 编组和解组：

Scala
:  @@snip [HttpServerWithTypedSample.scala]($test$/scala-2.12+/docs/http/scaladsl/HttpServerWithTypedSample.scala) { #akka-typed-json }


Next step is to define the @apidoc[Route$] that will communicate with the previously defined behavior
and handle all its possible responses:

下一步是定义 @apidoc[Route$]，它将与之前定义的行为通信并处理所有可能的响应：

Scala
:  @@snip [HttpServerWithTypedSample.scala]($test$/scala-2.12+/docs/http/scaladsl/HttpServerWithTypedSample.scala) { #akka-typed-route }


Finally, we create a `Behavior` that bootstraps the web server and use it as the root behavior of our actor system:

最后，我们创建一个 `Behavior` 来引导 Web 服务器，并使用它作为我们 actor 系统的根行为：

Scala
:  @@snip [HttpServerWithTypedSample.scala]($test$/scala-2.12+/docs/http/scaladsl/HttpServerWithTypedSample.scala) { #akka-typed-bootstrap }


Note that the `akka.actor.typed.ActorSystem` is converted with `toClassic`, which comes from
`import akka.actor.typed.scaladsl.adapter._`. If you are using an earlier version than Akka 2.5.26 this conversion method is named `toUntyped`.

注意，`akka.actor.typed.ActorSystem` 是用 `toClassic` 转换，它来自 `import akka.actor.typed.scaladsl.adapter._`。
如果你使用比 Akka 2.5.26 更早的版本，则这个转换方法称为 `toUntyped`。

## Dynamic Routing Example
**动态路由示例**

As the routes are evaluated for each request, it is possible to make changes at runtime. Please note that every access
may happen on a separated thread, so any shared mutable state must be thread safe.

在为每个请求评估路由时，可以在运行时进行改变。请注意，因为每次访问都可能发生在单独的线程，所以任何共享可变状态都必须是线程安全的。

The following is an Akka HTTP route definition that allows dynamically adding new or updating mock endpoints with
associated request-response pairs at runtime.

下面是一个 Akka HTTP 路由定义，它允许在运行时动态添加带有关联请求-响应对的新的或更新模拟端点。

Scala
:  @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #dynamic-routing-example }

Java
:  @@snip [HttpServerDynamicRoutingExampleTest.java]($test$/java/docs/http/javadsl/HttpServerDynamicRoutingExampleTest.java) { #dynamic-routing-example }

For example, let's say we do a POST request with body:

例如，我们对正文执行一个 POST 请求。

```json
{
  "path": "test",
  "requests": [
    {"id": 1},
    {"id": 2}
  ],
  "responses": [
    {"amount": 1000},
    {"amount": 2000}
  ]
}
```

Subsequent POST request to `/test` with body `{"id": 1}` will be responded with `{"amount": 1000}`.

对正文 `{"id": 1}` 到 `/test` 的后续 POST 请求将以 `{"amount": 1000]` 响应。

## Handling HTTP Server failures in the High-Level API
**在高级 API 里处理 HTTP 服务器失败**

There are various situations when failure may occur while initialising or running an Akka HTTP server.
Akka by default will log all these failures, however sometimes one may want to react to failures in addition
to them just being logged, for example by shutting down the actor system, or notifying some external monitoring
end-point explicitly.

当初始化和运行 Akka HTTP 服务器时，各种失败都可能发行。Akka 默认将记录所有失败，但是，有时也想在记录日志这外对失败作出反应, 
比如关闭 actor 系统，或者显示的通知外部监视器端点。

### Bind failures

For example the server might be unable to bind to the given port. For example when the port
is already taken by another application, or if the port is privileged (i.e. only usable by `root`).
In this case the "binding future" will fail immediately, and we can react to it by listening on the @scala[`Future`]@java[`CompletionStage`]'s completion:

例如服务器可能无法绑定以指定的端口。比如当端口已经用于另一个应用程序，或者端口是特权端口（例如：只能 `root` 使用）。
这种情况下，“绑定 Future”会立即失败，我们可以通过监听 @scala[Future]@java[CompletionStage] 的完成作出反应：

Scala
:  @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #binding-failure-high-level-example }

Java
:  @@snip [HighLevelServerBindFailureExample.java]($test$/java/docs/http/javadsl/server/HighLevelServerBindFailureExample.java) { #binding-failure-high-level-example }

@@@ note
For a more low-level overview of the kinds of failures that can happen and also more fine-grained control over them
refer to the @ref[Handling HTTP Server failures in the Low-Level API](../server-side/low-level-api.md#handling-http-server-failures-low-level) documentation.

有关可能发生失败的更低级概述，以及对它们的更精细控制，请参考 @ref[在低级 API 里处理 HTTP 服务器错误](../server-side/low-level-api.md#handling-http-server-failures-low-level) 文档。
@@@

### Failures and exceptions inside the Routing DSL
**路由 DSL 内部的失败和异常**

Exception handling within the Routing DSL is done by providing @apidoc[ExceptionHandler] s which are documented in-depth
in the @ref[Exception Handling](exception-handling.md) section of the documentation. You can use them to transform exceptions into
@apidoc[HttpResponse] s with appropriate error codes and human-readable failure descriptions.

路由 DSL 中的异常处理是通过提供 @apidoc[ExceptionHandler] 来完成的，在文档的 @ref[Exception Handling](exception-handling.md) 部分对此进入了深入介绍。 
你可以使用 @apidoc[ExceptionHandler] 以合适的错误代码和人类可读的失败描述来转换异常为 @apidoc[HttpResponse] 。

## File uploads
**文件上传**

For high level directives to handle uploads see the @ref[FileUploadDirectives](directives/file-upload-directives/index.md).

用于处理上传的高级指令见 @ref[FileUploadDirectives](directives/file-upload-directives/index.md) 。

Handling a simple file upload from for example a browser form with a *file* input can be done
by accepting a *Multipart.FormData* entity, note that the body parts are *Source* rather than
all available right away, and so is the individual body part payload so you will need to consume
those streams both for the file and for the form fields.

处理简单文件上传（form）表单，例如带有一个 *file* 输入框的浏览器（form）表单可以通过接受 *Multipart.FormData* 实体来完成。
注意，正文部分是 *Source* 而不是立即可用，因此，单个正文部分载荷也是如此，所以你需要同时对文件和表单字段消费这些流。

Here is a simple example which just dumps the uploaded file into a temporary file on disk, collects
some form fields and saves an entry to a fictive database:

这里是一个简单的示例，只是转储上传文件到磁盘上的临时文件里面，收集一些表单字段，并保存条目到一个虚构的数据库：

Scala
:  @@snip [FileUploadExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/FileUploadExamplesSpec.scala) { #simple-upload }

Java
:  @@snip [FileUploadExamplesTest.java]($test$/java/docs/http/javadsl/server/FileUploadExamplesTest.java) { #simple-upload }

You can transform the uploaded files as they arrive rather than storing them in a temporary file as
in the previous example. In this example we accept any number of `.csv` files, parse those into lines
and split each line before we send it to an actor for further processing:

你可以在上传文件到达时转换，而不是上前面例子里存储到一个临时文件里。在这个例子里，我们接受任意数量的 `.csv` 文件，
将其按行解析并分割每一行，然后发送到 actor 里进行进一步处理。

Scala
:  @@snip [FileUploadExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/FileUploadExamplesSpec.scala) { #stream-csv-upload }

Java
:  @@snip [FileUploadExamplesTest.java]($test$/java/docs/http/javadsl/server/FileUploadExamplesTest.java) { #stream-csv-upload }

## Configuring Server-side HTTPS
**配置服务器端 HTTPS**

For detailed documentation about configuring and using HTTPS on the server-side refer to @ref[Server-Side HTTPS Support](../server-side/server-https-support.md).

有关在服务器端配置和使用 HTTPS 的详细文档，参考 @ref[服务器端 HTTPS 支持](../server-side/server-https-support.md) 。
