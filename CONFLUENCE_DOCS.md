# 🔐 Checkmarx One CLI Automation Framework — Confluence Documentation

---

## 📖 Table of Contents

1. [Project Overview](#1-project-overview)
2. [Framework Architecture](#2-framework-architecture)
3. [Technology Stack](#3-technology-stack)
4. [Prerequisites](#4-prerequisites)
5. [Environment Variables](#5-environment-variables)
6. [Project Structure](#6-project-structure)
7. [Core Framework Components](#7-core-framework-components)
8. [Test Modules](#8-test-modules)
9. [Test Data (Data-Driven Testing)](#9-test-data-data-driven-testing)
10. [Reporting](#10-reporting)
11. [Running Tests Locally](#11-running-tests-locally)
12. [Running Tests via GitHub Actions (CI/CD)](#12-running-tests-via-github-actions-cicd)
13. [Parallel Execution](#13-parallel-execution)
14. [GitHub Actions Artifacts (Reports)](#14-github-actions-artifacts-reports)
15. [Troubleshooting](#15-troubleshooting)

---

## 1. Project Overview

**Project Name:** `cxone-cli-automation`
**Group ID:** `com.myorg`
**Version:** `1.0-SNAPSHOT`

This is a **Java-based CLI Test Automation Framework** built to validate the **Checkmarx One (CxOne) CLI tool (`cx`)**. The framework tests various CLI commands such as authentication, scan creation, project management, and configuration, ensuring the CLI tool behaves as expected across multiple scenarios.

The framework is designed to run both **locally on developer machines** and **remotely via GitHub Actions** CI/CD pipelines.

---

## 2. Framework Architecture

```
┌─────────────────────────────────────────────────┐
│                  Test Runner                    │
│             TestNG (testng.xml)                 │
└────────────────────┬────────────────────────────┘
                     │
        ┌────────────▼────────────┐
        │      Base Class         │
        │  (Setup / Teardown)     │
        │  - @BeforeSuite         │
        │  - @AfterSuite          │
        │  - ExtentReports Init   │
        └────────────┬────────────┘
                     │
     ┌───────────────┼───────────────┐
     ▼               ▼               ▼
 AuthTest        ScanTest       ProjectTest
 ConfigureTest   HelpTest   scanTestDataDriven
     │               │               │
     └───────────────┼───────────────┘
                     │
        ┌────────────▼────────────┐
        │      CLIHelper          │
        │ (Executes cx commands)  │
        └────────────┬────────────┘
                     │
          ┌──────────▼──────────┐
          │   CX CLI Tool (cx)  │
          │  (External Binary)  │
          └─────────────────────┘
                     │
     ┌───────────────▼───────────────┐
     │         Reporting Layer        │
     │  ExtentReports (HTML Report)   │
     │  Apache POI (Excel Report)     │
     └────────────────────────────────┘
```

---

## 3. Technology Stack

| Technology       | Version    | Purpose                                   |
|------------------|------------|-------------------------------------------|
| Java             | 17         | Primary programming language              |
| Maven            | 3.x        | Build and dependency management           |
| TestNG           | 7.10.2     | Test execution framework                  |
| ExtentReports    | 5.1.1      | HTML test reporting                       |
| Apache POI       | 5.2.5      | Excel report generation + data reading    |
| Jackson Databind | 2.17.2     | JSON parsing                              |
| Log4j            | 2.20.0     | Logging                                   |
| GitHub Actions   | -          | CI/CD pipeline                            |
| Checkmarx One CX CLI | Latest | System Under Test (SUT)               |

---

## 4. Prerequisites

Before cloning and running this project, ensure the following tools and software are installed and configured on your machine:

### 4.1 Java Development Kit (JDK)

- **Required Version:** Java 17 or higher
- Download: [https://adoptium.net/](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
- Verify Installation:
  ```bash
  java -version
  # Expected: java version "17.x.x" or higher
  ```
- Set `JAVA_HOME` environment variable pointing to your JDK installation directory.

---

### 4.2 Apache Maven

- **Required Version:** Maven 3.6+
- Download: [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
- Add Maven `bin` directory to your system `PATH`
- Verify Installation:
  ```bash
  mvn -version
  # Expected: Apache Maven 3.x.x
  ```

---

### 4.3 Checkmarx One CLI (`cx`)

The **CX CLI** is the actual system being tested. It must be installed on the machine running the tests.

- Download the latest CX CLI from the [Checkmarx One documentation portal](https://checkmarx.com/resource/documents/en/34965-68621-checkmarx-one-cli-quick-start-guide.html)
- Place the `cx.exe` binary in a known path (e.g., `C:\cx-cli\cx.exe`)
- Either:
  - Add `cx.exe` to your system `PATH` (so `cx` works from any terminal), **OR**
  - Set the `CX_CLI_PATH` environment variable pointing to the folder containing `cx.exe`

  ```
  CX_CLI_PATH=C:\cx-cli
  ```
- Verify Installation:
  ```bash
  cx --help
  ```

> ⚠️ **Note:** The `CLIHelper.java` class dynamically resolves the CLI path using `CX_CLI_PATH` environment variable, or falls back to `cx` on the system PATH.

---

### 4.4 Git

- Required for cloning the repository
- Download: [https://git-scm.com/downloads](https://git-scm.com/downloads)
- Verify Installation:
  ```bash
  git --version
  ```

---

### 4.5 IDE (Recommended)

- **IntelliJ IDEA** (Community or Ultimate) is recommended
- Download: [https://www.jetbrains.com/idea/download/](https://www.jetbrains.com/idea/download/)
- Other IDEs like Eclipse or VS Code with Java extensions also work

---

### 4.6 ScaResolver (Optional — for SCA scans)

- Required only when running **SCA (Software Composition Analysis)** scan test cases
- Download ScaResolver from the official Checkmarx documentation
- Set the `SCA_RESOLVER_PATH` environment variable:
  ```
  SCA_RESOLVER_PATH=C:\sca-resolver\ScaResolver.exe
  ```

---

## 5. Environment Variables

The framework requires the following environment variables to be set before running any tests. These are validated at suite startup via `EnvValidator.java`.

### 5.1 Required Variables

| Variable Name      | Description                                          | Example Value                                |
|--------------------|------------------------------------------------------|----------------------------------------------|
| `CX_BASE_URI`      | Base URL for Checkmarx One tenant                    | `https://ast.checkmarx.net`                  |
| `CX_BASE_AUTH_URI` | Authentication URI for Checkmarx One                 | `https://iam.checkmarx.net`                  |
| `CX_TENANT`        | Your Checkmarx One tenant name                       | `my-company-tenant`                          |
| `CX_APIKEY`        | API Key for authenticating with Checkmarx One        | `<your-api-key>`                             |

> ⚠️ If any of the above variables are missing, the suite will **fail immediately** with a `RuntimeException` before any test executes.

### 5.2 Optional Variables

| Variable Name        | Description                                         | Default Behaviour                           |
|----------------------|-----------------------------------------------------|---------------------------------------------|
| `CX_CLI_PATH`        | Full folder path to the `cx.exe` binary             | Falls back to `cx` on the system PATH       |
| `CX_CLIENT_ID`       | OAuth Client ID (alternative to API Key)            | Used in specific auth test cases            |
| `CX_CLIENT_SECRET`   | OAuth Client Secret (alternative to API Key)        | Used in specific auth test cases            |
| `SCA_RESOLVER_PATH`  | Path to ScaResolver executable                      | Required for SCA-specific test scenarios    |

### 5.3 How to Set Environment Variables

**Windows (Command Prompt):**
```cmd
set CX_BASE_URI=https://ast.checkmarx.net
set CX_BASE_AUTH_URI=https://iam.checkmarx.net
set CX_TENANT=my-tenant
set CX_APIKEY=your-api-key-here
set CX_CLI_PATH=C:\cx-cli
```

**Windows (PowerShell):**
```powershell
$env:CX_BASE_URI = "https://ast.checkmarx.net"
$env:CX_BASE_AUTH_URI = "https://iam.checkmarx.net"
$env:CX_TENANT = "my-tenant"
$env:CX_APIKEY = "your-api-key-here"
$env:CX_CLI_PATH = "C:\cx-cli"
```

**macOS/Linux:**
```bash
export CX_BASE_URI="https://ast.checkmarx.net"
export CX_BASE_AUTH_URI="https://iam.checkmarx.net"
export CX_TENANT="my-tenant"
export CX_APIKEY="your-api-key-here"
export CX_CLI_PATH="/usr/local/bin"
```

**Permanent (Windows) — System Properties:**
1. Right-click "This PC" → Properties → Advanced System Settings
2. Click "Environment Variables"
3. Under "User variables" or "System variables" → click **New**
4. Enter the variable name and value

---

## 6. Project Structure

```
Automation/
├── pom.xml                          # Maven build configuration & dependencies
├── testng.xml                       # TestNG suite configuration
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/myorg/cxone/
│   │   │       └── helpers/
│   │   │           ├── EnvValidator.java        # Validates required env variables
│   │   │           ├── ExcelDataProvider.java   # Reads test data from Excel
│   │   │           ├── Logger.java              # Wraps ExtentReports logging
│   │   │           └── TestConstants.java       # Stores shared constants & env lookups
│   │   └── resources/
│   │       ├── cx_help.txt                      # Snapshot of expected cx --help output
│   │       ├── ScanTestData.xlsx                # Excel data for data-driven scan tests
│   │       ├── JavaVulnerableLabE-master.zip    # Sample vulnerable Java project (zip)
│   │       ├── Phoenix-RealtimeGoat.zip         # Sample Phoenix project (zip)
│   │       └── JavaVulnerableLabE-master/       # Unzipped vulnerable Java project (folder)
│   │           └── Phoenix-RealtimeGoat/        # Unzipped Phoenix project (folder)
│   └── test/
│       └── java/
│           ├── PageObjects/
│           │   └── ScanInfo.java                # Model class for CLI scan output
│           ├── utils/
│           │   ├── Base.java                    # Base test class (lifecycle hooks)
│           │   ├── CLIHelper.java               # Executes cx CLI commands
│           │   ├── ExcelReportListener.java     # TestNG listener → Excel report
│           │   ├── ProjectUtils.java            # CLI project management helpers
│           │   ├── ReportManager.java           # ExtentReports HTML report setup
│           │   ├── ScanUtils.java               # CLI scan parsing & validation helpers
│           │   └── Utils.java                   # General utility methods
│           └── com/myorg/cxone/tests/
│               ├── AuthTest.java                # Authentication test cases
│               ├── ConfigureTest.java           # Configuration command test cases
│               ├── HelpTest.java                # CLI Help output validation tests
│               ├── ProjectTest.java             # Project management test cases
│               ├── ScanTest.java                # Scan creation & management tests
│               └── scanTestDataDriven.java      # Data-driven scan tests (Excel)
├── test-output/                     # Generated test reports (auto-created)
│   ├── ExtentReport_<timestamp>.html
│   └── TestReport.xlsx
└── .github/
    └── workflows/
        └── main.yml                 # GitHub Actions CI/CD pipeline
```

---

## 7. Core Framework Components

### 7.1 `Base.java` — Base Test Class

All test classes extend `Base`. It manages the **TestNG lifecycle** and **ExtentReports** initialization.

| Hook | Purpose |
|------|---------|
| `@BeforeSuite` | Validates all required environment variables, initializes ExtentReports |
| `@BeforeClass` | Creates a new test node in ExtentReport for the test class |
| `@BeforeMethod` | Creates a child node for each test method, logs start |
| `@AfterSuite` | Flushes and finalizes the HTML report |

---

### 7.2 `CLIHelper.java` — CLI Command Executor

Responsible for executing the `cx` CLI commands via `ProcessBuilder`. Supports three execution modes:

| Method | Description |
|--------|-------------|
| `runCommand(command)` | Runs a command and waits for full output |
| `runCommandUntilPattern(command, pattern, test)` | Streams output and stops when a specific pattern is matched (used for long-running scans) |
| `runCommandWithTimeout(command, timeoutMs)` | Runs a command with a time limit (used for interactive commands like `cx configure`) |

> **CLI Path Resolution:** The `CX_CLI_PATH` env variable is used first. If not set, falls back to `cx` on system PATH.

---

### 7.3 `ReportManager.java` — HTML Report Manager

- Creates a **timestamped** Extent HTML report at `test-output/ExtentReport_<timestamp>.html`
- Theme: **Dark**
- Report Title: `"Checkmarx CLI Test Report"`
- Automatically **cleans old** `ExtentReport_*.html` files before each run
- Attaches system info: OS, Java version, Tester name, Environment

---

### 7.4 `ExcelReportListener.java` — Excel Report Generator

- Implements TestNG's `IReporter` interface
- Registered in `testng.xml` as a **listener** — runs automatically after the suite
- Generates `test-output/TestReport.xlsx` with columns:
  - **Class Name** — Test class the test belongs to
  - **Test Case Name** — Name of the test method
  - **Status** — PASS / FAIL / SKIPPED
  - **CLI Command** — The actual `cx` command executed

---

### 7.5 `EnvValidator.java` — Environment Validator

- Runs at suite startup
- Checks: `CX_BASE_URI`, `CX_BASE_AUTH_URI`, `CX_TENANT`, `CX_APIKEY`
- Throws `RuntimeException` immediately if any variable is missing → **Fail Fast**

---

### 7.6 `ExcelDataProvider.java` — Data-Driven Test Data Provider

- Reads test data from `ScanTestData.xlsx` → sheet `ScanSheet`
- Supports **dynamic column headers**
- Expands `AdditionalFlags` column to generate **multiple test rows per scenario** (for testing different auth flag combinations)
- Used by `scanTestDataDriven.java` via TestNG's `@DataProvider`

---

### 7.7 `ScanUtils.java` — Scan Output Parser

- Parses CLI scan output and extracts key fields: `Scan ID`, `Project ID`, `Project Name`, `Status`, `Branch`, `Type`, `Engines`
- Returns a `ScanInfo` object (Page Object pattern)
- Contains validation helpers for asserting scan info

---

### 7.8 `Logger.java` — Logging Wrapper

Wraps ExtentReports logging with special handling for **multi-line CLI output** (wraps in `<pre>` tags for HTML formatting).

| Method | Behavior |
|--------|----------|
| `Logger.info(msg, test)` | Logs info to ExtentReport + console |
| `Logger.pass(msg, test)` | Marks test as passed in ExtentReport |
| `Logger.fail(msg, test)` | Marks test as failed in ExtentReport |

---

### 7.9 `TestConstants.java` — Constants & Configuration

Central store for:
- CLI expected output strings (e.g., `SUCCESS_AUTH_VALIDATE`)
- Project/scan source paths (zip, folder, git URL)
- Environment variable lookups (`CX_APIKEY`, `CX_CLIENT_ID`, etc.)

---

## 8. Test Modules

### 8.1 `AuthTest.java` — Authentication Tests

Tests the `cx auth validate` command and its edge cases.

| Test Method | Description |
|-------------|-------------|
| `testAuthValidate` | Validates successful auth using env variables |
| `testAuthValidateWhenApiKeyIsEmpty` | Auth with empty `--apikey` flag (should still pass via env vars) |
| `testAuthValidateWhenClientSecretIsEmpty` | Auth with empty `--client-id`/`--client-secret` (should still pass via env vars) |
| + more | Invalid API keys, missing URIs, etc. |

---

### 8.2 `ScanTest.java` — Scan Tests

Tests the `cx scan create`, `cx scan show`, `cx scan list` commands and various scan configurations.

| Test Category | Description |
|---------------|-------------|
| Source as folder | SAST scan with a local folder as source |
| Source as ZIP | SAST scan with a ZIP file as source |
| Source as Git URL | SAST scan with a remote Git repository |
| Missing branch | Verify error when branch is not provided |
| Invalid source | Verify error for non-existent source path |
| Scan without scan ID | Verify error for `scan show` without ID |
| Multiple scan types | SAST + SCA combined scans |
| SCA with resolver | SCA scan with ScaResolver path |

> Tests that create scans **clean up** after themselves by deleting the scan and project at the end.

---

### 8.3 `scanTestDataDriven.java` — Data-Driven Scan Tests

Reads test scenarios from `ScanTestData.xlsx` and executes them dynamically.

**Excel Sheet:** `ScanSheet`

**Columns:**
| Column | Description |
|--------|-------------|
| `ScenarioDescription` | Human-readable test name |
| `ScanTypes` | e.g., `sast`, `sca`, `sast,sca` |
| `AdditionalFlags` | Extra CLI flags (e.g., `--apikey %s`) |
| `ExpectedStatus` | Expected scan status (e.g., `Running`) |
| `ExpectedBranch` | Expected branch name |
| `ExpectedType` | Expected scan type |
| `ExpectedEngine` | Expected engine name |
| `ExpectedError` | If set, test expects this error message |

---

### 8.4 `ProjectTest.java` — Project Management Tests

Tests the `cx project create`, `cx project show`, `cx project list`, `cx project delete` commands.

| Test Method | Description |
|-------------|-------------|
| `createProjectTest` | Create project and verify it exists |
| `testCreateProjectWithEmptyName` | Verify error for empty project name |
| `testCreateProjectWithValidApplication` | Create project associated with an application |
| `deleteProject` | Delete a project and verify deletion |
| + format tests | Verify JSON/list/table output formats |

---

### 8.5 `HelpTest.java` — CLI Help Output Tests

Validates the `cx --help` and `cx help` output against a known-good snapshot file (`cx_help.txt`).

| Test Method | Description |
|-------------|-------------|
| `cliHelpTest` | Compare `cx --help` output to snapshot |
| `cliHelpTestWithoutHyphen` | Compare `cx help` output to snapshot |
| `testProjectHelp` | Validate `cx project --help` commands section |
| + sub-command help tests | Auth, scan, configure help validations |

> **Snapshot File:** `src/main/resources/cx_help.txt` — update this file when CLI version changes.

---

### 8.6 `ConfigureTest.java` — Configuration Tests

Tests the `cx configure` and `cx configure set` commands.

| Test Method | Description |
|-------------|-------------|
| `testConfigureDisplaysSetupGuide` | Verify setup guide URL appears on configure |
| `testConfigureSetInvalidProperty` | Verify error for unknown property name |
| `testConfigureSetEmptyPropertyNameAndValue` | Verify behavior with empty prop name/value |
| + valid property tests | Set valid properties and verify success |

---

## 9. Test Data (Data-Driven Testing)

### Excel File: `src/main/resources/ScanTestData.xlsx`

- **Sheet Name:** `ScanSheet`
- Used by `scanTestDataDriven.java`
- Each row represents one test scenario
- The `AdditionalFlags` column supports **credential placeholders**:
  - `%s` with `--apikey` → replaced with `CX_APIKEY` env variable
  - `%s` with `--client-id --client-secret` → replaced with `CX_CLIENT_ID` and `CX_CLIENT_SECRET`

### Sample Projects for Scanning:
| Resource | Path | Usage |
|----------|------|-------|
| Phoenix RealtimeGoat (ZIP) | `src/main/resources/Phoenix-RealtimeGoat.zip` | ZIP source scan testing |
| JavaVulnerableLabE (ZIP) | `src/main/resources/JavaVulnerableLabE-master.zip` | ZIP source scan testing |
| Phoenix RealtimeGoat (Folder) | `src/main/resources/Phoenix-RealtimeGoat/` | Folder source scan testing |
| Remote Git Repo | `https://github.com/vbarhate/JavaVulnerableLabE.git` | Git source scan testing |

---

## 10. Reporting

### 10.1 HTML Report (ExtentReports)

- **Location:** `test-output/ExtentReport_<yyyy-MM-dd_HH-mm-ss>.html`
- **Theme:** Dark
- **Contents:**
  - Test suite summary (Pass/Fail/Skip counts)
  - Individual test results organized by class → method hierarchy
  - CLI commands executed
  - Full CLI output (formatted in `<pre>` blocks)
  - System info panel (OS, Java version, Tester name, Environment)
- **Auto-cleanup:** Old HTML reports are deleted before each new run

### 10.2 Excel Report (Apache POI)

- **Location:** `test-output/TestReport.xlsx`
- **Contents:**
  - Class Name
  - Test Case Name
  - Status (PASS / FAIL / SKIPPED)
  - CLI Command used
- **Generated by:** `ExcelReportListener.java` (registered as a TestNG listener in `testng.xml`)

---

## 11. Running Tests Locally

### Step 1: Clone the Repository
```bash
git clone <your-repository-url>
cd Automation
```

### Step 2: Set Environment Variables
Set all required environment variables as described in [Section 5](#5-environment-variables).

### Step 3: Install CX CLI
Download and install the CX CLI. Set `CX_CLI_PATH` if not adding to PATH.

### Step 4: Build the Project
```bash
mvn clean compile
```

### Step 5: Run All Tests
```bash
mvn clean test
```

### Step 6: Run a Specific Test Class
```bash
mvn clean test -Dtest=AuthTest
mvn clean test -Dtest=ScanTest
mvn clean test -Dtest=ProjectTest
```

### Step 7: View Reports
- Open `test-output/ExtentReport_<timestamp>.html` in a browser
- Open `test-output/TestReport.xlsx` in Excel

---

## 12. Running Tests via GitHub Actions (CI/CD)

The project uses **GitHub Actions** to run tests automatically on push/pull request events or on a scheduled basis.

### 12.1 GitHub Secrets Configuration

Navigate to your repository → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

Add the following secrets:

| Secret Name          | Description                                |
|----------------------|--------------------------------------------|
| `CX_BASE_URI`        | Checkmarx One base URL                     |
| `CX_BASE_AUTH_URI`   | Checkmarx One auth URL                     |
| `CX_TENANT`          | Checkmarx One tenant name                  |
| `CX_APIKEY`          | Checkmarx One API Key                      |
| `CX_CLIENT_ID`       | OAuth Client ID (if used)                  |
| `CX_CLIENT_SECRET`   | OAuth Client Secret (if used)              |
| `SCA_RESOLVER_PATH`  | Path to SCA Resolver (if SCA tests run)    |

### 12.2 GitHub Actions Workflow (`main.yml`)

The workflow is located at `.github/workflows/main.yml`. Here is the recommended complete configuration:

```yaml
name: Checkmarx One CLI Automation Tests

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]
  workflow_dispatch:   # Allows manual trigger from GitHub UI

jobs:
  run-automation-tests:
    runs-on: windows-latest   # Uses Windows runner (required for cx.exe)

    steps:
      # Step 1: Checkout source code
      - name: Checkout Code
        uses: actions/checkout@v4

      # Step 2: Set up Java 17
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      # Step 3: Download and install CX CLI
      - name: Download CX CLI
        shell: powershell
        run: |
          $cxUrl = "https://download.checkmarx.com/CxOne/CLI/latest/ast-cli_windows_x64.zip"
          Invoke-WebRequest -Uri $cxUrl -OutFile cx-cli.zip
          Expand-Archive -Path cx-cli.zip -DestinationPath C:\cx-cli -Force
          echo "CX_CLI_PATH=C:\cx-cli" >> $env:GITHUB_ENV

      # Step 4: Run tests using Maven
      - name: Run Automation Tests
        shell: powershell
        env:
          CX_BASE_URI: ${{ secrets.CX_BASE_URI }}
          CX_BASE_AUTH_URI: ${{ secrets.CX_BASE_AUTH_URI }}
          CX_TENANT: ${{ secrets.CX_TENANT }}
          CX_APIKEY: ${{ secrets.CX_APIKEY }}
          CX_CLIENT_ID: ${{ secrets.CX_CLIENT_ID }}
          CX_CLIENT_SECRET: ${{ secrets.CX_CLIENT_SECRET }}
          SCA_RESOLVER_PATH: ${{ secrets.SCA_RESOLVER_PATH }}
          CX_CLI_PATH: C:\cx-cli
        run: mvn clean test --no-transfer-progress

      # Step 5: Upload ExtentReports HTML Report
      - name: Upload HTML Test Report
        uses: actions/upload-artifact@v4
        if: always()   # Upload even if tests fail
        with:
          name: ExtentReport-HTML
          path: test-output/ExtentReport_*.html
          retention-days: 30

      # Step 6: Upload Excel Report
      - name: Upload Excel Test Report
        uses: actions/upload-artifact@v4
        if: always()   # Upload even if tests fail
        with:
          name: TestReport-Excel
          path: test-output/TestReport.xlsx
          retention-days: 30
```

### 12.3 Downloading Reports from GitHub Actions

1. Navigate to your GitHub repository
2. Click **Actions** tab
3. Click on the desired workflow run
4. Scroll down to the **Artifacts** section
5. Download `ExtentReport-HTML` (HTML report) or `TestReport-Excel` (Excel report)

---

## 13. Parallel Execution

To reduce test execution time, tests can be run in **parallel** by updating `testng.xml`.

### 13.1 Enable Parallel Execution in `testng.xml`

Update `testng.xml` to enable parallel execution at the **class** or **methods** level:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="CheckmarxOneSuite"
       verbose="1"
       parallel="classes"
       thread-count="3"
       preserve-order="true">

    <listeners>
        <listener class-name="utils.ExcelReportListener"/>
    </listeners>

    <test name="CheckmarxOneTests"
          preserve-order="true"
          parallel="classes"
          thread-count="3">
        <classes>
            <class name="com.myorg.cxone.tests.HelpTest"/>
            <class name="com.myorg.cxone.tests.scanTestDataDriven"/>
            <class name="com.myorg.cxone.tests.ScanTest"/>
            <class name="com.myorg.cxone.tests.ProjectTest"/>
            <class name="com.myorg.cxone.tests.AuthTest"/>
            <class name="com.myorg.cxone.tests.ConfigureTest"/>
        </classes>
    </test>

</suite>
```

**Parallel modes explained:**

| Mode | Description |
|------|-------------|
| `parallel="classes"` | Each test class runs in its own thread (recommended) |
| `parallel="methods"` | Each test method runs in its own thread (more aggressive) |
| `parallel="tests"` | Each `<test>` tag runs in parallel |
| `thread-count="3"` | Number of parallel threads (tune based on machine resources) |

> ⚠️ **Thread Safety Note:** The framework already uses `ThreadLocal<ExtentTest>` in `Base.java` for `classLevelTest` and `testLevelTest`, so **ExtentReports is thread-safe** and parallel execution is fully supported.

### 13.2 Parallel Execution in GitHub Actions (Matrix Strategy)

For even faster CI execution, split test classes across multiple jobs using a matrix:

```yaml
jobs:
  test:
    runs-on: windows-latest
    strategy:
      matrix:
        test-class: [AuthTest, ScanTest, ProjectTest, HelpTest, ConfigureTest]
      fail-fast: false   # Continue other jobs even if one fails

    steps:
      - name: Checkout Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Download CX CLI
        shell: powershell
        run: |
          Invoke-WebRequest -Uri "https://download.checkmarx.com/CxOne/CLI/latest/ast-cli_windows_x64.zip" -OutFile cx-cli.zip
          Expand-Archive -Path cx-cli.zip -DestinationPath C:\cx-cli -Force

      - name: Run ${{ matrix.test-class }}
        shell: powershell
        env:
          CX_BASE_URI: ${{ secrets.CX_BASE_URI }}
          CX_BASE_AUTH_URI: ${{ secrets.CX_BASE_AUTH_URI }}
          CX_TENANT: ${{ secrets.CX_TENANT }}
          CX_APIKEY: ${{ secrets.CX_APIKEY }}
          CX_CLI_PATH: C:\cx-cli
        run: mvn clean test -Dtest=${{ matrix.test-class }} --no-transfer-progress

      - name: Upload Report for ${{ matrix.test-class }}
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: Report-${{ matrix.test-class }}
          path: |
            test-output/ExtentReport_*.html
            test-output/TestReport.xlsx
          retention-days: 30
```

---

## 14. GitHub Actions Artifacts (Reports)

### How Reports Are Generated in the Pipeline

| Report | Generator | Output Location |
|--------|-----------|-----------------|
| HTML (ExtentReport) | `ReportManager.java` + `Base.java @AfterSuite` | `test-output/ExtentReport_<timestamp>.html` |
| Excel | `ExcelReportListener.java` (TestNG IReporter) | `test-output/TestReport.xlsx` |

Both reports are generated **automatically** as part of test execution. The GitHub Actions workflow uploads them as **artifacts** using `actions/upload-artifact`.

### Report Retention

- Reports are retained for **30 days** by default in GitHub Actions
- Adjust `retention-days` in the workflow YAML as needed

### Accessing Reports

1. Go to **GitHub → Repository → Actions tab**
2. Click on the latest workflow run
3. Scroll to **Artifacts** section at the bottom of the page
4. Download `ExtentReport-HTML` or `TestReport-Excel`

---

## 15. Troubleshooting

### ❌ `Environment variable 'CX_APIKEY' is not set!`
**Cause:** Required environment variable is missing.
**Fix:** Set all required environment variables as described in [Section 5](#5-environment-variables).

---

### ❌ `'cx' is not recognized as an internal or external command`
**Cause:** `cx.exe` is not on the system PATH and `CX_CLI_PATH` is not set.
**Fix:** Either add the directory containing `cx.exe` to your PATH, or set:
```
CX_CLI_PATH=C:\path\to\cx-folder
```

---

### ❌ CLI Help test fails after a CX CLI update
**Cause:** The `cx --help` output changed and doesn't match `cx_help.txt`.
**Fix:** Run `cx --help`, copy the output, and update `src/main/resources/cx_help.txt`.

---

### ❌ Tests hang or timeout during scan creation
**Cause:** Checkmarx One server is slow to respond or the scan is taking too long.
**Fix:** Increase the timeout in `CLIHelper.runCommandWithTimeout()` or check Checkmarx One server status.

---

### ❌ Excel report (`TestReport.xlsx`) is locked / cannot be written
**Cause:** The file is open in Excel when the test runs.
**Fix:** Close the Excel file before running tests.

---

### ❌ SCA tests fail with `SCA_RESOLVER_PATH not found`
**Cause:** `SCA_RESOLVER_PATH` environment variable is not set or points to a wrong path.
**Fix:** Set `SCA_RESOLVER_PATH` to the correct path of `ScaResolver.exe`.

---

### ❌ GitHub Actions: Tests fail but reports are not uploaded
**Cause:** The upload step has `if: success()` (default) and tests failed.
**Fix:** Ensure upload steps use `if: always()` so reports are uploaded regardless of test outcome.

---

*Last Updated: March 2026*
*Maintained by: QA Automation Team*

