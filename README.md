This is an Android-compatible branch of Cf framework. Notable limitations include:

  - No DTLS support

###Implementation Note:

Cf project has been made Android-compatible by simply removing the .dtls package from the original californium project. The resulting compile errors were eliminated either by commenting out the relevant lines or by deleting some files.


####Potential Enhancements:

  - Mavenize the sample Android project
  - Use bouncy castle/spongy castle to implement DTLS support in Android (replacing the Java7 security providers used in the original californium project)
  - More samples (including CoAP client, proxy etc)
  - ProGuard-ize


Californium (Cf) CoAP framework in Java
=======================================

Californium is a Java CoAP implementation targeting back-end services. Thus, the
focus is on usability and features, not on resource-efficiency like for embedded
devices. The Java implementation fosters quick server and client development.

Cf is now a MAVEN PROJECT!

Use "mvn clean install" in the Cf root directory to build everything.
Standalone JARs of the examples will be copied to ./run/.
(For convenience they are directly included in the Git repository.)

The Maven repositories are:
http://maven.thingml.org/archiva/repository/thingml-release
http://maven.thingml.org/archiva/repository/thingml-snapshot

The build status can be followed on:
http://build.thingml.org/

For an Interop Server, please visit http://vs0.inf.ethz.ch/.
