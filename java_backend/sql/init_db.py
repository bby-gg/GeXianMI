"""Create users table and default admin (admin/admin123)."""
import pymysql

# Pre-computed BCrypt hash for "admin123"
BCRYPT_HASH = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh"

conn = pymysql.connect(
    host='localhost', user='root', password='3523048584',
    database='gexianmi', charset='utf8mb4'
)
cur = conn.cursor()

cur.execute("""
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(32) NOT NULL UNIQUE,
  password_hash VARCHAR(128) NOT NULL,
  role VARCHAR(16) NOT NULL DEFAULT 'operator',
  last_login_at DATETIME,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
""")
conn.commit()

cur.execute('SELECT COUNT(*) FROM users WHERE username = %s', ('admin',))
if cur.fetchone()[0] == 0:
    cur.execute('INSERT INTO users (username, password_hash, role) VALUES (%s, %s, %s)',
                ('admin', BCRYPT_HASH, 'admin'))
    conn.commit()
    print('OK - admin user created.')
else:
    print('OK - users table and admin already exist')

cur.close()
conn.close()
