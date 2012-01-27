// external libraries
var express = require('express');
var util = require('util');
var fs = require('fs');
var mongodb = require('mongodb');
// !external libraries

// global configuration
// Mongo DB
var MONGO_HOST = 'localhost';
var MONGO_PORT = 27017;
var MONGO_NAME = 'revminer';
var MONGO_OPTIONS = {native_parser: false, strict: true};

// HTTP Server
var HTTP_PORT = 80;
var SEND_LIMIT = 10;
// !global configuration

// global variables
var db = null;

var attributeMapping = {};
var similarData = {};
var polarityData = {};
var metadata = {};
var reviewData = {};
var placeDataDetailed = {};
var attributeCategories = {};
var filteredValues = {};

var allPlaces = {};
var allAttributes = {};
var allValues = {};
var allCities = {};
var allNeighborhoods = {};
// !global variables


// SCRIPT EXECUTION START


// connect to mongodb
db = new mongodb.Db(MONGO_NAME, new mongodb.Server(MONGO_HOST, MONGO_PORT, {}), MONGO_OPTIONS);

db.open(function(err, db) {
	if (err) throw err;
	console.log("connected to mongodb");

	// Get handle to 'data' collection
	db.collection('data', function(err, collection) {
		if (err) throw err;
		db.data = collection;
		load();
	});
});

// create HTTP server w/ socket.io enabled
var server = express.createServer(express.logger());
var io = require('socket.io').listen(server);
server.listen(HTTP_PORT);
server.use(express.static(__dirname + '/public'));

// configure socket.io
io.configure(function() {
	io.enable('browser client minification');
	io.enable('browser client etag');
	io.set('log level', 1);
	io.set('close timeout', 300);
	io.set('heartbeat timeout', 60);
	io.set('heartbeat interval', 120);

	io.set('transports', [
		'websocket'
	, 'flashsocket'
	, 'xhr-polling'
	, 'htmlfile'
	, 'jsonp-polling'
	]);

	// Set event handler for socket.io requests
	io.sockets.on('connection', handleConnection);
});


// FUNCTION LIBRARY

// Client event handlers
function handleConnection(client) {
	client.on('values', wrapWithHandler(handleClientValues(client)));
	client.on('change', wrapWithHandler(handleClientChange(client)));
	client.on('search', wrapWithHandler(handleClientSearch(client)));
	client.on('giveFeedback', wrapWithHandler(handleClientFeedback(client)));
}

/*
 * Wraps the provided function with a generic exception handler that will catch
 * all exceptions and prevent the exception from traveling further up the stack.
 */
function wrapWithHandler(f) {
	return (function(a) {
		try {
			f(a);
		} catch (e) {
			console.log("------- EXCEPTION -------");
			console.log(e)
			console.log("-------------------------");
		}
	});
}

function handleClientValues(client) {
	return function(attribute) {
		console.log("values: " + attribute);
		var attributeC = attribute;
		if (attribute in attributeMapping) {
			attributeC = attributeMapping[attribute];
		}
		getCommonValues(attributeC, client);
	}
}

function handleClientChange(client) {
	return function(query) {
		console.log("--- change ---");
		console.log(query);
		console.log(query.text);
		console.log(query.loc);
		console.log("--- !change ---");
		query = query.text;
		
		console.log("change: " + query);
		if (query === '') {
			sendMatch[client.id] = '';
			return;
		}
		query = query.toLowerCase().replace(/ /g, '-').replace(',', '');
		if (query in allPlaces) {
			console.log('exact match: ' + query);
			sendMatch(query, client);
		} else {
			partialMatch(query, client);
		}
	}
}

function handleClientSearch(client) {
	return function(query) {
		console.log("--- search ---");
		console.log(query);
		console.log(query.text);
		console.log(query.loc);
		console.log("--- !search ---");
		loc = {latitude: new Number(query.loc.latitude), longitude: new Number(query.loc.longitude)};
		query = query.text;
		
		var interesting = false;
		if (typeof(query) === 'object') {
			// query for interesting values
			query = query[0];
			interesting = true;
			console.log('search for interesting values: ' + query);
		} else {
				console.log("search: " + query);
		}
		if (query === '') {
			sendMatch[client.id] = '';
			return;
		}
		var startTime = new Date().getTime();
		query = query.toLowerCase();
		var placeQuery = query.replace(/ /g, '-').replace(',', '');
		// first try for an exact match
		// if that fails, check how many partial matches exist
		// if >= 2, show both the query results and the partial match
		//   but if there are not query results, just show partial matches
		// if 1, show just the partial match
		// if 0, show just the query results
		if (placeQuery in allPlaces) {
			console.log('exact match: ' + placeQuery);
			sendMatch(placeQuery, client, interesting);
			return;
		}

		// find all places that contain the placeQuery text
		var suggestions = [];
		for (var place in allPlaces) {
			if (place.indexOf(placeQuery) !== -1) {
				suggestions.push(place);
			}
			if (suggestions.length > SEND_LIMIT) {
				break;
			}
		}
		
		if (suggestions.length === 1) { // if only one partial match, send it back as a match
			console.log('partial match: ' + suggestions[0]);
			sendMatch(suggestions[0], client);
			return;
		}
		
		var processed = processQuery(query);
		var queries = clausesToQueries(processed);
		console.log('queries: ' + util.inspect(queries));
		
		if (queries.length === 0) { // if nothing can be extracted for the query just send the suggestions (partial matches)
			if (suggestions.length > 1)
				sendSuggestion(suggestions, client);
			else
				sendSuggestion(['No places matching your search were found.'], client);
			return;
		}

		
		var remainingDbFinds = queries.length;
		var candidates = 0;
		for (var i = 0; i < queries.length; ++i) {
			var dbquery = queries[i];
			db.data.find(dbquery, function(err, cursor) {
				cursor.toArray(function(err, results) {
					if (results.length === 0) { // if nothing matches the query, send the suggestions (partial matches)
						if (suggestions.length > 1)
							sendSuggestion(suggestions, client);
						else
							sendSuggestion(['No places matching your search were found.'], client);
						return;
					}
					
					var newCandidates = {};
					for (var j = 0; j < results.length; j++) {
						var result = results[j];
						if (!(result.p in newCandidates))
							newCandidates[result.p] = 0;
						newCandidates[result.p] += result.f;
					}
					
					if (candidates === 0) {
						candidates = newCandidates;
					} else {
						for (var nc in candidates) {
							if (nc in newCandidates) {
								console.log('candidates[' + nc + ']: ' + candidates[nc]);
								candidates[nc] *= newCandidates[nc];
								console.log('  updated candidates[' + nc + ']: ' + candidates[nc]);
							}
							else
								delete candidates[nc];
						}
						// TODO: if candidates.length is 0, revert candidates to a backupCandidates and set remainingDbFinds to 1 and queries.slice from 0 to i
						// basically, we want to do a back-off in case the query is too specific
						// but this only does a 1-clause back-off
					}
					remainingDbFinds--;

					
					if (remainingDbFinds === 0) { // once all queries have been processed order, truncate, and send results
						var sortedCandidates = topCandidates(candidates, loc);
						var queryResults = {};
						var queryMeta = {};
						var toExamine = [];
						client.emit('polarity', {place: null, polarity: null});
						for (var cc in sortedCandidates.top) {
							toExamine.push(cc);
							
							if (!(cc in queryResults)) {
								queryResults[cc] = [];
								queryMeta[cc] = [];
							}
							
							queryResults[cc] = sortedCandidates.top[cc];
							queryMeta[cc] = metadata[cc];
							console.log('latitude: ' + metadata[cc].Latitude);
							console.log('  longitude: ' + metadata[cc].Longitude);
							console.log('  distance: ' + calcDistance({longitude: new Number(metadata[cc].Longitude), latitude: new Number(metadata[cc].Latitude)}, loc) + ' mi');
						}

						sendMatch[client.id] = query;
						console.log('results: ' + util.inspect(queryResults));
						//console.log('meta: ' + util.inspect(queryMeta));
						client.emit('results', { meta:queryMeta, data: queryResults, query: util.inspect(queries), num: sortedCandidates.num, suggestions: suggestions, time: new Date().getTime() - startTime});
					}
				});
			});
		}
	}
}

function handleClientFeedback(client) {
	return function(feedback) {
		console.log("feedback given: " + feedback);
        fs.createWriteStream("feedback.txt", {
            flags: "a",
            encoding: "encoding",
            mode: 0666
        }).write(feedback + "\n-----------------------\n");
	}
}
// !Client event handlers

function getCommonValues(attribute, client) {
	//var allValues = db.data.group({cond: {a: attribute}, key: {v: true}, reduce: function(doc, out) {out.count++;}, initial: {f: 0}});
	db.data.group({v: true}, {a: attribute}, {count: 0}, function(doc, out) {out.count += doc.f;}, true, function(err, results) {
		results.sort(function(a, b) {return b.count - a.count;});
		var values = results.map(function(i) {return i.v;});
		client.emit('values', values.slice(0, SEND_LIMIT));
	});
}

function partialMatch(query, client) {
	var suggestions = [];
	for (var place in allPlaces) {
		if (place.indexOf(query) !== -1) {
			suggestions.push(place);
		}
		if (suggestions.length > SEND_LIMIT) {
			sendSuggestion(['Too many matches to list, continue typing...'], client);
			return;
		}
	}
	if (suggestions.length > 1)
		sendSuggestion(suggestions, client);
	else if (suggestions.length === 1) {
		console.log('partial match: ' + suggestions[0]);
		sendMatch(suggestions[0], client);
	} else if (suggestions.length === 0) {
		sendSuggestion(['Either search for a place, or something like: sushi fresh fish great service. Then press the &lt;return&gt; key.'], client);
	}
}

function avg2(arr) {
	if (!arr)
		return '00';
	var sum = 0;
	var count = 0;
	for (var i = 0; i < arr.length; i++) {
		sum += arr[i] * (i+1);
		count += arr[i];
	}
	if (count < 10) // 5 sometimes results in misclassified polarities
		return '00'; // not enough data to be sure
	return Math.round(10 * sum / count);
}

function sendMatch(placeName, client, interestingness) {
	if (sendMatch[client.id] === (interestingness ? 'I-' : '') + placeName) {
		console.log('already sent ' + placeName + ' to ' + client.id);
		return;
	}
	sendMatch[client.id] = (interestingness ? 'I-' : '') + placeName;
	db.data.find({p: placeName}, {p: 0}, {sort: [['f', -1]]}, function(err, cursor) {
	//db.data.find({p: placeName}, {p: 0}, function(err, cursor) {
		cursor.toArray(function(err, results) {
			if (results.length > 0) {
				// to revert to numerical sorting, comment out the next line and replace the line 3 lines above with the line 4 lines above
				//results.sort(function(a,b) {return parseInt(avg2(polarityData[b.v]), 10) - parseInt(avg2(polarityData[a.v], 10));});
				var placeCompiled = {};
     		for (var i = 0; i < results.length; ++i) {
					if (!(results[i].a in placeCompiled))
						placeCompiled[results[i].a] = {};
					var polarityValue = avg2(polarityData[results[i].v]) + results[i].v;
					placeCompiled[results[i].a][polarityValue] = [placeDataDetailed[placeName][results[i].a][results[i].v]];
				  placeCompiled[results[i].a][polarityValue].push(polarityData[results[i].v]);
        }

				var attributeCounter = [];
        for (var a in placeCompiled) {
					attributeCounter.push([a, 0]);
					for (var v in placeCompiled[a]) {
						attributeCounter[attributeCounter.length-1][1] += placeCompiled[a][v][0].length;
          }
				}

				attributeCounter = attributeCounter.sort(function(a,b) {return b[1] - a[1];});
				var tmpAttributes = [];
				for (var i = 0; i < attributeCounter.length; i++) {
					tmpAttributes.push([attributeCounter[i][0], placeCompiled[attributeCounter[i][0]], polarityData[results[i].v]]);//, polarityData[placeCompiled[i][0].substring(2)]]);
				}

				console.log("----------- DATA ----------");
				console.log(placeName);
				console.log(metadata[placeName]);
				console.log("----------- !DATA ----------");

  			client.emit('match', {place: placeName, data: tmpAttributes, reviews: reviewData[placeName], similar: similarData[placeName], attributeCategories: attributeCategories, filteredValues: filteredValues, meta: metadata[placeName]});
			} else {
				console.log('Place exists in allPlaces but not in db. DB not finished loading?');
      			return;
    	}
    });
	});
}

function sendSuggestion(suggestions, client) {
	sendMatch[client.id] = null;
	client.emit('suggestions', suggestions);
}

// return clauses in the form:
//	{clauses: {attribute1: [value1, value2], attribute2: [value3], attribute3: [], attributeNone: [value4]},
//   {cities: {city1, city2}
//   {neighborhoods: {neighborhood1, neighborhood2}}
function processQuery(query) {
	var result = {};
	result['clauses'] = []
	result['cities'] = [];
	result['neighborhoods'] = [];
	var tokens = query.replace(/[^a-zA-Z! 0-9]+/g, '').split(' ');
	var currentValues = [];
	for (var i = 0; i < tokens.length; ++i) {
		var token = tokens[i];
		// 1 word look-ahead for 2-word attributes
		if (i + 1 < tokens.length && token + ' ' + tokens[i+1] in allNeighborhoods) {
			console.log(token + ' ' + tokens[i+1]);
			result['neighborhoods'].push(token + ' ' + tokens[i+1]);
			++i;
		} else if (token in allNeighborhoods) {
			result['neighborhoods'].push(token);
		} else if (i + 1 < tokens.length && token + ' ' + tokens[i+1] in allCities) {
			result['cities'].push(token + ' ' + tokens[i+1]);
			++i;
		} else if (token in allCities) {
			result['cities'].push(token);
		}
		else if (i + 1 < tokens.length && token + ' ' + tokens[i+1] in allAttributes) {
			result['clauses'][token + ' ' + tokens[i+1]] = currentValues;
			currentValues = [];
			++i;
		} else if (token in allAttributes) {
			result['clauses'][token] = currentValues;
			currentValues = [];
		} else if (token in allValues) {
			currentValues.push(token);
		}
	}
	if (currentValues.length > 0) {
		result['clauses']['*'] = currentValues;
	}
	return result;
}

function clausesToQueries(processed) {
	var	dbqueries = [];
	for (var attribute in processed['clauses']) {
		var attributeC = attribute;
		if (attribute in attributeMapping) {
			attributeC = attributeMapping[attribute];
		}
		var values = processed['clauses'][attribute];
		if (values.length === 0)
			dbqueries.push({a: attributeC});
		else
			for (var i = 0; i < values.length; ++i) {
				var value = values[i];
				if (attribute === '*')
					dbqueries.push({v: value});
				else
					dbqueries.push({a: attributeC, v: value});
			}
	}

	// if empty by end, return empty array
	var isEmpty = false;
	if (dbqueries.length == 0) {
		dbqueries.push({});
		isEmpty = true;
	}

	// TODO: This chooses the last processed city
	for (var i=0; i<processed['cities'].length; i++) {
		isEmpty = false;
		var numQueries = dbqueries.length;
		for (var j=0; j<numQueries; j++) {
			dbqueries[j]['c'] = processed['cities'][i];
			/*if (dbqueries[j]['c']) {
				var newQuery = {};
				for (var a in dbqueries[j]) {
					newQuery[a] = dbqueries[j][a];
				}
				newQuery['c'] = processed['cities'][i];
				dbqueries.push(newQuery);
			} else {
				dbqueries[j]['c'] = processed['cities'][i];
			}*/
		}
	}

	// TODO: This chooses the last processed neighborhood
	for (var i=0; i<processed['neighborhoods'].length; i++) {
		isEmpty = false;
		var numQueries = dbqueries.length;
		for (var j=0; j<numQueries; j++) {
			dbqueries[j]['n'] = processed['neighborhoods'][i];
			/*if (dbqueries[j]['n']) {
				var newQuery = {};
				for (var a in dbqueries[j]) {
					newQuery[a] = dbqueries[j][a];
				}
				newQuery['n'] = processed['neighborhoods'][i];
				dbqueries.push(newQuery);
			} else {
				dbqueries[j]['n'] = processed['neighborhoods'][i];
			}*/
		}
	}

	if (!isEmpty) return dbqueries;
	else return [];
}

/* Narrows down a list of canidates based on their interestingness
 *
 * Returns up to SEND_LIMIT canidates
 */
function topCandidates(candidates, location) {
	var retVal = {};
	var candidatesArray = [];

	console.log("topCandidates()");
	console.log(candidates.first);

	// used to store the range of distances and scores
	var distRange = {min: Number.MAX_VALUE, max: Number.MIN_VALUE};
	var matchRange = {min: Number.MAX_VALUE, max: Number.MIN_VALUE};
	
	for (var candidate in candidates) {
		console.log("  candidate: " + candidate);
		var dist = calcDistance({longitude: metadata[candidate].Longitude, latitude: metadata[candidate].Latitude}, location);
		console.log("  candidates[candidate]: " + candidates[candidate]);
		candidatesArray.push([candidate, candidates[candidate], dist]);

		distRange.min = Math.min(dist, distRange.min);
		distRange.max = Math.max(dist, distRange.max);

		matchRange.min = Math.min(candidates[candidate], matchRange.min);
		matchRange.max = Math.max(candidates[candidate], matchRange.max);
	}

	console.log('range: ' + distRange.min + ', ' + distRange.max);
	
	candidates = candidatesArray.sort(function(a,b) {
			var ALPHA = 0.6; // Weight of distance

			// higher match is better
			var aMatchNorm = normalize(a[1], matchRange.min, matchRange.max);
			var bMatchNorm = normalize(b[1], matchRange.min, matchRange.max);
			var MatchDiff = aMatchNorm - bMatchNorm;

			// shorter distance is better
			var aDistNorm = 1 - normalize(a[2], distRange.min, distRange.max);
			var bDistNorm = 1 - normalize(b[2], distRange.min, distRange.max);
			var distDiff = bDistNorm - aDistNorm;

			var aScore = (1 - ALPHA) * (aMatchNorm) + ALPHA * aDistNorm * aDistNorm * aDistNorm;
			var bScore =  (1 - ALPHA) * (bMatchNorm) + ALPHA * bDistNorm * bDistNorm * bDistNorm;

			return bScore - aScore;
		});
	var chosenCandidates = {};

	for (var k = 0; k < candidates.length && k < SEND_LIMIT; k++) {
		var placeName = candidates[k][0];
		chosenCandidates[placeName] = candidates[k][1];
	}
	retVal.top = chosenCandidates;
	retVal.num = candidates.length;
	return retVal;
}

function normalize(val, min, max) {
	if (min == max) return 1;
	return (val - min)/(max-min);
}

// Data initialization
function load() {
  console.log('Loading data...');

  var dataDirs = {
    'attributeMapping':    {source: 'attributeMapping',
														callback: function(d) {attributeMapping = d;}},
    'similarData':         {source: 'similarData/all.similarData',
														callback: function(d) {similarData = d;}},
    'polarityData':        {source: 'polarityData/all.polarityData',
    												callback: function(d) {polarityData = d;}},
    'metadata':            {source: 'placeData/all.metaData',
    												callback: function(d) {metadata = d;}},
    'reviewData':          {source: 'reviewData/all.reviewData',
    												callback: function(d) {reviewData = d;}},
    'placeDataDetailed':   {source: 'placeDataDetailed/all.placeDataDetailed',
    												callback: function(d) {placeDataDetailed = d;}},
    'attributeCategories': {source: 'extractionsData/attributeCategories',
    												callback: function(d) {attributeCategories = d;}},
    'filteredValues':      {source: 'extractionsData/filteredValues',
    												callback: function(d) {filteredValues = d;}}
  };

  var counter = Object.keys(dataDirs).length;
  var loadIfDone = function() {
    counter--;
    if (counter === 0) {
      loadDatabase();
      console.log("Finished loading data!");
		}
  }

  for (name in dataDirs)
    loadFromFile(name, dataDirs[name].source, dataDirs[name].callback, loadIfDone);
}

function loadFromFile(name, dir, loadFunction, callback) {
	fs.readFile(dir, 'utf-8', function(err, dataString) {
		if (err) throw err;
		loadFunction(JSON.parse(dataString));
    console.log('Loaded ' + name);
    callback();
	});
}

function loadDatabase(callback) {
	console.log('Loading db');
	fs.readFile('placeData/all.placeData', 'utf-8', function(err, dataString) {
		if (err) throw err;
		var data = JSON.parse(dataString);
		delete dataString;

    for (var attribute in attributeMapping)
		  allAttributes[attribute] = null;

    for (var p in data) {
			allPlaces[p] = null;
			var city = metadata[p]['City'];
			var neighborhood = metadata[p]['Neighborhood'];
			if (city)
				allCities[city.toLowerCase()] = null;
			if (neighborhood)
				allNeighborhoods[neighborhood.toLowerCase()] = null;
			for (var a in data[p]) {
				allAttributes[a] = null;
				for (var v in data[p][a]) {
					allValues[v] = null;
				}
			}
		}
	});
}
// !Data initialization

/*
 * Calulcates the distance between to earth coordinates. Distance is calculated
 * "as the bird flies."
 *
 * return: Number of miles between two locations
 */
function calcDistance(loc1, loc2) {
	
    var b = loc1.latitude;
        c = loc1.longitude;
    a = (loc2.latitude - b) * Math.PI / 180;
    c = (loc2.longitude - c) * Math.PI / 180;
    b = b * Math.PI / 180;
    lat2 = loc2.latitude * Math.PI / 180;
    b = Math.sin(a / 2) * Math.sin(a / 2) + Math.sin(c / 2) * Math.sin(c / 2) * Math.cos(b) * Math.cos(lat2);
    b = 3963 * 2 * Math.atan2(Math.sqrt(b), Math.sqrt(1 - b));
    return b;
}