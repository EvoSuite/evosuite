
package de.unisb.cs.st.testability;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Vector;

// ----------------------------------------------------------------------------
/**
 * A simple static API for listing classes loaded in a JVM. {@link #getClassLocation(Class)}
 * from http://www.javaworld.com/javaworld/javaqa/2003-07/01-qa-0711-classsrc.html
 * also thrown in for a good measure. See individual methods for further details.<P>
 *
 * NOTE: use for testing/debugging only.
 *
 * @author (C) <a href="http://www.javaworld.com/columns/jw-qna-index.shtml">Vlad Roubtsov</a>, 2003
 */
public
abstract class ClassScope // this class is not instantiable
{
    // public: ................................................................

    /**
     * Given a class loader instance, returns all classes currently loaded
     * by that class loader.
     *
     * @param defining class loader to inspect [may not be null]
     * @return Class array such that every Class has 'loader' as its
     * defining class loader [never null, may be empty]
     *
     * @throws RuntimeException if the "classes" field hack is not possible
     * in this JRE
     */
    public static Class [] getLoadedClasses (final ClassLoader loader)
    {
        if (loader == null) throw new IllegalArgumentException ("null input: loader");
        if (CLASSES_VECTOR_FIELD == null) throw new RuntimeException ("ClassScope::getLoadedClasses() cannot be used in this JRE", CVF_FAILURE);

        try
        {
            final Vector classes = (Vector) CLASSES_VECTOR_FIELD.get (loader);
            if (classes == null) return EMPTY_CLASS_ARRAY;

            final Class [] result;

            // note: Vector is synchronized in Java 2, which helps us make
            // the following into a safe critical section:

            synchronized (classes)
            {
                result = new Class [classes.size ()];
                classes.toArray (result);
            }

            return result;
        }
        // this should not happen if <clinit> was successful:
        catch (IllegalAccessException e)
        {
            e.printStackTrace (System.out);

            return EMPTY_CLASS_ARRAY;
        }
    }

    /**
     * A convenience multi-loader version of {@link #getLoadedClasses(ClassLoader)}.
     *
     * @param an array of defining class loaders to inspect [may not be null]
     * @return Class array [never null, may be empty]
     *
     * @throws RuntimeException if the "classes" field hack is not possible
     * in this JRE
     */
    public static Class [] getLoadedClasses (final ClassLoader [] loaders)
    {
        if (loaders == null) throw new IllegalArgumentException ("null input: loaders");

        final List /* Class */ resultList = new LinkedList ();

        for (int l = 0; l < loaders.length; ++ l)
        {
            final ClassLoader loader = loaders [l];
            if (loader != null)
            {
                final Class [] classes = getLoadedClasses (loaders [l]);

                resultList.addAll (Arrays.asList (classes));
            }
        }

        final Class [] result = new Class [resultList.size ()];
        resultList.toArray (result);

        return result;
    }

    /**
     * Returns the class loader set "relevant" to the calling class, as
     * described in the article. Starting with the class that is the caller of
     * this method, it collects all class loaders that are loaders for all classes
     * on the call stack and their respective parent loaders.
     *
     * @return ClassLoader array [never null]
     *
     * @throws RuntimeException if the caller context resolver could not be
     * instantiated
     */
    public static ClassLoader [] getCallerClassLoaderTree ()
    {
        if (CALLER_RESOLVER == null) throw new RuntimeException ("ClassScope::getCallerClassLoaderTree() cannot be used in this JRE", CR_FAILURE);

        final Class [] callContext = CALLER_RESOLVER.getClassContext ();

        final Set /* ClassLoader */ resultSet = new HashSet ();

        for (int c = 2; c < callContext.length; ++ c)
        {
            getClassLoaderTree (callContext [c], resultSet);
        }

        final ClassLoader [] result = new ClassLoader [resultSet.size ()];
        resultSet.toArray (result);

        return result;
    }


    /**
     * Given a Class object, attempts to find its .class location [returns null
     * if no such definiton could be found].
     *
     * @return URL that points to the class definition [null if not found]
     */
    public static URL getClassLocation (final Class cls)
    {
        if (cls == null) throw new IllegalArgumentException ("null input: cls");

        URL result = null;
        final String clsAsResource = cls.getName ().replace ('.', '/').concat (".class");

        final ProtectionDomain pd = cls.getProtectionDomain ();
        // java.lang.Class contract does not specify if 'pd' can ever be null;
        // it is not the case for Sun's implementations, but guard against null
        // just in case:
        if (pd != null)
        {
            final CodeSource cs = pd.getCodeSource ();
            // 'cs' can be null depending on the classloader behavior:
            if (cs != null) result = cs.getLocation ();

            if (result != null)
            {
                // convert a code source location into a full class file location
                // for some common cases:
                if ("file".equals (result.getProtocol ()))
                {
                    try
                    {
                        if (result.toExternalForm ().endsWith (".jar") ||
                            result.toExternalForm ().endsWith (".zip"))
                            result = new URL ("jar:".concat (result.toExternalForm ())
                                .concat("!/").concat (clsAsResource));
                        else if (new File (result.getFile ()).isDirectory ())
                            result = new URL (result, clsAsResource);
                    }
                    catch (MalformedURLException ignore) {}
                }
            }
        }

        if (result == null)
        {
            // try to find 'cls' definition as a resource; this is not
            // documented to be legal but Sun's implementations seem to allow this:
            final ClassLoader clsLoader = cls.getClassLoader ();

            result = clsLoader != null ?
                clsLoader.getResource (clsAsResource) :
                ClassLoader.getSystemResource (clsAsResource);
        }

        return result;
    }

    // protected: .............................................................

    // package: ...............................................................

    // private: ...............................................................


    /**
     * A helper class to get the call context. It subclasses SecurityManager
     * to make getClassContext() accessible. An instance of CallerResolver
     * only needs to be created, not installed as an actual security
     * manager.
     */
    private static final class CallerResolver extends SecurityManager
    {
        protected Class [] getClassContext ()
        {
            return super.getClassContext ();
        }

    } // end of nested class


    private ClassScope () {} // this class is not extendible

    private static void getClassLoaderTree (final Class cls, final Set resultSet)
    {
        if ((cls != null) && (resultSet != null))
        {
            for (ClassLoader loader = cls.getClassLoader (); loader != null; loader = loader.getParent ())
            {
                resultSet.add (loader);
            }
        }
    }


    private static final Field CLASSES_VECTOR_FIELD; // set in <clinit> [can be null]
    private static final CallerResolver CALLER_RESOLVER; // set in <clinit> [can be null]

    private static final Class [] EMPTY_CLASS_ARRAY = new Class [0];
    private static final Throwable CVF_FAILURE, CR_FAILURE; // set in <clinit>

    static
    {
        Throwable failure = null;

        Field tempf = null;
        try
        {
            // this can fail if this is not a Sun-compatible JVM
            // or if the security is too tight:

            tempf = ClassLoader.class.getDeclaredField ("classes");
            if (tempf.getType () != Vector.class)
                throw new RuntimeException ("not of type java.util.Vector: " + tempf.getType ().getName ());

            tempf.setAccessible (true);
        }
        catch (Throwable t)
        {
            failure = t;
        }
        CLASSES_VECTOR_FIELD = tempf;
        CVF_FAILURE = failure;

        failure = null;
        CallerResolver tempcr = null;
        try
        {
            // this can fail if the current SecurityManager does not allow
            // RuntimePermission ("createSecurityManager"):

            tempcr = new CallerResolver ();
        }
        catch (Throwable t)
        {
            failure = t;
        }
        CALLER_RESOLVER = tempcr;
        CR_FAILURE = failure;
    }

} // end of class
// ----------------------------------------------------------------------------