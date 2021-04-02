//BSMALL0J JOB (ACCOUNT),'PROJDEF',CLASS=A,MSGCLASS=X,TYPRUN=SCAN       00010000
//*                                                                     00030000
//ASM      EXEC PGM=ASMA90,REGION=4096K,                                00040000
//             PARM='LIST,NOESD,NORLD,NOXREF,RENT'                      00050000
//SYSIN    DD  DSN=BSMALL.PROJDEFS.SOURCE(FLM@HLAS),DISP=SHR            00060001
//SYSLIB   DD  DISP=SHR,DSN=BSMALL.PROJDEFS.SOURCE                      00070001
//         DD  DISP=SHR,                                                00080000
//         DSN=ISP.SISPMACS                                             00090000
//         DD  DISP=SHR,                                                00100000
//         DSN=ISP.SISPMACS                                             00110000
//         DD  DISP=SHR,                                                00120000
//         DSN=SYS1.MACLIB                                              00130000
//SYSUT1   DD  UNIT=VIO,SPACE=(CYL,(2,2))                               00140000
//SYSLIN   DD  DSN=BSMALL.PROJDEFS.OBJ(FLM@HLAS),DISP=SHR               00150001
//SYSPRINT DD  DSN=BSMALL.PROJDEFS.ASMLIST(FLM@HLAS),DISP=SHR           00160001
//*                                                                     00170000
//LKED     EXEC PGM=IEWBLINK,                                           00180000
//             COND=(4,LT,ASM),                                         00190000
//             PARM='LIST,NCAL,REUS,RENT'                               00200000
//SYSLIN   DD  *                                                        00210000
           INCLUDE OBJECT                                               00220000
           MODE AMODE(24),RMODE(24)                                     00230000
           NAME FLM@HLAS(R)                                             00240001
//OBJECT   DD  DSN=*.ASM.SYSLIN,DISP=SHR                                00250000
//SYSUT1   DD  UNIT=VIO,SPACE=(CYL,(2,2))                               00260000
//SYSLMOD  DD  DSN=BSMALL.PROJDEFS.LOAD,DISP=SHR                        00270001
//SYSPRINT DD  DSN=BSMALL.PROJDEFS.LKEDLIST(FLM@HLAS),DISP=SHR          00280001
//*                                                                     00290000