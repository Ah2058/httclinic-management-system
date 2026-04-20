-- Create auth_db database if it doesn't exist
CREATE DATABASE IF NOT EXISTS auth_db;

-- Grant privileges to root user for both databases
GRANT ALL PRIVILEGES ON auth_db.* TO 'root'@'%' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON clinicdb.* TO 'root'@'%' IDENTIFIED BY 'root';
FLUSH PRIVILEGES;

