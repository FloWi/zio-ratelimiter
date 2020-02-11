import zio._
import zio.clock.Clock

import zio.duration._

object RateLimiter {
  def make(perSecond: Double, buffer: Int): ZIO[Clock, Nothing, RateLimiter] = {
    require(perSecond > 0 && buffer > 0)
    val period: Duration = periodFrom(perSecond)
    for {
      queue <- Queue.bounded[Unit](buffer)
      // keeps draining the queue at the given rate
      dequeueOp = queue.take.repeat(Schedule.fixed(period))
      //fork it, so it doesn't block the main thread/fiber
      _ <- dequeueOp.fork
    } yield {
      new RateLimiter(queue)
    }
  }
  private def periodFrom(perSecond: Double) =
    (1.second.toNanos.toDouble / perSecond).toInt.nanos
}

class RateLimiter private (queue: Queue[Unit]) {
  def rateLimit[R, E, A](effect: => ZIO[R, E, A]): ZIO[R, E, A] =
    queue.offer(()) *> effect
}
