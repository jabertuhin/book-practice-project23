package com.book.practice.repository

import cats.effect.IO
import cats.implicits._
import com.book.practice.models.{Book, BookWithId}
import com.book.practice.models.BookModels.{BookId, Message}
import scala.collection.mutable.HashMap

trait BookRepo {
  def addBook(book: Book): IO[BookId]
  def getBook(id: BookId): IO[Option[BookWithId]]
  def deleteBook(id: BookId): IO[Either[Message, Unit]]
  def updateBook(id: BookId, book: Book): IO[Either[Message, Unit]]
  def getBooks(): IO[List[BookWithId]]
}

class BookRepoImplementation extends BookRepo{
  val storage = HashMap[BookId, Book]().empty

  override def addBook(book: Book): IO[BookId] = IO {
    val bookId = BookId()
    val _ = storage.put(bookId, book)
    bookId
  }

  override def getBook(id : BookId): IO[Option[BookWithId]] = IO {
    storage.get(id).map(book => BookWithId(id.value, book.title, book.author))
  }

  override def deleteBook(id: BookId): IO[Either[Message, Unit]] = {
    for {
      removedBook <- IO(storage.remove(id))
      result = removedBook.toRight(s"Book with ${id.value} not found").void
    } yield result
  }

  override def updateBook(id: BookId, book: Book): IO[Either[Message, Unit]] = {
    for{
      bookOpt <- getBook(id)
      _ <- IO(bookOpt.toRight(s"Book wth ${id.value} not found").void)
      updatedBook = storage.put(id, book).toRight(s"Book wth ${id.value} not found").void
    } yield updatedBook
  }

  override def getBooks(): IO[List[BookWithId]] = IO {
    storage.map {
      case (id, book) => BookWithId(id.value, book.title, book.author)
    }.toList
  }
}