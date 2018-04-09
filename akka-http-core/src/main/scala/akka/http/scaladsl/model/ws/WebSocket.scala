/*
 * Copyright (C) 2009-2017 Lightbend Inc. <http://www.lightbend.com>
 */

package akka.http.scaladsl.model.ws

import java.util.concurrent.atomic.AtomicInteger

import akka.Done
import akka.http.scaladsl.model.ws
import akka.stream._
import akka.stream.scaladsl.{ Flow, Keep, Sink }
import akka.stream.stage._

import scala.concurrent.{ ExecutionContext, Future, Promise }
import scala.util.{ Failure, Success, Try }

object WebSocket {

  /**
   * When attempting to ignore incoming messages from the client-side on a WebSocket connection,
   * use this Sink instead of `Sink.ignore` since this one will also properly drain incoming [[ws.BinaryMessage.Streamed]]
   * and [[ws.TextMessage.Streamed]] messages.
   */
  val ignoreSink: Sink[Message, Future[Done]] =
    Flow[Message]
      .toMat(new WebSocketIgnoreSink())(Keep.right)

  private class WebSocketIgnoreSink extends GraphStageWithMaterializedValue[SinkShape[Message], Future[Done]] {
    val in: Inlet[Message] = Inlet("WebsocketIgnoreSink")

    override val shape: SinkShape[Message] = SinkShape(in)

    override def createLogicAndMaterializedValue(inherited: Attributes): (GraphStageLogic, Future[Done]) = {
      val promise = Promise[Done]()
      val pendingStreams = new AtomicInteger(1)

      (new GraphStageLogic(shape) {

        override def preStart(): Unit = pull(in)

        val doneCb: AsyncCallback[Unit] = getAsyncCallback[Unit](_ ⇒ {
          completeStage()
          promise.trySuccess(Done)
        })
        val failCb: AsyncCallback[Throwable] = getAsyncCallback[Throwable](e ⇒ {
          failStage(e)
          promise.tryFailure(e)
        })

        setHandler(in, new InHandler {
          override def onPush(): Unit = {
            implicit val ec: ExecutionContext = materializer.executionContext
            grab(in) match {
              case s: ws.TextMessage.Streamed ⇒
                pendingStreams.incrementAndGet()
                s.textStream.runWith(Sink.ignore)(interpreter.subFusingMaterializer).onComplete(onSubstreamComplete)
              case s: ws.BinaryMessage.Streamed ⇒
                pendingStreams.incrementAndGet()
                s.dataStream.runWith(Sink.ignore)(interpreter.subFusingMaterializer).onComplete(onSubstreamComplete)
              case _ ⇒
              // otherwise it is a Strict message, so we don't need to drain it
            }
            pull(in)
          }

          /** Called whenever draining a streamed message completes */
          def onSubstreamComplete(result: Try[Done]): Unit = result match {
            case Success(_) ⇒ onStreamFinish()
            case Failure(e) ⇒ failCb.invoke(e)
          }

          override def onUpstreamFinish(): Unit = onStreamFinish()

          /**
           * Called whenever either the 'main' stream or a substream finishes
           */
          def onStreamFinish(): Unit = {
            if (pendingStreams.decrementAndGet() == 0)
              doneCb.invoke(())
          }
        })
      }, promise.future)
    }
  }
}
