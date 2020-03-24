# 兼容性指导方针

## Binary Compatibility Rules
**二进制兼容性规则**

Akka HTTP follows the same binary compatibility rules as Akka itself.
In short it means that the versioning scheme should be read as `major.minor.patch`（`主要版本.次级版本.补丁版本`）,
wherein all versions with the same `major` version are backwards binary-compatible,
with the exception of `@ApiMayChange`, `@InternalApi` or `@DoNotInherit` marked APIs 
or other specifically documented special-cases.

Akka HTTP 遵循与 Akka 本身相同的二进制兼容性规则。简而言之，这意味着版本性方案应该读作 `major.minor.path`，
所有版本里 `major` 相同的都向后二进制兼容，被标记为 `@ApiMayChange`、`@InternalApi` 或 `@DoNotInherit` 的 API
或者其它被专门记录的特殊情况除外。

For more information and a detailed discussion of these rules and guarantees please refer to
@extref:[The @DoNotInherit and @ApiMayChange markers](akka-docs:common/binary-compatibility-rules.html#the-donotinherit-and-apimaychange-markers).

有关这些规则和保证的更多信息和详细建议请参考 @extref:[The @DoNotInherit and @ApiMayChange markers](akka-docs:common/binary-compatibility-rules.html#the-donotinherit-and-apimaychange-markers).

### Components with no Binary Compatibility Guarantee
**没有二进制兼容性保证的组件**

The following components and modules don't have the previously mentioned binary compatibility guaranteed within minor or
patch versions. However, binary compatibility will attempted to be kept as much as possible.

下列组件和模块在次级版本或补丁版本中没有前面提到的二进制兼容性保证。但是，将尝试尽可能的提供二进制兼容性。

#### akka-http

Scala
:   ```scala
    akka.http.scaladsl.server.directives.FileUploadDirectives#storeUploadedFile
    akka.http.scaladsl.server.directives.FileUploadDirectives#storeUploadedFiles
    akka.http.scaladsl.server.directives.FileUploadDirectives#fileUploadAll
    akka.http.scaladsl.marshalling.sse.EventStreamMarshalling
    akka.http.scaladsl.server.HttpApp
    akka.http.scaladsl.unmarshalling.sse.EventStreamParser
    akka.http.scaladsl.unmarshalling.sse.EventStreamUnmarshalling
    ```

Java
:   ```java
    akka.http.javadsl.common.PartialApplication#bindParameter
    akka.http.javadsl.server.Directives#anyOf (all overloads)
    akka.http.javadsl.server.Directives#allOf (all overloads)
    akka.http.javadsl.server.directives.FileUploadDirectives#storeUploadedFile
    akka.http.javadsl.server.directives.FileUploadDirectives#storeUploadedFiles
    akka.http.javadsl.server.directives.FileUploadDirectives#fileUploadAll
    akka.http.javadsl.server.HttpApp
    ```    

#### akka-http-caching

Scala
:   ```scala
    akka.http.caching.LfuCache
    akka.http.caching.scaladsl.Cache
    akka.http.scaladsl.server.directives.CachingDirectives
    ```

Java
:   ```java
    akka.http.caching.LfuCache
    akka.http.caching.javadsl.Cache
    akka.http.javadsl.server.directives.CachingDirectives
    ```    

#### akka-http-core

Scala
:   ```scala
    akka.http.scaladsl.ClientTransport
    akka.http.scaladsl.settings.PoolImplementation
    akka.http.scaladsl.settings.ClientConnectionSettings#transport
    akka.http.scaladsl.settings.ClientConnectionSettings#withTransport
    akka.http.scaladsl.settings.ConnectionPoolSettings#poolImplementation
    akka.http.scaladsl.settings.ConnectionPoolSettings#responseEntitySubscriptionTimeout
    akka.http.scaladsl.settings.ConnectionPoolSettings#withPoolImplementation
    akka.http.scaladsl.settings.ConnectionPoolSettings#withResponseEntitySubscriptionTimeout
    akka.http.scaladsl.settings.Http2ServerSettings
    akka.http.scaladsl.settings.PreviewServerSettings
    akka.http.scaladsl.settings.ServerSentEventSettings
    akka.http.scaladsl.model.headers.CacheDirectives.immutableDirective
    akka.http.scaladsl.model.headers.X-Forwarded-Host
    akka.http.scaladsl.model.headers.X-Forwarded-Proto
    ```

Java
:   ```java
    akka.http.javadsl.ClientTransport
    akka.http.javadsl.settings.ClientConnectionSettings#getTransport
    akka.http.javadsl.settings.ClientConnectionSettings#withTransport
    akka.http.javadsl.settings.ConnectionPoolSettings#getPoolImplementation
    akka.http.javadsl.settings.ConnectionPoolSettings#getResponseEntitySubscriptionTimeout
    akka.http.javadsl.settings.ConnectionPoolSettings#withPoolImplementation
    akka.http.javadsl.settings.ConnectionPoolSettings#withResponseEntitySubscriptionTimeout
    akka.http.javadsl.settings.PoolImplementation
    akka.http.javadsl.settings.PreviewServerSettings
    akka.http.javadsl.settings.ServerSentEventSettings
    ```
  
## Versioning and Compatibility
**版本和兼容性**

Starting from version 10.1.0, there will be two active release branches:
- The "current" release line (in master), where we will basically continue to evolve Akka HTTP in the same way as currently. New features will introduced here incrementally.
- The "previous" release line (in a release-10.x branch), where the focus is on stability. We will continue to maintain the previous release by fixing serious bugs but it will not see new features. Previous releases will see less frequent releases over time.

从版本 10.1.0 开始，将有两个活跃发布分支：
- “当前”发布线（在 master 分支），基本上我们将以相同的方式继续发展 Akka HTTP。这里将逐步引入新功能。
- “先前”发布线（在 release-10.x 分支），关注稳定性。我们将通过修复严重 bug 来继续维护以前版本，但不会看到新功能。随着时间的推移，以前的发布将不再频繁发布。

It is planned to rotate versions in an annual fashion. Meaning a new minor version will be created every year.
Whenever a new minor version is created, it will move the at that point current minor version release series over into maintenance mode, making it the "previous".
The former "previous" release has reached its end of life at this point. This way every release line is supported for two years.

计划以每年的方式滚动版本。意味着每年都将创建一个新的次级版本。每当一个新的次级版本被创建，该版本就会将当前的次级版本发行系列移到维护模式，使其成为“先前”版本。
在此，以前的“先前”版本已到使用寿命。这样，每个发布线都支持两年。

The Akka HTTP Team currently does not intend to break binary compatibility, i.e. no binary incompatible 11.x.y release is currently planned.

Akka HTTP 团队当前不打算破坏二进制兼容性，即，当前没有计划二进制不兼容的 11.x.y 发布。
    
## Specific versions inter-op discussion
**特定版本互操作讨论**

In this section we discuss some of the specific cases of compatibility between versions of Akka HTTP and Akka itself.

这个部分我们讨论 Akka HTTP 和 Akka 本身之间兼容性的一些特殊情况。

For example, you may be interested in those examples if you encountered the following exception in your system when upgrading parts 
of your libraries: `Detected java.lang.NoSuchMethodError error, which MAY be caused by incompatible Akka versions on the classpath. Please note that a given Akka version MUST be the same across all modules of Akka that you are using, e.g. if you use akka-actor [2.5.3 (resolved from current classpath)] all other core Akka modules MUST be of the same version. External projects like Alpakka, Persistence plugins or Akka HTTP etc. have their own version numbers - please make sure you're using a compatible set of libraries.`

例如，如果当你的系统在升级部分库时遇到以下异常，你也许对这些示例感兴趣：`Detected java.lang.NoSuchMethodError error, which MAY be caused by incompatible Akka versions on the classpath. Please note that a given Akka version MUST be the same across all modules of Akka that you are using, e.g. if you use akka-actor [2.5.3 (resolved from current classpath)] all other core Akka modules MUST be of the same version. External projects like Alpakka, Persistence plugins or Akka HTTP etc. have their own version numbers - please make sure you're using a compatible set of libraries.`

### Compatibility with Akka
**与 Akka 的兼容性**

Akka HTTP 10.1.x is (binary) compatible with Akka >= 2.5.11
and future Akka 2.x versions that are released during the lifetime of Akka HTTP 10.1.x.
To facilitate supporting multiple minor versions of Akka we do not depend on `akka-stream`
explicitly but mark it as a `provided` dependency in our build. That means that you will *always* have to add
a manual dependency to `akka-stream`.

Akka HTTP 10.1.x 与 Akka >= 2.5.11 和未来的 Akka 2.x 版本（二进制）兼容，这些版本在 Akka HTTP 10.1.x 的生存期内发布。 
为了促进支持多个次级版本号的 Akka，我们不显示依赖 `akka-stream`，我们在构建里标记它为 `provided` 依赖。那意味着你 *总是* 需要手动添加依赖 `akka-stream`。

sbt
:   @@@vars
    ```
    val akkaVersion = "$akka.version$"
    val akkaHttpVersion = "$project.version$"
    libraryDependencies += "com.typesafe.akka" %% "akka-http"   % akkaHttpVersion
    libraryDependencies += "com.typesafe.akka" %% "akka-actor"  % akkaVersion
    libraryDependencies += "com.typesafe.akka" %% "akka-stream" % akkaVersion
    // If testkit used, explicitly declare dependency on akka-streams-testkit in same version as akka-actor
    libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit"   % akkaHttpVersion % Test
    libraryDependencies += "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion     % Test
    ```
    @@@

Gradle
:   @@@vars
    ```
    compile group: 'com.typesafe.akka', name: 'akka-http_$scala.binary_version$',   version: '$project.version$'
    compile group: 'com.typesafe.akka', name: 'akka-actor_$scala.binary_version$',  version: '$akka.version$'
    compile group: 'com.typesafe.akka', name: 'akka-stream_$scala.binary_version$', version: '$akka.version$'
    // If testkit used, explicitly declare dependency on akka-streams-testkit in same version as akka-actor
    testCompile group: 'com.typesafe.akka', name: 'akka-http-testkit_$scala.binary_version$',   version: '$project.version$'
    testCompile group: 'com.typesafe.akka', name: 'akka-stream-testkit_$scala.binary_version$', version: '$akka.version$'
    ```
    @@@
    
Maven
:   @@@vars
    ```
    <dependency>
      <groupId>com.typesafe.akka</groupId>
      <artifactId>akka-actor_$scala.binary_version$</artifactId>
      <version>$akka.version$</version>
    </dependency>
    <!-- Explicitly depend on akka-streams in same version as akka-actor-->
    <dependency>
      <groupId>com.typesafe.akka</groupId>
      <artifactId>akka-stream_$scala.binary_version$</artifactId>
      <version>$akka.version$</version>
    </dependency>
    <dependency>
      <groupId>com.typesafe.akka</groupId>
      <artifactId>akka-http_$scala.binary_version$</artifactId>
      <version>$project.version$</version>
    </dependency>
    <!-- If testkit used, explicitly declare dependency on akka-streams-testkit in same version as akka-actor-->
    <dependency>
      <groupId>com.typesafe.akka</groupId>
      <artifactId>akka-http-testkit_$scala.binary_version$</artifactId>
      <version>$project.version$</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>com.typesafe.akka</groupId>
      <artifactId>akka-stream-testkit_$scala.binary_version$</artifactId>
      <version>$akka.version$</version>
      <scope>test</scope>
    </dependency>
    ```
    @@@
