// external libraries
var fs = require('fs');
var mongodb = require('mongodb');
// !external libraries

// global configuration
// Mongo DB
var MONGO_HOST = 'localhost';
var MONGO_PORT = 27017;
var MONGO_NAME = 'revminer';
var MONGO_OPTIONS = {native_parser: false};
// Other options
var LOAD_METHOD = 1; // 0 is parallel load, 1 is sequential load (more complex, slower, but uses less memory theoretically)
// !global configuration

// global variables
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

// initialization
if (LOAD_METHOD === 1) {
  MONGO_OPTIONS.strict = true;
}
var db = new mongodb.Db(MONGO_NAME, new mongodb.Server(MONGO_HOST, MONGO_PORT, {}), MONGO_OPTIONS);
var oKeysCache; // for LOAD_METHOD 1

// connect to mongodb
db.open(function(err, db) {
  if (err) throw err;
  console.log("connected to mongodb");

  // reset the DB
  db.dropDatabase(function() {
    console.log('Database reset');

    // initialize the collection objects
    console.log('creating collection');
    db.createCollection('data', function(err, collection) {
      if (err) throw err;
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
    if (counter === 0) {
      console.log("counter == 0 - loading DB");
      loadDatabase();
    }
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

function loadDatabase() {  
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

    process.exit(); // all done
	});
}





