@ECHO OFF
setlocal

REM ��������� ��� ��������� ��室����� JAVA
REM V 1.1 - �ᯮ������ ������� �������.
REM V 1.00 - ��ࢠ� �ᯫ��樮���� �����.

REM==================================================
REM ��⠭���� ��६�����
SET RUNDIR=%~dp0
SET FILES_PATH=%RUNDIR%

REM==================================================
REM ���� 䠩��� �믮������
javaw -jar MTBmessenger.jar STOP
pause
