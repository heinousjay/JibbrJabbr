JibbrJabbr
==========

An experiment in Java HTTP server concepts.
-------------------------------------------

Requires [JDK 7](http://openjdk.java.net/install/index.html)
and you'll probably want [Maven 3](http://maven.apache.org).
Maven 2 also works, for now.

Doesn't do a whole heck of a lot, yet.  Run the build,
put JibbrJabbr-server/JibbrJabbr/target/JibbrJabbr-1.0-SNAPSHOT-all.jar in a directory
of your choosing, then run

`$JDK7_HOME/bin/java -jar JibbrJabbr-1.0-SNAPSHOT-all.jar /path/to/your/test/directory`

and browse to

[http://localhost:8080](http://localhost:8080)

There is a partial chat sample living in JibbrJabbr-server/JibbrJabbr-kernel/src/test/resources/ you can use to explore.

The capabilities of the system are currently very limited.  There 
is a notion of modules as defined in the commonjs modules spec,
so you can include a script using require('./whatever')

There is a basic jQuery-like API for document manipulations.

DON'T DO ANYTHING SERIOUS WITH THIS.  It is far from production ready.
It likely has massive security holes.  The API is barely specified,
never mind implemented.  You'd be crazy to use this as anything but
a toy.

