var output;

function init(wsUri) {
  output = document.getElementById("output");
  registerWebSocket(wsUri);
}

function registerWebSocket(wsUri) {
  websocket = new WebSocket(wsUri);
  websocket.onopen    = function(evt) { writeToScreen("CONNECTED") };
  websocket.onclose   = function(evt) { writeToScreen("DISCONNECTED") };
  websocket.onerror   = function(evt) { writeToScreen('<span style="color: red;">ERROR: </span> ' + evt.data) };
  websocket.onmessage = function(evt) { onMessage(evt) };
  window.onunload     = function()    { websocket.close() };
}

function onMessage(evt) {
  message = JSON.parse(evt.data)
  if (message.type === "Ping") {
    writeToScreen('<span style="color: orange;">PING: </span>' + message.roundTripTime);
    doSend({type:"Pong"})
  } else {
    writeToScreen('<span style="color: blue;">GOT: </span>' + message);
  }
}

function doSend(message) {
  if (typeOf(message) === "Sring")       { printAndSend(message) }
  else if (typeOf(message) === "Object") { printAndSend(JSON.stringify(message)) }
  else                                   { writeToScreen('<span style="color: red;">ERROR:</span> Sending not possible, unkown data type ' + typeOf(message)) }
}

function doPing() {
  doSend({type:"Ping",roundTripTime:100})
}

function printAndSend(message) {
  writeToScreen('<span style="color: green;">PUT: </span>' + message);
  websocket.send(message);
}

function writeToScreen(message) {
  var pre = document.createElement("p");
  pre.style.wordWrap = "break-word";
  pre.innerHTML = message;
  output.appendChild(pre);
}
