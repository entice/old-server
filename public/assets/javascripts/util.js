var stringConstructor = "test".constructor;
var arrayConstructor = [].constructor;
var objectConstructor = {}.constructor;

function typeOf(object) {
    if (object === null)                               { return "null"; }
    else if (object === undefined)                     { return "undefined"; }
    else if (object.constructor === stringConstructor) { return "String"; }
    else if (object.constructor === arrayConstructor)  { return "Array"; }
    else if (object.constructor === objectConstructor) { return "Object"; }
    else                                               { return "unknown";}
}
