#!/bin/bash
mvn clean package
cat unix_self_exec.sh target/launcher-1.2.1-SNAPSHOT.jar > target/launcher-unix && chmod uog+x target/launcher-unix
cat win_self_exec.bat target/launcher-1.2.1-SNAPSHOT.jar > target/launcher-win
