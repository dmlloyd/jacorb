@echo off
set JACORBDEF=-Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton
set CP=../../classes
set XCP=../../lib/jacorb.jar;../../lib/logkit-1.2.jar;../../lib/avalon-framework-4.1.5.jar;../../lib/concurrent-1.3.2.jar;../../lib/antlr-2.7.2.jar;
java -Xbootclasspath/p:%XCP% -cp %CP% %JACORBDEF% -Dcustom.props=%1.properties org.jacorb.test.sas.%2_Client ftp://greene.case.syr.edu/pub/CSIv2InterOpTestBed/Johnson_%1_POA.ior guest password
