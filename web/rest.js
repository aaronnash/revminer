//--------------------------------------------------
// Modified code originally written by Greg Brandt, Corneliu Suciu
// CSE 454
// Winter 2012
// 
// REST proxy for socket.io calls
//--------------------------------------------------

// Module dependencies
var journey = require('journey')
  , http = require('http')
  , io = require('socket.io-client');

// Network parameters
var serverPort = 8080
  , revminerURL = 'http://localhost';

// Connect to revminer
var revminerSocket = io.connect(revminerURL);
revminerSocket.on('connect', function() {
    console.log('Connected to ' + revminerURL);
});

// Create a journey router
var router = new(journey.Router);

// Create the routing table
router.map(function() {

    // GET /revminer/:query
    this.get(/revminer\/(.*)\/(.*)\/(.*)$/).bind(function(req, res, text, lat, long) {
        console.log("Revminer query: '" + unescape(text) + "' (" + lat + ")");
        
        req.connection.setTimeout(10000);
        
        // Revminer event handlers
        revminerSocket.on('results',     function(data) { res.send({"results": data}); });
        revminerSocket.on('suggestions', function(data) { res.send({"suggestions": data}); });
        revminerSocket.on('match',       function(data) { 
            res.send({"match": data}); 
            revminerSocket.emit('search', '');  // null search to clear gunk
        });

        // search revminer
        if (text !== '')
            revminerSocket.emit('search', {text: unescape(text).replace('not ', '!'), loc: {latitude: lat, longitude: long}});
    });
});

// Create the HTTP server
http.createServer(function (request, response) {
    var body = "";

    request.addListener('data', function (chunk) { body += chunk});
    request.addListener('end', function() {
        // Dispatch the request to the router
        router.handle(request, body, function(result) {
            response.writeHead(result.status, result.headers);
            response.end(result.body);
        });
    });

}).listen(serverPort);
console.log("Server listening on port " + serverPort);
