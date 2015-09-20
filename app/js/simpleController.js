angular.module('weatherApp')

.controller('SimpleCtrl', function($scope, $state, SessionService, $timeout) {
    $scope.today = new Date();
    $scope.weatherCurrent;
    $scope.imageLink = 'http://openweathermap.org/img/w/01d.png';
    $scope.forecast;
    $scope.imageLinkF = [];
    $scope.nextDays = [];
    
    //Create days of coming week
    for (j = 1; j < 7; j++) { 
        var nextD = new Date();    
        nextD.setDate(nextD.getDate()+j);
        $scope.nextDays.push(nextD);
    }
    
    
    //Gets current Weather
    SessionService.getWeather().then(function() {
        $scope.weatherCurrent = SessionService.weatherCurrent();
        console.log($scope.weatherCurrent);
        //create imagelink for weather
        $scope.imageLink = 'http://openweathermap.org/img/w/' + $scope.weatherCurrent['weather'][0].icon + ".png";
    }, function(err) {
        console.log('Geen info opgehaald');
    });
    
    SessionService.getForecast().then(function() {
        $scope.forecast = SessionService.weatherForecast();
        //create imagelink for weatherForecast
        for (i = 0; i < $scope.forecast.list.length; i++) {
            $scope.imageLinkF.push('http://openweathermap.org/img/w/' + $scope.forecast['list'][i]['weather'][0].icon + ".png");
        }
    }, function(err) {
        console.log('Geen info opgehaald');
    });
    
    
    
    
})