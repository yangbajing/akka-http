# 缓存
*Caching*

Akka HTTP's caching support provides a lightweight and fast in-memory caching
functionality based on futures. The primary use-case is the "wrapping" of an
expensive operation with a caching layer that, based on a certain key of type
`K`, runs the wrapped operation only once and returns the the cached value for
all future accesses for the same key (as long as the respective entry has not
expired).

Akka HTTP 的缓存支持提供了基于 Future 的轻量级、快速的内存缓存功能。主要用途是“包装”一个昂贵的操作于缓存层，
基于某个类型的键 `K`，被包装的操作只运行一次并返回缓存的值，（这个值将用于）所有未来使用相同键的访问（只要相关条目没有过期）。

Akka HTTP comes with one implementations of the @apidoc[Cache] API built on [Caffeine]
featuring frequency-biased cache eviction semantics with support for
time-based entry expiration.

Akka HTTP 附带了一个构建在 [Caffeine] 之上的 @apidoc[Cache] API 实现，它具有基于频率的缓存回收语义，支持基于时间的条目过期。

 [Caffeine]: https://github.com/ben-manes/caffeine/

## Dependency
**依赖**

To use Akka HTTP Caching, add the module to your project:

要使用 Akka HTTP 缓存，添加模块到你的项目：

@@dependency [sbt,Gradle,Maven] {
  group="com.typesafe.akka"
  artifact="akka-http-caching_$scala.binary.version$"
  version="$project.version$"
}

## Basic design
**基础设计**

The central idea of the cache API is to not store the actual values of type `T`
themselves in the cache but rather the corresponding futures, i.e. instances of
type @java[`CompletableFuture<T>`]@scala[`Future[T]`]. This approach has the
advantage of taking care of the thundering herds problem where many
requests to a particular cache key (e.g. a resource URI) arrive before the first
one could be completed. Normally (without special guarding techniques, like
so-called "cowboy" entries) this can cause many requests to compete for system
resources while trying to compute the same result thereby greatly reducing
overall system performance. When you use an Akka HTTP cache the very first
request that arrives for a certain cache key causes a future to be put into the
cache which all later requests then "hook into". As soon as the first request
completes all other ones complete as well. This minimizes processing time and
server load for all requests.

缓存 API 的核心思想是不将类型为 `T` 的实际的值存储到缓存中，而是存储相应的 Future，例如：@java[`CompletableFuture<T>`]@scala[`Future[T]`] 类型的实例。
这个方法的优势是可以解决惊群问题，其中对特定缓存键（例如：一个资源 URI）的许多请求的第一个完成之前到达。
通常（不用特别的保卫技术，比如所谓的“牛仔”条目）这可能导致许多请求争用系统资源，并试图计算相同的结果，进而大大降低了系统的性能。
当你使用 Akka HTTP 缓存时，到达某个缓存键的第一个请求导致一个 future 被放入缓存，所有后面的请求都会“勾入”该缓存。
一旦第一个请求完成，所有其它请求也将完成。这将最小化所有请求的处理时间和服务器负载。

All Akka HTTP cache implementations adheres to the @apidoc[Cache]
@java[interface]@scala[class], which allows you to interact with the
cache.

所有 Akka HTTP 缓存实现都遵循 @apidoc[Cache] @java[接口]@scala[类]，这样允许你自己的实现与缓存交互。

Along with the cache API, the routing DSL provides several @ref:[caching
directives](../routing-dsl/directives/caching-directives/index.md) to use
caching in your routes.

依托缓存 API，路由 DSL 提供了各种 @ref:[缓存指令](../routing-dsl/directives/caching-directives/index.md) 用于你的路由设计。

## Frequency-biased LFU cache
**基于频率的 LFU 缓存**

The frequency-biased LFU cache implementation has a defined maximum number of entries it can
store. After the maximum capacity is reached the cache will evict entries that are
less likely to be used again. For example, the cache may evict an entry
because it hasn't been used recently or very often.

基于频率的 LFU 缓存实现定义了它能存储条目的最大数量。最大容量达到后，缓存将驱逐那些不太可能再次使用的条目。
例如，因为条目最近或不经常使用，缓存可能会驱逐它。

Time-based entry expiration is enabled when time-to-live and/or time-to-idle
expirations are set to a finite duration. The former provides an
upper limit to the time period an entry is allowed to remain in the cache while
the latter limits the maximum time an entry is kept without having been
accessed, ie. either read or updated. If both values are finite the time-to-live
has to be greater or equal than the time-to-idle.

当生存时间和/或空闲时间超期被设置为有限持续时间时，基于时间的条目超期被启用。
前者规定了一个条目允许保存在缓存中的时间上限，而后者限制了一个条目在不被访问（例如：读或更新）的情况下保留的最长时间。  
如果两者都是有限的，那么生存时间必须大于或等于空闲时间。

@@@ note

Expired entries are only evicted upon next access (or by being thrown out by the
capacity constraint), so they might prevent garbage collection of their values
for longer than expected.

过期条目只在下次访问时才会被驱逐（或者由于超过容量限制而被抛出），因上它们可能会有着比预期更长的时间来阻止垃圾回收。
@@@

For simple cases, configure the capacity and expiration settings in your
`application.conf` file via the settings under `akka.http.caching` and use
@java[`LfuCache.create()`]@scala[`LfuCache.apply()`] to create the cache.
For more advanced usage you can create an @apidoc[LfuCache$] with settings
specialized for your use case:

对于简单情况，在你的 `application.conf` 文件里通过 `akka.http.caching` 下的设置来配置缓存容量和过期时间并使用 @java[`LfuCache.create()`]@scala[`LfuCache.apply()`] 创建缓存。
对于更多高级的方法，你可以创建一个 @apidoc[LfuCache$] ，有着专门用于你的用例的设置： 

Java
:  @@snip [CachingDirectivesExamplesTest.java]($root$/src/test/java/docs/http/javadsl/server/directives/CachingDirectivesExamplesTest.java) { #create-cache-imports #caching-directives-import #time-unit-import #keyer-function #create-cache }

Scala
:  @@snip [CachingDirectivesExamplesSpec.java]($root$/src/test/scala/docs/http/scaladsl/server/directives/CachingDirectivesExamplesSpec.scala) { #keyer-function #create-cache }
