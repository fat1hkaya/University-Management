@echo off
set BASE=c:\Users\fatih\OneDrive\Masaüstü\University-Management-System-in-JavaFX\University-Management-System-in-JavaFX-7a0e6685fb0d906320616df962823e1e1e3a4b57\UniversitiyManagementSystem
set SRC=%BASE%\src
set OUT=%BASE%\target\classes

set MP=C:\Users\fatih\.m2\repository\org\openjfx\javafx-base\17.0.12\javafx-base-17.0.12-win.jar;C:\Users\fatih\.m2\repository\org\openjfx\javafx-controls\17.0.12\javafx-controls-17.0.12-win.jar;C:\Users\fatih\.m2\repository\org\openjfx\javafx-fxml\17.0.12\javafx-fxml-17.0.12-win.jar;C:\Users\fatih\.m2\repository\org\openjfx\javafx-graphics\17.0.12\javafx-graphics-17.0.12-win.jar;C:\Users\fatih\.m2\repository\org\xerial\sqlite-jdbc\3.46.1.3\sqlite-jdbc-3.46.1.3.jar;C:\Users\fatih\.m2\repository\org\slf4j\slf4j-api\2.0.7\slf4j-api-2.0.7.jar;C:\Users\fatih\.m2\repository\org\slf4j\slf4j-simple\2.0.7\slf4j-simple-2.0.7.jar;C:\Users\fatih\.m2\repository\org\mindrot\jbcrypt\0.4\jbcrypt-0.4.jar

if not exist %OUT% mkdir %OUT%

"C:\Program Files\Java\jdk-17\bin\javac.exe" --module-path "%MP%" -d "%OUT%" -encoding UTF-8 @"%BASE%\filelist.txt"

if %ERRORLEVEL% equ 0 (
    echo Compile successful.
) else (
    echo Compile failed with exit code %ERRORLEVEL%
)
