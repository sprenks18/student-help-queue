# student-help-queue README
Server and desktop client for knowing who should be helped next in a networked lab

## Configuration

Update ''configuration.prop'' to customize the server's location.  
(Needs to be changed by all who run; or, everyone uses
the same jar file to run the code.)

Update ''hostmap.prop'' with the locations of the machines so that you can find which student is requesting help.

## Running

To run the server:

> java -cp labhelp.jar labhelp.HelpServer

There is a script on the server to run to start the help server on start.
Put the script (labhelp-server.service) in /etc/systemd/system/

To run the client:

> java -jar labhelp.jar


