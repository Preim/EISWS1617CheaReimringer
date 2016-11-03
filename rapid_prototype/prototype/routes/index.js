var express = require('express');
var router = express.Router();
var mongoDB = require('mongoskin');
var db =mongoDB.db('mongodb://localhost/mydb?auto_reconnect=true',{safe: true});

db.bind('profiles');

var profilesCollection = db.profiles;

/* GET home page. */
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


	//matching kriterien berechnen.
	var matching_sports = req.body.bikesports[0]
	var location = req.body.location;
	var speed = req.body.averageSpeed;
	console.log(matching_sports);
	profilesCollection.find({bikesports: matching_sports}).toArray(function(error, result){
		console.log(result);

	});
	console.log("data: " + JSON.stringify(req.body));
	
	//MAtching: Anfrage an datenbank


	//Event erstellen und vorschlagen

	res.writeHead(200, 'OK');
	res.end();
})

router.get('/profiles/:id', function(req, res){
	//find ressource 'profile :id'
});
router.post('/events', function(req, res){
	//post data to ressource '/events'
});

router.get('/notification', function(req, res){

});



module.exports = router;
