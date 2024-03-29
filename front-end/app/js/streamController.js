//Handles the websocket for the livestreambox
var ws;
angular.module('weatherApp')

.controller('StreamCtrl', function($scope) {

  updateData = function(message) {
    //console.log('called!');
    $scope.liveStream = JSON.parse(message.data);
    $scope.$apply();
  };

  //console.log('Livestream opened!');
  ws = new WebSocket('ws://188.226.144.15:9000/stream');
  ws.onmessage = updateData;
});
