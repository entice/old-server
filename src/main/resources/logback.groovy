import static ch.qos.logback.classic.Level.*

// This is a workaround to make the hostname visible for a subscope
context.name = hostname

//displayStatusOnConsole()
setupAppenders()
setupLoggers()


def displayStatusOnConsole() {
  statusListener OnConsoleStatusListener
}


def setupAppenders() {
  def logfileDate = timestamp('yyyy-MM-dd')

  appender('logfile', FileAppender) {
    file = "logs/server.${logfileDate}.log"
    encoder(PatternLayoutEncoder) {
      pattern = "%-23date{ISO8601} %property{app.env} ${context.name}:%property{server.port} %-20.20thread [%-5level] %logger %X{akkaSource} >> %m%n%rEx"
    }
  }

  appender('systemOut', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
      pattern = "%-35(%relative : %cyan(%-10.10thread)) [%highlight(%.-4level)] %logger{0} %X{akkaSource} %red(>>) %m%n%rEx"
    }
  }
}


def setupLoggers() {
  logger("entice.server.implementation.loggers.NullLogger", getLogLevel(), [], false)
  logger("entice.server.implementation.loggers.TestLogger", getLogLevel(), ['systemOut'], false)
  logger("Cake", getLogLevel(), ['systemOut'])
  root(getLogLevel(), (isTestEnv() ? [] : ['logfile']))
}


def getLogLevel() {
  (isDevelopmentEnv() || isTestEnv()) ? DEBUG : INFO
}


def isDevelopmentEnv() {
  def env =  System.properties['app.env'] ?: 'DEV'
  env == 'DEV'
}


def isTestEnv() {
  def env =  System.properties['app.env'] ?: 'TEST'
  env == 'TEST'
}
