@echo off
mysql -u root -p3523048584 < "%~dp0init_users.sql"
echo Done. Check for errors above.
pause
