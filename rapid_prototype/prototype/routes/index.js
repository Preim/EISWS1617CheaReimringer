var express = require('express');
var router = express.Router();
var mongoDB = require('mongoskin');
var db =mongoDB.db('mongodb://localhost/mydb?auto_reconnect=true',{safe: true});

db.bind('profiles');

var profilesCollection = db.profiles;

/* GET home page. */
router.get('/profiles', function (req, res, next) {
    profilesCollection.findItems(function(error, result){
        if (error)
            next(error);
        else{
        	console.log(result)
            res.writeHead(200, {'Content-Type': 'application/json'});
            res.end(JSON.stringify(result));
        };
    });
});
router.get('/', function(req, res, next) {
  res.render('index', { title: 'Express' });
});

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
	var matching_sports = req.body.bikesports[0]
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
})


/*router.get('/profiles/:id', function(req, res){
	//find ressource 'profile :id'
	profilesCollection.find({id: req.params.id}).toArray(function(error, result){
		console.log(result);
	});
	res.setHeader('Content-Type','application/json');
	res.send(JSON.stringify(result));
	res.end();
});

router.post('/events', function(req, res){
	//post data to ressource '/events'
	eventsCollection.update({id: req.body.id}, req.body, {upsert: true}, function(error){
		if (error) {
			return console.log('could not update');
		};
		console.log('updated. (upsert: true)');
	});
});*/
module.exports = router;
