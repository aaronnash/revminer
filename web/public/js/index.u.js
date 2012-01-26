var DEFAULT_MSG;

function load() {
	processHash();
	getGeo();
}

// TODO FIXME: queries are sometimes being sent twice (especially non-place queries). need to fix this
function processHash() {
	// Process hash
	var searchBox = document.getElementById('searchBox');
	var hashValue = decodeURI(window.location.hash.substr(1));
	if (searchBox.value !== hashValue) {
		searchBox.value = hashValue;
		search();
	}
}

// Try and get longitude and latitude of user's location
function getGeo() {
	// If we've already stored lat/long, get out
	if (myLat && myLong) return;

	// If we have access to HTML5, go get it
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(
			function(pos) {
				// If error could be greater than 250 meters away, don't store coordinates
				if (pos.coords.accuracy > 250) return;
				myLat = pos.coords.latitude;
				myLong = pos.coords.longitude;},
			function(error) {
				if (error.code != error.PERMISSION_DENIED)
					alert("Error getting geolocation");
			});
	}
}
window.onhashchange = load; // process the back button
window.onload = load;

String.prototype.trim=function(){return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');};

// First letter of every word is capitalized. Others are lowercase-ed
String.prototype.toCapitalize = function() {
	return this.toLowerCase().replace(/^.|\s\S/g, function(a) { return a.toUpperCase(); });
}

// no leading 0s allowed
// 7string --> true
// 007string --> false
function validateInt(iString) {
	return (("" + parseInt(iString)) == iString);
}

// Pretty HTML output of the place's name
// placeName = subway-seattle-2    Grab "Subway" and "2"
// address = 12345 NE 12th Pl, Seattle, WA   Grab "Seattle"
// extraPretty = do we want this to be extraPretty? true/false
function prettyPlaceName(placeName, address, extraPretty) {
	placeName = placeName.replace(/-/g, ' ').toCapitalize();
	var splitPlaceName = placeName.split(' ');
	var city = address.split(',');
	city = city[city.length-2].trim();

    // Check if the place is Subway-seattle-5, we should store that 5
    // and properly store the city
    var testInt = splitPlaceName.pop();
   // If city is two words, pop the second word
	if (city.indexOf(' ') > -1) {
		splitPlaceName.pop();
	}
    if (validateInt(testInt)) {
        city += " " + testInt;
        splitPlaceName.pop();
	}

	if (extraPretty)
		city = '</h1><h2>' + city + '</h2>';
	else
		city = ', ' + city;
	return splitPlaceName.join(' ') + city;
}

function makeClickable(placesArray) {
	if (placesArray)
		return '<a href="#" onclick="return exampleClicked(this);">' + placesArray.join('</a><br><a href="#" onclick="return exampleClicked(this); ">').replace(/-/g, ' ') + '</a>'; // not prettified :/
	else
		return '';
}

function polarityAvg(arr) {
	if (!arr)
		return 3.757;
	var sum = 0;
	var count = 0;
	for (var i = 0; i < arr.length; i++) {
		sum += arr[i] * (i+1);
		count += arr[i];
	}
	if (count < 10) // 5 sometimes results in misclassified polarities
		return 3.757; // not enough data to be sure
	return sum / count;
}

function polarityToColor(polarity, text) {
  if (text) {
    if (polarity > 4.2) {
      return '00cc00';
    } else if(polarity > 3.7) {
      return '006600';
    } else if (polarity > 3) {
      return '222222';
    } else if (polarity > 2.3) {
      return '880000';
    } else {
      return 'ee0000';
    }
  } else {
    if (polarity > 4.2) {
      return '00ff00';
    } else if(polarity > 3.7) {
      return '008800';
    } else if (polarity > 3) {
      return '4c4c4c';
    } else if (polarity > 2.3) {
      return '880000';
    } else {
      return 'ff0000';
    }
  }
}

var socket = io.connect();

var reviews;
var placeData;
var categorizedData;
var meta;
var similar;
var backStack = [];
var myLat;
var myLong;
var sortOrder = [false, false, false, false];

var SIDEBAR_ELEMS = ['Overview', 'Food', 'Service', 'Decor',
                     'Overall', 'Other', 'Similar'];
var MAX_ATTRIBUTES_SHOWN = 15;
var MAX_VALUES_SHOWN = 6;

socket.on('match', function(msg) {
  categorizedData = {};
  placeData = {};
  meta = msg.meta;
  similar = msg.similar;
  reviews = msg.reviews;

  var attributeCategories = msg.attributeCategories;
  var categoryAttributeCount = {};
  var categoryPolarities = {};
  var attributeCount = 0;
  for (var ai = 0, loopLen = msg.data.length; ai < loopLen; ai++) {
    var attributeName = msg.data[ai][0];
    var attributeData = msg.data[ai][1];
    var category = attributeCategories[attributeName];
    if (!(category in categorizedData)) {
      categorizedData[category] = {};
      categoryPolarities[category] = [];
      categoryAttributeCount[category] = 0;
    }

    if (categoryAttributeCount[category]++ >= MAX_ATTRIBUTES_SHOWN) {
      continue;
    }

    categorizedData[category][attributeName] = {};
    placeData[attributeName] = {};
    valueCount = 0;

    for (var value in attributeData) {
      if (valueCount++ >= MAX_VALUES_SHOWN) {
        break;
      }
      // Check if we should add this value based on some heuristic
      count = attributeData[value][0].length;
      //polarity = parseInt(value.slice(0, 2), 10);
      polarity = polarityAvg(attributeData[value][1]);

      for (i = 0; i < count; i++) {
        attributeData[value][0][i][0] = attributeData[value][0][i][0].trim();
      }
      placeData[attributeName][value.slice(2)] = attributeData[value][0]
      categorizedData[category][attributeName][value.slice(2)] =
        {'count': count, 'polarity': polarity};
      if (!(value in msg.filteredValues)) {
        // sqrt is hack to make color bars not weight very frequent too much
        for (i = 0; i < Math.sqrt(count) + 1; i++) {
          categoryPolarities[category].push(polarity);
        }
      }
    }
  }

   var heading = '<div id="header" style="height:60px"><div style="float:left; margin-right:25px"><h1>' + prettyPlaceName(msg.place, msg.meta['Address'], true) + '</div><div id="opendistance" style="float:right"><span' + getOpen(msg.meta, false) + '</span>';
  if (myLat && myLong) heading += '<br>' + getLocation(msg.meta);
  heading += '</div></div>';

  var sidebar = '<div><div id="sidebar" style="clear:both"><table>';
  for (var i = 0; i < SIDEBAR_ELEMS.length; i++) {
    var elem = SIDEBAR_ELEMS[i];
    sidebar += '<tr><td class="sb_elem" id="' + elem + '" style="color:#ff6600"';
    sidebar += 'onclick="sidebarClicked(\'' + elem + '\');">' + elem + '</td>';

    if (elem in categorizedData) {
      sidebar += '<td><canvas id="' + elem + 'Bar" width="100" height="14">' +
                 '</canvas></td>'
    }
    sidebar += '</tr>';
  }
  sidebar += '</table></div>';

  outputHtml(heading + sidebar + '<div id="content" style="clear:right">bla</div></div>');
  sidebarClicked(SIDEBAR_ELEMS[0]);
  for (var i = 0; i < SIDEBAR_ELEMS.length; i++) {
    var elem = SIDEBAR_ELEMS[i];
    if (elem in categorizedData) {
      setTiles(100, 14, 7, elem, categoryPolarities[elem]);
    }
  }
});

var fadeOut;

function submitFeedback() {
	var feedbackBox = document.getElementById('feedbackBox');
	if (feedbackBox.value)
		socket.emit('giveFeedback', feedbackBox.value);
	document.getElementById('feedbackBox').value = "Thanks!";
	if (!fadeOut)
	fadeOut = setTimeout("toggleFeedback()", 2000);
}

function toggleFeedback() {
	var feedbackForm = document.getElementById('feedbackForm');
    if (feedbackForm.style.display == 'inline-block')
        feedbackForm.style.display = 'none';
    else
        feedbackForm.style.display = 'inline-block';
    clearTimeout(fadeOut);
    fadeOut = null;
    document.getElementById('feedbackBox').value = "";
}

var tooltip=function() {
  var id = 'tt';
  var top = 3;
  var left = 3;
  var maxw = 300;
  var speed = 15;
  var interval = 10;
  var delay = 500;
  var endalpha = 92;
  var alpha = 0;
  var timer, timer2;
  var tt, t, c, b, h;
  var ie = document.all ? true : false;
  return {
    show:function(v, w) {
      if (tt == null) {
        tt = document.createElement('div');
        tt.setAttribute('id',id);
        t = document.createElement('div');
        t.setAttribute('id',id + 'top');
        c = document.createElement('div');
        c.setAttribute('id',id + 'cont');
        b = document.createElement('div');
        b.setAttribute('id',id + 'bot');
        tt.appendChild(t);
        tt.appendChild(c);
        tt.appendChild(b);
        document.body.appendChild(tt);
        tt.style.opacity = 0;
        tt.style.filter = 'alpha(opacity=0)';
        document.onmousemove = this.pos;
      }
      c.innerHTML = v;
      tt.style.width = w + 'px';
      if (tt.offsetWidth > maxw) {
        tt.style.width = maxw + 'px'
      }

      // make visible then invisible again to figure out h
      tt.style.display = 'block';
      h = parseInt(tt.offsetHeight) + top;
      tt.style.display = 'none';

      tt.timer2 = setTimeout("tooltip.display()", delay);
    },
    pos:function(e) {
      var u = ie ? event.clientY + document.documentElement.scrollTop : e.pageY;
      var l = ie ? event.clientX + document.documentElement.scrollLeft : e.pageX;
      tt.style.top = (u - h) + 'px';
      tt.style.left = (l + left) + 'px';
    },
    display:function() {
      tt.style.display = 'block';
      h = parseInt(tt.offsetHeight) + top;
      clearInterval(tt.timer);
      tt.timer = setInterval(function() {tooltip.fade(1)}, interval);
    },
    fade:function(d) {
      var a = alpha;
      if ((a != endalpha && d == 1) || (a != 0 && d == -1)) {
        var i = speed;
        if (endalpha - a < speed && d == 1) {
          i = endalpha - a;
        } else if (alpha < speed && d == -1) {
           i = a;
        }
        alpha = a + (i * d);
        tt.style.opacity = alpha * .01;
        tt.style.filter = 'alpha(opacity=' + alpha + ')';
      } else {
        clearInterval(tt.timer);
        if (d == -1) {
          tt.style.display = 'none'
        }
      }
    },
    hide:function() {
      clearTimeout(tt.timer2);
      clearInterval(tt.timer);
      tt.timer = setInterval(function() {tooltip.fade(-1)}, interval);
    }
  };
}();

function showTooltip(attribute, value) {
  tooltip.show('"' + String(placeData[attribute][value][0][0]).replace(/^\s*/, "").replace(/\s*$/, "") + '"',  300);
}

function sidebarClicked(elem) {
  for (var i = 0; i < SIDEBAR_ELEMS.length; i++) {
    e = SIDEBAR_ELEMS[i];
    text = document.getElementById(e);
    if (e == elem) {
      text.style.color = '#000000';
    } else {
      text.style.color = '#ff6600';
    }
  }

  var html = '<h4>' + elem + '</h4>';
  if (elem in categorizedData) {
    backStack.push([sidebarClicked, elem]);
    attributeData = categorizedData[elem];
    html += '<table>';
    for (attribute in attributeData) {
      html += '<tr><td class="attribute"><a style="color:#ff6600" href="#"'
        + 'onclick="return attributeClicked(\'' + attribute + '\');">' + attribute + '</a></td><td>';
      var valueList = [];
      var valueSorted = [];
      for (value in attributeData[attribute]) {
        valueSorted.push([value, attributeData[attribute][value]['polarity']]);
      }
      valueSorted.sort(function(a,b) {return b[1] - a[1];});

      for (i = 0; i < valueSorted.length; i++) {
        value = valueSorted[i][0];
        polarity = valueSorted[i][1];
        count = attributeData[attribute][value]['count'];
        var valueString = '<a href="#" style="color:#' + polarityToColor(polarity, true)
          + '" onclick="return valueClicked(\'' + attribute + '\',\'' + value + '\');"'
          + 'onmouseover="showTooltip(\'' + attribute + '\',\'' + value + '\');"'
          + 'onmouseout="tooltip.hide();"'
          + '>' + value.replace('!', 'not ') + '</a>';        if (count > 1) {
          valueString += ' (' + count + ')';
        }
        valueList.push(valueString);
      }
      html += valueList.join(', ') + '</td></tr>';
    }
    html += '</table></div>'
  } else if (elem == 'Similar') {
    html += 'People had similar opinions about<br><br>';
    html +=  makeClickable(similar) + '</div>';
  } else if (elem == 'Overview') {
    var ignore = {'Latitude': 0, 'Longitude': 0, 'Business Name': 0,
                  'Business type': 0, 'Category': 0, 'Categories': 0,
                  'Hours': 0}
    var header = {'Address': 0, 'Phone number': 0};
    html += '<table>';

    var attributeTD = function(attribute, value) {
      return '<c style="color:#ff6600">' + attribute + ':</c>  ' +
             '<c style="color:#007700">' + value + '<c>';
    }

    var c
    if (meta['Categories']) {
      c = 'Categories';
    } else if (meta['Category']) {
      c = 'Category';
    }
    if (c) {
      categoryStrings = meta[c].split(', ');
      categories = [];
      for (var i = 0; i < categoryStrings.length; i++) {
        if (categories.indexOf(categoryStrings[i]) == -1) {
          categories.push(categoryStrings[i]);
        }
      }
      html += '<tr><td>' + attributeTD('Categories', categories.join(', ')) +
              '</td></tr>'
      html+= '<tr><td></td></tr>';
    }

    for (attribute in header) {
      html += '<tr><td><c style="color:#007700">' + meta[attribute] +
              '</c></td></tr>'
    }
    html += '</table><br>'

    if ('Hours' in meta) {
      html += '<table><tr>';
      html += '<td style="padding-right:5px;"><c style="color:#ff6600">Hours:</c></td>';
      html += '<td><c style="color:#007700">' + meta['Hours'] + '<c></td>';
      html += '</td></table>';
    }

    html += '<table><tr>';
    var n = 0;
    for (attribute in meta) {
      if (attribute in ignore || attribute in header) {
        continue;
      }
      n += 1;

      if (n % 2 == 0) {
        html += '<td>' + attributeTD(attribute, meta[attribute]) + '</td>';
        html+= '</tr><tr>';
      } else {
        html += '<td style="padding-right:40px">' +
                attributeTD(attribute, meta[attribute]) + '</td>';
      }
    }
    html += '</tr>'
  }

  document.getElementById('content').innerHTML = html;
}

function setTiles(w, h, cornerw, category, polarities) {
  polarities = polarities.sort().reverse()
  var barCanvas = document.getElementById(category + 'Bar');
  var bar = barCanvas.getContext('2d');

  /*bar.beginPath();
  bar.moveTo(cornerw, h);
  bar.quadraticCurveTo(0, h, 0, h/2);
  bar.quadraticCurveTo(0, 0, cornerw, 0);
  bar.lineTo(w - cornerw, 0);
  bar.quadraticCurveTo(w, 0, w, h/2);
  bar.quadraticCurveTo(w, h, w - cornerw, h);
  bar.lineTo(cornerw, h)
  bar.clip();*/

  var skipSize = 2;
  var skipLength = 0;
  var lastColor = 0;
  for (var i = 0; i < polarities.length; i++) {
    var nextColor = polarityToColor(polarities[i], false);
    if (skipSize > -1 && nextColor != lastColor && i != 0) {
      skipLength += skipSize;
    }
    lastColor = nextColor;
  }
  var tileWidth = (w - skipLength) / polarities.length;

  var x = 0;
  var lastColor = 0;
  for (var i = 0; i < polarities.length; i++) {
    var nextColor = polarityToColor(polarities[i], false);

    if (skipSize > -1 && nextColor != lastColor && i != 0) {
      bar.fillStyle = '#ffffff';
      bar.fillRect(x, 0, x + skipSize, h);
      x += skipSize;
    }
    bar.fillStyle = '#' + nextColor;
    bar.fillRect(x, 0, x + tileWidth + 1, h);
    x += tileWidth;
    lastColor = nextColor;
  }
}

socket.on('suggestions', function(msg) {
	if (msg.length > 1)
		outputHtml(makeClickable(msg));
	else // a notification message
		outputHtml(msg[0]);
});

socket.on('results', function(msg) {
	var serverMsg = '<h3>Processed Query: ' + msg.query + ' in ' + msg.time + 'ms (' + msg.num + ' results)</h3>';
	serverMsg += '<table id="results"><tr>';
	serverMsg += '<td id="sort-name" style="color:#0000AA" onmouseover="this.style.cursor=\'pointer\'" onclick="sortBy(\'placeName\', 0, this);">Restaurant Name</td>';
	//serverMsg += '<td id="sort-score" style="color:#000000; text-decoration:underline" onmouseover="this.style.cursor=\'pointer\'" onclick="sortBy(\'score\', 1, this);">Relevance</td> &nbsp;';
	serverMsg += '<td id="sort-open" style="color:#0000AA" onmouseover="this.style.cursor=\'pointer\'" onclick="sortBy(\'isOpen\', 2, this);">Open</td>';
	if (myLat && myLong) serverMsg += '<td id="sort-distance" style="color:#0000AA; cursor: pointer"  onclick="sortBy(\'distance\', 3, this);">Distance</td> &nbsp;';
	serverMsg += '<td id="sort-price" style="color:#0000AA" onmouseover="this.style.cursor=\'pointer\'" onclick="sortBy(\'price\', 4, this);">Price</td> &nbsp;';
	var count = 1;
	for (var placeName in msg.data) {
		serverMsg += '<tr id=result_' + count + '><td class="placeName"><a href="#" onclick="return exampleClicked(this);">' + prettyPlaceName(placeName, msg.meta[placeName]['Address']) + '</a></td>';
	//	serverMsg += '<td class="score">' + msg.data[placeName] + '</td>&nbsp;&nbsp;';
		serverMsg += '<td class="isOpen">' + getOpen(msg.meta[placeName], true) + '</td>';
		if (myLat && myLong)
		    serverMsg += '<td class="distance">' + getLocation(msg.meta[placeName]) + '</td>&nbsp;&nbsp;';
		if (msg.meta[placeName]['Price Range'])
		    serverMsg += '<td class="price">' + msg.meta[placeName]['Price Range'] + '</td>';
		serverMsg += '</tr>'
		count++;
	}
	serverMsg += '</table>';
	if (msg.suggestions.length > 0) {
		serverMsg = '<div id="similar"><h3>Name Matches</h3>' + makeClickable(msg.suggestions) + '</div><div id="extractions">' + serverMsg + '</div>';
	}
	outputHtml(serverMsg);
});
socket.on('connect', function() {
	var searchBox = document.getElementById('searchBox');
	if (searchBox.value.length > 0)
		socket.emit('search', searchBox.value.replace('not ', '!'));
});

function sortBy(characteristic, index, elem) {
	var max;
	if (elem.className) {
		sortOrder[index] = !sortOrder[index];
		max = sortOrder[index];
	} else {
		max = false;
		sortOrder = [false, false, false, false];
	}

	var sortable = document.getElementById('results').rows[0].getElementsByTagName('td');
	for (var i=0; i<sortable.length; i++) {
		sortable[i].style.color = '#0000AA';
		sortable[i].style.textDecoration = 'none';
		sortable[i].removeAttribute('class');
	}
	elem.style.color = '#000000';
	elem.style.textDecoration = 'underline';
	elem.className = 'sortable-selected';

	var sortedResult = [];
	var table = document.getElementById('results');

	for (var i=1; i<table.rows.length; i++) {
		var name = 'result_' + i;
		var data = document.getElementById(name).childNodes;
		for (var j=0; j<data.length; j++) {
			if (data[j].className == characteristic) {
				var insideText = data[j].innerHTML.trim();
				if (insideText.indexOf('<') == 0)
					insideText = insideText.substring(insideText.indexOf('>')+1,
									insideText.indexOf('<', 1));
				sortedResult.push(insideText + '_' + name);
			}
		}
	}
	sortedResult.sort(function(a, b) {
		if (!a && !b) return -1;
		if (!a) return 1;
		if (!b) return -1;
		var result = parseFloat(b)-parseFloat(a);

		// If we're comparing strings
		if (!result && result != 0) {
			var aVal = a.substring(0, a.indexOf('_'));
			var bVal = b.substring(0, b.indexOf('_'));
			if (aVal < bVal) result = 1
			else if (aVal > bVal) result = -1;
			else return parseInt(a.substr(a.lastIndexOf('_')+1)) - parseInt(b.substr(b.lastIndexOf('_')+1));
		}

		if (max) return result;
		else return -result;
	});
	for (var i=sortedResult.length-1; i>=0; i--) {
		var name = sortedResult[i].substr(sortedResult[i].indexOf('_')+1);
		var row = document.getElementById(name);
		row.parentNode.insertBefore(row, table.rows[1]);
	}
}

function outputHtml(html) {
	if (!DEFAULT_MSG)
		DEFAULT_MSG = document.getElementById('serverMsg').innerHTML;
	if (!html)
		html = DEFAULT_MSG;
	document.getElementById('serverMsg').innerHTML = html;
}

function backClicked() {
  backStack.pop();
  var back = backStack.pop();
  back[0](back[1])
}

function valueClicked(attribute, value) {
  backStack.push([valueClicked, [attribute, value]]);
  tooltip.hide();
  var html = '<table width="100%"><tr><td><h4>Reviews mentioning \"' + value + ' ' + attribute + '\"</h4></td>';
  html += '<td><div class="sb_elem" style="color:#ff6600"';
  html += 'onclick="backClicked();">Back</div></td></tr></table>';

  data = placeData[attribute][value];
  for (i = 0; i < data.length; i++) {
    sentence = data[i][0];
    review = reviews[data[i][1]];

    var ind = review.indexOf(sentence);
    if (ind != -1) {
      html += review.slice(0, ind);
      html += '<font color="#ff6600">' + sentence + '</font>';
      html += review.slice(ind + sentence.length, review.length);
    } else {
      html += review;
    }
    html += '<br><br>';
  }

  document.getElementById('content').innerHTML = html;
  return false;
}

function displayReview(attribute, value, i) {
  backStack.push([displayReview, [attribute, value, i]]);
  var html = '<table width="100%"><tr><td><h4>Source of the sentence </h4></td>';
  html += '<td style="padding-right:0"><div class="sb_elem" style="color:#ff6600"';
  html += 'onclick="backClicked();">Back</div></td></tr></table>';

  review = reviews[placeData[attribute][value][i][1]];
  sentence = placeData[attribute][value][i][0];
  var ind = review.indexOf(sentence);
  if (ind != -1) {
    html += review.slice(0, ind);
    html += '<font color="#ff6600">' + sentence + '</font>';
    html += review.slice(ind + sentence.length, review.length);
  } else {
    html += review;
  }

  document.getElementById('content').innerHTML = html;
  return false;
}

function attributeClicked(attribute) {
  backStack.push([attributeClicked, attribute]);
  var html = '<table width="100%"><tr><td><h4>Review sentences about \"' + attribute + '\"</h4></td>';
  html += '<td><div class="sb_elem" style="color:#ff6600"';
  html += 'onclick="backClicked();">Back</div></td></tr></table>';

  for (value in placeData[attribute]) {
    for (i = 0; i < placeData[attribute][value].length; i++) {
      review = placeData[attribute][value][i][1];
      sentence = placeData[attribute][value][i][0];

      html += '<a style="cursor:pointer" onclick="return displayReview(\'' + attribute + '\',\'' + value + '\',' + i + ');">"' +  sentence + '"</a>';
      html += '<br><br>';
    }
  }

  document.getElementById('content').innerHTML = html;
  return false;
}

function exampleClicked(exampleAnchor) {
	var searchBox = document.getElementById('searchBox');
	searchBox.value = exampleAnchor.innerHTML;
	search();
	return false;
}

function search(isChange) {
	var searchBoxVal = document.getElementById('searchBox').value.toLowerCase();
	if (search.lastMsg !== searchBoxVal || !isChange) {
		search.lastMsg = searchBoxVal;
		var ev = 'search';
		if (isChange)
			ev = 'change';
		if (searchBoxVal.length === 0) {
			outputHtml(DEFAULT_MSG);
		}
		//if (socket.connected) {
			socket.emit(ev, searchBoxVal.replace('not ', '!'));
		//}
	}
	if (!isChange) {
		location.href = 'http://' + location.hostname + '/#' + encodeURI(searchBoxVal);
	}
}

function getLocation(meta) {
	if (!meta ||
		!(meta['Latitude'] && meta['Longitude'])) return "";

	var lat1 = meta['Latitude'];
	var lon1 = meta['Longitude'];
  	var radius = 3963; // miles

	lat2 = myLat;
	lon2 = myLong;

	var dLat = (lat2-lat1) * Math.PI / 180;
	var dLon = (lon2-lon1) * Math.PI / 180;
	lat1 = (lat1) * Math.PI / 180;
	lat2 = (lat2) * Math.PI / 180;

	var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
    	    Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
	var b = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	var dist = radius * b;

  	// Round to one decimal place
  	dist = Math.round(dist*10)/10;

  	//<img src=dir-s.png width='10' height = '25'>
  	var angle = Math.atan(dLat/dLon);
  	if (angle <= -3*Math.PI/8) {
  		if (dLon < 0)
	  		return dist + " mi. S";
	  	else
	  		return dist + " mi. N";
  	} else if (angle <= -Math.PI/8) {
  		if (dLon < 0)
	  		return dist + " mi. SE";
	  	else
	  		return dist + " mi. NW";
  	} else if (angle <= Math.PI/8) {
  		if (dLon < 0)
	  		return dist + " mi. E";
	  	else
	  		return dist + " mi. W";
  	} else if (angle <= 3*Math.PI/8) {
  		if (dLon < 0)
	  		return dist + " mi. NE";
	  	else
	  		return dist + " mi. SW";
  	} else { // if (angle <= Math.PI/2)
  		if (dLon < 0)
	  		return dist + " mi. N";
	  	else
	  		return dist + " mi. S";
  	}
}

function getOpen(meta, getImage) {
	if (meta['Hours']) {
		var data = meta['Hours'];
		var times = data.split(" & ");

		var enumDays;
		var openTimes = new Array(7);
		for (var i=0; i<openTimes.length; i++) {
			openTimes[i] = new Array();
		}
		for (i=0; i<times.length; i++) {
			var numIndex = times[i].search(/[0-9]/);
			var days = times[i].substring(0, numIndex);
			var hours = times[i].substring(numIndex, times[i].length);
			openTimes = getOpenTimes(days, hours, openTimes);
		}

		var now = new Date();
		if (openTimes[now.getDay()]) {
			var curTime = now.getHours()*60 + now.getMinutes();

			var tempStart = 0;
			for (i=0; i<openTimes[now.getDay()].length; i++) {
				var startTime = openTimes[now.getDay()][i].start;
				var endTime = openTimes[now.getDay()][i].end;
				if ((endTime == startTime) ||
					(endTime < startTime && (curTime <= endTime || curTime >= startTime)) ||
					(curTime >= startTime && curTime <= endTime)) {
					if (getImage) {
						var closeString = 'Closes at: ' + getTimeFromMinutes(endTime);
						return '<img height="25" width="25" src="green-check.png" alt="Now Open" onmouseover="tooltip.show(\'' + closeString + '\', 100)" onmouseout="tooltip.hide()">';
					} else
						return 	' class="isOpen" style="color:#007700">Currently Open';
				} else if ((endTime < startTime && (curTime > startTime)) ||
						(curTime < startTime)) {
					tempStart = startTime;
				}
			}
		}
		if (getImage) {
			var startString = 'Next Open at: ' + getTimeFromMinutes(tempStart);
			return '<img height="25" width="25" src="red-x.png" alt="Now Closed" onmouseover="tooltip.show(\'' + startString + '\', 100)" onmouseout="tooltip.hide()">';
		} else
			return 	' class="isOpen" style="color:#AA0000">Currently Closed';
	}
	if (getImage)
		return '<img height="25" width="25" src="question-mark.png" alt="Unknown hours">';
	else
		return '>';
}

function getOpenTimes(daysString, hoursString, openTimes) {
	var enumerated = new Array();
	enumerated['sun'] = 0;
	enumerated['mon'] = 1;
	enumerated['tue'] = 2;
	enumerated['wed'] = 3;
	enumerated['thu'] = 4;
	enumerated['fri'] = 5;
	enumerated['sat'] = 6;

	var split = hoursString.split(" ");
	var days = daysString.split(",");
	for (var i=0; i<days.length; i++) {
		var day = days[i];
		var index=day.indexOf("-");
		if (index == -1) { // doesn't exist
			openTimes[enumerated[day.trim().toLowerCase()]].push({start:getMinutes(split[0], split[1]), end:getMinutes(split[3], split[4])});
		} else {
			var start = enumerated[day.substring(0, index).trim().toLowerCase()];
			var end = enumerated[day.substring(index+1, day.length).trim().toLowerCase()];
			if (end < start) {
				for (var j=0; j<= end; j++) {
					openTimes[j].push({start:getMinutes(split[0], split[1]), end:getMinutes(split[3], split[4])});
				}
				end = 6
			}
			for (var j=start; j<=end; j++) {
			 	openTimes[j].push({start:getMinutes(split[0], split[1]), end:getMinutes(split[3], split[4])});
			}
		}
	}
	return openTimes;
}

function getTimeFromMinutes(minutesTime) {
	var hours, minutes, ampm;
	minutes = minutesTime%60;
    if (minutes < 10) minutes += '0';

	hours = parseInt(minutesTime/60);
	if (hours >= 12) ampm = 'pm';
	else ampm = 'am';
	if (hours > 12) hours -= 12;
	if (hours == 0) hours = 12;

	return hours + ":" + minutes + ampm;
}

function getOpenHours(hoursString) {
	var split = hoursString.split(" ");
	var openHours = {start:getMinutes(split[0], split[1]), end:getMinutes(split[3], split[4])};
	return openHours;
}

function getMinutes(time, period) {
	var hour = parseInt(time);
	var minutes = hour * 60;
	if (time == 12)
		minutes = 0;
	if (time.indexOf(":") != -1)
		minutes += parseInt(time.substring(time.indexOf(":")+1));
	if (period == 'pm')
		minutes += 720;
	return minutes;
}
