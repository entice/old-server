The server manages authorization and gamestate propagation.

**Installation**  
Install SBT (Simple Build Tool) and MongoDB. MongoDB should be running with defaults.  
Then issue:
- `sbt`
- `compile`
- `test` (All tests should be running, if not, type `exit` then again `sbt` and `test`, if they are still not running, post an issue describing your problem)
- `run` (Server status should be visible in ca. 5-10 lines of output, no exceptions should occur)