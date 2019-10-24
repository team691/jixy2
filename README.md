# jixy2

A Java library for the Pixy2 vision-processing camera.
Aims to be similar in API to the [C/C++ pixy2 library](
https://docs.pixycam.com/wiki/doku.php?id=wiki:v2:full_api).

Developed initially for use in FIRST FRC robot programs,
but possible to be extended to other platforms by way of
the DeviceLink interface, where platform-specific code
for sending and receiving data to and from the Pixy2
device should be included in a class implementing
DeviceLink.

An example robot program including I2CWPILink, a DeviceLink
for communicating with the Pixy2 over I2C from the roboRIO,
is included in example/.
