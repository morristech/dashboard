'use strict'
/**
 * Created by excilys on 06/06/16.
 */

angular
  .module('dashboardFrontApp')
  .controller('BundleAddController', ['$scope', '$http', '$window', 'API', 'DATE', 'bundleService', 'dateService', function ($scope, $http, $window, API, DATE, bundleService, dateService) {
    $scope.bundle = {};
    $scope.bundle.validity = {};
    $scope.nav = "Add a bundle";
    $scope.title = "Add";
    $scope.regexDate = DATE.REGEXP;
    $scope.loading = false;

    $scope.submit = function (isValid) {
      if (isValid && dateService.validateDates($scope.bundle.validity.start, $scope.bundle.validity.end, $scope) ) {
        $scope.loading = true;
        bundleService.create($scope.bundle).then(
          function(success) {
            $window.location.href = '#/dashboard';
          }, function(error) {
            $scope.loading = false;
            $scope.error = "Erreur lors de l'envoi du formulaire";
          });
      }
    }
  }]);
