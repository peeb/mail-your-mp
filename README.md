# Mail Your MP!

## What?

A simple ClojureScript application prototype to allow [38 Degrees][38degrees] members to quickly locate and email their local constituency MP.

The source code for the application can be found under `src/mmp`.

## Why?

The idea is to have a simple and small, standalone web tool to lookup who your local MP is and provide her email address to contact.

## How?

The app is written in [ClojureScript][cljs] in a [functional reactive programming][frp] style. Application behaviour flows from immutable application state - reacting continuously to coordinated changes in the value of this state. When a valid postcode is entered the application searches for the Westminster constituency for this postcode and, if found, searches for and stores the available data for the sitting MP for the constituency the postcode belongs to. 

Essentially, we "react" to the MP information stored in the application state. By resetting state, we reset the application.

As a point of potential interest, the networking (or AJAX) code is written in a [communicating sequential processes][csp] style, which overcomes the infamous [callback hell][hell] which continues to plague JavaScript.

## Who?

This application was written by [Paul Burt][pycurious].

[38degrees]: http://www.38degrees.org.uk/
[pycurious]: https://twitter.com/pycurious
[cljs]: https://github.com/clojure/clojurescript
[frp]: https://en.wikipedia.org/wiki/Functional_reactive_programming
[csp]: https://en.wikipedia.org/wiki/Communicating_sequential_processes
[hell]: http://callbackhell.com/
