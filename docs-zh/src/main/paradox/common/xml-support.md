# XML 支持
*XML Support*

Akka HTTP's @ref[marshalling](marshalling.md) and @ref[unmarshalling](unmarshalling.md)
infrastructure makes it rather easy to seamlessly support specific wire representations of your data objects, like JSON,
XML or even binary encodings.

Akka HTTP 的 @ref[编组](marshalling.md) 和 @ref[解组](unmarshalling.md) 基础设施使得无缝支持对象的线上表示变得相当容易，例如：JSON、XML或二进制编码。 

@@@ div { .group-java }

Akka HTTP does not currently provide a Java API for XML support. If you need to
produce and consume XML, you can write a @ref[custom marshaller](marshalling.md#custom-marshallers)
using [Jackson], which is also the library used for providing @ref[JSON support](json-support.md#jackson-support).

@@ snip [#jackson-xml-support] ($root$/src/test/java/docs/http/javadsl/JacksonXmlSupport.java) { #jackson-xml-support }

The custom XML (un)marshalling code shown above requires that you depend on the `jackson-dataformat-xml` library.

@@dependency [sbt,Gradle,Maven] {
  group="com.fasterxml.jackson.dataformat"
  artifact="jackson-dataformat-xml"
  version="$jackson.version$"
}

@@@

@@@ div { .group-scala }

For XML Akka HTTP currently provides support for [Scala XML][scala-xml] right out of the box through it's
`akka-http-xml` module.

对于 XML，Akka HTTP 当前通过 `akka-http-xml` 模块对 [Scala XML][scala-xml] 提供了开箱即用的支持 。

## Scala XML Support
**Scala XML 支持**

The @scaladoc[ScalaXmlSupport](akka.http.scaladsl.marshallers.xml.ScalaXmlSupport) trait provides a `FromEntityUnmarshaller[NodeSeq]` and `ToEntityMarshaller[NodeSeq]` that
you can use directly or build upon.

@scaladoc[ScalaXmlSupport](akka.http.scaladsl.marshallers.xml.ScalaXmlSupport) trait 提供了 `FromEntityUnmarshaller[NodeSeq]` 和 `ToEntityMarshaller[NodeSeq]`，你可以直接使用或者在它基础之上构建自己的。

In order to enable support for (un)marshalling from and to XML with [Scala XML][scala-xml] `NodeSeq` you must add
the following dependency:

为了启用 XML 和 [Scala XML][scala-xml] `NodeSeq` 的(解)编组支持，你必须添加下面的依赖：

@@dependency [sbt,Gradle,Maven] {
  group="com.typesafe.akka"
  artifact="akka-http-xml_$scala.binary.version$"
  version="$project.version$"
}

Once you have done this (un)marshalling between XML and `NodeSeq` instances should work nicely and transparently,
by either using `import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._` or mixing in the
`akka.http.scaladsl.marshallers.xml.ScalaXmlSupport` trait.

一旦你完成 XML 和 `NodeSeq` 实例之间的(解)编组功能，它就可以很好的、透明的工作。
使用 `import akka.http.scaladsl.marshallers.xml.ScalaXmlSupport._` （导入隐式转换）或者混入 `akka.http.scaladsl.marshallers.xml.ScalaXmlSupport` trait。

@@@

 [scala-xml]: https://github.com/scala/scala-xml
 [jackson]: https://github.com/FasterXML/jackson
