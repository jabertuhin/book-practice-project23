package com.book.practice

import cats.effect.{ExitCode, IO, IOApp}
import com.book.practice.repository.{BookRepo, BookRepoImplementation}
import com.book.practice.routes.BookRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Router

object Main extends IOApp {
  private val bookRepo: BookRepo = new BookRepoImplementation

  val httpRoutes = Router[IO](
    "/" -> BookRoutes.routes(bookRepo)
  ).orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO]
      .bindHttp(9000, "0.0.0.0")
      .withHttpApp(httpRoutes)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
