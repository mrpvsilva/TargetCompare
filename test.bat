@echo off
chcp 65001 > nul

if not exist bin mkdir bin

echo [1/3] Compilando fontes principais...
javac -encoding UTF-8 -cp "lib\*" -d bin -sourcepath src ^
    src\targetcompare\Compare.java ^
    src\targetcompare\Gene.java ^
    src\targetcompare\Mirna.java ^
    src\com\som\core\*.java ^
    src\com\som\grafico\*.java ^
    src\com\som\view\*.java
if %ERRORLEVEL% neq 0 (
    echo FALHA na compilacao dos fontes principais.
    pause & exit /b 1
)

echo [2/3] Compilando testes...
if not exist bin\test mkdir bin\test
javac -encoding UTF-8 -cp "lib\*;bin" -d bin\test ^
    test\targetcompare\GeneTest.java ^
    test\com\som\core\RedeTest.java
if %ERRORLEVEL% neq 0 (
    echo FALHA na compilacao dos testes.
    pause & exit /b 1
)

echo [3/3] Executando testes...
java -cp "lib\*;bin;bin\test" org.junit.runner.JUnitCore ^
    targetcompare.GeneTest ^
    com.som.core.RedeTest

if %ERRORLEVEL% neq 0 (
    echo.
    echo TESTES FALHARAM.
    pause & exit /b 1
)
echo.
echo Todos os testes passaram.
pause
