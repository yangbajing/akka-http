# HttpRequest 和 HttpResponse
*HttpRequest and HttpResponse*

All 3 Akka HTTP Client API levels use the same basic model of @apidoc[HttpRequest] and @apidoc[HttpResponse].

所有 3 个 Akka HTTP 客户端 API 级别都使用相同的 @apidoc[HttpRequest] 和 @apidoc[HttpResponse] 基本模型。

## Creating requests
**创建请求**

You can create simple `GET` requests:

可以创建简单 `GET` 请求：

Scala
:  @@snip[HttpClientExampleSpec.scala](/docs/src/test/scala/docs/http/scaladsl/HttpClientExampleSpec.scala){ #create-simple-request }

Java
:  @@snip[ClientSingleRequestExample.java](/docs/src/test/java/docs/http/javadsl/ClientSingleRequestExample.java){ #create-simple-request }

Or more complicated ones, like this `POST`:

或者更复杂的请求，像这个 `POST`：

Scala
:  @@snip[HttpClientExampleSpec.scala](/docs/src/test/scala/docs/http/scaladsl/HttpClientExampleSpec.scala){ #create-post-request }

Java
:  @@snip[ClientSingleRequestExample.java](/docs/src/test/java/docs/http/javadsl/ClientSingleRequestExample.java){ #create-post-request }

See the API documentation of @apidoc[HttpRequest] for more information on how to customize your requests.

有关怎样自定义请求的更多信息，见 @apidoc[HttpRequest] 的 API 文档。

## Processing responses
**处理响应**

When you receive a response, you can use the @ref[Marshalling](../common/marshalling.md) API to convert the response entity into an object:

当收到一个响应，可以使用 @ref[编组](../common/marshalling.md) API 来将响应实体转换为一个对象：

Scala
:  @@snip[HttpClientExampleSpec.scala](/docs/src/test/scala/docs/http/scaladsl/HttpClientExampleSpec.scala){ #unmarshal-response-body }

Java
:  @@snip[ClientSingleRequestExample.java](/docs/src/test/java/docs/http/javadsl/ClientSingleRequestExample.java){ #unmarshal-response-body }
