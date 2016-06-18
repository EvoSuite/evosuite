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
AVOID String concatenation in Loggers

writing something like:

logger.debug("this is not "+ foo + " very " + bar +" efficient");

is not efficient, as most of the time debug logs are deactivated, and concatenating strings is
expensive. Recall String is immutable, and each "+" create a new String object.
The above logging can be rewritten into:

logger.debug("this is not {} very {} efficient", foo, bar);

Note: not a big deal for "warn"/"error", as those are/should be rare... but it can become
quite an overhead for trace/debug/info



--------------------------------------------
EvoSuite should be DETERMINISTIC

EvoSuite uses randomized algorithms. However, given the same random seed, the behavior should be
fully deterministic. This is essential for debugging EvoSuite. Unfortunately, there are few libraries/APIs
that are non-deterministic, like for example HashMap and HashSet. Rather use equivalent classes that
are deterministic, ie LinkedHashMap and LinkedHashSet.
Note: we have system tests to check if EvoSuite remains deterministic, eg see BaseDeterminismSystemTest.




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



---------------------------------------------
HOW TO MAKE A RELEASE

To use the Maven plugin, and to link the "runtime" jar (plus dependencies)
to a project, EvoSuite needs to be released and deployed on an accessible
repository.

Assume current EvoSuite version is x.y.z-SNAPSHOT. You need choose
a new version number (x for major, y for minor, and z for patch).

Once chosen a new version a.b.c (with *no* SNAPSHOT, and it is fine to
have a.b.c == x.y.z), create a new Git branch with name equal to this
new version number.
Then, from command line execute:

  mvn versions:set -DnewVersion=a.b.c

This command will go through all the pom files in the project, and replace
the version numbers there with the new one.
Commit and push the changed pom files.

To deploy to Maven Central, execute:

  mvn clean source:jar javadoc:jar verify -PsignJars -DskipTests   deploy

Note: this requires that you have configured GPG on your machine with the right
valid keys.

If the "Deploy" job ends correctly, then the new release has been deployed.
It might take up to 2 hours before it will be visible on Maven Central.

Now, on the Git master branch, need to set a new SNAPSHOT version:

  mvn versions:set -DnewVersion=a.b.(c+1)-SNAPSHOT

(eg, here we just incremented the patch number by one).
Commit and push the modified pom files.






