angular.module('weatherApp')

.controller('NavCtrl', function($scope, $state, SessionService, $timeout) {
    $scope.getResults = function(city) {
        SessionService.saveLocation(city.name);
        $state.go('result', {}, {reload: true});
    }    
})