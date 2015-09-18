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
            views: {

                // the main template will be placed here (relatively named)
                '': { templateUrl: 'templates/results.html' },

                // the child views will be defined here (absolutely named)
                'currentBox@result': { templateUrl: 'templates/currentWeather.html' },
                'forecastBox@result': { templateUrl: 'templates/forecast.html' },
                'pastBox@result': { templateUrl: 'templates/pastDays.html' },
                'livestreamBox@result': { templateUrl: 'templates/livestream.html' }     
            }
        })        
        $urlRouterProvider.otherwise('/main');
        
})