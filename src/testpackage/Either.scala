package testpackage
//hide std library `Option` and `Either`, since we are writing our own in this chapter
import scala.{Option => _, Either => _, Right =>_, Left=>_,_}
import testpackage.{List => _, _}
import scala.collection.immutable.List

sealed trait Either[+E,+A] {
  def map[B](f: A => B): Either[E, B] =
    this match {
      case Right(a) => Right(f(a))
      case Left(e) => Left(e)
    }

  def flatMap[EE >: E, B](f: A => Either[EE, B]): Either[EE, B] =
    this match {
      case Left(e) => Left(e)
      case Right(a) => f(a)
    }
  def orElse[EE >: E, AA >: A](b: => Either[EE, AA]): Either[EE, AA] =
    this match {
      case Left(_) => b
      case Right(a) => Right(a)
    }
  def map2[EE >: E, B, C](b: Either[EE, B])(f: (A, B) => C):
  Either[EE, C] = for { a <- this; b1 <- b } yield f(a,b1)
}
case class Left[+E](get: E) extends Either[E,Nothing]
case class Right[+A](get: A) extends Either[Nothing,A]

object Either {
  //  def mean(xs: IndexedSeq[Double]): Either[String, Double] =
  //    if (xs.isEmpty)
  //      Left("mean of empty list!")
  //    else
  //      Right(xs.sum / xs.length)

  def safeDiv(x: Int, y: Int): Either[Exception, Int] =
    try Right(x / y)
    catch { case e: Exception => Left(e) }

  def Try[A](a: => A): Either[Exception, A] =
    try Right(a)
    catch { case e: Exception => Left(e) }

  def traverse[E,A,B](es: List[A])(f: A => Either[E, B]): Either[E, List[B]] =
    es match {
      case Nil => Right(Nil)
      case h::t => (f(h) map2 traverse(t)(f))(_ :: _)
    }

  def traverse_1[E,A,B](es: List[A])(f: A => Either[E, B]): Either[E, List[B]] =
    es.foldRight[Either[E,List[B]]](Right(Nil))((a, b) => f(a).map2(b)(_ :: _))

  def sequence[E,A](es: List[Either[E,A]]): Either[E,List[A]] =
    traverse(es)(x => x)

  case class Person(name: Name, age: Age)

  sealed class Name(val value: String)

  sealed class Age(val value: Int)


  def mkName(name: String): Either[String, Name] =
    if (name == "" || name == null) Left("Name is empty.")
    else Right(new Name(name))

  def mkAge(age: Int): Either[String, Age] =
    if (age < 0) Left("Age is out of range.")
    else Right(new Age(age))

  def mkPerson(name: String, age: Int): Either[String, Person] =
    mkName(name).map2(mkAge(age))(Person(_, _))


  def main(args: Array[String]): Unit = {
    val l = List(1,2)
    def asdf(a:Int): Either[String, Int] =
      if (a<0) Left("kleiner null") else Right(a)


    val test = traverse(l)(asdf(_))

    val p = mkPerson("test", -1)
    println(test)
  }


  /*
  There are a number of variations on `Option` and `Either`. If we want to accumulate multiple errors, a simple
  approach is a new data type that lets us keep a list of errors in the data constructor that represents failures:

  trait Partial[+A,+B]
  case class Errors[+A](get: Seq[A]) extends Partial[A,Nothing]
  case class Success[+B](get: B) extends Partial[Nothing,B]

  There is a type very similar to this called `Validation` in the Scalaz library. You can implement `map`, `map2`,
  `sequence`, and so on for this type in such a way that errors are accumulated when possible (`flatMap` is unable to
  accumulate errors--can you see why?). This idea can even be generalized further--we don't need to accumulate failing
  values into a list; we can accumulate values using any user-supplied binary function.

  It's also possible to use `Either[List[E],_]` directly to accumulate errors, using different implementations of
  helper functions like `map2` and `sequence`.*/
}

