# 服务器端 HTTP/2 (预览)
*Server-Side HTTP/2 (Preview)*

@@@ warning

Server-Side HTTP/2 support in akka-http is currently available as a preview.
This means it is ready to be evaluated, but the APIs and behavior are likely to change.

akka-http 的服务器端 HTTP/2 支持当前作为预览版可用。
这意味着已通过评估，但是 API 和行为可能会改变。

@@@

## Dependency
**依赖**

To use Akka HTTP2 Support, add the module to your project:

要使用 Akka HTTP2 支持，添加下面模块到你的项目：

@@dependency [sbt,Gradle,Maven] {
  group="com.typesafe.akka"
  artifact="akka-http2-support_$scala.binary.version$"
  version="$project.version$"
}

## Enable HTTP/2 support
**启用 HTTP/2 支持**

HTTP/2 can then be enabled through configuration:

HTTP/2 能够通过配置启用：

```
akka.http.server.preview.enable-http2 = on
```

## Use `bindAndHandleAsync` and HTTPS
**使用 `bindAndHandleAsync` 和 HTTPS**

HTTP/2 is primarily used over a secure connection (known as "over HTTPS" or "with TLS"), which also takes care of protocol negotiation and falling back to plain HTTPS when the client does not support HTTP/2.
See the @ref[HTTPS section](server-https-support.md) for how to set up HTTPS.

HTTP/2 主要用于安全连接（称为“HTTPS”或“使用 TLS”），Akka HTTP/2 还负责协议协商，当客户端不支持 HTTP/2 时回退到纯 HTTPS。有关怎样设置 HTTPS，见 @ref[HTTPS section](server-https-support.md) 。

You can use @scala[@scaladoc[Http().bindAndHandleAsync](akka.http.scaladsl.HttpExt)]@java[@javadoc[Http().get(system).bindAndHandleAsync()](akka.http.javadsl.HttpExt)] as long as you followed the above steps:

你可以使用 @scala[@scaladoc[Http().bindAndHandleAsync](akka.http.scaladsl.HttpExt)]@java[@javadoc[Http().get(system).bindAndHandleAsync()](akka.http.javadsl.HttpExt)] ，只要遵循以上步骤：

Scala
:   @@snip[Http2Spec.scala]($test$/scala/docs/http/scaladsl/Http2Spec.scala) { #bindAndHandleSecure }

Java
:   @@snip[Http2Test.java]($test$/java/docs/http/javadsl/Http2Test.java) { #bindAndHandleSecure }

Note that `bindAndHandle` currently does not support HTTP/2, you must use `bindAndHandleAsync`.

注意，`bindAndHandle` 当前不支持 HTTP/2，必须使用 `bindAndHandleAsync` 。

### HTTP/2 without HTTPS
**HTTP/2 不使用 HTTPS**

While un-encrypted connections are allowed by HTTP/2, this is [sometimes discouraged](https://http2.github.io/faq/#does-http2-require-encryption).

虽然 HTTP/2 允许使用非加密连接，但这是 [不推荐的](https://http2.github.io/faq/#does-http2-require-encryption) 。

There are 2 ways to implement un-encrypted HTTP/2 connections: by using the
[HTTP Upgrade mechanism](http://httpwg.org/specs/rfc7540.html#discover-http)
or by starting communication in HTTP/2 directly which requires the client to
have [Prior Knowledge](https://httpwg.org/specs/rfc7540.html#known-http) of
HTTP/2 support.

有2种方式实现非加密的 HTTP/2 连接：使用 [HTTP 升级机制](http://httpwg.org/specs/rfc7540.html#discover-http) ，或者，支持启动 HTTP/2 通信，这要求客户端有HTTP/2 支持的 [先验知识](https://httpwg.org/specs/rfc7540.html#known-http) 。

*译注：第2种方式可理解为需要客户端直接以 HTTP/2 协议发起请求*

We support both approaches transparently on the same port. This feature is automatically enabled when HTTP/2 is enabled:

我们在相同端口上透明地上面两种方式。当 HTTP/2 启用时这个特性被自动启用：

Scala
:   @@snip[Http2Spec.scala]($test$/scala/docs/http/scaladsl/Http2Spec.scala) { #bindAndHandlePlain }

Java
:   @@snip[Http2Test.java]($test$/java/docs/http/javadsl/Http2Test.java) { #bindAndHandlePlain }

#### h2c Upgrade
**h2c 升级**

*译注：h2c 指 HTTP/2 的明文版本，既不建立在 TLS 之上*

The advantage of switching from HTTP/1.1 to HTTP/2 using the
[HTTP Upgrade mechanism](http://httpwg.org/specs/rfc7540.html#discover-http)
is that both HTTP/1.1 and HTTP/2 clients can connect to the server on the
same port, without being aware beforehand which protocol the server supports.

使用 [HTTP 升级机制](http://httpwg.org/specs/rfc7540.html#discover-http) 切换 HTTP/1.1 到 HTTP/2 的优势是，在事先不知道服务器支持哪种协议的情况下，HTTP/1.1 和 HTTP/2 客户都可以连接到服务器的相同端口，

The disadvantage is that relatively few clients support switching to HTTP/2
in this way. Additionally, HTTP/2 communication cannot start until the first
request has been completely sent. This means if your first request may be
large, it might be worth it to start with an empty OPTIONS request to switch
to HTTP/2 before sending your first 'real' request, at the cost of a roundtrip.

劣势是相对较少的客户端支持这种方式切换到 HTTP/2。另外，直到第一个请求完成发送，HTTP/2 通信不能启动。这意味着，如果你的第一个请求很大，那么在发送你的第一个“真实”请求之前从一个空的 OPTIONS 请求开始，然后切换到 HTTP/2 是值得的，代价是多了一个往返请求。

#### h2c with prior knowledge
**具有先验知识的 h2c**

The other option is to connect and start communicating in HTTP/2 immediately.
The downside of this approach is the client must know beforehand that the
server supports HTTP/2.
For the reason this approach is known as h2c with
Prior Knowledge](http://httpwg.org/specs/rfc7540.html#known-http) of HTTP/2
support.

另一种选择是在 HTTP/2 中立即连接并启动通信。这种方法的缺点是客户端必须事先知道服务器支持 HTTP/2。由于这个原因，这种方式称为具有 HTTP/2 支持的 [先验知识](http://httpwg.org/specs/rfc7540.html#known-http) 的 h2c。

## Testing with cURL
**使用 cURL 进行测试**

At this point you should be able to connect, but HTTP/2 may still not be available.

此时你应用能够连接，但 HTTP/2 可能仍然不可用。

You'll need a recent version of [cURL](https://curl.haxx.se/) compiled with HTTP/2 support (for OSX see [this article](https://simonecarletti.com/blog/2016/01/http2-curl-macosx/)). You can check whether your version supports HTTP2 with `curl --version`, look for the nghttp2 extension and the HTTP2 feature:

你需要编译了 HTTP/2 支持的 [cURL](https://curl.haxx.se/) 的最近版本（对于 OSX 见 [这篇文章](https://simonecarletti.com/blog/2016/01/http2-curl-macosx/)）。你可以使用 `curl --version` 检测你的版本是否支持 HTTP2，寻找 nghttp2 扩展和 HTTP2 特性：

```
curl 7.52.1 (x86_64-pc-linux-gnu) libcurl/7.52.1 OpenSSL/1.0.2l zlib/1.2.8 libidn2/0.16 libpsl/0.17.0 (+libidn2/0.16) libssh2/1.8.0 nghttp2/1.23.1 librtmp/2.3
Protocols: dict file ftp ftps gopher http https imap imaps ldap ldaps pop3 pop3s rtmp rtsp scp sftp smb smbs smtp smtps telnet tftp
Features: AsynchDNS IDN IPv6 Largefile GSS-API Kerberos SPNEGO NTLM NTLM_WB SSL libz TLS-SRP HTTP2 UnixSockets HTTPS-proxy PSL
```

When you connect to your service you may now see something like:

当你连接到你的服务时，现在你可能看到这样的东西：

```
$ curl -k -v https://localhost:8443
(...)
* ALPN, offering h2
* ALPN, offering http/1.1
(...)
* ALPN, server accepted to use h2
(...)
> GET / HTTP/1.1
(...)
< HTTP/2 200
(...)
```

If your curl output looks like above, you have successfully configured HTTP/2. However, on JDKs up to version 9, it is likely to look like this instead:

如果你的 curl 输出类似上面，那代表你的服务器成功的配置了 HTTP/2。但是，在 JDK 9 之前，它的输出看起来是这样的：

```
$ curl -k -v https://localhost:8443
(...)
* ALPN, offering h2
* ALPN, offering http/1.1
(...)
* ALPN, server did not agree to a protocol
(...)
> GET / HTTP/1.1
(...)
< HTTP/1.1 200 OK
(...)
```

This shows `curl` declaring it is ready to speak `h2` (the shorthand name of HTTP/2), but could not determine whether the server is ready to, so it fell back to HTTP/1.1. To make this negotiation work you'll have to configure ALPN as described below.

这显示 `curl` 声明它已准备好讲 `h2`（HTTP/2 的简称），但无法决定服务器是否已准备好，所以回退到 HTTP/1.1。要使此协商工作，你需要按下面描述配置 ALPN。 

## Application-Layer Protocol Negotiation (ALPN)
**应用层协议协商（ALPN）**

[Application-Layer Protocol Negotiation (ALPN)](https://en.wikipedia.org/wiki/Application-Layer_Protocol_Negotiation) is used to negotiate whether both client and server support HTTP/2.

[应用层协议协商（ALPN）](https://en.wikipedia.org/wiki/Application-Layer_Protocol_Negotiation) 用于协商客户端和服务器是否都支持 HTTP/2。

ALPN support comes with the JVM starting from version 9. If you're on a previous version of the JVM, you'll have to load a Java Agent to provide this functionality. We recommend the agent from the [Jetty](http://www.eclipse.org/jetty/) project, `jetty-alpn-agent`.

从版本 9 开始，ALPN 支持进入 JVM。如果你使用 JVM 的之前版本，你需要加载一个 Java 代理提供这个功能。我们推荐来自 [Jetty](http://www.eclipse.org/jetty/) 项目的代理，`jetty-alpn-agent`。

### manually
**手动**

This agent can be loaded with the `-javaagent` JVM option:

该代理可以通过使用 `-javaagent` JVM 选项加载：

@@@vars
```
  java -javaagent:/path/to/jetty-alpn-agent-$alpn-agent.version$.jar -jar app.jar
```
@@@

### sbt

sbt can be configured to load the agent with the [sbt-javaagent plugin](https://github.com/sbt/sbt-javaagent):

sbt 可能配置 [sbt-javaagent 插件](https://github.com/sbt/sbt-javaagent) 加载代理：

@@@vars
```
  .enablePlugins(JavaAgent)
  .settings(
    javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "$alpn-agent.version$" % "runtime"
  )
```
@@@

This should automatically load the agent when running, testing, or even in distributions made with [sbt-native-package](https://github.com/sbt/sbt-native-packager).

这将在运行、测试时自动加载代理，甚至使用 [sbt-native-package](https://github.com/sbt/sbt-native-packager) 制作的发行版中也是如此。

@@@ div { .group-java}

### maven

To configure maven to load the agent when running `mvn exec:exec`, add it as a 'runtime' dependency:

要配置 maven 在运行 `mvn exec:exec` 时加载代理，添加它的 'runtime' 依赖范围：

@@@@vars
```
<dependency>
    <groupId>org.mortbay.jetty.alpn</groupId>
    <artifactId>jetty-alpn-agent</artifactId>
    <version>$alpn-agent.version$</version>
    <scope>runtime</scope>
</dependency>
```
@@@@

and use the `maven-dependency-plugin`:

并使用 `maven-dependency-plugin`：

```
<plugin>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>2.5.1</version>
    <executions>
        <execution>
            <id>getClasspathFilenames</id>
            <goals>
                <goal>properties</goal>
            </goals>
        </execution>
     </executions>
</plugin>
```

to add it to the `exec-maven-plugin` arguments:

再添加 `exec-maven-plugin` 参数：

```
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <version>1.6.0</version>
    <configuration>
        <executable>java</executable>
        <arguments>
            <argument>-javaagent:${org.mortbay.jetty.alpn:jetty-alpn-agent:jar}</argument>
            <argument>-classpath</argument>
            <classpath />
            <argument>com.example.HttpServer</argument>
        </arguments>
    </configuration>
</plugin>
```

@@@
