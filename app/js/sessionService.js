angular.module('weatherApp')

.service('SessionService', function($q, $http) {
    var weatherCurrent;
    var weatherForecast;
    var analytics;
    var liveStream;
    var city;
    
	var saveLocation = function(loc) {
        return $q(function(resolve, reject) {
            city = loc;
            resolve();
        });
    }
    
	//Makes request to the request handler and sets all data in the variables. It signals back to the controller when all data is received and set
    var getWeather = function() {
        return $q(function(resolve, reject) { //$q is needed else controller continues before data is properly set in weatherCurrent
            var link = "http://localhost:9000/getData/" + city;
            $http.get(link).
                then(function(response) {
                    weatherCurrent = response.data.CURRENTWEATHER;
                    weatherForecast = response.data.FORECAST;
                    analytics = response.data.ANALYTICS;
                    resolve('get weather succes');
                }, function(response) {
                    reject('error');
                });
        })
        
    }  
    
    return {
        saveLocation: saveLocation,
        getWeather: getWeather,
		weatherCurrent: function() {return weatherCurrent;},
        weatherForecast: function() {return weatherForecast;},
        analytics: function() {return analytics;},
    }
    
})
