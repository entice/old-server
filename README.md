The server manages authorization and gamestate propagation.

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
