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
	var id = req.body.id;
	var speed = req.body.speed;
	var distance = req.body.distance;
	profilesCollection.insert(req.body, function(error, profilesCollection){
		if (error) next(error);
		else{
			console.log("success");
		}
	});

	console.log("data: " + JSON.stringify(req.body));
	//Profil in die Datenbank schreiben
	//MAtching: Anfrage an datenbank

	res.writeHead(200, 'OK');
	res.end();
})



module.exports = router;
