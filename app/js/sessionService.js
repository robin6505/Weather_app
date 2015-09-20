angular.module('weatherApp')

.service('SessionService', function($q, $http) {
    var weatherCurrent;
    var weatherForecast;
    var city;
    var apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6"
    
    function saveLocation(loc) {
        return $q(function(resolve, reject) {
            city = loc;
            console.log(loc); //debuglog
            resolve();
        });
    }
    
    var getWeather = function() {
        return $q(function(resolve, reject) { //$q is needed else controller continues before data is properly set in weatherCurrent
            var link = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric" + apiKey;
            $http.get(link).
                then(function(response) {
                    weatherCurrent = response.data;
                    //console.log(weatherCurrent); //debug log
                    resolve('get weather succes');
                }, function(response) {
                    reject('error');
                });
        })
        
    }
    
    var getForecast = function() {
        return $q(function(resolve, reject) { //$q is needed else controller continues before data is properly set in weatherCurrent
            var link = "http://api.openweathermap.org/data/2.5/forecast/daily?q=" + city + "&cnt=6" + "&units=metric" + apiKey;
            $http.get(link).
                then(function(response) {
                    weatherForecast = response.data;
                    //console.log(weatherCurrent); //debug log
                    resolve('get forecast succes');
                }, function(response) {
                    reject('error');
                });
        })
        
    }
    
    return {
        saveLocation: saveLocation,
        getWeather: getWeather,
        getForecast: getForecast,
		weatherCurrent: function() {return weatherCurrent;},
        weatherForecast: function() {return weatherForecast;}
    }
    
})

//apikey: 2058452e3cf07873e426f1d723339ec6