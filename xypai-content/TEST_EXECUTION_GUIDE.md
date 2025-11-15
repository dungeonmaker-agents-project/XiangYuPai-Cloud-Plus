# Test Execution Guide

**Module**: xypai-content
**Created**: 2025-11-15
**Status**: Ready for Execution

---

## Prerequisites

### 1. Database Setup

Create a separate test database to avoid affecting development data:

```sql
-- Connect to MySQL
mysql -u root -p

-- Create test database
CREATE DATABASE xypai_content_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Import schema
USE xypai_content_test;
SOURCE E:\Users\Administrator\Documents\GitHub\RuoYi-Cloud-Plus\xypai-content\sql\xypai_content.sql;

-- Verify tables created
SHOW TABLES;
```

**Expected tables**:
- `feed` - Feed/post table
- `comment` - Comment table
- `topic` - Topic/hashtag table
- `feed_topic` - Feed-topic association
- `interaction_like` - Like records
- `interaction_collect` - Collect/save records
- `interaction_share` - Share records
- `report` - Report records

### 2. Redis Setup

Ensure Redis is running for caching tests:

```bash
# Check if Redis is running
redis-cli ping
# Expected: PONG

# Or start Redis
redis-server

# Verify connection
redis-cli -n 15 ping
# Database 15 is used for tests (configured in application-test.yml)
```

### 3. Configuration

The test configuration file has been created at:
```
xypai-content/src/main/resources/application-test.yml
```

Key configurations:
- **Database**: `xypai_content_test` (separate from dev)
- **Redis Database**: `15` (separate from dev/prod)
- **Nacos**: Disabled for tests
- **Dubbo Registry**: Disabled for tests
- **Logging**: INFO level for Spring, DEBUG for xypai-content

**Environment Variables** (optional):
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_USERNAME=root
export DB_PASSWORD=your_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

---

## Running Tests

### Option 1: Run All Tests

Execute all test classes:

```bash
cd E:\Users\Administrator\Documents\GitHub\RuoYi-Cloud-Plus\xypai-content
mvn clean test
```

Expected output:
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.dromara.content.page.DiscoveryMainPageTest
[INFO] Running org.dromara.content.page.PublishFeedPageTest
[INFO] Running org.dromara.content.page.FeedDetailPageTest
[INFO] Running org.dromara.content.flow.CompleteUserFlowTest
[INFO] Tests run: 100+, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Option 2: Run Specific Test Class

Run tests for a specific page:

```bash
# Discovery Main Page tests (25+ scenarios)
mvn test -Dtest=DiscoveryMainPageTest

# Publish Feed Page tests (31+ scenarios)
mvn test -Dtest=PublishFeedPageTest

# Feed Detail Page tests (40+ scenarios)
mvn test -Dtest=FeedDetailPageTest

# Complete User Flow tests (7 flows)
mvn test -Dtest=CompleteUserFlowTest
```

### Option 3: Run Specific Test Method

Run a single test scenario:

```bash
# Test hot feed algorithm
mvn test -Dtest=DiscoveryMainPageTest#shouldReturnHotFeeds_whenSortedByHotScore

# Test publish validation
mvn test -Dtest=PublishFeedPageTest#shouldRejectEmptyContent_whenPublish

# Test report duplicate prevention
mvn test -Dtest=FeedDetailPageTest#shouldPreventDuplicate_whenReportWithin24Hours
```

### Option 4: Run Tests by Category (Nested Class)

Run all tests in a nested test class:

```bash
# Run all "Recommend Tab" tests
mvn test -Dtest=DiscoveryMainPageTest$RecommendTabTests

# Run all "Publish Validation" tests
mvn test -Dtest=PublishFeedPageTest$PublishFeedValidationTests

# Run all "Submit Report" tests
mvn test -Dtest=FeedDetailPageTest$SubmitReportTests
```

---

## Code Coverage

### Generate Coverage Report

Run tests with JaCoCo coverage:

```bash
cd E:\Users\Administrator\Documents\GitHub\RuoYi-Cloud-Plus\xypai-content
mvn clean test jacoco:report
```

### View Coverage Report

Open the HTML report in your browser:

```bash
# Windows
start target/site/jacoco/index.html

# Or manually open:
E:\Users\Administrator\Documents\GitHub\RuoYi-Cloud-Plus\xypai-content\target\site\jacoco\index.html
```

### Coverage Metrics

The report shows:
- **Line Coverage**: % of code lines executed during tests
- **Branch Coverage**: % of decision branches covered
- **Method Coverage**: % of methods invoked
- **Class Coverage**: % of classes instantiated

**Coverage Goals**:
- Line Coverage: **≥ 80%** (enforced: ≥ 60%)
- Branch Coverage: **≥ 70%**
- Controller Coverage: **100%** (all endpoints)
- Service Coverage: **≥ 85%**

---

## Test Structure

### Test Organization

```
xypai-content/src/test/java/org/dromara/content/
├── base/
│   ├── BaseIntegrationTest.java       # Base class with common setup
│   └── TestDataFactory.java           # Test data builders
├── page/
│   ├── DiscoveryMainPageTest.java     # Discovery page tests (25+ tests)
│   ├── PublishFeedPageTest.java       # Publish page tests (31+ tests)
│   └── FeedDetailPageTest.java        # Detail page tests (40+ tests)
└── flow/
    └── CompleteUserFlowTest.java      # Integration flows (7 flows)
```

### Test Classes

| Test Class | Purpose | Scenarios | LOC |
|------------|---------|-----------|-----|
| BaseIntegrationTest | Base setup & utilities | N/A | ~105 |
| TestDataFactory | Test data creation | N/A | ~400 |
| DiscoveryMainPageTest | Discovery page API tests | 25+ | ~1,200 |
| PublishFeedPageTest | Publish page API tests | 31+ | ~900 |
| FeedDetailPageTest | Detail page API tests | 40+ | ~1,500 |
| CompleteUserFlowTest | End-to-end flows | 7 | ~600 |
| **TOTAL** | | **100+** | **~4,700** |

---

## Understanding Test Results

### Successful Test Run

```
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 15.234 s
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 100, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Failed Test Example

```
[ERROR] Tests run: 25, Failures: 1, Errors: 0, Skipped: 0
[ERROR] Failures:
[ERROR]   DiscoveryMainPageTest.shouldReturnHotFeeds_whenSortedByHotScore:145
    Expected: hot score descending order
    Actual: [100, 95, 90, 85] but was [100, 90, 95, 85]
```

**How to debug**:
1. Check test name to understand what failed
2. Read the assertion message
3. Check line number in test file
4. Review implementation in service/controller
5. Fix the issue and re-run

---

## Common Issues & Solutions

### Issue 1: Database Connection Failed

**Error**:
```
java.sql.SQLNonTransientConnectionException: Could not connect to address=(host=localhost)(port=3306)
```

**Solutions**:
1. Verify MySQL is running: `mysql -u root -p`
2. Check database exists: `SHOW DATABASES LIKE 'xypai_content_test';`
3. Verify credentials in application-test.yml
4. Check firewall/network settings

### Issue 2: Redis Connection Failed

**Error**:
```
redis.clients.jedis.exceptions.JedisConnectionException: Could not get a resource from the pool
```

**Solutions**:
1. Verify Redis is running: `redis-cli ping`
2. Check Redis port: `netstat -an | grep 6379`
3. Verify Redis config in application-test.yml
4. Try: `redis-server` to start Redis

### Issue 3: Test Database Schema Missing

**Error**:
```
org.springframework.jdbc.BadSqlGrammarException: Table 'xypai_content_test.feed' doesn't exist
```

**Solutions**:
1. Import schema: `mysql -u root -p xypai_content_test < sql/xypai_content.sql`
2. Verify tables: `mysql -u root -p xypai_content_test -e "SHOW TABLES;"`
3. Check table structure: `DESCRIBE feed;`

### Issue 4: Authentication Mock Not Working

**Error**:
```
java.lang.NullPointerException: Mock token not initialized
```

**Solutions**:
1. Ensure test extends `BaseIntegrationTest`
2. Call `super.setUp()` in `@BeforeEach` if overriding
3. Use `getAuthHeader()` for auth header
4. Check `testUserToken` is not null

### Issue 5: Tests Pass Individually but Fail Together

**Cause**: Test isolation issue, tests affecting each other

**Solutions**:
1. Verify `@Transactional` on BaseIntegrationTest (auto-rollback)
2. Clear Redis between tests if needed
3. Use unique test data for each test
4. Check for static state or shared resources

### Issue 6: RPC Service Not Available

**Error**:
```
org.apache.dubbo.rpc.RpcException: No provider available for remote service
```

**Note**: This is expected for tests. RPC services (UserService, MediaService) should be mocked.

**Solutions**:
1. Tests currently use mock data for UserInfo
2. For real RPC testing, set up test Dubbo services
3. Or use `@MockBean` to mock RPC service calls

---

## Test Data Management

### Test Users

Default test user:
- **User ID**: `1001L`
- **Token**: `mock-test-token-1001`

Additional users can be created using TestDataFactory.

### Test Data Builders

Use TestDataFactory for consistent test data:

```java
// Create feeds
Feed publicFeed = testDataFactory.createPublicFeed(userId, "Content");
Feed privateFeed = testDataFactory.createPrivateFeed(userId, "Private content");
Feed hotFeed = testDataFactory.createHotFeed(userId);
List<Feed> feeds = testDataFactory.createMultipleFeeds(userId, 20);

// Create comments
Comment comment = testDataFactory.createComment(feedId, userId, "Nice post!");
Comment reply = testDataFactory.createReply(feedId, userId, parentId, replyToUserId, "Thanks!");

// Create topics
Topic hotTopic = testDataFactory.createHotTopic("旅行");
Topic normalTopic = testDataFactory.createNormalTopic("美食");

// Create DTOs
FeedPublishDTO dto = testDataFactory.createFeedPublishDTO("Content");
CommentDTO commentDTO = testDataFactory.createCommentDTO(feedId, "Comment");
ReportDTO reportDTO = testDataFactory.createReportDTO("feed", feedId, "spam");
```

### Data Cleanup

Tests use `@Transactional` for automatic rollback:
- Each test runs in a transaction
- All database changes are rolled back after test
- No manual cleanup needed

---

## CI/CD Integration

### GitHub Actions Example

Create `.github/workflows/test.yml`:

```yaml
name: Run Tests

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root
          MYSQL_DATABASE: xypai_content_test
        ports:
          - 3306:3306
        options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=3

      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: --health-cmd="redis-cli ping" --health-interval=10s --health-timeout=5s --health-retries=3

    steps:
      - uses: actions/checkout@v3

      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Import Database Schema
        run: |
          mysql -h 127.0.0.1 -u root -proot xypai_content_test < xypai-content/sql/xypai_content.sql

      - name: Run Tests with Coverage
        run: |
          cd xypai-content
          mvn clean test jacoco:report

      - name: Upload Coverage Report
        uses: codecov/codecov-action@v3
        with:
          files: ./xypai-content/target/site/jacoco/jacoco.xml
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any

    stages {
        stage('Setup') {
            steps {
                sh 'mysql -u root -p${MYSQL_PASSWORD} -e "CREATE DATABASE IF NOT EXISTS xypai_content_test"'
                sh 'mysql -u root -p${MYSQL_PASSWORD} xypai_content_test < xypai-content/sql/xypai_content.sql'
            }
        }

        stage('Test') {
            steps {
                dir('xypai-content') {
                    sh 'mvn clean test jacoco:report'
                }
            }
        }

        stage('Coverage') {
            steps {
                jacoco execPattern: 'xypai-content/target/jacoco.exec'
            }
        }
    }

    post {
        always {
            junit 'xypai-content/target/surefire-reports/*.xml'
        }
    }
}
```

---

## Performance Benchmarks

### Expected Test Execution Times

| Test Class | Scenarios | Expected Time |
|------------|-----------|---------------|
| DiscoveryMainPageTest | 25+ | 5-8 seconds |
| PublishFeedPageTest | 31+ | 4-6 seconds |
| FeedDetailPageTest | 40+ | 7-10 seconds |
| CompleteUserFlowTest | 7 | 3-5 seconds |
| **Total** | **100+** | **20-30 seconds** |

**Note**: Times may vary based on:
- Database performance
- Redis availability
- System resources
- Number of test data records

---

## Next Steps

1. ✅ **Setup complete** - Database, Redis, configuration ready
2. ✅ **Tests created** - 100+ test scenarios across 6 files
3. ✅ **Coverage configured** - JaCoCo plugin added to pom.xml
4. ⏳ **Run tests** - Execute `mvn clean test`
5. ⏳ **Review coverage** - Aim for ≥80% line coverage
6. ⏳ **Fix failures** - Address any failing tests
7. ⏳ **Add edge cases** - Expand test coverage as needed
8. ⏳ **Integrate CI/CD** - Add to build pipeline
9. ⏳ **Production ready** - Deploy with confidence

---

## Additional Resources

- **Test Summary**: See `TEST_IMPLEMENTATION_SUMMARY.md` for detailed test documentation
- **Test Organization**: See `TEST_ORGANIZATION.md` for test structure planning
- **Frontend API Docs**: See `e:\Users\Administrator\Documents\GitHub\XiangYuPai-Doc\Action-API\模块化架构\03-content模块\`
- **Backend Code**: See `E:\Users\Administrator\Documents\GitHub\RuoYi-Cloud-Plus\xypai-content\`

---

**Created**: 2025-11-15
**Author**: Claude Code AI
**Version**: 1.0
