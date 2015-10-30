angular.module('weatherApp')

.service('SessionService', function($q, $http) {
    var weatherCurrent;
    var weatherForecast;
    var analytics;
    var liveStream;
    var city;
    var apiKey = "&APPID=2058452e3cf07873e426f1d723339ec6";
    
    
    
    
    var cityList = [3117735, 524901, 2988507, 4219762, 5128581, 5368361, 1850147, 3413829, 360630, 2643743];
    
    function saveLocation(loc) {
        return $q(function(resolve, reject) {
            city = loc;
            console.log(loc); //debuglog
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
                    console.log("SESSION SERVICE ANALYTICS: " + analytics); //debug log
                    resolve('get weather succes');
                }, function(response) {
                    reject('error');
                });
        })
        
    }
    
    
    var getLiveStream = function() {
        return $q(function(resolve, reject) { //$q is needed else controller continues before data is properly set in weatherCurrent
            var rand = cityList[Math.floor(Math.random() * cityList.length)];
            var link = "http://api.openweathermap.org/data/2.5/weather?id=" + rand + "&units=metric" + apiKey;
            $http.get(link).
                then(function(response) {
                    if (response.status === 200) {
                        liveStream = response.data;
                        console.log(liveStream); //debug log
                        resolve('get weather succes');
                    } else {
                        reject('error');
                    }
                }, function(response) {
                    reject('error');
                });
        })
        
    }
    
    
    
    return {
        saveLocation: saveLocation,
        getWeather: getWeather,
        getLiveStream: getLiveStream,
		weatherCurrent: function() {return weatherCurrent;},
        weatherForecast: function() {return weatherForecast;},
        analytics: function() {return analytics;},
        liveStream: function() {return liveStream;}
    }
    
})

//apikey: 2058452e3cf07873e426f1d723339ec6
