The server manages authorization and gamestate propagation.

**Installation**  
Install SBT (Simple Build Tool) and MongoDB. MongoDB should be running with defaults.  
This can be done by:
- `sudo apt-get install mongodb mongodb-server`
- `sudo service mongodb start`

To seed the database issue:
- `sbt` (Opens the Simple-Build-Tool console)
- `project seed`
- `run` (Say 'Y' to seed the database. MongoDB should be running)

To start the server issue:
- If you're not yet in sbt, issue `sbt`
- `test` (All tests should be running, if not, type `exit` then again `sbt` and `test`, if they are still not running, post an issue describing your problem)
- `run` (Server status should be visible in ca. 5-10 lines of output, no exceptions should occur)
