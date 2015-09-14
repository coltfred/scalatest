/*
 * Copyright 2001-2013 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatest

import org.scalactic.Prettifier
import org.scalatest.exceptions.TestFailedException

sealed abstract class Fact {

  val rawFactMessage: String
  val rawSimplifiedFactMessage: String
  val rawMidSentenceFactMessage: String
  val rawMidSentenceSimplifiedFactMessage: String

  val factMessageArgs: IndexedSeq[Any]
  val simplifiedFactMessageArgs: IndexedSeq[Any]
  val midSentenceFactMessageArgs: IndexedSeq[Any]
  val midSentenceSimplifiedFactMessageArgs: IndexedSeq[Any]

  val composite: Boolean
  val prettifier: Prettifier

  val cause: Option[Throwable] = None

  def isTrue: Boolean

  final def isFalse: Boolean = !isTrue

  final def toBoolean: Boolean = isTrue

  def toAssertion: Assertion

  /**
   * Get a negated version of this Fact, sub type will be negated and all messages field will be substituted with its counter-part.
   *
   * @return a negated version of this Fact
   */
  def unary_!(): Fact = Fact.Unary_!(this)

  def ||(rhs: => Fact): Fact = if (isTrue) this else Fact.Binary_||(this, rhs)

  def &&(rhs: => Fact): Fact = if (isFalse) this else Fact.Binary_&&(this, rhs)

  /**
   * Construct failure message to report if a fact fails, using <code>rawFactMessage</code>, <code>factMessageArgs</code> and <code>prettifier</code>
   *
   * @return failure message to report if a fact fails
   */
  def factMessage: String = isTrue.toString + ": " + (if (factMessageArgs.isEmpty) rawFactMessage else makeString(rawFactMessage, factMessageArgs))

  def simplifiedFactMessage: String = isTrue.toString + ": " + (if (simplifiedFactMessageArgs.isEmpty) rawSimplifiedFactMessage else makeString(rawSimplifiedFactMessage, simplifiedFactMessageArgs))

  /**
   * Construct failure message suitable for appearing mid-sentence, using <code>rawMidSentenceFactMessage</code>, <code>midSentenceFactMessageArgs</code> and <code>prettifier</code>
   *
   * @return failure message suitable for appearing mid-sentence
   */
  def midSentenceFactMessage: String = isTrue.toString + ": " + (if (midSentenceFactMessageArgs.isEmpty) rawMidSentenceFactMessage else makeString(rawMidSentenceFactMessage, midSentenceFactMessageArgs))

  def midSentenceSimplifiedFactMessage: String = isTrue.toString + ": " + (if (midSentenceSimplifiedFactMessageArgs.isEmpty) rawMidSentenceSimplifiedFactMessage else makeString(rawMidSentenceSimplifiedFactMessage, midSentenceSimplifiedFactMessageArgs))

  private def makeString(raw: String, args: IndexedSeq[Any]): String =
    Resources.formatString(raw, args.map(Prettifier.default).toArray)

  override def toString: String = factMessage
}

object Fact {

  case class False(
    rawFactMessage: String,
    rawSimplifiedFactMessage: String,
    rawMidSentenceFactMessage: String,
    rawMidSentenceSimplifiedFactMessage: String,
    factMessageArgs: IndexedSeq[Any],
    simplifiedFactMessageArgs: IndexedSeq[Any],
    midSentenceFactMessageArgs: IndexedSeq[Any],
    midSentenceSimplifiedFactMessageArgs: IndexedSeq[Any],
    composite: Boolean = false,
    override val cause: Option[Throwable] = None,
    prettifier: Prettifier = Prettifier.default
  ) extends Fact {

    def isTrue: Boolean = false

    def toAssertion: Assertion = throw new TestFailedException(factMessage, 2)
  }

/*
factMessage is the simplified one, if need be, and simplifiedFactMessage is a simpler one.
*/

  /**
   * Companion object for the <code>False</code> case class.
   *
   * @author Bill Venners
   */
  object False {

    /**
     * Factory method that constructs a new <code>False</code> with passed <code>factMessage</code>, 
     * <code>negativeFailureMessage</code>, <code>midSentenceFactMessage</code>, 
     * <code>midSentenceNegatedFailureMessage</code>, <code>factMessageArgs</code>, and <code>negatedFailureMessageArgs</code> fields.
     * <code>factMessageArgs</code>, and <code>negatedFailureMessageArgs</code> will be used in place of <code>midSentenceFactMessageArgs</code>
     * and <code>midSentenceNegatedFailureMessageArgs</code>.
     *
     * @param rawFactMessage raw failure message to report if a match fails
     * @param rawMidSentenceFactMessage raw failure message to report if a match fails
     * @param factMessageArgs arguments for constructing failure message to report if a match fails
     * @return a <code>False</code> instance
     */
    def apply(
      rawFactMessage: String,
      rawMidSentenceFactMessage: String,
      factMessageArgs: IndexedSeq[Any]
    ): False =
      new False(
        rawFactMessage,
        rawFactMessage,
        rawMidSentenceFactMessage,
        rawMidSentenceFactMessage,
        factMessageArgs,
        factMessageArgs,
        factMessageArgs,
        factMessageArgs,
        false,
        None,
        Prettifier.default
      )
  
    /**
     * Factory method that constructs a new <code>False</code> with passed <code>rawFactMessage</code>,
     * <code>rawNegativeFailureMessage</code>, <code>rawMidSentenceFactMessage</code>, and
     * <code>rawMidSentenceNegatedFailureMessage</code> fields.  All argument fields will have <code>Vector.empty</code> values.
     * This is suitable to create False with eager error messages, and its mid-sentence messages need to be different.
     *
     * @param rawFactMessage raw failure message to report if a match fails
     * @param rawMidSentenceFactMessage raw failure message to report if a match fails
     * @return a <code>False</code> instance
     */
    def apply(
      rawFactMessage: String,
      rawMidSentenceFactMessage: String
    ): False =
      new False(
        rawFactMessage,
        rawFactMessage,
        rawFactMessage,
        rawFactMessage,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        false,
        None,
        Prettifier.default
      )

    /**
     * Factory method that constructs a new <code>False</code> with passed <code>rawFactMessage</code>,
     * <code>rawSimplifiedFactMessage</code>, <code>rawMidSentenceFactMessage</code>, and
     * <code>rawMidSentenceSimplifiedFactMessage</code> fields.  All argument fields will have <code>Vector.empty</code> values.
     * This is suitable to create False with eager error messages, and its simplified and mid-sentence messages need to be different.
     *
     * @param rawFactMessage raw message to report for this fact
     * @param rawSimplifiedFactMessage raw simplified to report for this fact
     * @param rawMidSentenceFactMessage raw mid-sentence message to report for this fact
     * @param rawMidSentenceSimplifiedFactMessage raw mid-sentence simplified message to report for this fact
     * @return a <code>False</code> instance
     */
    def apply(
      rawFactMessage: String,
      rawSimplifiedFactMessage: String,
      rawMidSentenceFactMessage: String,
      rawMidSentenceSimplifiedFactMessage: String
    ): False =
      new False(
        rawFactMessage,
        rawFactMessage,
        rawMidSentenceFactMessage,
        rawMidSentenceFactMessage,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        false,
        None,
        Prettifier.default
      )

    /**
     * Factory method that constructs a new <code>False</code> with passed <code>rawFactMessage</code>,
     * <code>rawSimplifiedFactMessage</code>, <code>rawMidSentenceFactMessage</code>, and
     * <code>rawMidSentenceSimplifiedFactMessage</code> fields.  All argument fields will have <code>Vector.empty</code> values.
     * This is suitable to create False with eager error messages, and its simplified and mid-sentence messages need to be different.
     *
     * @param rawFactMessage raw message to report for this fact
     * @param rawSimplifiedFactMessage raw simplified to report for this fact
     * @param rawMidSentenceFactMessage raw mid-sentence message to report for this fact
     * @param rawMidSentenceSimplifiedFactMessage raw mid-sentence simplified message to report for this fact
     * @param factMessageArgs arguments for <code>rawFactMessage</code> and <code>rawMidSentenceFactMessage</code>
     * @param simplifiedFactMessageArgs arguments for <code>rawSimplifiedFactMessage</code> and <code>rawMidSentenceSimplifiedFactMessage</code>
     * @return a <code>False</code> instance
     */
    def apply(
      rawFactMessage: String,
      rawSimplifiedFactMessage: String,
      rawMidSentenceFactMessage: String,
      rawMidSentenceSimplifiedFactMessage: String,
      factMessageArgs: IndexedSeq[Any],
      simplifiedFactMessageArgs: IndexedSeq[Any]
    ): False =
      new False(
        rawFactMessage,
        rawSimplifiedFactMessage,
        rawMidSentenceFactMessage,
        rawMidSentenceSimplifiedFactMessage,
        factMessageArgs,
        simplifiedFactMessageArgs,
        factMessageArgs,
        simplifiedFactMessageArgs,
        false,
        None,
        Prettifier.default
      )

    /**
     * Factory method that constructs a new <code>False</code> with passed <code>rawFactMessage</code>,
     * <code>rawSimplifiedFactMessage</code>, <code>rawMidSentenceFactMessage</code>, and
     * <code>rawMidSentenceSimplifiedFactMessage</code> fields.  All argument fields will have <code>Vector.empty</code> values.
     * This is suitable to create False with eager error messages, and its simplified and mid-sentence messages need to be different.
     *
     * @param rawFactMessage raw message to report for this fact
     * @param rawSimplifiedFactMessage raw simplified to report for this fact
     * @param rawMidSentenceFactMessage raw mid-sentence message to report for this fact
     * @param rawMidSentenceSimplifiedFactMessage raw mid-sentence simplified message to report for this fact
     * @param factMessageArgs arguments for <code>rawFactMessage</code>
     * @param simplifiedFactMessageArgs arguments for <code>rawSimplifiedFactMessage</code>
     * @param midSentenceFactMessageArgs arguments for <code>rawMidSentenceFactMessage</code>
     * @param midSentenceSimplifiedFactMessageArgs arguments for <code>rawMidSentenceSimplifiedFactMessage</code>
     * @return a <code>False</code> instance
     */
    def apply(
      rawFactMessage: String,
      rawSimplifiedFactMessage: String,
      rawMidSentenceFactMessage: String,
      rawMidSentenceSimplifiedFactMessage: String,
      factMessageArgs: IndexedSeq[Any],
      simplifiedFactMessageArgs: IndexedSeq[Any],
      midSentenceFactMessageArgs: IndexedSeq[Any],
      midSentenceSimplifiedFactMessageArgs: IndexedSeq[Any]
    ): False =
      new False(
        rawFactMessage,
        rawSimplifiedFactMessage,
        rawMidSentenceFactMessage,
        rawMidSentenceSimplifiedFactMessage,
        factMessageArgs,
        simplifiedFactMessageArgs,
        midSentenceFactMessageArgs,
        midSentenceSimplifiedFactMessageArgs,
        false,
        None,
        Prettifier.default
      )
  
    /**
     * Factory method that constructs a new <code>False</code> with passed <code>rawFactMessage</code>, and
     * <code>rawNegativeFailureMessage</code> fields. The <code>rawMidSentenceFactMessage</code> will return the same
     * string as <code>rawFactMessage</code>, and the <code>rawMidSentenceNegatedFailureMessage</code> will return the
     * same string as <code>rawNegatedFailureMessage</code>.  All argument fields will have <code>Vector.empty</code> values.
     * This is suitable to create False with eager error messages that have same mid-sentence messages.
     *
     * @param rawFactMessage raw failure message to report if a match fails
     * @param rawNegatedFailureMessage raw message with a meaning opposite to that of the failure message
     * @return a <code>False</code> instance
     */
    def apply(
      rawFactMessage: String
    ): False =
      new False(
        rawFactMessage,
        rawFactMessage,
        rawFactMessage,
        rawFactMessage,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        false,
        None,
        Prettifier.default
      )
  
    /**
     * Factory method that constructs a new <code>False</code> with passed <code>rawFactMessage</code>,
     * <code>rawNegativeFailureMessage</code>, <code>factMessageArgs</code> and <code>negatedFailureMessageArgs</code> fields.
     * The <code>rawMidSentenceFactMessage</code> will return the same string as <code>rawFactMessage</code>, and the
     * <code>rawMidSentenceNegatedFailureMessage</code> will return the same string as <code>rawNegatedFailureMessage</code>.
     * The <code>midSentenceFactMessageArgs</code> will return the same as <code>factMessageArgs</code>, and the
     * <code>midSentenceNegatedFailureMessageArgs</code> will return the same as <code>negatedFailureMessageArgs</code>.
     * This is suitable to create False with lazy error messages that have same mid-sentence and use different arguments for
     * negated messages.
     *
     * @param rawFactMessage raw failure message to report if a match fails
     * @param rawNegatedFailureMessage raw message with a meaning opposite to that of the failure message
     * @param factMessageArgs arguments for constructing failure message to report if a match fails
     * @param negatedFailureMessageArgs arguments for constructing message with a meaning opposite to that of the failure message
     * @return a <code>False</code> instance
     */
    def apply(
      rawFactMessage: String,
      factMessageArgs: IndexedSeq[Any]
    ) =
      new False(
        rawFactMessage,
        rawFactMessage,
        rawFactMessage,
        rawFactMessage,
        factMessageArgs,
        factMessageArgs,
        factMessageArgs,
        factMessageArgs,
        false,
        None,
        Prettifier.default
      )
  }
  
  case class True(
    rawFactMessage: String,
    rawSimplifiedFactMessage: String,
    rawMidSentenceFactMessage: String,
    rawMidSentenceSimplifiedFactMessage: String,
    factMessageArgs: IndexedSeq[Any],
    simplifiedFactMessageArgs: IndexedSeq[Any],
    midSentenceFactMessageArgs: IndexedSeq[Any],
    midSentenceSimplifiedFactMessageArgs: IndexedSeq[Any],
    composite: Boolean = false,
    override val cause: Option[Throwable] = None,
    prettifier: Prettifier = Prettifier.default
  ) extends Fact {
  
    def isTrue: Boolean = true
  
    def toAssertion: Assertion = Succeeded
  }
  
  /**
   * Companion object for the <code>True</code> case class.
   *
   * @author Bill Venners
   */
  object True {
  
    /**
     * Factory method that constructs a new <code>True</code> with passed code>factMessage</code>, 
     * <code>negativeFailureMessage</code>, <code>midSentenceFactMessage</code>, 
     * <code>midSentenceNegatedFailureMessage</code>, <code>factMessageArgs</code>, and <code>negatedFailureMessageArgs</code> fields.
     * <code>factMessageArgs</code>, and <code>negatedFailureMessageArgs</code> will be used in place of <code>midSentenceFactMessageArgs</code>
     * and <code>midSentenceNegatedFailureMessageArgs</code>.
     *
     * @param rawFactMessage raw failure message to report if a match fails
     * @param rawMidSentenceFactMessage raw failure message to report if a match fails
     * @param factMessageArgs arguments for constructing failure message to report if a match fails
     * @return a <code>True</code> instance
     */
    def apply(
      rawFactMessage: String,
      rawMidSentenceFactMessage: String,
      factMessageArgs: IndexedSeq[Any]
    ): True =
      new True(
        rawFactMessage,
        rawFactMessage,
        rawMidSentenceFactMessage,
        rawMidSentenceFactMessage,
        factMessageArgs,
        factMessageArgs,
        factMessageArgs,
        factMessageArgs,
        false,
        None,
        Prettifier.default
      )
  
    /**
     * Factory method that constructs a new <code>True</code> with passed <code>rawFactMessage</code>,
     * <code>rawNegativeFailureMessage</code>, <code>rawMidSentenceFactMessage</code>, and
     * <code>rawMidSentenceNegatedFailureMessage</code> fields.  All argument fields will have <code>Vector.empty</code> values.
     * This is suitable to create True with eager error messages, and its mid-sentence messages need to be different.
     *
     * @param rawFactMessage raw failure message to report if a match fails
     * @param rawMidSentenceFactMessage raw failure message to report if a match fails
     * @return a <code>True</code> instance
     */
    def apply(
      rawFactMessage: String,
      rawMidSentenceFactMessage: String
    ): True =
      new True(
        rawFactMessage,
        rawFactMessage,
        rawMidSentenceFactMessage,
        rawMidSentenceFactMessage,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        false,
        None,
        Prettifier.default
      )

    /**
     * Factory method that constructs a new <code>True</code> with passed <code>rawFactMessage</code>,
     * <code>rawFactMessage</code>, <code>rawSimplifiedFactMessage</code>, <code>rawMidSentenceFactMessage</code> and
     * <code>rawMidSentenceSimplifiedFactMessage</code> fields.  All argument fields will have <code>Vector.empty</code>
     * values.  This is suitable to create True with eager error messages, and its simplified and mid-sentence messages
     * need to be different.
     *
     * @param rawFactMessage raw message to report for this fact
     * @param rawSimplifiedFactMessage raw simplified message to report for this fact
     * @param rawMidSentenceFactMessage raw mid-sentence message to report for this fact
     * @param rawMidSentenceSimplifiedFactMessage raw mid-sentence simplified message to report for this fact
     * @return a <code>True</code> instance
     */
    def apply(
      rawFactMessage: String,
      rawSimplifiedFactMessage: String,
      rawMidSentenceFactMessage: String,
      rawMidSentenceSimplifiedFactMessage: String
    ): True =
      new True(
        rawFactMessage,
        rawSimplifiedFactMessage,
        rawMidSentenceFactMessage,
        rawMidSentenceSimplifiedFactMessage,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        false,
        None,
        Prettifier.default
      )

    /**
     * Factory method that constructs a new <code>True</code> with passed <code>rawFactMessage</code>,
     * <code>rawFactMessage</code>, <code>rawSimplifiedFactMessage</code>, <code>rawMidSentenceFactMessage</code> and
     * <code>rawMidSentenceSimplifiedFactMessage</code> fields.  All argument fields will have <code>Vector.empty</code>
     * values.  This is suitable to create True with eager error messages, and its simplified and mid-sentence messages
     * need to be different.
     *
     * @param rawFactMessage raw message to report for this fact
     * @param rawSimplifiedFactMessage raw simplified message to report for this fact
     * @param rawMidSentenceFactMessage raw mid-sentence message to report for this fact
     * @param rawMidSentenceSimplifiedFactMessage raw mid-sentence simplified message to report for this fact
     * @param factMessageArgs arguments for <code>rawFactMessage</code> and <code>rawMidSentenceFactMessage</code>
     * @param simplifiedFactMessageArgs arguments for <code>rawSimplifiedFactMessage</code> and <code>rawMidSentenceSimplifiedFactMessage</code>
     * @return a <code>True</code> instance
     */
    def apply(
      rawFactMessage: String,
      rawSimplifiedFactMessage: String,
      rawMidSentenceFactMessage: String,
      rawMidSentenceSimplifiedFactMessage: String,
      factMessageArgs: IndexedSeq[Any],
      simplifiedFactMessageArgs: IndexedSeq[Any]
    ): True =
      new True(
        rawFactMessage,
        rawSimplifiedFactMessage,
        rawMidSentenceFactMessage,
        rawMidSentenceSimplifiedFactMessage,
        factMessageArgs,
        simplifiedFactMessageArgs,
        factMessageArgs,
        simplifiedFactMessageArgs,
        false,
        None,
        Prettifier.default
      )

    /**
     * Factory method that constructs a new <code>True</code> with passed <code>rawFactMessage</code>,
     * <code>rawFactMessage</code>, <code>rawSimplifiedFactMessage</code>, <code>rawMidSentenceFactMessage</code> and
     * <code>rawMidSentenceSimplifiedFactMessage</code> fields.  All argument fields will have <code>Vector.empty</code>
     * values.  This is suitable to create True with eager error messages, and its simplified and mid-sentence messages
     * need to be different.
     *
     * @param rawFactMessage raw message to report for this fact
     * @param rawSimplifiedFactMessage raw simplified message to report for this fact
     * @param rawMidSentenceFactMessage raw mid-sentence message to report for this fact
     * @param rawMidSentenceSimplifiedFactMessage raw mid-sentence simplified message to report for this fact
     * @param factMessageArgs arguments for <code>rawFactMessage</code>
     * @param simplifiedFactMessageArgs arguments for <code>rawSimplifiedFactMessage</code>
     * @param midSentenceFactMessageArgs arguments for <code>rawMidSentenceFactMessage</code>
     * @param midSentenceSimplifiedFactMessageArgs arguments for <code>rawMidSentenceSimplifiedFactMessage</code>
     * @return a <code>True</code> instance
     */
    def apply(
      rawFactMessage: String,
      rawSimplifiedFactMessage: String,
      rawMidSentenceFactMessage: String,
      rawMidSentenceSimplifiedFactMessage: String,
      factMessageArgs: IndexedSeq[Any],
      simplifiedFactMessageArgs: IndexedSeq[Any],
      midSentenceFactMessageArgs: IndexedSeq[Any],
      midSentenceSimplifiedFactMessageArgs: IndexedSeq[Any]
    ): True =
      new True(
        rawFactMessage,
        rawSimplifiedFactMessage,
        rawMidSentenceFactMessage,
        rawMidSentenceSimplifiedFactMessage,
        factMessageArgs,
        simplifiedFactMessageArgs,
        midSentenceFactMessageArgs,
        midSentenceSimplifiedFactMessageArgs,
        false,
        None,
        Prettifier.default
      )
  
    /**
     * Factory method that constructs a new <code>True</code> with passed <code>rawFactMessage</code>, and
     * <code>rawNegativeFailureMessage</code> fields. The <code>rawMidSentenceFactMessage</code> will return the same
     * string as <code>rawFactMessage</code>, and the <code>rawMidSentenceNegatedFailureMessage</code> will return the
     * same string as <code>rawNegatedFailureMessage</code>.  All argument fields will have <code>Vector.empty</code> values.
     * This is suitable to create True with eager error messages that have same mid-sentence messages.
     *
     * @param rawFactMessage raw failure message to report if a match fails
     * @return a <code>True</code> instance
     */
    def apply(
      rawFactMessage: String
    ): True =
      new True(
        rawFactMessage,
        rawFactMessage,
        rawFactMessage,
        rawFactMessage,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        Vector.empty,
        false,
        None,
        Prettifier.default
      )
  
    /**
     * Factory method that constructs a new <code>True</code> with passed <code>rawFactMessage</code>,
     * <code>rawNegativeFailureMessage</code>, <code>factMessageArgs</code> and <code>negatedFailureMessageArgs</code> fields.
     * The <code>rawMidSentenceFactMessage</code> will return the same string as <code>rawFactMessage</code>, and the
     * <code>rawMidSentenceNegatedFailureMessage</code> will return the same string as <code>rawNegatedFailureMessage</code>.
     * The <code>midSentenceFactMessageArgs</code> will return the same as <code>factMessageArgs</code>, and the
     * <code>midSentenceNegatedFailureMessageArgs</code> will return the same as <code>negatedFailureMessageArgs</code>.
     * This is suitable to create True with lazy error messages that have same mid-sentence and use different arguments for
     * negated messages.
     *
     * @param rawFactMessage raw failure message to report if a match fails
     * @param factMessageArgs arguments for constructing failure message to report if a match fails
     * @return a <code>True</code> instance
     */
    def apply(
      rawFactMessage: String,
      factMessageArgs: IndexedSeq[Any]
    ) =
      new True(
        rawFactMessage,
        rawFactMessage,
        rawFactMessage,
        rawFactMessage,
        factMessageArgs,
        factMessageArgs,
        factMessageArgs,
        factMessageArgs,
        false,
        None,
        Prettifier.default
      )
  }

  case class Unary_!(underlying: Fact) extends Fact {

    // Ah, need to do the !({0}) thing
    val rawFactMessage: String = underlying.rawSimplifiedFactMessage
    val rawSimplifiedFactMessage: String = underlying.rawFactMessage
    val rawMidSentenceFactMessage: String = underlying.rawMidSentenceSimplifiedFactMessage
    val rawMidSentenceSimplifiedFactMessage: String = underlying.rawMidSentenceFactMessage
    val factMessageArgs: IndexedSeq[Any] = underlying.simplifiedFactMessageArgs
    val simplifiedFactMessageArgs: IndexedSeq[Any] = underlying.factMessageArgs
    val midSentenceFactMessageArgs: IndexedSeq[Any] = underlying.midSentenceSimplifiedFactMessageArgs
    val midSentenceSimplifiedFactMessageArgs: IndexedSeq[Any] = underlying.midSentenceFactMessageArgs
    val composite: Boolean = underlying.composite
    val prettifier: Prettifier = underlying.prettifier

    def isTrue: Boolean = !(underlying.isTrue)

    def toAssertion: Assertion = ???

    override def unary_!(): org.scalatest.Fact = underlying

    override def factMessage: String = super.factMessage

    override def simplifiedFactMessage: String = super.simplifiedFactMessage

    override def midSentenceFactMessage: String = super.midSentenceFactMessage

    override def midSentenceSimplifiedFactMessage: String = super.midSentenceSimplifiedFactMessage
  }

  class Binary_&&(left: Fact, right: => Fact) extends Fact {

    private lazy val rightResult = right

    val rawFactMessage: String = {
      if (left.isFalse) left.rawFactMessage
      else Resources.rawCommaDoubleAmpersand
    }
    val rawSimplifiedFactMessage: String = {
      if (left.isFalse) left.rawSimplifiedFactMessage
      else Resources.rawCommaDoubleAmpersand
    }
    val rawMidSentenceFactMessage: String = {
      if (left.isFalse) left.rawMidSentenceFactMessage
      else Resources.rawCommaDoubleAmpersand
    }
    val rawMidSentenceSimplifiedFactMessage: String = {
      if (left.isFalse) left.rawMidSentenceSimplifiedFactMessage
      else Resources.rawCommaDoubleAmpersand
    }
    val factMessageArgs: IndexedSeq[Any] = {
      if (left.isFalse) Vector(FactMessage(left)) // Keep full message if short circuiting the error message
      else Vector(SimplifiedFactMessage(left), MidSentenceFactMessage(rightResult)) // Simplify if combining
    }
    val simplifiedFactMessageArgs: IndexedSeq[Any] = {
      if (left.isFalse) Vector(SimplifiedFactMessage(left))
      else Vector(SimplifiedFactMessage(left), MidSentenceSimplifiedFactMessage(rightResult))
    }
    val midSentenceFactMessageArgs: IndexedSeq[Any] = {
      if (left.isFalse) Vector(MidSentenceFactMessage(left)) // Keep full message if short circuiting the error message
      else Vector(MidSentenceSimplifiedFactMessage(left), MidSentenceFactMessage(rightResult)) // Simplify if combining
    }
    val midSentenceSimplifiedFactMessageArgs: IndexedSeq[Any] = {
      if (left.isFalse) Vector(MidSentenceFactMessage(left))
      else Vector(MidSentenceSimplifiedFactMessage(left), MidSentenceSimplifiedFactMessage(rightResult))
    }

    val composite: Boolean = true
    val prettifier: Prettifier = left.prettifier

    def isTrue: Boolean = left.isTrue && rightResult.isTrue

    def toAssertion: Assertion = ???
  }

  object Binary_&& {
    def apply(left: Fact, right: => Fact): Fact = new Binary_&&(left, right)
  }

  class Binary_||(left: Fact, right: => Fact) extends Fact {

    private lazy val rightResult = right

    val rawFactMessage: String = {
      if (left.isTrue) left.rawFactMessage
      else Resources.rawCommaDoublePipe
    }
    val rawSimplifiedFactMessage: String = {
      if (left.isTrue) left.rawSimplifiedFactMessage
      else Resources.rawCommaDoublePipe
    }
    val rawMidSentenceFactMessage: String = {
      if (left.isTrue) left.rawMidSentenceFactMessage
      else Resources.rawCommaDoublePipe
    }
    val rawMidSentenceSimplifiedFactMessage: String = {
      if (left.isTrue) left.rawMidSentenceSimplifiedFactMessage
      else Resources.rawCommaDoublePipe
    }
    val factMessageArgs: IndexedSeq[Any] = {
      if (left.isTrue) Vector(FactMessage(left))
      else Vector(FactMessage(left), MidSentenceFactMessage(rightResult))
    }
    val simplifiedFactMessageArgs: IndexedSeq[Any] = {
      if (left.isTrue) Vector(SimplifiedFactMessage(left))
      else Vector(FactMessage(left), MidSentenceSimplifiedFactMessage(rightResult))
    }
    val midSentenceFactMessageArgs: IndexedSeq[Any] = {
      if (left.isTrue) Vector(MidSentenceFactMessage(left))
      else Vector(MidSentenceSimplifiedFactMessage(left), MidSentenceFactMessage(rightResult))
    }
    val midSentenceSimplifiedFactMessageArgs: IndexedSeq[Any] = {
      if (left.isTrue) Vector(MidSentenceSimplifiedFactMessage(left))
      else Vector(MidSentenceSimplifiedFactMessage(left), MidSentenceSimplifiedFactMessage(rightResult))
    }

    val composite: Boolean = true
    val prettifier: Prettifier = left.prettifier

    def isTrue: Boolean = left.isTrue || right.isTrue

    def toAssertion: Assertion = ???
  }

  object Binary_|| {
    def apply(left: Fact, right: => Fact): Fact = new Binary_||(left, right)
  }

  private[scalatest] def commaAnd(leftComposite: Boolean, rightComposite: Boolean): String = (leftComposite,rightComposite) match {
    case (false,false) => Resources.rawCommaAnd
    case (false,true) => Resources.rawRightParensCommaAnd
    case (true,false) => Resources.rawLeftParensCommaAnd
    case (true,true) => Resources.rawBothParensCommaAnd
  }

  private[scalatest] def commaBut(leftComposite: Boolean, rightComposite: Boolean): String = (leftComposite,rightComposite) match {
    case (false,false) => Resources.rawCommaBut
    case (false,true) => Resources.rawRightParensCommaBut
    case (true,false) => Resources.rawLeftParensCommaBut
    case (true,true) => Resources.rawBothParensCommaBut
  }

  private[scalatest] class MyLazyMessage(raw: String, args: IndexedSeq[Any]) {
    override def toString: String = Resources.formatString(raw, args.map(Prettifier.default).toArray)
  }

  // Idea is to override toString each time it is used.
  private[scalatest] sealed abstract class LazyMessage {
    val nestedArgs: IndexedSeq[Any]
  }

  private[scalatest] case class FactMessage(fact: Fact) extends LazyMessage {
    val nestedArgs: IndexedSeq[Any] = fact.factMessageArgs
    override def toString: String = fact.factMessage
  }

  private[scalatest] case class MidSentenceFactMessage(fact: Fact) extends LazyMessage {
    val nestedArgs: IndexedSeq[Any] = fact.midSentenceFactMessageArgs
    override def toString: String = fact.midSentenceFactMessage
  }

  private[scalatest] case class SimplifiedFactMessage(fact: Fact) extends LazyMessage {
    val nestedArgs: IndexedSeq[Any] = fact.simplifiedFactMessageArgs
    override def toString: String = fact.simplifiedFactMessage
  }

  private[scalatest] case class MidSentenceSimplifiedFactMessage(fact: Fact) extends LazyMessage {
    val nestedArgs: IndexedSeq[Any] = fact.midSentenceSimplifiedFactMessageArgs
    override def toString: String = fact.midSentenceSimplifiedFactMessage
  }
}
