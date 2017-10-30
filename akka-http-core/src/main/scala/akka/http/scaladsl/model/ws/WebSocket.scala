/*
 * Copyright (C) 2009-2017 Lightbend Inc. <http://www.lightbend.com>
 */

package akka.http.scaladsl.model.ws

import akka.Done
import akka.http.scaladsl.model.ws
import akka.stream.Materializer
import akka.stream.scaladsl.Sink

import scala.concurrent.Future

object WebSocket {

  /**
   * When attempting to ignore incoming messages from the client-side on a WebSocket connection,
   * use this Sink instead of `Sink.ignore` since this one will also properly drain incoming [[ws.BinaryMessage.Streamed]]
   * and [[ws.TextMessage.Streamed]] messages.
   */
  def ignoreSink(implicit mat: Materializer): Sink[Message, Future[Done]] =
    Sink.foreach[ws.Message] {
      case s: ws.TextMessage.Streamed   ⇒ s.textStream.runWith(Sink.ignore)
      case s: ws.BinaryMessage.Streamed ⇒ s.dataStream.runWith(Sink.ignore)
      case _                            ⇒ // otherwise it is a Strict message, so we don't need to drain it
    }

}
