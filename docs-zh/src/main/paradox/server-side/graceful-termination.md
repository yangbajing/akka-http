# 优雅的终止
*Graceful termination*

## Graceful termination using `ServerTerminator`
**使用 `ServerTerminator` 来优雅的终止**

Akka HTTP provides two APIs to "stop" the server, either of them are available via the
@java[@javadoc[ServerBinding](akka.http.javadsl.ServerBinding)]
@scala[@scaladoc[ServerBinding](akka.http.scaladsl.Http$$ServerBinding)]
obtained from starting the server (by using any of the `bind...` methods on the
@java[@javadoc[Http](akka.http.javadsl.Http)]@scala[@scaladoc[Http](akka.http.scaladsl.HttpExt)] extension).

Akka HTTP 提供了两个 API 来“停止”服务器，其中任何一个都可以通过从启动服务器
（使用在 @java[@javadoc[Http](akka.http.javadsl.Http)]@scala[@scaladoc[Http](akka.http.scaladsl.HttpExt)] 扩展上的任何 `bind...` 方法）
获得的 @java[@javadoc[ServerBinding](akka.http.javadsl.ServerBinding)]@scala[@scaladoc[ServerBinding](akka.http.scaladsl.Http$$ServerBinding)] 得到。

The first method, called `unbind()` causes the server to *stop accepting new connections*, however any existing
connections that are still being used will remain active until the client chooses to close them.
It only unbinds the port on which the http server has been listening. This allows HTTP server to finish streaming any
responses that might be still in flight and eventually terminate the entire system. If your application uses long-lived
connections, this does mean that these can delay the termination of your system indefinitely.

第一个方法，调用 `unbind()` 导致服务器 *停止接受新的连接*，但是任何已存在连接将保持活动状态直到客户端选择关闭它们。
如果只是解绑HTTP 服务器兼听的端口。这允许 HTTP 服务器完成流式传输（任何还在运行的响应继续）并最终终止整个系统。
如果你的应用程序使用长-生命连接，这意味着这些可能无限期地延迟终止你的系统。

A better and more graceful solution to terminate an Akka HTTP server is to use the
@java[@javadoc[ServerBinding.terminate(Duration)](akka.http.javadsl.ServerBinding#terminate-java.time.Duration-)]
@scala[@scaladoc[ServerBinding.terminate(FiniteDuration)](akka.http.scaladsl.Http$$ServerBinding#terminate%28FiniteDuration%29:Future[HttpTerminated])]
method, which not only performs the unbinding, but also
handles replying to new incoming requests with (configurable) "terminating" HTTP responses.
It also allows setting a deadline after which any connections that are still alive will be shut down forcefully.
More precisely, termination works by following these steps:

终止 Akka HTTP 服务器的一个更好、更优雅的解决方案是使用 
@java[@javadoc[ServerBinding.terminate(Duration)](akka.http.javadsl.ServerBinding#terminate-java.time.Duration-)]
@scala[@scaladoc[ServerBinding.terminate(FiniteDuration)](akka.http.scaladsl.Http$$ServerBinding#terminate%28FiniteDuration%29:Future[HttpTerminated])] 方法，该方法不仅执行端口解绑，还处理回复，对新传入请求使用“terminating”（可配置）HTTP 响应。
它还允许设置一个最后期限，此后期限之后，任何还存活的连接都将被强制关闭。
更准确的说，终止通过下面这些步骤：

First, the server port is unbound and no new connections will be accepted (same as invoking `unbind()`).
Immediately the 
@java[@javadoc[ServerBinding#whenTerminationSignalIssued](akka.http.javadsl.ServerBinding#whenTerminationSignalIssued--) `CompletionStage`]
@scala[@scaladoc[ServerBinding#whenTerminationSignalIssued](akka.http.scaladsl.Http$$ServerBinding#whenTerminationSignalIssued:Future[Deadline]) `Future`]
is completed.
This can be used to signal parts of the application that the HTTP server is shutting down and they should clean up as well.
Note also that for more advanced shut down scenarios you may want to use the @extref[Coordinated Shutdown](akka-docs:/actors.html#coordinated-shutdown) capabilities of Akka.

第一，服务器端口被解绑，并且没有新的连接被接受（同调用 `unbind()`）。
@java[@javadoc[ServerBinding#whenTerminationSignalIssued](akka.http.javadsl.ServerBinding#whenTerminationSignalIssued--) `CompletionStage`]
@scala[@scaladoc[ServerBinding#whenTerminationSignalIssued](akka.http.scaladsl.Http$$ServerBinding#whenTerminationSignalIssued:Future[Deadline]) `Future`] 完成时（解绑也立即完成）。
这可以用于通知应用程序的其它部分，HTTP 服务器正在关闭，它们也应该清理。
注意，对于更高级的关闭场景，你也许想要使用 Akka 的 @extref[协调关闭](akka-docs:/actors.html#coordinated-shutdown) 功能。 

Next, all in flight requests will be handled. If a request is "in-flight" (being handled by user code), it is given `hardDeadline` time to complete.

第二，所以飞行中（已接受的请求，但还未被处理）的请求将被处理。如果一个请求是“在-飞行中”（由用户代码处理），则给它一个“硬截止”时间来完成。

- if a connection has no "in-flight" request, it is terminated immediately  
- if user code emits a response within the timeout, then this response is sent to the client with a `Connection: close` header and connection is closed.
- if it is a streaming response, it is also mandated that it shall complete within the deadline, and if it does not
  the connection will be terminated regardless of status of the streaming response. This is because such response could be infinite,
  which could trap the server in a situation where it could not terminate if it were to wait for a response to "finish".
    - existing streaming responses must complete before the deadline as well.
      When the deadline is reached the connection will be terminated regardless of status of the streaming responses.
- if user code does not reply with a response within the deadline we produce a special @java[`akka.http.javadsl.settings.ServerSettings.getTerminationDeadlineExceededResponse`]@scala[`akka.http.scaladsl.settings.ServerSettings.terminationDeadlineExceededResponse`] 

- 如果一个连接没有“在-飞行中”的请求，它将被立即终止
- 如果用户代码在超时内发出一个响应，则该响应带一个 `Connection: close` 头发送到客户端后，连接被关闭
- 如果是一个流式响应，需在截止期限（deadline）内完成，如果没有，连接被终止，而不管流式响应的状态为何。这是因为这样的响应可能是无限的，
  这可能使服务器陷入这样一种情况，如果它一直等待响应“完成”，那么服务器将不能终止。
    - 在截止期限（deadline）内，存在的流式响应必须完成。当截止期限到达，连接被终止，而不管流式响应的状态为何。
- 如果用户代码在截止期限内没有回复响应，系统生成一个特定的 @java[`akka.http.javadsl.settings.ServerSettings.getTerminationDeadlineExceededResponse`]@scala[`akka.http.scaladsl.settings.ServerSettings.terminationDeadlineExceededResponse`] 响应。

HTTP response (e.g. `503 Service Unavailable`) with a `Connection: close` header and close connection.

HTTP 响应（例如：`503 Service Unavailable`）具有一个 `Connection: close` 头并关闭连接。

During that time incoming requests continue to be served. The existing connections will remain alive until the 
`hardDeadline` is exceeded, yet no new requests will be delivered to the user handler. All such drained responses will be replied to with an termination response (as explained in step 2).

在此期间，继续为传入请求提供服务。已存在的连接将保持活动状态到超过“硬截止期”（hardDealine）为止，但是没有新的请求被送达用户处理程序。
所以这些耗尽的响应都将以终止响应回复（如步骤2阐述的那样）。

Finally, all remaining alive connections are forcefully terminated once the `hardDeadline` is exceeded.
The `whenTerminated` (exposed by `ServerBinding`) @java[CompletionStage]@scala[future] is completed as well, so the
graceful termination (of the `ActorSystem` or entire JVM itself can be safely performed, as by then it is known that no
connections remain alive to this server).

最后，所有剩余活动连接在超过“硬截止期”时被强制终止。`whenTerminated`（由 `ServerBinding` 暴露）返回的 @java[CompletionStage]@scala[Future] 也完成，
既优雅终止（`ActorSystem` 或整个 JVM 自身都能够安全地执行（终止），因为知道没有到服务器的活动连接）。

Note that the termination response is configurable in `ServerSettings`, and by default is an `503 Service Unavailable`,
with an empty response entity.

注意，终止响应可在 `ServerSettings` 里配置，默认是以空响应实体返回 `503 Service Unavailable`。

Starting a graceful termination is as simple as invoking the terminate() method on the server binding:

启优雅终止简单就像在绑定服务器上调用 `terminate()` 方法一样简单。

Scala
:   @@snip [HttpServerExampleSpec.scala]($test$/scala/docs/http/scaladsl/HttpServerExampleSpec.scala) { #graceful-termination }

Java
:   @@snip [HttpServerExampleDocTest.java]($test$/java/docs/http/javadsl/server/HttpServerExampleDocTest.java) { #graceful-termination }

## Akka Coordinated Shutdown
**Akka 协调关闭**

@@@ note
  
  NOT IMPLEMENTED YET.
  
  没有实现。
  
  Coordinated shutdown support is not yet implemented in Akka HTTP; 
  The goal is for it to invoke the graceful termination process as described above automatically when shutdown is requested.
  See the issue [#1210](https://github.com/akka/akka-http/issues/1210) for more details.
  
  Akka HTTP 里的协调关闭支持还未实现；
  目标是，当请求被关闭时如上述描述般自动的调用优雅终止过程。
  更名详细信息见问题 [#1210](https://github.com/akka/akka-http/issues/1210)。

@@@

Coordinated shutdown is Akka's managed way of shutting down multiple modules / sub-systems (persistence, cluster, http etc)
in a predictable and ordered fashion. For example, in a typical Akka application you will want to stop accepting new HTTP connections, and then shut down the cluster etc. 

协调关闭是 Akka 以可预测和有序的方式关闭多个模块 / 子系统（持久化、集群、HTTP 等）的管理方式。
