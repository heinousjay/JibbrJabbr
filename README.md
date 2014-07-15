JibbrJabbr
==========

An experiment in Java HTTP server concepts.
-------------------------------------------

[![Build Status](https://travis-ci.org/heinousjay/JibbrJabbr.png?branch=master)](https://travis-ci.org/heinousjay/JibbrJabbr)

Requires [JDK 8](http://openjdk.java.net/install/index.html)

Build using Gradle, for example

`./gradlew build`

then you can start the sample with

`cd build/libs`

`java -jar JibbrJabbr-0.5.jar app=../../test/src/test/resources/app2/app`

then browse to 

[http://localhost:8080](http://localhost:8080)

The capabilities of the system are currently limited.  There 
is a notion of modules as defined in the commonjs modules spec,
so you can include a script name whatever.js in the same directory
using 

`var whatever = require('whatever');`

You can subscribe to browser events and manipulate documents using jQuery-esque syntax,
in the server script, as in

`$('#button').click(function() { $('#output').text('you clicked a button!'); }`

There is an RPC mechanism that allows you to invoke in-browser javascript
(that follows certain rules) from the server directly.  The server is
event-driven and 100% asynchronous, however the API appears synchronous.

DON'T DO ANYTHING SERIOUS WITH THIS.  It is far from production ready.
It likely has massive security holes.  The API is barely specified,
never mind implemented.  You'd be crazy to use this as anything but
a toy.

If you try running the tests and you're getting weird errors, make sure you've enabled assertions.

Owes a spiritual debt to [webbit](https://github.com/webbit/webbit).  Owes debts of a different
nature to [Guice](https://code.google.com/p/google-guice/), [Netty](https://github.com/netty/netty),
[Rhino](https://github.com/mozilla/rhino), [jsoup](https://github.com/jhy/jsoup), and 
[jQuery](https://github.com/jquery/jquery)