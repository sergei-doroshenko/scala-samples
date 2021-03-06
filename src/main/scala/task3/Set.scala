package task3

/*
 * Functional Set
 * Implement operations for Set that is defined as a function. You may not use other collections to accumulate state.
 * State is accumulated only by closures.
 *
 * The only things you need to change are those `???` symbols (implementation), the other code remains unchanged.
 *
 * Please, use tests to check you implementation (scalaBasic/lecture3/SetTest.scala).
 */
object Set {
  type Set = Weekday.Value => Boolean

  val empty: Set = _ => false

  val singleton: Weekday.Value => Set = day => x => x == day
  //              return type
  val singleton2: (Weekday.Value => Set) = {
    // closure - return
    (day: Weekday.Value) => (x: Weekday.Value) => (x == day)
  }
  val fromList: List[Weekday.Value] => Set = _.foldRight(empty) { case (day, set) =>
    if (contains(day, set)) set else insert(day, set)
  }

  def union(left: Set, right: Set): Set = x => left(x) || right(x)

  def difference(left: Set, right: Set): Set = x => left(x) && !right(x)

  def intersection(left: Set, right: Set): Set = x => left(x) && right(x)

  def insert(day: Weekday.Value, set: Set): Set = set match {
    case set if Set.contains(day, set) => set
    case _ => x =>
      if (x == day) true
      else set.apply(x)
  }

  def insert2(day: Weekday.Value, set: Set): Set = union(singleton(day), set)
  def insert3(day: Weekday.Value, set: Set): Set = someDay => (someDay == day) || set(someDay)

  /**
   * Full syntax.
   * @param day a Weekday
   * @param set a function (Set, parent function)
   * @return a function that return true for a 'day' and for other days for which 'set' returns true
   */
  def insert1(day: Weekday.Value, set: Set): Set = set match {
    case set if Set.contains(day, set) => set
    case _ => (x: Weekday.Value) => {
      if (x == day) true
      else set.apply(x)
    }
  }

  def remove(day: Weekday.Value, set: Set): Set = set match {
    case set if !Set.contains(day, set) => set
    case _ => x =>
      if (x == day) false
      else set(x)
  }

  def remove3(day: Weekday.Value, set: Set): Set = someDay => (someDay != day) && set(someDay)

  def contains(day: Weekday.Value, set: Set): Boolean = set match {
    case Set.empty => false
    case _ => set(day)
  }

  def isEmpty(set: Set): Boolean =
    set match {
      case Set.empty => true
      case _ => false
    }

  def isEqual(left: Set, right: Set): Boolean = {
    Weekday.values.filter(v => left(v) != right(v)).isEmpty
  }

  def isEqual2(left: Set, right: Set): Boolean = !Weekday.values.exists(day => left(day) ^ right(day))
  def isEqual3(left: Set, right: Set): Boolean = Weekday.values.forall(day => left(day) == right(day))

  def isSubsetOf(left: Set, right: Set): Boolean = {
    Weekday.values.filter(v => left(v)).subsetOf(Weekday.values.filter(x => right(x)))
  }

  def isSubsetOf2(left: Set, right: Set): Boolean = !Weekday.values.exists(day => left(day) && !right(day))
  def isSubsetOf3(left: Set, right: Set): Boolean = Weekday.values.forall(day => !left(day) || right(day))

  // In mathematics, two sets are said to be disjoint sets if they have no element in common.
  def isDisjointFrom(left: Set, right: Set): Boolean = {
    Weekday.values.filter(v => left(v) && right(v)).isEmpty
  }

  def isDisjointFrom2(left: Set, right: Set): Boolean = isEmpty(intersection(left, right))
  def isDisjointFrom3(left: Set, right: Set): Boolean = Weekday.values.forall(day => !(left(day) && right(day)))
}
