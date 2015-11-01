angular.module('weatherApp')

.controller('SimpleCtrl', function($scope, $state, SessionService, $timeout, $interval) {
    $scope.today = new Date();
    $scope.weatherCurrent;
    $scope.imageLink = 'http://openweathermap.org/img/w/01d.png';
    $scope.forecast;
    $scope.imageLinkF = [];
    $scope.nextDays = [];
    $scope.liveStream = [];
    $scope.imageLinkLS;
    $scope.analytics = [[], [], [],[]];
        
    //Create days of coming week
    for (j = 1; j < 7; j++) { 
        var nextD = new Date();    
        nextD.setDate(nextD.getDate()+j);
        $scope.nextDays.push(nextD);
    }
    
    
    //Gets Weather Data
        SessionService.getWeather().then(function() {
            //Get current Weather
            $scope.weatherCurrent = SessionService.weatherCurrent();
            console.log($scope.weatherCurrent);
            //create imagelink for current weather
            $scope.imageLink = 'http://openweathermap.org/img/w/' + $scope.weatherCurrent['weather'][0].icon + ".png";
            
            //Get Forecast
            $scope.forecast = SessionService.weatherForecast();
            console.log($scope.forecast);
            //Create imagelink for forecasts
            for (i = 0; i < $scope.forecast.list.length; i++) {
                $scope.imageLinkF.push('http://openweathermap.org/img/w/' + $scope.forecast['list'][i]['weather'][0].icon + ".png");
            }
            
            //Get analytics
            var analyticData = SessionService.analytics();
            //Divide in four temperature groups
            console.log("DIT IS ANALYTIC DATA:" +analyticData)
            for (i = 0; i<analyticData.length; i++) {
                if (analyticData[i].value.tempGroup < 0) {
                    $scope.analytics[0].push(analyticData[i]._id)
                } else if (analyticData[i].value.tempGroup < 3) {
                    $scope.analytics[analyticData[i].value.tempGroup].push(analyticData[i]._id)
                } else {
                    $scope.analytics[3].push(analyticData[i]._id)
                }
            }
            console.log($scope.analytics)
            
        }, function(err) {
            console.log('Geen info opgehaald');
        });
    
    
    //SessionService.getForecast().then(function() {
      //  $scope.forecast = SessionService.weatherForecast();
        //create imagelink for weatherForecast
        //for (i = 0; i < $scope.forecast.list.length; i++) {
         //   $scope.imageLinkF.push('http://openweathermap.org/img/w/' + $scope.forecast['list'][i]['weather'][0].icon + ".png");
        //}
   // }, function(err) {
     //   console.log('Geen info opgehaald');
    //});
    
    // var getLiveStream = function() {
        // ws = new WebSocket('ws://localhost:9000/stream')
        // ws.onmessage = function( message ) { 
            // $scope.liveStream = JSON.parse(message.data);
            // console.log($scope.liveStream)
            // $scope.$apply();
        // }
    // };
    // var logMes = function() {console.log($scope.livestream)}
    // var setLiveStreamData = function(data) {
        // $scope.liveStream = JSON.parse(data);
        // $scope.imageLinkLS = 'http://openweathermap.org/img/w/' + $scope.liveStream['weather'][0].icon + ".png";
        // console.log($scope.liveStream.name);
        // console.log($scope.imageLinkLS);
    // }
    // getLiveStream();
})
