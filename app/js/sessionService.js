angular.module('weatherApp')

.service('SessionService', function($q, $http) {
    var weatherCurrent;
    var weatherForecast;
    var analytics;
    var liveStream;
    var city;
    var apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6";
    
    function saveLocation(loc) {
        return $q(function(resolve, reject) {
            city = loc;
            resolve();
        });
    }
    
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

//apikey: 2058452e3cf07873e426f1d723339ec6
