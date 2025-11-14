# Manual MySQL Script Execution Guide

## Quick Start

### Step 1: Test MySQL Connection

```powershell
# Option 1: If MySQL is in PATH
mysql -u root -p1123 -e "SELECT VERSION();"

# Option 2: If using Docker
docker exec -i <mysql-container-name> mysql -u root -p1123 -e "SELECT VERSION();"

# Option 3: Full path (adjust as needed)
"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p1123 -e "SELECT VERSION();"
```

---

## Phase 1: RuoYi Base Modules

### 1.1 Nacos Configuration Database
```powershell
cd script\sql
mysql -u root -p1123 < ry-config.sql
```

### 1.2 RuoYi Cloud Core
```powershell
mysql -u root -p1123 < ry-cloud.sql
```

### 1.3 RuoYi Job
```powershell
mysql -u root -p1123 < ry-job.sql
```

### 1.4 Seata
```powershell
mysql -u root -p1123 < ry-seata.sql
```

### 1.5 RuoYi Workflow
```powershell
mysql -u root -p1123 < ry-workflow.sql
```

---

## Phase 2: XYPAI Custom Modules

### 2.1 XYPAI Auth Database

**First, create the database:**
```sql
CREATE DATABASE IF NOT EXISTS `xypai_auth` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;
```

**Then execute the table script:**
```powershell
cd ..\..\xypai-security\sql
mysql -u root -p1123 xypai_auth < 01_create_auth_user_table.sql
```

### 2.2 XYPAI User Module

**Execute in order:**
```powershell
cd ..\..\xypai-user\sql

# Step 1: Create database
mysql -u root -p1123 < 01_create_database.sql

# Step 2: Create tables
mysql -u root -p1123 xypai_user < 02_create_tables.sql

# Step 3: Create indexes
mysql -u root -p1123 xypai_user < 03_create_indexes.sql

# Step 4: (Optional) Insert test data
mysql -u root -p1123 xypai_user < 04_init_test_data.sql

# Step 5: (Optional) Verify
mysql -u root -p1123 xypai_user < 99_verify.sql
```

### 2.3 XYPAI Trade Module

```powershell
cd ..\..\xypai-trade\sql

# Step 1: Create database
mysql -u root -p1123 < 00_create_database.sql

# Step 2: Create/upgrade tables (in order)
mysql -u root -p1123 xypai_trade < v7.1_service_order_upgrade.sql
mysql -u root -p1123 xypai_trade < v7.1_service_review_create.sql
mysql -u root -p1123 xypai_trade < v7.1_user_wallet_create.sql
mysql -u root -p1123 xypai_trade < v7.1_transaction_create.sql
mysql -u root -p1123 xypai_trade < v7.1_service_stats_create.sql
```

### 2.4 XYPAI Chat Module

```powershell
cd ..\..\xypai-chat\eve_workspace\sql

# Step 1: Create database
mysql -u root -p1123 < 01_create_database.sql

# Step 2: Create tables (v7.0)
mysql -u root -p1123 xypai_chat < 02_create_tables_v7.0.sql

# Step 3: Upgrade to v7.1
mysql -u root -p1123 xypai_chat < 03_upgrade_to_v7.1.sql

# Step 4: Create indexes
mysql -u root -p1123 xypai_chat < 04_create_indexes.sql

# Step 5: (Optional) Insert test data
mysql -u root -p1123 xypai_chat < 05_init_test_data.sql
```

### 2.5 XYPAI Content Module

**Note:** SQL scripts for xypai-content module need to be verified. If they exist, execute them in a similar pattern.

---

## Verification Commands

### Check all databases created:
```sql
SHOW DATABASES LIKE '%ry%';
SHOW DATABASES LIKE '%xypai%';
```

### Check tables in a database:
```sql
USE xypai_user;
SHOW TABLES;
```

### Count tables in each database:
```sql
SELECT 
    TABLE_SCHEMA AS 'Database',
    COUNT(*) AS 'Table Count'
FROM information_schema.TABLES
WHERE TABLE_SCHEMA IN ('ry-config', 'ry-cloud', 'ry-job', 'ry-seata', 'ry-workflow', 
                       'xypai_auth', 'xypai_user', 'xypai_trade', 'xypai_chat')
GROUP BY TABLE_SCHEMA;
```

---

## Troubleshooting

### MySQL not found in PATH
- Add MySQL bin directory to PATH
- Or use full path: `"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"`

### Access denied
- Verify password: `1123`
- Check user privileges: `SHOW GRANTS FOR 'root'@'localhost';`

### Script execution errors
- Check script syntax
- Verify database exists before running table scripts
- Check for foreign key dependencies

### Using Docker MySQL
```powershell
# Execute script in Docker container
docker exec -i <container-name> mysql -u root -p1123 < script.sql

# Or copy script to container first
docker cp script.sql <container-name>:/tmp/
docker exec -i <container-name> mysql -u root -p1123 < /tmp/script.sql
```

---

## Expected Databases After Execution

| Database | Purpose | Tables (approx) |
|----------|---------|-----------------|
| `ry-config` | Nacos config | ~10 |
| `ry-cloud` | Core system | ~50 |
| `ry-job` | Job scheduling | ~10 |
| `ry-seata` | Distributed TX | ~10 |
| `ry-workflow` | Workflow engine | ~20 |
| `xypai_auth` | Authentication | 1 (user) |
| `xypai_user` | User profiles | ~8 |
| `xypai_trade` | Trading/Orders | ~5 |
| `xypai_chat` | Chat/Messages | ~10 |

---

## Next Steps

After successful execution:
1. ✅ Verify all databases created
2. ✅ Update application.yml files with correct database names
3. ✅ Test application connections
4. ✅ Run application startup tests

