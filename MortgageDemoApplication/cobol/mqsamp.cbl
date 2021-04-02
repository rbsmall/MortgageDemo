       ID DIVISION.
      ** test the binder include for mq stubs
       PROGRAM-ID. MQSAMP.
       ENVIRONMENT DIVISION.
       DATA DIVISION.
       WORKING-STORAGE SECTION.
       01  W00-QMGR                    PIC X(48).
       01  W03-HCONN                   PIC S9(9) BINARY VALUE 0.
       01  W03-HOBJ                    PIC S9(9) BINARY VALUE 0.
       01  W03-OPENOPTIONS             PIC S9(9) BINARY.
       01  W03-COMPCODE                PIC S9(9) BINARY.
       01  W03-REASON                  PIC S9(9) BINARY.
      * 01  MQM-OBJECT-DESCRIPTOR.
      *     COPY CMQODV.
      * 01  MQM-MESSAGE-DESCRIPTOR.
      *     COPY CMQMDV.
      * 01  MQM-PUT-MESSAGE-OPTIONS.
      *     COPY CMQPMOV SUPPRESS.
        01  MQM-CONSTANTS.
            COPY CMQV SUPPRESS.
        PROCEDURE DIVISION.
           DISPLAY 'HELLO'.
           CALL 'MQCONN' USING W00-QMGR
                 W03-HCONN
                 W03-COMPCODE
                 W03-REASON.