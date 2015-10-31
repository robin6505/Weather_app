var ws;
angular.module('weatherApp')

.controller('SimpleCtrl', function($scope, $state, SessionService, $timeout, $interval) {

  $scope.updateMe = function() {
    $scope.liveStream = "grunn";
  };

  updateData = function(message) {
    console.log('called!');
    $scope.liveStream = message.data;
    $scope.liveStream.name = "gro.ningen";
    $scope.$apply();
  };

  console.log('Livestream opened!');
  ws = new WebSocket('wss://echo.websocket.org');
  ws.onmessage = updateData;
});
