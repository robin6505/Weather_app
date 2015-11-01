var weatherApp = angular.module('weatherApp', ['ui.router']);

weatherApp.config(function($stateProvider, $urlRouterProvider) {
    
    $stateProvider
        
        // HOME STATES AND NESTED VIEWS ========================================
        .state('main', {
            url: '/main',
            templateUrl: 'templates/main.html'
            
        })
        .state('result', {
            url: '/result',
            //controller: 'SimpleCtrl',
            views: {

                // the main template 
                '': { templateUrl: 'templates/results.html', controller: 'SimpleCtrl' },

                // the child views
                'currentBox@result': { templateUrl: 'templates/currentWeather.html'},
                'forecastBox@result': { templateUrl: 'templates/forecast.html'},
                'pastBox@result': { templateUrl: 'templates/pastDays.html' },
                'livestreamBox@result': { templateUrl: 'templates/livestream.html', controller: 'StreamCtrl' },
                'analysisBox@result': { templateUrl: 'templates/weatherAnalysis.html'}                
            }
        })        
        $urlRouterProvider.otherwise('/main');
        
})