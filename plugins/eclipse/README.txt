Building Eclipse projects with Maven is cumbersome, as mixing Eclipse and
Maven dependencies seems rather impossible. For now, the build thus isn't
automated.

To build the plugin, evosuite-eclipse-core expects a copy of evosuite.jar.
Then, either build a deployable feature from Eclipse for
evosuite-eclipse-feature, or build the evosuite-eclipse-site from within
Eclipse.
