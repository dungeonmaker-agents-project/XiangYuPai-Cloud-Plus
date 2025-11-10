# MySQL Script Execution Plan

## Overview
This document outlines the plan to execute all MySQL initialization scripts for the RuoYi-Cloud-Plus project, including base RuoYi modules and XYPAI custom modules.

## MySQL Connection Details
- **Host**: localhost (127.0.0.1)
- **Username**: root
- **Password**: 1123
- **Port**: 3306 (default)

---

## Execution Order

### Phase 1: RuoYi Base Modules
These are the core RuoYi Cloud framework databases that need to be initialized first.

#### 1.1 Nacos Configuration Database
- **Script**: `script/sql/ry-config.sql`
- **Database**: `ry-config`
- **Purpose**: Stores Nacos configuration center data
- **Order**: Must be first (Nacos needs this)

#### 1.2 RuoYi Cloud Core Database
- **Script**: `script/sql/ry-cloud.sql`
- **Database**: `ry-cloud` (or `ry-cloud`)
- **Purpose**: Core system tables (users, roles, permissions, etc.)
- **Order**: Second

#### 1.3 RuoYi Job Database
- **Script**: `script/sql/ry-job.sql`
- **Database**: `ry-job`
- **Purpose**: Distributed job scheduling system
- **Order**: Third

#### 1.4 Seata Database
- **Script**: `script/sql/ry-seata.sql`
- **Database**: `ry-seata`
- **Purpose**: Distributed transaction coordinator
- **Order**: Fourth

#### 1.5 RuoYi Workflow Database
- **Script**: `script/sql/ry-workflow.sql`
- **Database**: `ry-workflow`
- **Purpose**: Workflow engine
- **Order**: Fifth

---

### Phase 2: XYPAI Custom Modules
These are the custom business modules for the XYPAI platform.

#### 2.1 XYPAI Auth Database
- **Database**: `xypai_auth` (needs to be created first)
- **Scripts**:
  1. Create database (manual or script)
  2. `xypai-security/sql/01_create_auth_user_table.sql`
- **Purpose**: User authentication and authorization
- **Order**: First (other modules may depend on auth)

#### 2.2 XYPAI User Module
- **Database**: `xypai_user`
- **Scripts** (in order):
  1. `xypai-user/sql/01_create_database.sql`
  2. `xypai-user/sql/02_create_tables.sql`
  3. `xypai-user/sql/03_create_indexes.sql`
  4. `xypai-user/sql/04_init_test_data.sql` (optional)
  5. `xypai-user/sql/99_verify.sql` (optional verification)
- **Purpose**: User profile and user-related business data
- **Order**: Second

#### 2.3 XYPAI Trade Module
- **Database**: `xypai_trade`
- **Scripts** (in order):
  1. `xypai-trade/sql/00_create_database.sql`
  2. `xypai-trade/sql/v7.1_service_order_upgrade.sql`
  3. `xypai-trade/sql/v7.1_service_review_create.sql`
  4. `xypai-trade/sql/v7.1_user_wallet_create.sql`
  5. `xypai-trade/sql/v7.1_transaction_create.sql`
  6. `xypai-trade/sql/v7.1_service_stats_create.sql`
  7. `xypai-trade/sql/00_init_trade_database.sql` (alternative: runs all above)
- **Purpose**: Trading, orders, wallets, transactions
- **Order**: Third

#### 2.4 XYPAI Chat Module
- **Database**: `xypai_chat`
- **Scripts** (in order):
  1. `xypai-chat/eve_workspace/sql/01_create_database.sql`
  2. `xypai-chat/eve_workspace/sql/02_create_tables_v7.0.sql`
  3. `xypai-chat/eve_workspace/sql/03_upgrade_to_v7.1.sql`
  4. `xypai-chat/eve_workspace/sql/04_create_indexes.sql`
  5. `xypai-chat/eve_workspace/sql/05_init_test_data.sql` (optional)
  6. `xypai-chat/eve_workspace/sql/99_reset_all.sql` (optional: reset script)
- **Purpose**: Chat and messaging functionality
- **Order**: Fourth

#### 2.5 XYPAI Content Module
- **Database**: `xypai_content` (needs verification)
- **Scripts**: TBD - Need to check if SQL scripts exist
- **Purpose**: Content management (posts, comments, media)
- **Order**: Fifth

---

## Execution Strategy

### Option 1: Manual Step-by-Step (Recommended for first run)
Execute each script individually to monitor progress and catch errors early.

### Option 2: Automated Batch Script
Create a PowerShell batch script that executes all scripts in sequence.

### Option 3: MySQL Source Command
Use MySQL's `SOURCE` command to execute scripts from within MySQL client.

---

## Prerequisites

1. ✅ MySQL server running and accessible
2. ✅ MySQL client tools installed (mysql command line)
3. ✅ User `root` with password `1123` has privileges to create databases
4. ✅ Verify connection: `mysql -u root -p1123 -e "SELECT VERSION();"`

---

## Verification Steps

After each phase, verify:
1. Database created successfully
2. Tables created correctly
3. No errors in execution logs
4. Test connection from application

---

## Notes

- **Password**: The scripts in comments mention `-proot` but actual password is `1123`
- **xypai_auth**: May need manual database creation before running table scripts
- **xypai_content**: Need to verify if SQL scripts exist
- **Test Data**: Some modules have optional test data scripts (04_init_test_data.sql)
- **Indexes**: Some modules have separate index creation scripts
- **Updates**: Update scripts are in `script/sql/update/` directory (not needed for fresh install)

---

## Next Steps

1. ✅ Test MySQL connection
2. ⏳ Execute Phase 1 (RuoYi Base)
3. ⏳ Execute Phase 2 (XYPAI Modules)
4. ⏳ Verify all databases and tables
5. ⏳ Update application configuration if needed

