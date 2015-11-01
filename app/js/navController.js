angular.module('weatherApp')

.controller('NavCtrl', function($scope, $state, SessionService, $timeout) {
    $scope.getResults = function(city) {
        if(typeof city === 'undefined'){
            alert("Enter a city name in the text field");
        } else {
            SessionService.saveLocation(city.name);
            $state.go('result', {}, {reload: true});
        }
    }    
})