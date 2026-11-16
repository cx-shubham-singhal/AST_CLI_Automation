# Regression gap analysis — cases not yet covered by the Java suite

Source: CLI repo `ast-cli` (main @ 288727f9) integration tests + command source, compared with `src/test/java/**`.
Status: **candidate list, nothing implemented yet.** Exact error strings were taken from the Go source but the
`cx` binary was not run — verify each when implementing.

Legend — **Grp**: S = sanity, R = regression. **Needs**: what must exist in the tenant/machine (see section 7).
Cases marked ⚠ are destructive / change credentials, config or findings (use unique names, temp HOME / config path, clean up).
Cases marked ⛔ need an external service (SCM token, proxy, mail, OpenAI endpoint) — proposed to skip unless you can provide it.

Corrections found while analysing: `project create` has no `--criticality` / `--private-package`; there is no
"image" remediation subcommand; `utils logs` / `utils telemetry` are not commands (`scan logs` and `cx telemetry ai` are).

## 1. Auth / configure / utils / help / global flags (≈ 50)

| ID | Case | Grp | Needs |
|---|---|---|---|
| AUTH-01 | validate with client-id/secret only | S | client creds |
| AUTH-02 | validate with apikey only | S | api key |
| AUTH-03 | validate with explicit --base-auth-uri + --tenant | R | client creds |
| AUTH-04 | validate with wrong tenant | R | client creds |
| AUTH-05 | validate with malformed apikey (not a JWT) | S | – |
| AUTH-06 | validate with empty --base-auth-uri | R | client creds |
| AUTH-07 | validate with only --base-uri, no creds | R | – |
| AUTH-08 | validate through unreachable --proxy | R | – |
| AUTH-09 | precedence: flag overrides env var | R | api key |
| AUTH-10 | precedence: env var overrides config file | R | api key |
| AUTH-11 | both apikey and client-id/secret supplied | R | both |
| AUTH-12 | only client-id or only client-secret | R | – |
| AUTH-13 | auth register missing each required flag | R | – |
| AUTH-14 | auth register happy path ⚠ | R | admin user |
| AUTH-15 | auth login / logout (help text only in CI) ⚠⛔ | R | – |
| AUTH-16 | help for auth login/logout/register/validate | R | – |
| CFG-01 | CX_CONFIG_FILE_PATH env var loaded | R | – |
| CFG-02 | --config-file-path flag | R | – |
| CFG-03 | config file not found / is a directory | R | – |
| CFG-04 | config file without permission (Linux/mac only) | R | – |
| CFG-05 | set then show round trip on a custom path | S | – |
| CFG-06 | remaining properties (proxy type, ntlm domain, agent name, origin, timeout, optional flags, ignore proxy) ⚠ | R | – |
| CFG-07 | secrets masked in `configure show` | R | – |
| UTL-01 | `cx version` and `--version` | S | – |
| UTL-02 | `utils env` (no secret leakage) | R | – |
| UTL-03 | `utils completion` bash/zsh/fish/powershell + invalid shell | R | – |
| UTL-04 | `utils mask` valid and invalid file | R | sample json |
| UTL-05 | `utils tenant` (+ --format json) | S | api key |
| UTL-06 | `utils tenant --format xml` error | R | api key |
| UTL-07 | `utils learn-more --query-id` (console/json) | R | SAST licence, query id |
| UTL-08 | learn-more missing / invalid query id | R | api key |
| UTL-09 | `utils import` help + bad args | R | – |
| UTL-10 | `utils pr` / `utils remediation` required-flag errors | R | – |
| UTL-11 | `--help` for every `utils` subcommand | R | – |
| HLP-01 | help for remaining top-level commands (data-driven) | R | – |
| HLP-02 | unknown command / unknown flag | S | – |
| CHT-01..03 | chat kics/sast with invalid key (OpenAI / Azure) ⛔ | R | external endpoint |
| CHT-04 | chat required-flag validation | R | – |
| ROOT-01 | `--debug` output | S | api key |
| ROOT-02 | `--log-file` / `--log-file-console` + invalid dirs | R | api key |
| ROOT-03 | `--retry` / `--retry-delay` on unreachable host | R | – |
| ROOT-04 | `--timeout` | R | – |
| ROOT-05 | `--insecure` ⛔ | R | self-signed endpoint |
| ROOT-06 | `--ignore-proxy` vs proxy env | R | api key |
| ROOT-07 | proxy type / NTLM / Kerberos flags ⛔ | R | proxy |
| ROOT-08 | `--agent` / `--optional-flags` in request headers | R | api key |
| ROOT-09 | `--format` json/list/table/invalid on list commands | R | api key |
| FF-01 | feature flags fetched without error | R | api key |
| RL-01 | SCM rate-limit retry ⛔ | skip | mock + tokens |

## 2. Project / groups / results / triage / bfl / import / hooks / PR / user-count (≈ 90)

| ID | Case | Grp | Needs |
|---|---|---|---|
| PRJ-01 | create with --main-branch (verify in show) | R | – |
| PRJ-02 | create with --repo-url | R | – |
| PRJ-03 | create with valid --groups | R | group G1 |
| PRJ-04 | create with invalid group | R | – |
| PRJ-05 | create with --ssh-key (valid / bad path) | R | dummy key file |
| PRJ-06 | tags `a,b:val` appear in `project tags` | R | – |
| PRJ-07 | list --filter names / name-starts-with | R | project P |
| PRJ-08 | list --filter groups | R | G1 has a project |
| PRJ-09 | list --filter tags-keys / tags-values | R | tagged project |
| PRJ-10 | project with no tags | R | untagged project |
| PRJ-11 | list --filter invalid key | R | – |
| PRJ-12 | list --limit 0 / negative / non-numeric | R | – |
| PRJ-13 | `project branches` | S | project with scan |
| PRJ-14 | branches --filter | R | project with scan |
| PRJ-15 | branches missing/invalid project | R | – |
| PRJ-16 | create with application user cannot see | R | – |
| PRJ-17 | create with application then list --filter application-id | R | application A1 |
| PRJ-18 | show/delete without --project-id | R | – |
| GRP-01 | create with group user lacks permission ⛔ | R | limited-rights key |
| RES-01 | show summaryConsole | S | golden scan |
| RES-02 | show summaryJSON (+ counters) | S | golden scan |
| RES-03 | show summaryHTML | S | golden scan |
| RES-04 | show json | S | golden scan |
| RES-05 | sarif | R | golden scan |
| RES-06 | sonar | R | golden scan |
| RES-07 | markdown | R | golden scan |
| RES-08 | gl-sca / gl-sast | R | SAST+SCA scan |
| RES-09 | pdf | R | golden scan |
| RES-10 | pdf options valid/invalid | R | golden scan |
| RES-11 | pdf email invalid address | R | golden scan |
| RES-12 | pdf email valid ⛔ | R | SMTP |
| RES-13 | sbom (CycloneDxJson) | R | SCA scan |
| RES-14 | sbom on SAST-only scan errors | R | SAST-only scan |
| RES-15 | invalid format / invalid scan-id | S | – |
| RES-16 | missing --scan-id | S | – |
| RES-17 | --output-name / --output-path honoured | S | golden scan |
| RES-18 | --severity filter | R | mixed severities |
| RES-19 | --state filter | R | mixed states |
| RES-20 | --status new/recurrent | R | 2 scans same project |
| RES-21 | upper-case severity/state accepted | R | golden scan |
| RES-22 | --filter limit/offset/sort/query/group | R | golden scan |
| RES-23 | --sast-filter / --sca-filter / --iac-security-filter | R | golden scan |
| RES-24 | --sca-hide-dev-test-dependencies | R | SCA scan with dev deps |
| RES-25 | snoozed/muted results excluded | R | scan with muted findings |
| RES-26 | multiple formats in one call | R | golden scan |
| RES-27 | `results exit-code` | S | completed scan |
| RES-28 | `results risk-management` | R | feature flag |
| RES-29 | `results codebashing` | R | codebashing licence |
| RES-30 | codebashing bad params / bad auth | R | – |
| RES-31 | publishedAt in json report | R | golden scan |
| RES-32 | jsonv2 report | R | golden scan |
| TRI-01 | `triage get-states` | S | – |
| TRI-02 | `triage show` SAST predicates | S | SAST finding |
| TRI-03 | `triage update` state/severity ⚠ | R | SAST finding (restore after) |
| TRI-04 | SCA triage update ⚠ | R | SCA finding |
| TRI-05 | invalid scan-type / state / severity | R | – |
| TRI-06 | unknown similarity id | R | – |
| TRI-07 | custom states ⚠ | R | custom state defined |
| BFL-01 | `results bfl` valid | R | SAST scan + query id |
| BFL-02 | bfl invalid ids / missing flags | R | – |
| IMP-01..04 | import SARIF / zip / new project / malformed SARIF | R | import licence + sample files |
| EXC-01 | `--exclude-git-folder` | R | folder with .git |
| EXC-02 | extension filters (new, backward-compat, conservative) | R | sample dir |
| IGN-01 | `ignore-vulnerability --help` (hidden cmd) | R | – |
| RRD-01 | `results-redundancy` | R | SAST scan |
| PR-01..04 | PR decoration GitHub/GitLab/Azure/Bitbucket ⛔ (PR-02/04 need no SCM) | R | tokens + PRs |
| UC-01..03 | user-count per SCM ⛔ | R | tokens |
| UC-04 | user-count bad token | S | – |
| HK-01..03 | pre-commit / pre-receive secrets hooks ⚠ | R | temp git repo, secrets licence |
| HK-04 | agenthooks install/help ⚠ | R | temp HOME |

## 3. Scan create / management — FAST validation cases (≈ 37, <10 s, no scan created)

| ID | Case | Grp |
|---|---|---|
| SCN-01 | empty project name | S |
| SCN-02..04 | threshold: limit 0, unparseable, missing value | R |
| SCN-05..07 | --sca-exploitable-path bad value / without SAST; --sca-last-sast-scan-time invalid | R |
| SCN-08 | --project-private-package invalid | R |
| SCN-09 | --iac-security-preset-id not a UUID | R |
| SCN-10..11 | --sast-base-branch empty / without --sast-incremental | R |
| SCN-12 | --branch-primary with value | R |
| SCN-13..14 | --ssh-key with non-SSH source / no argument | R |
| SCN-15 | --scan-timeout negative | R |
| SCN-16 | invalid scan type mixed with valid | R |
| SCN-17..19 | --sbom-only with non-SCA / report-format sbom without SCA / bad or missing file | R |
| SCN-20..21 | --sca-resolver with zip; --no-scan without --sbom-first | R |
| SCN-22..23 | --git-commit-history bad value / without scs | R |
| SCN-24..31 | --container-images: `dir:` prefix, unknown prefix, no tag, empty name/tag, empty after prefix, registry-only, tar missing/wrong ext/compressed, several invalid at once | R |
| SCN-33 | scs without repo URL/token | R |
| SCN-34 | missing zip / unsupported extension source | R |
| SCN-35 | scan show/delete/cancel/tags/workflow with missing or bogus id | S |
| SCN-36 | scan list invalid filters + --format json/list/table | R |
| SCN-37 | invalid --report-format / pdf options / pdf email on scan create | R |
| SCN-38 | unknown flags not yet used | R |

## 4. Scan create / management — SLOW cases (real scan or tenant call, ≈ 28)

| ID | Case | Grp | Needs |
|---|---|---|---|
| SCN-40 | git HTTPS public source | R | public repo |
| SCN-41 | git URL + token (good / bad) | R | private repo + PAT |
| SCN-42 | SSH source + --ssh-key | R | repo + deploy key |
| SCN-43 | --file-filter / --file-include / ant patterns | R | – |
| SCN-44 | --exclude-git-folder (+ --include-csv-json) | R | folder with .git |
| SCN-45 | --skip-default-filter (folder, zip) | R | – |
| SCN-46 | .gitignore filter present / absent | R | folders |
| SCN-47 | threshold semantics (pass/fail, SCA/IaC/containers, with report) | R | vulnerable source |
| SCN-48 | policy gate: --ignore-policy / --policy-timeout | R | break-build policy |
| SCN-49 | --async then show | S | – |
| SCN-50 | --scan-timeout 1 | R | slow source |
| SCN-51 | --resubmit (existing / missing project) | R | project with prior scan |
| SCN-52 | incremental + --sast-base-branch | R | prior scan on base branch |
| SCN-53 | --sast-preset-name valid / invalid | R | known preset |
| SCN-54 | --sast-filter, --sast-engine-verbose, workflow/logs | R | SAST |
| SCN-55 | scan logs sast / iac-security | R | completed scan |
| SCN-56 | scan workflow / tags / list filters | R | completed scan |
| SCN-57 | cancel then delete | R | – |
| SCN-58 | iac-security platforms / filter / valid preset UUID | R | IaC, preset UUID |
| SCN-59 | containers filters (file-folder, package, image-tag, non-final stages, system containers) | R | containers, docker |
| SCN-60 | --container-images real images / tar / tag variants | R | containers, docker/registry |
| SCN-61 | scs: scorecard + secret detection | R | SCS licence, GitHub token |
| SCN-62 | --sca-private-package, --sca-exploitable-path true | R | SAST+SCA |
| SCN-63 | report outputs on scan create (json/pdf/sarif/sbom/summaryHTML/gl-sast) | R | – |
| SCN-64 | project association: groups / application / tags (new vs existing) | R | G1, A1 |
| SCN-65 | --debug, apikey without tenant, multipart upload | R | – |
| SCN-66 | branch with whitespace/special characters | R | – |
| SCN-67 | dangling symlinks (Linux/mac only) | skip on Windows | – |

## 5. Realtime / developer-assist scanners (≈ 50)

Already covered: happy-path ASCA, OSS (~34 manifests), secrets and one containers file. **No** iac-realtime test, no negative paths.

| ID | Case | Grp | Fixture / Needs |
|---|---|---|---|
| RT-01..03 | iac-realtime Dockerfile, K8s yaml, negative file | S/R | existing; docker + DevAssist licence |
| RT-04..05 | iac Terraform, CloudFormation, compose, Helm, Ansible, tfvars, proto | R | NEW fixtures |
| RT-06..07 | iac --engine podman / invalid engine | R | existing |
| RT-08..11 | iac unsupported ext, missing -s, nonexistent file, directory | R | existing |
| RT-12 | iac empty Dockerfile | R | NEW empty.dockerfile |
| RT-13..14 | iac --ignored-file-path (suppresses; missing/malformed) | R | NEW ignore json |
| RT-15..16 | oss unsupported go.sum / package-lock.json / Gemfile.lock | R | existing, no tenant needed |
| RT-17 | oss: Podfile, Cartfile, pubspec.yaml, Package.swift, podspec, constraints/packages txt | R | NEW fixtures |
| RT-18 | oss multiple comma-separated files | R | existing |
| RT-19..22 | oss: empty manifest, malformed, zero-byte, 1000+ deps | R | NEW fixtures |
| RT-23 | oss nonexistent / directory / no -s | R | – |
| RT-24 | oss --ignored-file-path | R | NEW ignore json |
| RT-25 | oss output schema | S | existing |
| RT-26..29 | secrets: negative paths, more file types (.env, id_rsa…), clean file, ignore | R | NEW .env, ignore json |
| RT-30..33 | containers: compose + helm, empty Dockerfile, negatives, ignore | R | existing + NEW |
| RT-34..39 | ASCA: no -s, nonexistent file, unsupported language, more languages (java/cs/go/php/rb/kt), flags (--asca-latest-version, --agent, --asca-location variants), ignore | S/R | NEW language fixtures |
| RT-40..43 | `ignore-vulnerability` add / idempotent / --remove / @file / stdin / bad --scan-type / per scanner | S/R | temp files |
| RT-44 | `scan kics-realtime` | R | docker |
| RT-45 | `scan sca-realtime` (valid + missing dir) | R | ScaResolver download |
| RT-46..47 | `utils remediation kics` / `sca` | R | KICS results fixture |
| RT-48 | learn-more | R | SAST tenant |
| RT-49 | `cx telemetry ai` | R | – |
| RT-50 | realtime on tenant without DevAssist licence | R | second tenant (optional) |

## 6. Suggested build order

1. **Batch 1 — no tenant setup, fast, safe (~110 cases):** SCN-01..38 validation cases, RT-07..11/15/16/23/26/32/34/35/38/40..42,
   AUTH-05/07/12/16, CFG-01..05/07, UTL-01/03/11, HLP-01/02, ROOT-01..04/06/08/09, RES-15/16, TRI-05/06, PRJ-04/11/12/15/18.
2. **Batch 2 — needs a "golden" project + scans (~60 cases):** all RES-*, TRI-01/02, BFL-*, PRJ-07..17, SCN-55/56.
3. **Batch 3 — needs licences/tools (~40 cases):** realtime fixtures, iac/kics-realtime, containers, SCN-58..60, remediation.
4. **Batch 4 — external/destructive (decide later):** SCM, PR decoration, user-count, mail, proxy, hooks, register/login.

## 7. What you would need to create / enable

**Credentials**
- Valid API key and valid client id/secret (already used today).
- Optional: a limited-rights user/API key (GRP-01), a tenant without DevAssist licence (RT-50), a no-tenant-claim API key (SCN-65).

**Tenant objects**
- Group **G1** (plus one the limited user cannot use). Applications **A1**, **A2** (`QAApplicationForAutomation` is used today).
- A stable **"golden" project** with completed scans containing SAST, SCA, IaC and Containers findings with mixed
  severities, states (some muted/snoozed) and statuses; keep the scan id, a similarity id, a query id and an SCA vulnerability.
- A second scan on the same project (status=recurrent), a project with scans on 2+ branches, a prior scan on a base
  branch (incremental), an SCA-only scan and a SAST-only scan.
- A project with no tags, and one tagged `a` and `b:val`.
- A custom triage state; a tenant policy with break-build for a project; a known IaC preset UUID and SAST preset name.

**Licences / feature flags**: SAST, SCA, IaC, API Security, Containers, SCS (scorecard/secret detection), DevAssist/AI
protection (realtime), codebashing, risk management, SBOM, SARIF import, secrets detection (hooks), optional SMTP (PDF email).

**Machine / repos**: docker or podman; ScaResolver (already at `C:\Plugins\ScaResolver`); a public git URL;
optionally a private repo + PAT, an SSH repo + deploy key, a GitHub token (scorecard); SCM tokens + open PRs only if you
want PR decoration / user-count; a folder containing `.git` and `.gitignore`; a temp git repo with a fake secret; sample
SARIF / SARIF zip / malformed SARIF, a dummy SSH key file, and the new realtime fixture files listed in section 5.
