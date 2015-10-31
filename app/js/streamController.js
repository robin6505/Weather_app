//var ws;
angular.module('weatherApp')

.controller('StreamCtrl', function($scope, $state, SessionService, $timeout, $interval) {

   $scope.updateMe = function() {
    $scope.liveStream = "grunn";
  };

  updateData = function(message) {
    console.log('called!');
    $scope.liveStream = JSON.parse(message.data);
    //$scope.liveStream.name = "gro.ningen";
    $scope.$apply();
  };

  console.log('Livestream opened!');
  ws = new WebSocket('ws://localhost:9000/stream');
  ws.onmessage = updateData;
});