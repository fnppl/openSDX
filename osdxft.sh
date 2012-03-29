#!/bin/bash

java -cp "./bin:lib/jdom.jar:lib/bcprov-jdk16-145.jar:jdom.jar:bcprov-jdk16-145.jar:opensdx_secgui.jar" org.fnppl.opensdx.file_transfer.OSDXFileTransferClient $*

