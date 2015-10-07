angular.module('weatherApp')

.controller('SimpleCtrl', function($scope, $state, SessionService, $timeout, $interval) {
    $scope.today = new Date();
    $scope.weatherCurrent;
    $scope.imageLink = 'http://openweathermap.org/img/w/01d.png';
    $scope.forecast;
    $scope.imageLinkF = [];
    $scope.nextDays = [];
    $scope.liveStream;
    $scope.imageLinkLS;
    $scope.analytics = [{}, {}, {}, {}, {}];
    
    //dummydata analysis
    $scope.analysisData = [
        {
            name: "London",
            temp: 24,
            humidity: 67,
            wind: 5
        },
        {
            name: "Amsterdam",
            temp: 18,
            humidity: 91,
            wind: 11
        },
        {
            name: "New York",
            temp: 26,
            humidity: 88,
            wind: 4
        },
        {
            name: "Staphorst",
            temp: 12,
            humidity: 78,
            wind: 1
        },
        {
            name: "Melbourne",
            temp: 32,
            humidity: 50,
            wind: 2
        }
    ];
    
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
    
    var getLiveStream = function() {
        SessionService.getLiveStream().then(function() {
            $scope.liveStream = SessionService.liveStream();
            console.log($scope.liveStream);
            //create imagelink for weather
            $scope.imageLinkLS = 'http://openweathermap.org/img/w/' + $scope.liveStream['weather'][0].icon + ".png";
        }, function(err) {
            console.log('Geen info opgehaald');
        }) 
    };
    getLiveStream();
    $interval(getLiveStream, 5000);
    // $interval(function(){
       // SessionService.getLiveStream().then(function() {
            // $scope.liveStream = SessionService.liveStream();
            // console.log($scope.liveStream);
            //create imagelink for weather
            // $scope.imageLinkLS = 'http://openweathermap.org/img/w/' + $scope.liveStream['weather'][0].icon + ".png";
        // }, function(err) {
            // console.log('Geen info opgehaald');
        // }) 
    // }, 5000);
    
    // function getLiveStream() {
        // SessionService.getLiveStream().then(function() {
            // $scope.liveStream = SessionService.liveStream();
            // console.log($scope.liveStream);
           // create imagelink for weather
            // $scope.imageLinkLS = 'http://openweathermap.org/img/w/' + $scope.liveStream['weather'][0].icon + ".png";
        // }, function(err) {
            // console.log('Geen info opgehaald');
        // })
    // }
    $scope.changeInfo = function(dat) {
        console.log("IK BEN HIER" + dat);
        for (i=0; i<$scope.analysisData.length; i++) {
            $scope.analytics[i].name = $scope.analysisData[i].name;
            $scope.analytics[i].info = $scope.analysisData[i][dat];
        }
    }
    $scope.changeInfo('temp');
    
    
    
})