# driveby

This is a Scala SDK for accessing Microsoft OneDrive.

Add to your SBT build with

    libraryDependencies += "net.koofr" %% "driveby" % "0.1.2"

## Usage

You need a Microsoft developer account and a created application.

Create an instance of `WebAuth` passing in your settings and send your user off to url given by `WebAuth#start`.

After OAuth cycle is complete pass in the destination url through `WebAuth#finish`. This will give you an instance of `UserAuth` containing tokens and expiration time. You should save this data and use it to build an `AuthConfig`. Using this `AuthConfig` instance you can create a client that exposes OneDrive API for the authenticated user. 

OneDrive does not identify files and folders by paths but uses some identifiers. This shows in the methods of `Client`. In order to work with paths use `Client#resolvePath` to resolve a path into a usable id.