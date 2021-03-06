<!--
#    Licensed to the Apache Software Foundation (ASF) under one
#    or more contributor license agreements.  See the NOTICE file
#    distributed with this work for additional information
#    regarding copyright ownership.  The ASF licenses this file
#    to you under the Apache License, Version 2.0 (the
#    "License"); you may not use this file except in compliance
#    with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing,
#    software distributed under the License is distributed on an
#    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#    KIND, either express or implied.  See the License for the
#    specific language governing permissions and limitations
#    under the License.
-->

# Native GPS Location for Cordova - Android

This plugin provides information about the device's location, such as
latitude and longitude. Source of location information is Global Positioning System (GPS). There is no guarantee that the API returns the
device's actual location.

## GPSLocation.getCurrentPosition

Returns the device's current position to the `geolocationSuccess`
callback with a `Position` object as the parameter.  If there is an
error, the `geolocationError` callback is passed a
`PositionError` object.

    GPSLocation.getCurrentPosition(geolocationSuccess,
                                             [geolocationError],
                                             [geolocationOptions]);

### Parameters

- __geolocationSuccess__: The callback that is passed the current position.

- __geolocationError__: _(Optional)_ The callback that executes if an error occurs.

- __geolocationOptions__: _(Optional)_ The geolocation options.


### Example

    // onSuccess Callback
    // This method accepts a Position object, which contains the
    // current GPS coordinates
    //
    var onSuccess = function(position) {
        alert('Latitude: '          + position.coords.latitude          + '\n' +
              'Longitude: '         + position.coords.longitude         + '\n' +
              'Altitude: '          + position.coords.altitude          + '\n' +
              'Accuracy: '          + position.coords.accuracy          + '\n' +
              'Altitude Accuracy: ' + position.coords.altitudeAccuracy  + '\n' +
              'Heading: '           + position.coords.heading           + '\n' +
              'Speed: '             + position.coords.speed             + '\n' +
              'Timestamp: '         + position.timestamp                + '\n');
    };

    // onError Callback receives a PositionError object
    //
    function onError(error) {
        alert('code: '    + error.code    + '\n' +
              'message: ' + error.message + '\n');
    }

    GPSLocation.getCurrentPosition(onSuccess, onError);

## GPSLocation.watchPosition

Returns the device's current position when a change in position is detected.
When the device retrieves a new location, the `geolocationSuccess`
callback executes with a `Position` object as the parameter.  If
there is an error, the `geolocationError` callback executes with a
`PositionError` object as the parameter.

    var watchId = GPSLocation.watchPosition(geolocationSuccess,
                                                      [geolocationError],
                                                      [geolocationOptions]);

### Parameters

- __geolocationSuccess__: The callback that is passed the current position.

- __geolocationError__: (Optional) The callback that executes if an error occurs.

- __geolocationOptions__: (Optional) The geolocation options.

### Returns

- __String__: returns a watch id that references the watch position interval. The watch id should be used with `GPSLocation.clearWatch` to stop watching for changes in position.

### Example

    // onSuccess Callback
    //   This method accepts a `Position` object, which contains
    //   the current GPS coordinates
    //
    function onSuccess(position) {
        var element = document.getElementById('geolocation');
        element.innerHTML = 'Latitude: '  + position.coords.latitude      + '<br />' +
                            'Longitude: ' + position.coords.longitude     + '<br />' +
                            '<hr />'      + element.innerHTML;
    }

    // onError Callback receives a PositionError object
    //
    function onError(error) {
        alert('code: '    + error.code    + '\n' +
              'message: ' + error.message + '\n');
    }

    // Options: throw an error if no update is received every 30 seconds.
    //
    var watchID = GPSLocation.watchPosition(onSuccess, onError, { timeout: 30000 });


## geolocationOptions

Optional parameters to customize the retrieval of the geolocation
`Position`.

    { maximumAge: 3000, timeout: 5000 };

### Options

- __timeout__: The maximum length of time (milliseconds) that is allowed to pass from the call to `GPSLocation.getCurrentPosition` or `geolocation.watchPosition` until the corresponding `geolocationSuccess` callback executes. If the `geolocationSuccess` callback is not invoked within this time, the `geolocationError` callback is passed a `PositionError.TIMEOUT` error code. (Note that when used in conjunction with `geolocation.watchPosition`, the `geolocationError` callback could be called on an interval every `timeout` milliseconds!) _(Number)_

- __maximumAge__: Accept a cached position whose age is no greater than the specified time in milliseconds. _(Number)_

## GPSLocation.clearWatch

Stop watching for changes to the device's location referenced by the
`watchID` parameter.

    GPSLocation.clearWatch(watchID);

### Parameters

- __watchID__: The id of the `watchPosition` interval to clear. (String)

### Example

    // Options: watch for changes in position, and use the most
    // accurate position acquisition method available.
    //
    var watchID = GPSLocation.watchPosition(onSuccess, onError, { enableHighAccuracy: true });

    // ...later on...

    GPSLocation.clearWatch(watchID);

## Position

Contains `Position` coordinates and timestamp, created by the geolocation API.

### Properties

- __coords__: A set of geographic coordinates. _(Coordinates)_

- __timestamp__: Creation timestamp for `coords`. _(Date)_

## Coordinates

A `Coordinates` object is attached to a `Position` object that is
available to callback functions in requests for the current position.
It contains a set of properties that describe the geographic coordinates of a position.

### Properties

* __latitude__: Latitude in decimal degrees. _(Number)_

* __longitude__: Longitude in decimal degrees. _(Number)_

* __altitude__: Height of the position in meters above the ellipsoid. _(Number)_

* __accuracy__: Accuracy level of the latitude and longitude coordinates in meters. _(Number)_

* __altitudeAccuracy__: Accuracy level of the altitude coordinate in meters. _(Number)_

* __heading__: Direction of travel, specified in degrees counting clockwise relative to the true north. _(Number)_

* __speed__: Current ground speed of the device, specified in meters per second. _(Number)_

### Android Quirks

__altitudeAccuracy__: Not supported by Android devices, returning `null`.

## PositionError

The `PositionError` object is passed to the `geolocationError`
callback function when an error occurs with GPSLocation.

### Properties

- __code__: One of the predefined error codes listed below.

- __message__: Error message describing the details of the error encountered.

### Constants

- `PositionError.PERMISSION_DENIED`
  - Returned when users do not allow the app to retrieve position information. This is dependent on the platform.
- `PositionError.POSITION_UNAVAILABLE`
  - Returned when the device is unable to retrieve a position. In general, this means the device is not connected to a network or can't get a satellite fix.
- `PositionError.TIMEOUT`
  - Returned when the device is unable to retrieve a position within the time specified by the `timeout` included in `geolocationOptions`. When used with `GPSLocation.watchPosition`, this error could be repeatedly passed to the `geolocationError` callback every `timeout` milliseconds.
