@echo off
chcp 65001 > nul
javac -encoding UTF-8 -cp "lib\*" -d bin -sourcepath src src\targetcompare\Compare.java src\targetcompare\Gene.java src\targetcompare\Mirna.java src\com\som\core\*.java src\com\som\grafico\*.java src\com\som\view\*.java
if %ERRORLEVEL% neq 0 pause
