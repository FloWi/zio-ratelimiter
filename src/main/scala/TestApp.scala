import zio.ZIO
import zio.console._

// the easiast way to run a ZIO effect, is to implement zio.App
object TestApp extends zio.App {
  override def run(args: List[String]) =
    for {
      limiter <- RateLimiter.make(perSecond = 1.0, buffer = 2)
      _       <- doSomething(1000, limiter)
    } yield 0 // this is our zio.App's exit code.

  def doSomething(n: Int, limiter: RateLimiter) =
    ZIO.foreach(1 to n) { i =>
      limiter.rateLimit(putStrLn(i.toString))
    }
}
