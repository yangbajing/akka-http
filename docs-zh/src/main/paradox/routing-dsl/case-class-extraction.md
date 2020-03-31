@@@ div { .group-java }

This section is only relevant when using the Scala API

本部分只当使用 Scala API 时相关
@@@

@@@ div { .group-scala }
# Case Class 提取
**Case Class Extraction**

The value extraction performed by @ref[Directives](directives/index.md) is a nice way of providing your route logic with interesting request
properties, all with proper type-safety and error handling. However, in some case you might want even more.
Consider this example:

通过 @ref[Directives](directives/index.md) 执行的值提取是为你的路由逻辑提供为感兴趣的请求属性的一种好方法，所以这些属性都具有合适的类型-安全性和错误处理。
但是，有些情况下，你可能会想更多。举个例子：

@@snip [CaseClassExtractionExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/CaseClassExtractionExamplesSpec.scala) { #example-1 }

Here the @ref[parameters](directives/parameter-directives/parameters.md) directives is employed to extract three `Int` values, which are then used to construct an
instance of the `Color` case class. So far so good. However, if the model classes we'd like to work with have more
than just a few parameters the overhead introduced by capturing the arguments as extractions only to feed them into the
model class constructor directly afterwards can somewhat clutter up your route definitions.

这里，`parameters` 指令用于提取三个 `Int` 值，然后这三个值用于构造 `Color` case 类的实例。到目前为止还不错。
但是，如果我们想要处理的模型类不止有几个参数，那么捕获作为提取的参数并将其直接提供给模型类构造函数所带来的开销，可能你的路由定义有些混乱。

If your model classes are case classes, as in our example, Akka HTTP supports an even shorter and more concise
syntax. You can also write the example above like this:

如果你的模型类是 case 类，在我们的例子中，Akka HTTP 支持一种更短且更简洁的语法。也可以像这样写上面的例子：

@@snip [CaseClassExtractionExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/CaseClassExtractionExamplesSpec.scala) { #example-2 }

You can postfix any directive with extractions with an `as(...)` call. By simply passing the companion object of your
model case class to the `as` modifier method the underlying directive is transformed into an equivalent one, which
extracts only one value of the type of your model class. Note that there is no reflection involved and your case class
does not have to implement any special interfaces. The only requirement is that the directive you attach the `as`
call to produces the right number of extractions, with the right types and in the right order.

你可以使用 `as(...)` 调用为任何带有提取的指令添加后缀。通过简单的传递模型 case 类的伴身对象到 `as` 修改器方法，将底层指令转换为等效的指令，
该指令只提取模型类类型的一个值。注意，这里没有反射参与并且你的 case 类没有实现任何特定接口。唯一的要求是，附加的指令 `as` 调用生成正确的提取数量，
具有正确的类型和正确的顺序。

If you'd like to construct a case class instance from extractions produced by *several* directives you can first join
the directives with the `&` operator before using the `as` call:

@@snip [CaseClassExtractionExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/CaseClassExtractionExamplesSpec.scala) { #example-3 }

Here the `Color` class has gotten another member, `name`, which is supplied not as a parameter but as a path
element. By joining the `path` and `parameters` directives with `&` you create a directive extracting 4 values,
which directly fit the member list of the `Color` case class. Therefore you can use the `as` modifier to convert
the directive into one extracting only a single `Color` instance.

Generally, when you have routes that work with, say, more than 3 extractions it's a good idea to introduce a case class
for these and resort to case class extraction. Especially since it supports another nice feature: validation.

@@@@warning { title="Caution" }
There is one quirk to look out for when using case class extraction: If you create an explicit companion
object for your case class, no matter whether you actually add any members to it or not, the syntax presented above
will not (quite) work anymore. Instead of `as(Color)` you will then have to say `as(Color.apply)`. This behavior
appears as if it's not really intended, so this might be improved in future Scala versions.
@@@@

## Case Class Validation

In many cases your web service needs to verify input parameters according to some logic before actually working with
them. E.g. in the example above the restriction might be that all color component values must be between 0 and 255.
You could get this done with a few @ref[validate](directives/misc-directives/validate.md) directives but this would quickly become cumbersome and hard to
read.

If you use case class extraction you can put the verification logic into the constructor of your case class, where it
should be:

@@snip [CaseClassExtractionExamplesSpec.scala]($test$/scala/docs/http/scaladsl/server/CaseClassExtractionExamplesSpec.scala) { #example-4 }

If you write your validations like this Akka HTTP's case class extraction logic will properly pick up all error
messages and generate a @apidoc[ValidationRejection] if something goes wrong. By default, `ValidationRejections` are
converted into `400 Bad Request` error response by the default @ref[RejectionHandler](rejections.md#the-rejectionhandler), if no
subsequent route successfully handles the request.

@@@
