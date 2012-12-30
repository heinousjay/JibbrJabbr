Welcome to JibbrJabbr!

here is the requisite chat example, par excellence.

the programming model has a few interesting features

1. threading concerns are handled under the covers,
   to present as consistent a programming experience
   as possible.  at all phases, you program as
   though you are interacting with a single client in
   a single thread

2. there is a transparent rpc mechanism with no
   configuration. define a function in the client
   file according to the conventions presented there,
   and it can be called from the server like it lives
   in that environment. this is unidirectional, no
   mechanism is provided to allow the client to
   call arbitrary functions on the server, however...

3. events can be handled on the server. register for
   the events like you would in jQuery. in fact, the
   server API shares a fair bit with jQuery, although
   there are some important differences.
