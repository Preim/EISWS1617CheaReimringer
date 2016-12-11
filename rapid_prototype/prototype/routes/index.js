var express = require('express');
var router = express.Router();
var mongoDB = require('mongoskin');
var BSON = require('mongodb').BSONPure;
var db =mongoDB.db('mongodb://localhost/mydb?auto_reconnect=true',{safe: true});
var http = require('http');

db.bind('profiles');

var profilesCollection = db.profiles;

db.bind('events');

var eventsCollection = db.events;


router.get('/', function(req, res, next) {
	res.render('index', { title: 'Express' });
});


/* GET home page. */
router.get('/profiles', function (req, res, next) {
    profilesCollection.findItems(function(error, result){
        if (error)
            next(error);
        else{
        	console.log(result);
            res.writeHead(200, {'Content-Type': 'application/json'});
            res.end(JSON.stringify(result));
        };
    });
});

/* GET /profiles */
router.post('/profiles', function(req, res){
	// Update. Wenn nicht existent, insert.
	profilesCollection.update({id: req.body.id}, req.body, {upsert: true}, function(error){
		if (error) {
			return console.log('could not update');
		};
		console.log('updated. (upsert: true)');
	});


	//matching kriterien festlegen
	// Matchingkriterien: Gemeinsame Radsportart, Durchschnitliche Geschwindigkeit ist min. 2km/h bzw. max. 2km/h groesser als medianSpeed
	var matching_sports = req.body.bikesports[0];
	var location = req.body.location;
	var medianSpeed = req.body.averageSpeed;
	var minSpeed = medianSpeed - 2;
	if (minSpeed > 0) {
		minSpeed = 0;
	};
	var maxSpeed = medianSpeed + 2;
	console.log(matching_sports);
	var resultsArray;
	profilesCollection.find({ $and: [{bikesports: matching_sports}, {averageSpeed: {$gte: minSpeed}},{averageSpeed: {$lte: maxSpeed}},]}).toArray(function(error, results){
    	if (error)
    		next(error);
    	else{
    		console.log(results);
			resultsArray = results;
			var jsonObject = {results: resultsArray};
			res.writeHead(200, {'Content-Type': 'application/json'});
    		res.end(JSON.stringify(jsonObject));
    	};
	});
});



router.get('/profiles/:id', function(req, res, error){
	console.log("GET: " + JSON.stringify(req.url));
    console.log("param: _ID:" + req.params.id);
    //DEFECTIVE: var obj_id = BSON.ObjectID.createFromHexString(req.params.id);
	//find ressource 'profile :id' db.ObjectID.createFromHexString(req.params.id)
	console.log("test");
	profilesCollection.find({_id: mongoDB.helper.toObjectID(req.params.id)}).toArray(function(error, result){
		//console.log(result);
		if (error) 
			next(error);
		else{
			//console.log(result);
            res.writeHead(200, {'Content-Type': 'application/json'});
            res.end(JSON.stringify(result));

		};
	});
});

/*router.post('/events', function(req, res){
	//post data to ressource '/events'
	eventsCollection.update({id: req.body.id}, req.body, {upsert: true}, function(error){
		if (error) {
			return console.log('could not update');
		};
		console.log('updated. (upsert: true)');
	});
});*/

router.get('/events', function(req, res, next){
	eventsCollection.findItems(function(error, result){
        if (error)
            next(error);
        else{
        	console.log(result)
            res.writeHead(200, {'Content-Type': 'application/json'});
            res.end(JSON.stringify(result));
        };
    });
})

router.post('/events', function(req, res){
	//post data to ressource '/events'
	req.body.date = new Date(req.body.date);
	console.log(req.body);
	eventsCollection.update({id: req.body.id}, req.body, {upsert: true}, function(error){
		if (error) {
			return console.log('could not update');
		};
		console.log('updated. (upsert: true)');
		res.writeHead(200, 'OK');
		res.end();
	});
});

router.get('/events/:id', function(req, res, error){
	console.log("GET: " + JSON.stringify(req.url));
    console.log("param: _ID:" + req.params.id);
    //var obj_id = BSON.ObjectID.createFromHexString(req.params.id);
    eventsCollection.find({_id: mongoDB.helper.toObjectID(req.params.id)}).toArray(function(error, result) {
        if (error){
            next(error);
        }else {
            console.log('Result:');
            console.log(result[0]);
            console.log(result[0].start);
            console.log(result[0].date);
            var daysUntilEvent = daysfromTodayToEventDate(result[0].date)+1;
            console.log(daysUntilEvent +  " days until event");
			if (daysUntilEvent<=10) {
				//TODO: Wetterdatenabfragen
				var options = {
	  				host: 'api.wunderground.com',
	  				path: '/api/318295f477098775/forecast10day/lang:DE/q/Germany/'+ 'Cologne.json'
				};
				var str = '';
				callback = function(response) {
	  				//var str = '';

	  				//another chunk of data has been recieved, so append it to `str`
	  				response.on('data', function (chunk) {
	    				str += chunk;
	  				});

	  				//the whole response has been recieved, so we just print it out here
	  				response.on('end', function () {
	  					var weatherdata = JSON.parse(str);
	  					console.log("High: " + weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].high["celsius"] + 
	  						" Low: " + weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].low["celsius"] + 
	  						" Conditions: " + weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].conditions);
	  					result[0]['high'] = weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].high["celsius"];
	  					result[0]['low'] = weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].high["celsius"];
	  					result[0]['conditions'] = weatherdata.forecast.simpleforecast.forecastday[daysUntilEvent].conditions;
	  				});
				}

				http.request(options, callback).end();

			}
            res.writeHead(200, {'Content-Type': 'application/json'});
            res.end(JSON.stringify(result));

        }
    });
});

function daysBetweenDays(date1, date2){
	var oneday = 86400000; // 1 Tag in ms

	var date1_ms = date1.getTime();
	var date2_ms = date2.getTime();

	// Differenz berechnen
	var diff_ms = date2_ms - date1_ms;

	// in Tagen konvertieren
	return Math.round(diff_ms/oneday);

}
function daysfromTodayToEventDate(date){
	var event_date = new Date(date.getFullYear(), date.getMonth(), date.getDate());
	var today = new Date();
	return daysBetweenDays(today, event_date);
}
module.exports = router;
