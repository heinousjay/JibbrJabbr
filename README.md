JibbrJabbr
==========

An experiment in Java HTTP server concepts.
-------------------------------------------

[![Build Status](https://travis-ci.org/heinousjay/JibbrJabbr.png?branch=master)](https://travis-ci.org/heinousjay/JibbrJabbr)

Requires [JDK 7](http://openjdk.java.net/install/index.html)

Build using Gradle, for example

`./gradlew build`

then you can start the sample with

`cd build/libs`
`java -jar Jibbr-Jibbr-0.5.jar app=../../test/src/test/resources/app2/app`

then browse to 

[http://localhost:8080](http://localhost:8080)

The capabilities of the system are currently limited.  There 
is a notion of modules as defined in the commonjs modules spec,
so you can include a script using require('./whatever').  You can
subscribe to events from the server

There is a basic jQuery-like API for document manipulations.

DON'T DO ANYTHING SERIOUS WITH THIS.  It is far from production ready.
It likely has massive security holes.  The API is barely specified,
never mind implemented.  You'd be crazy to use this as anything but
a toy.

If you try running the tests and you're getting weird errors, make sure you've enabled assertions.

Owes a spiritual debt to [webbit](https://github.com/webbit/webbit).  Owes debts of a different
nature to [Guice](https://code.google.com/p/google-guice/), [Netty](https://github.com/netty/netty),
[Rhino](https://github.com/mozilla/rhino), [jsoup](https://github.com/jhy/jsoup), and 
[jQuery](https://github.com/jquery/jquery)