var express = require('express');
var util = require('util');
var fs = require('fs');
var mongodb = require('mongodb');

var MONGO_PORT = 27017;
var SEND_LIMIT = 50;
var LOAD_METHOD = 1; // 0 is parallel load, 1 is sequential load (more complex, slower, but uses less memory theoretically)

var default_port = 27017;
var dbOpts = {native_parser: false};
if (LOAD_METHOD === 1)
	dbOpts.strict = true;
var db = new mongodb.Db('revminer', new mongodb.Server('localhost', MONGO_PORT, {}), dbOpts);
var oKeysCache; // for LOAD_METHOD 1

function get_first_keys(data, keys) {
	var ki = oKeysCache;
	for (var i = keys[0]; i < ki.length; ++i) {
		var kj = Object.keys(data[ki[i]]);
		for (var j = keys[1]; j < kj.length; ++j) {
			var kk = Object.keys(data[ki[i]][kj[j]]);
			for (var k = keys[2]; k < kk.length; ++k) {
				return [i, j, k];
			}
		}
	}
}

function get_next_keys(data, keys) {
	var i = keys[0];
	var j = keys[1];
	var k = keys[2];

	var ki = oKeysCache; //Object.keys(data);
	var kj = Object.keys(data[ki[i]]);
	var kk = Object.keys(data[ki[i]][kj[j]]);

	if (k + 1 < kk.length)
		 return [i, j, k + 1];
	if (j + 1 < kj.length)
		 return get_first_keys(data, [i, j+1, 0]);
	if (i + 1 < ki.length) {
		 return get_first_keys(data, [i+1, 0, 0]);
	}
	return;
}

var attributeMapping = {};
var similarData = {};
var polarityData = {};
var metadata = {};
var reviewData = {};
var placeDataDetailed = {};
var attributeCategories = {};
var filteredValues = {};

function load() {
  console.log('Created database and indices. Loading data...');

  var dataDirs = {
    'attributeMapping':    'attributeMapping',
    'similarData':         'similarData/all.similarData',
    'polarityData':        'polarityData/all.polarityData',
    'metadata':            'placeData/all.metaData',
    'reviewData':          'reviewData/all.reviewData',
    'placeDataDetailed':   'placeDataDetailed/all.placeDataDetailed',
    'attributeCategories': 'extractionsData/attributeCategories',
    'filteredValues':      'extractionsData/filteredValues'
  };

  var loadFunctions = {
    'attributeMapping':    function(d) {attributeMapping = d;},
    'similarData':         function(d) {similarData = d;},
    'polarityData':        function(d) {polarityData = d;},
    'metadata':            function(d) {metadata = d;},
    'reviewData':          function(d) {reviewData = d;},
    'placeDataDetailed':   function(d) {placeDataDetailed = d;},
    'attributeCategories': function(d) {attributeCategories = d;},
    'filteredValues':      function(d) {filteredValues = d;}
  };

  var counter = Object.keys(dataDirs).length;
  var loadIfDone = function() {
    counter--;
    if (counter === 0)
      loadDatabase();
  }

  for (name in dataDirs)
    loadFromFile(name, dataDirs[name], loadFunctions[name], loadIfDone);
}

function loadFromFile(name, dir, loadFunction, callback) {
	fs.readFile(dir, 'utf-8', function(err, dataString) {
		if (err) throw err;
		loadFunction(JSON.parse(dataString));
    console.log('Loaded ' + name);
    callback();
	});
}

var allPlaces = {};
var allAttributes = {};
var allValues = {};
var allCities = {};
var allNeighborhoods = {};

function loadDatabase(callback) {
	console.log('Loading db');
	fs.readFile('placeData/all.placeData', 'utf-8', function(err, dataString) {
		if (err) throw err;
		var data = JSON.parse(dataString);
		console.log('dataString parsed into data');
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
					if (LOAD_METHOD === 0)
						db.data.insert({p:p, a:a, v:v, f:data[p][a][v], c:city, n:neighborhood});
				}
			}
		}
		if (LOAD_METHOD === 1) {
			oKeysCache = Object.keys(data);
			var current_keys = get_first_keys(data, [0, 0, 0]);
			(function insert() {
				if (!current_keys) {
					console.log('All db loading complete');
					return;
				}
				var key = {};
				key.p = oKeysCache[current_keys[0]];
				key.a = Object.keys(data[key.p])[current_keys[1]];
				key.v = Object.keys(data[key.p][key.a])[current_keys[2]];
				key.f = data[key.p][key.a][key.v];
				key.c = metadata[key.p]['City'].toLowerCase();
				if (metadata[key.p]['Neighborhood'])
					key.n = metadata[key.p]['Neighborhood'].toLowerCase();
				else
					key.n = '';
				db.data.insert(key, insert);
				current_keys = get_next_keys(data, current_keys);
			})();
		}
	});
}

// connect to mongodb and reset database
db.open(function(err, db) {
	if (err) throw err;
	console.log("connected to mongodb");
	db.dropDatabase(function() {
		console.log('Database reset');
		// initialize the collection objects
		db.createCollection('data', function(err, collection) {
			db.data = collection;

      var counter = 4; // 4 indexes to create
			var loadIfDone = function(err, obj) {
      	counter--;
				if (counter === 0)
					load();
      }

      db.data.createIndex([['p', 1]], loadIfDone);
			db.data.createIndex([['a', 1], ['v', 1]], loadIfDone);
			db.data.createIndex([['a', 1]], loadIfDone);
			db.data.createIndex([['v', 1]], loadIfDone);
		});
	});
});

var server = express.createServer(express.logger());
var io = require('socket.io').listen(server);
server.listen(80);
server.use(express.static(__dirname + '/public'));

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
});

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

function computeInterestingness(value, count) {
  var ratings = polarityData[value];
  var df = 0;
  for (var r = 0; r < ratings.length; r++) {
    df += ratings[r];
  }
  var tfidf = Math.log(1000000/df) * count;
  return tfidf;
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

function topCandidates(candidates) {
	var retVal = {};
	var candidatesArray = [];
	for (var candidate in candidates) {
		candidatesArray.push([candidate, candidates[candidate]]);
	}
	candidates = candidatesArray.sort(function(a,b) {return b[1] - a[1];});
	var chosenCandidates = {};
	var numChosenCandidates = 0;
	for (var k = 0; k < candidates.length; k++) {
		var placeName = candidates[k][0];
		chosenCandidates[placeName] = candidates[k][1];
		if (numChosenCandidates >= SEND_LIMIT)
			break;
		numChosenCandidates++;
	}
	retVal.top = chosenCandidates;
	retVal.num = candidates.length;
	return retVal;
}

io.sockets.on('connection', function(client) {
	client.on('values', function(attribute) {
		console.log("values: " + attribute);
		var attributeC = attribute;
		if (attribute in attributeMapping) {
			attributeC = attributeMapping[attribute];
		}
		getCommonValues(attributeC, client);
	});
	client.on('change', function(query) {
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
	});
	client.on('search', function(query) {
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
		var suggestions = [];
		for (var place in allPlaces) {
			if (place.indexOf(placeQuery) !== -1) {
				suggestions.push(place);
			}
			if (suggestions.length > SEND_LIMIT) {
				break;
			}
		}
		if (suggestions.length === 1) {
			console.log('partial match: ' + suggestions[0]);
			sendMatch(suggestions[0], client);
			return;
		}
		var processed = processQuery(query);
		var queries = clausesToQueries(processed);
		console.log('queries: ' + util.inspect(queries));
		if (queries.length === 0) {
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
					if (results.length === 0) {
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
							if (nc in newCandidates)
								candidates[nc] *= newCandidates[nc];
							else
								delete candidates[nc];
						}
						// TODO: if candidates.length is 0, revert candidates to a backupCandidates and set remainingDbFinds to 1 and queries.slice from 0 to i
						// basically, we want to do a back-off in case the query is too specific
						// but this only does a 1-clause back-off
					}
					remainingDbFinds--;
					if (remainingDbFinds === 0) {
						var sortedCandidates = topCandidates(candidates);
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
						}

						sendMatch[client.id] = query;
						console.log('results: ' + util.inspect(queryResults));
						client.emit('results', { meta:queryMeta, data: queryResults, query: util.inspect(queries), num: sortedCandidates.num, suggestions: suggestions, time: new Date().getTime() - startTime});
					}
				});
			});
		}
	});
	client.on('giveFeedback', function(feedback) {
		console.log("feedback given: " + feedback);
        fs.createWriteStream("feedback.txt", {
            flags: "a",
            encoding: "encoding",
            mode: 0666
        }).write(feedback + "\n-----------------------\n");
	});
});
