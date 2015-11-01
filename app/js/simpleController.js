angular.module('weatherApp')

.controller('SimpleCtrl', function($scope, $state, SessionService) {
    $scope.today = new Date();
    $scope.weatherCurrent;
    $scope.imageLink = 'http://openweathermap.org/img/w/01d.png';
    $scope.forecast;
    $scope.imageLinkF = [];
    $scope.nextDays = [];
    $scope.liveStream = [];
    $scope.imageLinkLS;
    $scope.analytics = [[], [], [],[]];
        
    //Create days of coming week needed for forecast
    for (j = 1; j < 7; j++) { 
        var nextD = new Date();    
        nextD.setDate(nextD.getDate()+j);
        $scope.nextDays.push(nextD);
    }
    
    
    //Gets Weather Data
    SessionService.getWeather().then(function() {
        //Get current Weather
        $scope.weatherCurrent = SessionService.weatherCurrent();
     
        //create imagelink for current weather
        $scope.imageLink = 'http://openweathermap.org/img/w/' + $scope.weatherCurrent['weather'][0].icon + ".png";
        
        //Get Forecast
        $scope.forecast = SessionService.weatherForecast();
        
        //Create imagelink for forecasts
        for (i = 0; i < $scope.forecast.list.length; i++) {
            $scope.imageLinkF.push('http://openweathermap.org/img/w/' + $scope.forecast['list'][i]['weather'][0].icon + ".png");
        }
        
        //Get analytics
        var analyticData = SessionService.analytics();
        //Divide in four temperature groups based on label temperatureGroup. This label is givin via analytics app with map/reduce
        for (i = 0; i<analyticData.length; i++) {
            if (analyticData[i].value.tempGroup < 0) {
                $scope.analytics[0].push(analyticData[i]._id)
            } else if (analyticData[i].value.tempGroup < 3) {
                $scope.analytics[analyticData[i].value.tempGroup].push(analyticData[i]._id)
            } else {
                $scope.analytics[3].push(analyticData[i]._id)
            }
        }
        
    }, function(err) {
        console.log('Did not get any info');
    });
})
