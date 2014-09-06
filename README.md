[![Build Status](https://travis-ci.org/entice/server.svg?branch=master)](https://travis-ci.org/entice/server)

The server manages worlds.
It authorises access to them and propagates the appropriate game-state.

**Installation**

Install GIT, SBT (Simple Build Tool) and MongoDB. MongoDB should be running with defaults.
This can be done by:

- `sudo apt-get install mongodb mongodb-server`
- `sudo service mongodb start`

Clone the repository with GIT, then change to that new directory:

- `git clone https://github.com/entice/server.git entice.server`
- `cd entice.server`

Before doing any of the other steps, enter the Simple-Build-Tool console:

- `sbt`

If you want to optionally seed the database do:

- `project seed`
- `run` (Type `Y` to proceed. MongoDB should be running)
- `project root` (This takes you back to the server project)

To start the server issue:

- `test` (All tests should be running, if not, type `exit` then again `sbt` and `test`, if they are still not running, post an issue describing your problem)
- `run` (Server status should be visible in ca. 5-10 lines of output, no exceptions should occur)


**Configuration**

The akka configuration is in `conf/akka.conf`, any server specific configuration is in `conf/config.json`. This should be self-explanatory.


**Options**

The following java system properties can be set to configure the server:

- `-Dserver.host=` the hostname that can be used to access the server
- `-Dserver.port=` with some port the server should bind to as int
- `-Dapp.env=` with either `DEV` / `TEST` (SBT sets these) or `PROD` (on heroku) to set the application's environment
