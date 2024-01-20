package com.book.practice.routes

import cats.effect.IO
import com.book.practice.models.Book
import com.book.practice.models.BookModels.{BookId, Message}
import com.book.practice.repository.BookRepo
import io.circe.Json
import io.circe.generic.auto._
import org.http4s.dsl.Http4sDsl
import org.http4s.HttpRoutes
import org.http4s.circe.CirceEntityCodec._

object BookRoutes {
  private def errorBody(message: Message) = Json.obj(
    ("message", Json.fromString(message))
  )

  def routes(bookRepo: BookRepo): HttpRoutes[IO] = {
    val dsl = new Http4sDsl[IO] {}
    import dsl._

    HttpRoutes.of[IO] {
      case _ @ GET -> Root / "books" =>
        bookRepo.getBooks().flatMap(books => Ok(books))

      case req @ POST -> Root / "books" =>
        req.decode[Book]{ book =>
          bookRepo.addBook(book).flatMap(id =>
            Created(Json.obj(("id", Json.fromString(id.value))))
          )
        }

      case _ @ GET -> Root / "books" / id =>
        bookRepo.getBook((BookId(id))).flatMap{
          case None => NotFound()
          case Some(book) => Ok(book)
        }

      case req @ PUT -> Root / "books" / id =>
        req.decode[Book]{ book =>
          bookRepo.updateBook(BookId(id), book).flatMap{
            case Left(message) => NotFound(errorBody(message))
            case Right(_) => Ok()
          }
        }

      case _ @ DELETE -> Root / "books" / id =>
        bookRepo.deleteBook(BookId(id)).flatMap{
          case Left(message) => NotFound(errorBody(message))
          case Right(_) => Ok()
        }
    }
  }
}
