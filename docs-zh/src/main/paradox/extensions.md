# 6. 扩展

There are several third party libraries that expand the functionality of Akka Http.

有一些第三方库，它们扩展了 Akka HTTP 的功能。

Among those, we want to highlight the following:

其中，我们要强调下面这些：

- [akka-http-json](https://github.com/hseeberger/akka-http-json): Integrate some of the best JSON libs in Scala with Akka HTTP
    将一些 Scala 里最好的 JSON 库与 Akka HTTP 集成
- [Swakka](https://github.com/jtownson/swakka): A Scala library for creating Swagger definitions in a type-safe fashion wth Akka-Http. Generates Open API (a.k.a. Swagger) from code
    一个 Scala 库，通过 Akka-Http 以类型安全的方式创建 Swapper 定义。从代码生成开放 API（又称为 Swagger）
- [Guardrail](https://github.com/twilio/guardrail): Guardrail is a code generation tool, capable of reading from OpenAPI/Swagger specification files and generating Akka HTTP code
    Guardrail 是一个代码生成工具，能从 OpenAPi/Swagger 定义文件阅读并生成 Akka HTTP 代码
- [akka-http-cors](https://github.com/lomigmegard/akka-http-cors): Akka Http directives implementing the CORS specifications defined by W3C
    实现了 W3C 定义的 CORS 规范的 Akka HTTP 指令
- [akka-http-session](https://github.com/softwaremill/akka-http-session): Web & mobile client-side akka-http sessions, with optional JWT support
    网页 & 移动客户端 Akka HTTP 会话（管理），可选使用 JWT 支持
- [sttp](https://github.com/softwaremill/sttp): Library that provides a clean, programmer-friendly API to define HTTP requests and execute them using one of the wrapped backends, akka-http among them.
    该库提供了清晰的、程序员友好的 API 来定义 HTTP 请求，并使用其中一个包装的后端，akka-http 来执行它们。

请参阅 Scala 索引以获取更详细的列表。[![主题为 akka-http 的模块](https://index.scala-lang.org/count.svg?q=topics:akka-http&subject=akka-http&color=orange&style=flat-square&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNTggMTkwIiBlbmFibGUtYmFja2dyb3VuZD0ibmV3IDAgMCAyNTggMTkwIj48ZyBmaWxsPSIjZmZmIj48cGF0aCBvcGFjaXR5PSIuNSIgZD0iTTIxMC41IDE3Mi44YzM1LjkgMjguNyA1OC45LTU3IDEuNy03Mi44LTQ4LTEzLjMtOTYuMyA5LjUtMTQ0LjcgNjIuNy4xIDAgODkuNC0zMi43IDE0MyAxMC4xeiIvPjxwYXRoIGQ9Ik02OC40IDE2MmMzNC40LTM1LjcgOTEuNi03NS41IDE0NC45LTYwLjggMTIuNCAzLjUgMjEuMiAxMC43IDI2LjkgMTkuM2wtNTAuNC0xMDEuN2MtNy4yLTExLjUtMjUuNi05LjEtMzYtLjNsLTEzMy4yIDExMS42Yy0xMi4xIDEwLjQtMTIuOSAyOC44LTEuNiA0MC4xIDkuOSA5LjkgMjUuNiAxMC44IDM2LjUgMmwxMi45LTEwLjJ6Ii8+PC9nPjwvc3ZnPg)](https://index.scala-lang.org/search?topics=akka-http)