# HttpApp 引导程序
*HttpApp Bootstrap*

@@@ warning { title="API may change" }
This is experimental and the API is subjected to change in future releases of Akka HTTP.
For further information about this marker, see @extref:[The @DoNotInherit and @ApiMayChange markers](akka-docs:common/binary-compatibility-rules.html#the-donotinherit-and-apimaychange-markers)
in the Akka documentation.

这是实验性的并且在未来的 Akka HTTP 发布里随时变化。有关该标记的进一步信息，见 Akka 文档里的 @extref:[@DoNotInherit 和 @ApiMayChange 标记](akka-docs:common/binary-compatibility-rules.html#the-donotinherit-and-apimaychange-markers) 。
@@@

@@toc { depth=1 }

## Introduction
**导读**

The objective of @apidoc[HttpApp] is to help you start an HTTP server with just a few lines of code.
This is accomplished just by extending @apidoc[HttpApp] and implementing the `routes()` method.
If desired, @apidoc[HttpApp] provides different hook methods that can be overridden to change its default behavior.

@apidoc[HttpApp] 的目的是帮助你仅用几行代码行来启动一个 HTTP 服务器。通过扩展 @apidoc[HttpApp] 并实例 `routes` 方法来完成。
如有需要， @apidoc[HttpApp] 提供了不同的挂勾方法，可以将其重写以改变默认行为。

Please note that @apidoc[HttpApp] is not the canonical way of starting an akka-http server. It is a tool to get up and running fast. For integrating into already existing Akka applications that already bring their @apidoc[akka.actor.ActorSystem] using `Http.bindAndHandle` (which is also just a single line of code) is recommended over using @apidoc[HttpApp].

请注意， @apidoc[HttpApp] 不是启动 akka-http 服务器的权威方法。它是一种快速启动并运行的工具。
为了集成到已经提供了 @apidoc[akka.actor.ActorSystem] 的现有 Akka 应用程序中，建议使用 `Http.bindAndHandle` （它也只有一行代码），而不使用 @apidoc[HttpApp] 。  

## Minimal Example
**小的示例**

The following example shows how to start a server:

下面例子显示怎样启动一个服务器：

Scala
:   @@snip [HttpAppExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpAppExampleSpec.scala) { #minimal-routing-example }

Java
:   @@snip [HttpAppExampleTest.java]($test$/java/docs/http/javadsl/server/HttpAppExampleTest.java) { #minimal-imports #minimal-routing-example }

Firstly we define @scala[an `object` (it can also be a `class`)]@java[a `class`] that extends @apidoc[HttpApp] and we just implement the routes this server will handle.
After that, we can start a server just by providing a `host` and a `port`. Calling `startServer` blocks the current thread until the server is signaled for termination.
The default behavior of @apidoc[HttpApp] is to start a server, and shut it down after `ENTER` is pressed. When the call to `startServer` returns the server is properly shut down.

首先，我们定义 @scala[一个 `object` (也可以使用 `class`)]@java[一个 `class`] 扩展 @apidoc[HttpApp] 并实现这个服务器将处理的路由。
然后，我们通过提供 `host` 和 `port` 启动服务器。调用 `startServer` 阻塞当前线程直接服务器发出终止信息。
@apidoc[HttpApp] 的默认行为是启动一个服务器，然后按 `ENTER` 后将其关闭。当对 `startServer` 的调用返回时，服务器已正确关闭。

## Reacting to Bind Failures
**应对绑定失败**

@apidoc[HttpApp] provides different hooks that will be called after a successful and unsuccessful initialization. For example, the server
might not start due to the port being already in use, or because it is a privileged one.

当初始化成功或失败后， @apidoc[HttpApp] 提供了不同的挂勾被调用。例如：由于端口已被占用导致服务器无法启动或者该端口是一个特权端口。

Here you can see an example server that overrides the `postHttpBindingFailure` hook and prints the error to the console (this is also the default behavior)

这里你可以看到一个示例服务器，重写了 `postHttpBindingFailure` 钩子并打印错误到控制台（这也是默认行为）。

Scala
:   @@snip [HttpAppExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpAppExampleSpec.scala) { #failed-binding-example }

Java
:   @@snip [HttpAppExampleTest.java]($test$/java/docs/http/javadsl/server/HttpAppExampleTest.java) { #imports #bindingError }

So if the port `80` would be already taken by another app, the call to `startServer` returns immediately and the `postHttpBindingFailure` hook will be called.

如果端口 `80` 已经被其它应用占用，调用 `startServer` 立即返回并且 `postHttpBindingFailure` 钩子将被调用。

## Providing your own Server Settings
**提供你自己的服务器设置**

@apidoc[HttpApp] reads the default @apidoc[ServerSettings] when one is not provided.
In case you want to provide different settings, you can simply pass it to `startServer` as illustrated in the following example:

当没有提供 @apidoc[ServerSettings] 时， @apidoc[HttpApp] 读取默认设置。你希望提供不同的设置，可以如例所示简单传递配置到 `startServer`： 

Scala
:   @@snip [HttpAppExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpAppExampleSpec.scala) { #with-settings-routing-example }

Java
:   @@snip [HttpAppExampleTest.java]($test$/java/docs/http/javadsl/server/HttpAppExampleTest.java) { #imports #with-settings-routing-example }

## Providing your own Actor System
**提供你自己的 Actor 系统**

@apidoc[HttpApp] creates its own @apidoc[akka.actor.ActorSystem] instance when one is not provided.
In case you already created an @apidoc[akka.actor.ActorSystem] in your application you can
pass it to `startServer` as illustrated in the following example:

当没有提供 `ActorSystem` 时， @apidoc[HttpApp] 创建它自己的 @apidoc[akka.actor.ActorSystem] 实例

Scala
:   @@snip [HttpAppExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpAppExampleSpec.scala) { #with-actor-system }

Java
:   @@snip [HttpAppExampleTest.java]($test$/java/docs/http/javadsl/server/HttpAppExampleTest.java) { #minimal-imports #ownActorSystem }

When you provide your own @apidoc[akka.actor.ActorSystem] you are responsible for terminating it. For more fine-grained control over the shutdown of various parts of the application, take a look at @scala[@extref[Coordinated Shutdown](akka25-docs:scala/actors.html#coordinated-shutdown)]@java[@extref[Coordinated Shutdown](akka25-docs:java/actors.html#coordinated-shutdown)] extension which is available since Akka 2.5.0.

当你提供自己的 @apidoc[akka.actor.ActorSystem] 时你有责任终止它。要更细粒度的控制应用程序各个部分的关闭，请查看从 Akka 2.5.0 开始可用的
[@extref[协调关闭](akka25-docs:scala/actors.html#coordinated-shutdown)]@java[@extref[协调关闭](akka25-docs:java/actors.html#coordinated-shutdown)] 扩展。

## Providing your own Actor System and Settings
**提供你自己的 Actor 系统和设置**

@apidoc[HttpApp] offers another overloaded `startServer` method where you can pass, on top of the `host` and `port`,
your previously created @apidoc[akka.actor.ActorSystem] and your custom @apidoc[ServerSettings].
You can see an example in the following code snippet:

@apidoc[HttpApp] 提供另一个重载的 `startServer` 方法，你可以在 `host` 和 `port` 之上传递先前创建的 @apidoc[akka.actor.ActorSystem] 和 自定义 @apidoc[ServerSettings] 。
你可以在下面代码片段中看到示例：

Scala
:   @@snip [HttpAppExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpAppExampleSpec.scala) { #with-actor-system-settings }

Java
:   @@snip [HttpAppExampleTest.java]($test$/java/docs/http/javadsl/server/HttpAppExampleTest.java) { #imports #ownActorSystemAndSettings }

## Overriding Termination Signal
**覆盖终止信号**

As already described previously, the default trigger that shuts down the server is pressing `ENTER`.
For simple examples this is sufficient, but for bigger applications this is, most probably, not what you want to do.
@apidoc[HttpApp] can be configured to signal the server termination just by overriding the method `waitForShutdownSignal`.
This method must return a @scala[`Future`]@java[`CompletionStage`] that, when terminated, will shutdown the server.

如前所述，关闭服务器的默认触发器是按 `ENTER`。对于简单的示例，这已足够，但是对于较大的应用程序，这很可能不是想要执行的操作。
可以将 @apidoc[HttpApp] 配置为仅通过覆盖方法 `waitForShutdownSignal` 来发出服务器终止信号。
此方法必须返回一个 @scala[`Future`]@java[`CompletionStage`] ，当 `Future` 终止时，将关闭服务器。

This following example shows how to override the default termination signal:

下面例子显示怎样覆盖默认终止信号：

Scala
:   @@snip [HttpAppExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpAppExampleSpec.scala) { #override-termination-signal }

Java
:   @@snip [HttpAppExampleTest.java]($test$/java/docs/http/javadsl/server/HttpAppExampleTest.java) { #imports #selfClosing #serverTerminationSignal }

Here the termination signal is defined by a future that will be automatically completed after 5 seconds. 

这里终止信号由一个 future 定义，它将在 5 秒后自动完成。

## Getting Notified on Server Shutdown
**服务器关闭时得到通知**

There are some cases in which you might want to clean up any resources you were using in your server. In order to do this
in a coordinated way, you can override @apidoc[HttpApp]'s `postServerShutdown` method.

在某些情况下，你可能希望清理服务器中正在使用的所有资源。为了以协调的方式进行，可以覆盖 @apidoc[HttpApp] 的 `postServerShutdown` 方法。

Here you can find an example:

你可以在这里找到示例：

Scala
:   @@snip [HttpAppExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpAppExampleSpec.scala) { #cleanup-after-shutdown }

Java
:   @@snip [HttpAppExampleTest.java]($test$/java/docs/http/javadsl/server/HttpAppExampleTest.java) { #imports #postShutdown }

The `postServerShutdown` method will be only called once the server attempt to shutdown has completed. Please notice that in
the @scala[case that `unbind` fails to stop the server, this method will also be called with a failed `Try`.]@java[exception that this method is called with, may be null. It will be a non-null one only when `unbind` fails to stop the server.]

只在服务器尝试关闭完成后才调用 `postServerShutdown` 方法。请注意， 
@scala[在 `unbind` 无法停止服务器的情况下，该方法也将使用一个错误的 `Try` 调用。]
@java[当 `unbind` 无法停止服务器时，`failure` 内部的 `Throwable` 为 `null`。] 
