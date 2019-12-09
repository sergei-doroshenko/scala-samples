package task5

import task5.SerializationApp.p

// Implement a simple library for Json serialization using Type Classes pattern.
// We want be able to serialize any arbitrary object into a json
// string by providing a conversion to Json AST (abstract syntax tree).
class Serialization {

}

trait Serializable[T] {
  def serialize(obj: T): JsField
}

object Serializable {
  def apply[T](implicit evidence: Serializable[T]): Serializable[T] = evidence

  object ops {
    def serialize[T: Serializable](value: T): JsField = Serializable[T].serialize(value)

    implicit val intCanSerialize: Serializable[Int] = (obj: Int) => JsNumber(obj)

    implicit val personCanSerialize: Serializable[Person] = (p: Person) => JsObject(
      "name" -> JsString(p.name),
      "age" -> JsNumber(p.age)
    )
  }
}

trait JsField
case class JsObject(fields: (String, JsField)*) extends JsField {
  override def toString: String = fields.map(field => s""""${field._1}":${field._2}""").mkString("{", ",", "}")
}
case class JsNumber(value: Int) extends JsField {
  override def toString: String = value.toString
}
case class JsString(value: String) extends JsField {
  override def toString: String = "\"" + value + "\"";
}

// For example:
//
// We have class
// case class Person(name: String, age: Int)
//
// For this class we should provide a conversion to Json AST (through implicit scope) like this:
//
// def serialize(p: Person) = JSObject(
// "name" -> JsString(p.name),
// "age" -> JsNumber(p.age)
// )
//
// Then we can serialize person:
//
// val p = Person("personName", 30)
// serialize(p) // {"name": "personName", "age": 30}
