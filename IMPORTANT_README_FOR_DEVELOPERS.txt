NOTES FOR DEVELOPERS

These notes are meant for developers working on EvoSuite.
There are several rules of thumb regarding how to write "good code",
but often rules are either too generic and not tailored for a given particular
piece of software (e.g., different kinds of architectures).

The rules of thumb described here in this document are not meant to be either exhaustive nor absolute.
Rigid rules are not substitute for common sense, as they are rather guidelines that can
be ignored in some special cases.
Furthermore, the guidelines need to be _realistic_ and easy to use: there would be no point
to ask for detailed comments on each single method/field and 100% coverage test suites...



---------------------------------------------
AVOID System.out AND System.err

EvoSuite uses a logging framework, in which client processes do communicate with Master through TCP.
For debugging  and logging errors in a class Foo, create a logger in the following way:

private static Logger logger = LoggerFactory.getLogger(Foo.class);

It is important to keep the same name 'logger' to make things consistent among classes.
If the logging should be part the actual output for the console user, then rather use: 

LoggingUtils.getEvoLogger()



---------------------------------------------
DO NOT USE System.exit

Better to throw an exception, as the entry point of EvoSuite does some logging when ends.
Furthermore, System.exit becomes problematic when unit testing EvoSuite.



---------------------------------------------
STATIC VARIABLES ARE YOUR ENEMY

Static variables should be either constant or representing transient data (eg cache information whose presence/missing
has only effect on performance, not on functionality).
Having "classes with states" is usually a poor OO design (an exception to this rule is org.evosuite.Properties).
If those are really needed, then you should rather use a singleton pattern. 
This is not just to be pedantic, but, really, non-constant static variables make unit testing far much harder
and lead to code that is more difficult to understand and maintain. 



---------------------------------------------
HOW TO WRITE JUNIT TEST CASES

Until EvoSuite will not be applicable to itself, there is the need to write manual test cases.
They should be put in the "src/test/java" folder, following the same package structure as EvoSuite code.
A unit test suite for SUT org.evosuite.somepackage.Foo should be called org.evosuite.somepackage.FooTest.
This is useful for several reasons:
- Need to know what class the test case is supposed to unit test by just looking at its name
- Should be easy to identify if a class has a test suite for it
- If in same package, then the test suite can access package/protected fields/methods
- Having "Test" as postfix (instead of a prefix) is useful for when searching for classes by name (eg CTRL-SHIFT-t in Eclipse)

If for testing there is the need to create additional, support classes used as data input for EvoSuite,
then those will need to be put in the com.examples.with.different.packagename package.

---------------------------------------------
AVOID TOO LONG METHODS

Too long methods (eg more than 100 lines) should be split, as difficult to understand.
For this task, in Eclipse, you can right-click on a code snippet and "Refactor -> Extract Method..." 



---------------------------------------------
WRITE COMMENTS

In the ideal world, each class/method/field would have nice, detailed, appropriate code comments.
But even in such a beautiful world, everything would go to hell at the first code change, as that might
require manually changing most of the code comments.

Cannot really quantify how much comments one should write, but at least it would be good to have:
- brief (1-2 sentences) description of what the class is useful for (just before the class declaration) 
- for fields that are data structures (collections,arrays) some comments would be useful, as long and detailed 
  variable names are not practical

When writing a comment for a class/method/field, use JavaDoc style:
/**
*/
In this way, tools like Eclipse will show the comments when you hover with the mouse over them.
  

---------------------------------------------
IF CANNOT AVOID EXTERNAL SIDE-EFFECTS, DO DOCUMENT IT!!!

If a call on a object has side-effects outside the class itself (eg writing to disk, add a system hook thread),
then this needs to be documented (see point on how to write comments).  


---------------------------------------------
PRE and POST CONDITIONS

- Pre-conditions of 'public' methods should throw exceptions explicitly (eg, IllegalArgumentException and IllegalStateException).
  Whenever possible, it is worth to write pre-conditions to public methods.
  If exceptions are thrown with 'throw new ...', then recall to add "throws ..." to the method signature.
- Pre-conditions of 'private' methods and post-conditions (both public and private methods) should use the keyword 'assert'.
  (An exception is when the validation of inputs of a public method is delegated/moved to a private method: in this case use 'throw'.)
  Post-conditions are good, but often are difficult to write.
  Note: a post-condition does not to be complete to be useful (ie find bugs). For example, if we have 'A && B', but the writing
  of 'B' is too difficult (or time consuming), still having just 'A' as post-condition can help  
  
  
---------------------------------------------
FIELDS/CONSTRUCTORS/METHODS ORDER IN A CLASS 

when writing a new class (or re-factoring a current one), fields should come first, followed by class constructors and then the other methods.

  