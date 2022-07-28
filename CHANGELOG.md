# ANNIS changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

### Fixed

- No `Row outside dataProvider size` error message when the corpus list is
  empty.

## [Unreleased]

## [4.9.5] - 2022-07-26

### Fixed

- Corpus selection was not reliably shown when executing a corpus query or
  corpus selecting by using the URL.
- Update to graphANNIS 2.2.2 to avoid out of memory errors for results which a
  large number of matches.


## [4.9.4] - 2022-07-08

### Fixed

- Ignore terminal namespace when null in tree visualizers. The tree visualizer
  used to fail, when the terminal namespace mapping is not configured, thus
  null. Null was then matched against the terminal node's actual namespace in
  ANNIS4. In ANNIS3, though, the namespace for terminals was ignored when the
  mapping was not configured (i.e. null). This behavior is now restored.

## [4.9.3] - 2022-07-01

### Fixed

- Update to graphANNIS 2.2.1, which fixes a bug where the left/right context was
  switched when using a segmentation as context.

## [4.9.2] - 2022-07-01

### Fixed

- Documents which special characters (like %) in their name could not be opened
  in full text visualizes (like the document browser).
- The `raw_text` visualizer did not work properly and did not show the whole
  text.
- Avoid issues with possible problematic random number generation in visualizer
  result ID. Since the ID is only locally used for one match view, the impact of
  a flawed random generator are limited, but it is still bad practice to create
  a new random generator each time.
- Multiple texts in parallel corpora where treated as a single text, causing
  several problems. E.g. the KWIC view was showing a single token row with gaps
  instead of several token rows for parallel corpora.
- Fixed issue with qualified annotation names in HTML visualizer, which resulted
  in incomplete results.

## [4.9.1] - 2022-06-30

### Fixed

- Addressed some non-security issues confusing static code analyzers (#782)
- Bumps commons-email from 1.3.2 to 1.5.
- Update Kotlin test dependency to 1.6 and also Karibu Testing
  (which needs the Kotlin dependency)
- Do not restart possible crashed graphANNIS service in integration tests.

## [4.9.0] - 2022-06-15

###  Added

- Allow to change the displayed order of corpus metadata annotations using the
  `corpus_annotation_order` configuration in the `corpus-config.toml`
  file. (#500)
- Automatically restart the bundled web service when it crashed.

### Fixed

- Only show warning on MacOS with M1 processor instead if exiting.
- Use several lines for showing the MacOS M1 warning, because there is no
  automatic line brake.

## [4.8.0] - 2022-05-31

### Fixed

- Upgrade to graphANNIS 2.1.0 to fix near operator which failed to work with
  segmentation constraint and also remove the db.lock file when ANNIS is closed
- Do not skip error messages when started service is aborted. (#761)

### Added

- Because of the update to graphANNIS 2.1.0, it is also now possible to easily
  allow read-only access to all corpora without login and configuring an
  authentication provider using the graphANNIS `anonymous_access_all_corpora`
  configuration parameter.

## [4.7.1] - 2022-05-30

### Fixed

- Upgrade to graphANNIS 2.0.6 to fix subgraph generation when a segmentation was
  defined as context and the match includes a token that is not covered by a
  segmentation node (there are gaps in the segmentation). Because token where
  missing from the graph, it could appear in ANNIS that there are gaps in the
  data and that the token order is incorrect.

## [4.7.0] - 2022-05-19

### Added

- Show corpus metadata with corpus hits and not only the document metadata (#768)

## [4.6.7] - 2022-05-12

### Fixed

- Upgraded to graphANNIS 2.0.5. 
  Fix timeout handling for queries with a lot of intermediate results, but less
  than 1000 matches.

## [4.6.6] - 2022-04-22

### Fixed

- Upgraded to graphANNIS 2.0.4. This fixes wrong number of results when using the non-existant
  operators with node attribute searches without a value (e.g. `dipl !_=_ norm`).
- Fix error message in corpus browser window when there is a meta annotation with an invalid name
  (like e.g. a space "corpus description" in the Bematac corpus).
- HTML visualizer might output the elements several times (#755).

## [4.6.5] - 2022-04-02

### Fixed

- Mitigate against security issue [CVE-2022-22965
  ](https://tanzu.vmware.com/security/cve-2022-22965) by upgrading Spring boot
  dependency to 2.5.12 See
  <https://spring.io/blog/2022/03/31/spring-framework-rce-early-announcement>
  for for information.

## [4.6.4] - 2022-04-01

### Fixed

- Fix several issues with corpora containing an Umlaut or other special
  characters. There is often now a distinction between using the decoded corpus
  name or the "raw" node names, e.g. to get the corpus or HTML visualizer
  configuration. This also updates graphANNIS to 2.0.3 which includes the
  corresponding fixes.

## [4.6.3] - 2022-03-29

### Fixed

- Upgraded to graphANNIS 2.0.1. Among other things, it resolves issues when
  importing large GraphML or relANNIS corpora and issues with translating
  relANNIS resolver mappings. While this is a major graphANNIS release because
  of some internal API changes, no data migration or other upgrade steps are
  required. The AQL interpretation also remained the same.
- Show visible error message when 32-Bit Java is used to start ANNIS Desktop
  (#742)
- Mention the possibility to import GraphML in the import panel (#744)
- Fix configuration example for user configuration with Keycloak

## [4.6.2] - 2022-01-06

### Fixed

- Updated to graphANNIS 1.5.0 to fix issues with the import of relANNIS 3.3
  files with missing segmentation information. This graphANNIS release also
  improves the performance of the relANNIS import in general.

## [4.6.1] - 2021-12-12

### Fixed

- Enforce upgrade of (unused) log4j dependency to 2.15.0.
  While we do have log4j in our classpath, Spring is not configured per default
  to actually use log4j for debugging and thus ANNIS should be unaffected by
  CVE-2021-44228 (see https://spring.io/blog/2021/12/10/log4j2-vulnerability-and-spring-boot). 
  But it is better safe to than to be sorry, so we force Spring to use the fixed 
  log4j version.


## [4.6.0] - 2021-12-08

### Changed

- Improved available space for corpus names in corpus list table
- More prominent highlight of corpus and document name in match list

### Fixed

- Updated to graphANNIS 1.4.1 to fix issues with the relANNIS import. This
  also improves the performance for the `tok` query on large corpora.

## [4.5.3] - 2021-12-06

### Fixed

- Updated to Vaadin Version 8.14.3
- Fixed memory consumption issues during relANNIS import by updating to graphANNIS 1.4.0

## [4.5.2] - 2021-09-20

### Fixed

- AQL code editor: avoid undefined variable by defining a default value

## [4.5.1] - 2021-09-20

### Fixed

- Fix "java.io.IOException: unexpected end of stream" exception for some queries
  with optional nodes by updating to graphANNIS to 1.3.0
- Do not highlight optional nodes with colors in query text editor

## [4.5.0] - 2021-09-16

### Added

- Add operator negation without existence assumption to AQL by upgrading to graphANNIS 1.2.1. 
  Optional and possible non-existing nodes are marked with the suffix `?` and can be combined 
  with negated operators like `!>*`. This means you can e.g. search for all sentences without a 
  noun with the query like `cat="S" !>* pos="NN"?`. More information can be found
  in the User Guide in the section "Operator Negation".

## [4.4.0] - 2021-09-10

### Added

- Add operator negation with existence assumption to AQL
  by upgrading to graphANNIS 1.1.0

## [4.3.1] - 2021-08-27

### Fixed

- Fetching the segmentation span in CSVExporter could fail if only one token was covered.

## [4.3.0] - 2021-08-26

### Added

- Support the `segmentation` parameter in the CSVExporter. This allows to get the spanned text
  not from the token (which can be empty), but from the given segmentation layer.
  
### Fixed

- User guide was still mentioning the non-existing WekaExporter

## [4.2.0] - 2021-08-25

### Changed

- Always show the selected corpora at the beginning of the grid, even when not included in the 
  current filter. This should make it much easier to spot corpora that where selected by mistake. 
  (also fixes issue #563).

### Fixed

- Exception thrown when URL with corpus fragment (`#c=...`) was openend
- Fix width of the corpus list columns when scrolling to long corpus names
- Info and browse document buttons were not always visible without scrolling
- Removed internal usage of the `clone()` function which has been reported
  as "blocker" code smell by the sonarcloud static code analysis.

## [4.1.4] - 2021-08-24

### Fixed

- Matches for documents or (sub-) corpora where not shown. There is a special SingleCorpusResultPanel which should have been used, but was not triggered.
- Media player was not loaded in result view
- Security update in dependency jsoup (from 1.11.2 to 1.14.2)

## [4.1.3] - 2021-08-21

### Fixed

- Query result order was not considered in result view

## [4.1.2] - 2021-08-20

### Fixed

- Use updated graphANNIS version 1.0.2 to fix issues in performance of subgraph extraction
- Instance configurations where not loaded (including the default one)

## [4.1.1] - 2021-08-20

### Fixed

- Do not use the default read timeout in the HTTP REST client (#712)
- Use an GitHub Action that only uploads the release files instead of creating a new release
- Download the test corpora before building the release artifacts

## [4.1.0] - 2021-08-19

### Added

- Support for Oauth2 authorization services like https://github.com/korpling/shibboleth-oauth2-forwarding instead of only OpenID Connect

### Changed

- Release binaries are generated by GitHub Actions and added to GitHub Releases 
  (instead of releasing ANNIS on Maven Central)
  
### Fixed

- Editing a group shows an error message about a missing
  `PopupTwinColumnSelectMultiline` class.
- Default server configuration now creates the needed SQL tables for the reference
  links. This fixes an error message in the administrator view.
- Removed instable inclusion of CITATION.cff file in "About ANNIS" window.


## [4.0.0] - 2021-08-17

### Fixed

- Do not discard dominance edge, when only the target node has an incoming unnamed incoming edge.
  For the GUM corpus with mixed dominance for syntax and RST trees, this caused some segments to
  have no connection to any token. (#696)
- Fix display of document meta annotations in the document browser 
- Desktop UI aborted execution on KDE when trying to open the browser (#702)

## [4.0.0-beta.6] - 2021-04-01

### Fixed

- Validating or executing erroneous AQL queries triggered fallback 
  dialog for unexpected errors instead of providing user-friendly feedback.

## [4.0.0-beta.5] - 2021-02-18

### Changed

- Upgrade to graphANNIS 0.31.0 which brings its own REST service
- User interface now uses Spring Boot to create a runnable jar file which can be executed easily without explicit installation.

### Removed

- REST service has been removed 
- Kickstarter has no user interface for importing and deleting corpora, use the web administration instead

## [4.0.0-beta.4] - 2020-03-26

### Changed

- Upgraded Vaadin from 7 to 8 using the compatibility layer
- Updated graphANNIS to version 0.27.1 with multiple fixes for issues discovered in the previous beta release

### Fixed

- Create multiple textual data sources in Salt document for parallel corpora instead of merging them into one STextualDS
- When querying multiple corpora at once, getting the next page could fail because of some issues with the offset generation
- Allow underscore in named node critera [#643](https://github.com/korpling/ANNIS/issues/643)

## [4.0.0-beta.3] - 2019-10-18

### Fixed

- RST document visualizer did not order token properly [#615](https://github.com/korpling/ANNIS/issues/615)
- Actually retrieve `edge_name` attribute from database when fetching the corpus annotations  [#616](https://github.com/korpling/ANNIS/issues/616)
- On import, assign correct sub-type `p` for pointing relations entries without annotation: **You need to re-import the corpora to get a correct example query in the corpus browser**.
- Use context instead of whole location URI to resolve the iframe vis URL [#581](https://github.com/korpling/ANNIS/issues/581)
- Login was failing since web service URL was not properly used in login request handler
- ZIP-Import might fail because of large directory names
- CSV export was had a mismatch between the order of the metadata keys in the header and in the column
- Using a segmentation for the context definition did not work [#624](https://github.com/korpling/ANNIS/issues/624)
- Frequency analysis and export where not possible with AQL quirks mode and queries that are invalid in the newest AQL version (e.g `meta::`)

## [4.0.0-beta.2] - 2019-07-22

### Fixed

- Document explorer does not show metadata specified in `document_browser.json` [#610](https://github.com/korpling/ANNIS/issues/610)

### Changed

- Update graphANNIS to version 0.22.0 to fix issues with Kickstarter on Windows and MacOS

## [4.0.0-beta.1] - 2019-05-26

This **beta** pre-release is a complete overhaul of the ANNIS service backend.
Instead of using the relational database PostgreSQL, a custom AQL implementation based on graphs called [graphANNIS](https://github.com/korpling/graphANNIS) is used.

ANNIS 4 currently only supports a sub-set of the ANNIS Query Langugage (AQL) compared to ANNIS 3.
Full support is planned, but some backward-compatible features of AQL will only be available in a compatibility mode.
See the chapter "Differences in Compatibility Mode" of the User Guide in in the Tutorial for more information.
For adminstrators, there are instructions on how to migrate from ANNIS 3 to 4 in the User Guide.

Since the whole backend has been replaced, we expect more bugs that need to be fixed than in usual feature-releases. 
Please report any issues in the [GitHub issue tracker](https://github.com/korpling/ANNIS/issues).

## Version 3.6.0

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/42?closed=1

### Fixed Bugs
- [#592](https://github.com/korpling/ANNIS/issues/592) Restrict visjs keyboard shortcuts to elements

### Enhancements
- [#603](https://github.com/korpling/ANNIS/issues/603) Use Maven gitflow plugin for releases
- [#600](https://github.com/korpling/ANNIS/issues/600) Allow to specify custom online help URL in instance configuration
- [#599](https://github.com/korpling/ANNIS/issues/599) Generate the online help from selected content from the user guide
- [#595](https://github.com/korpling/ANNIS/issues/595) New User and Developer Guide
- [#593](https://github.com/korpling/ANNIS/issues/593) Initial version of SentStructure.js visualizer


## Version 3.5.1

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/61?closed=1

### Fixed Bugs
- [#596](https://github.com/korpling/ANNIS/issues/596) Grid hides annotation layer with the name "tok" if configured to hide the tokenization

## Version 3.5.0

This is the first stable release of the 3.5.x series.
It is the same as 3.5.0-rc3 but with an updated version number.
See the changelog for enhancements and bug-fixes for the 3.5.x series 
since the 3.4.4 release.

## Version 3.5.0-rc3

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/60?closed=1

### Fixed Bugs
- [#594](https://github.com/korpling/ANNIS/issues/594) Update to Jetty 9 and Spring 4.3 for security reasons


## Version 3.5.0-rc2

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/59?closed=1

### Fixed Bugs
- [#591](https://github.com/korpling/ANNIS/issues/591) NullPointerException when not using virtual keyboard

## Version 3.5.0-rc1

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/58?closed=1

### Fixed Bugs
- [#589](https://github.com/korpling/ANNIS/issues/589) Empty temporary directories created for each match in result, but never deleted
- [#587](https://github.com/korpling/ANNIS/issues/587) Fix frequency query output when OR alternatives use different orders

### Enhancements
- [#586](https://github.com/korpling/ANNIS/issues/586) Add button to copy AQL

## Version 3.5.0-preview7

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/57?closed=1

### Fixed Bugs
- [#576](https://github.com/korpling/ANNIS/issues/576) GridExporter fails for texts with "," in name

### Enhancements
- [#583](https://github.com/korpling/ANNIS/issues/583) Allow to configure the web server port on the command line for the Kickstarter

## Version 3.5.0-preview6

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/56?closed=1

### Fixed Bugs
- [#575](https://github.com/korpling/ANNIS/issues/575) Optimize PostgreSQL lock usage for installations with large number of corpora
- [#572](https://github.com/korpling/ANNIS/issues/572) Problem with "," in text names
- [#568](https://github.com/korpling/ANNIS/issues/568) Control characters in annotations leads to invalid XML generated by the REST service


## Version 3.5.0-preview5

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/55?closed=1

### Fixed Bugs
- [#566](https://github.com/korpling/ANNIS/issues/566) Wrong count for some queries using the near operator ^
- [#540](https://github.com/korpling/ANNIS/issues/540) AQL-editor and bidirectional text

### Enhancements
- [#565](https://github.com/korpling/ANNIS/issues/565) Support PostgreSQL 10

## Version 3.5.0-preview4

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/54?closed=1

### Fixed Bugs
- [#561](https://github.com/korpling/ANNIS/issues/561) Fix initial corpus set selection when pre-selected corpus is not visible 
- [#555](https://github.com/korpling/ANNIS/issues/555) Code mirror keybindings interfere with some keyboard layouts

### Enhancements
- [#562](https://github.com/korpling/ANNIS/issues/562) Add benchmark modes to perform different kind of benchmarks

## Version 3.5.0-preview3

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/53?closed=1

### Fixed Bugs
- [#558](https://github.com/korpling/ANNIS/issues/558) Update Vaadin Version to avoid Denial of Service attack vector
- [#557](https://github.com/korpling/ANNIS/issues/557) Inserting an unknown context leads to NullPointerException

### Enhancements
- [#523](https://github.com/korpling/ANNIS/issues/523) Exporting spans from corpora with more than one primary text

## Version 3.5.0-preview2

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/52?closed=1

### Enhancements
- [#551](https://github.com/korpling/ANNIS/issues/551) Feature/kidko exporter two pass
- [#541](https://github.com/korpling/ANNIS/issues/541) Add virtual keyboards for Ethiopic
- [#538](https://github.com/korpling/ANNIS/issues/538) arabic in raw_text

## Version 3.5.0-preview1

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestone/51?closed=1

### Fixed Bugs
- [#536](https://github.com/korpling/ANNIS/issues/536) Queries violating non-reflexivity constraint result in "Client response status: 500"
- [#533](https://github.com/korpling/ANNIS/issues/533) Left context not displayed when number not pre-defined
- [#528](https://github.com/korpling/ANNIS/issues/528) Can't use CSV exporter for OR-queries with different numbers of nodes in clauses.
- [#524](https://github.com/korpling/ANNIS/issues/524) Rendering overlaps in grid view not always shown in Firefox

### Enhancements
- [#537](https://github.com/korpling/ANNIS/issues/537) Better handling of concurrent users.
- [#535](https://github.com/korpling/ANNIS/issues/535) Use new interactive VisJS visualization from Salt in ANNIS
- [#531](https://github.com/korpling/ANNIS/issues/531) update to Vaadin 7.7.x
- [#529](https://github.com/korpling/ANNIS/issues/529) Refactor exporter infrastructure and add the possibility to recreate the timeline in Salt
- [#526](https://github.com/korpling/ANNIS/issues/526) Set mnemonic on "overwrite" checkbox in ImportDialog to "w"
- [#522](https://github.com/korpling/ANNIS/issues/522) HTML Template support Issue #513 Master

## Version 3.4.4

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.4+is%3Aclosed

### Fixed Bugs
- [#519](https://github.com/korpling/ANNIS/issues/519) update to Vaadin 7.6.6
- [#516](https://github.com/korpling/ANNIS/issues/516) Incorrect hit submatch colors in arch_dependency and KWIC
- [#515](https://github.com/korpling/ANNIS/issues/515) Escaping example_queries


## Version 3.4.3

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.3+is%3Aclosed

### Fixed Bugs
- [#514](https://github.com/korpling/ANNIS/issues/514) not escaped user input in REST API

## Version 3.4.2

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.2+is%3Aclosed

### Fixed Bugs
- [#511](https://github.com/korpling/ANNIS/issues/511) Embedded visualization keeps loading forever
- [#510](https://github.com/korpling/ANNIS/issues/510) Wrong color in tree visualization for child nodes

## Version 3.4.1

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.1+is%3Aclosed

### Fixed Bugs
- [#509](https://github.com/korpling/ANNIS/issues/509) Link from embedded visualization to search UI is gone in 3.4.0

## Version 3.4.0

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0+is%3Aclosed

### Fixed Bugs
- [#508](https://github.com/korpling/ANNIS/issues/508) "X is not a valid annotation name" error is not removed when "Search" button is clicked.
- [#506](https://github.com/korpling/ANNIS/issues/506) possible problems with Apache Collections library and serialization
- [#497](https://github.com/korpling/ANNIS/issues/497) Can't close match reference window while preview is loading
- [#453](https://github.com/korpling/ANNIS/issues/453) Dependencies not showing in Safari Browser

## Version 3.4.0-rc5

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-rc5+is%3Aclosed

### Fixed Bugs
- [#499](https://github.com/korpling/ANNIS/issues/499) GraphML output of the Salt type is not using the most specific class/type

## Version 3.4.0-rc4

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-rc4+is%3Aclosed

### Fixed Bugs
- [#496](https://github.com/korpling/ANNIS/issues/496) speaker icon in grid does nothing when visualization is embedded
- [#495](https://github.com/korpling/ANNIS/issues/495) invalid GraphML
- [#494](https://github.com/korpling/ANNIS/issues/494) fails to parse tok!="..." when used in the  AQL short form

## Version 3.4.0-rc3

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-rc3+is%3Aclosed

### Fixed Bugs
- [#493](https://github.com/korpling/ANNIS/issues/493) can't set "default-base-text-segmentation" property to token
- [#491](https://github.com/korpling/ANNIS/issues/491) htmlvis won't output spans which have the same annotation name and the same token span

### Enhancements
- [#492](https://github.com/korpling/ANNIS/issues/492) htmlvis should support a pseudoregion that covers all token

## Version 3.4.0-rc2

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-rc2+is%3Aclosed

### Fixed Bugs
- [#490](https://github.com/korpling/ANNIS/issues/490) LegacyGraphConverter modifies original Salt graph

## Version 3.4.0-rc1

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-rc1+is%3Aclosed

### Fixed Bugs
- [#489](https://github.com/korpling/ANNIS/issues/489) query builder won't work with "tok != "value"
- [#485](https://github.com/korpling/ANNIS/issues/485) Reference links for single matches fail when annotation namespace has a space character

### Enhancements
- [#488](https://github.com/korpling/ANNIS/issues/488) allow to test the UI with Selenium
- [#487](https://github.com/korpling/ANNIS/issues/487) re-order the physical layout of the fact tables so that columns that are more likely to be used in a (Merge) join are at the beginning
- [#486](https://github.com/korpling/ANNIS/issues/486) better PostgreSQL planner estimations for "same span" operator

## Version 3.4.0-preview10

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-preview10+is%3Aclosed

### Fixed Bugs
- [#483](https://github.com/korpling/ANNIS/issues/483) fix wrong command: start needs to be launch
- [#479](https://github.com/korpling/ANNIS/issues/479) color highlighting differs in KWIC
- [#478](https://github.com/korpling/ANNIS/issues/478) htmlvis: can't use only "annis:BEGIN" or "annis:END"
- [#474](https://github.com/korpling/ANNIS/issues/474) BEGIN and END instructions not working in htmlvis
- [#473](https://github.com/korpling/ANNIS/issues/473) frequency definition: can't select more than one

### Enhancements
- [#484](https://github.com/korpling/ANNIS/issues/484) update to jquery 2.2
- [#482](https://github.com/korpling/ANNIS/issues/482) Better statistics for span and annotation columns
- [#481](https://github.com/korpling/ANNIS/issues/481) update to Vaadin 7.6.x
- [#475](https://github.com/korpling/ANNIS/issues/475) mapping properties contain "+" instead of spaces in embedded visualizer

## Version 3.4.0-preview9

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-preview9+is%3Aclosed

### Enhancements
- [#472](https://github.com/korpling/ANNIS/issues/472) don't output the same frequency definition twice
- [#471](https://github.com/korpling/ANNIS/issues/471) Update to Salt 3.0
- [#469](https://github.com/korpling/ANNIS/issues/469) grid: allow to choose which annotations should show their namespace


## Version 3.4.0-preview8

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-preview8+is%3Aclosed

### Fixed Bugs
- [#468](https://github.com/korpling/ANNIS/issues/468) switching to next page results in empty entry in history panel
- [#467](https://github.com/korpling/ANNIS/issues/467) switching to next page does not work when corpus was de-selected after submitting the query
- [#465](https://github.com/korpling/ANNIS/issues/465) executing search with empty corpus selection throws exception
- [#464](https://github.com/korpling/ANNIS/issues/464) Can't query for corpora where document names contain ","

### Enhancements
- [#466](https://github.com/korpling/ANNIS/issues/466)  allow to import ZIP files in Kickstarter

## Version 3.4.0-preview7

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-preview7+is%3Aclosed

### Fixed Bugs
- [#458](https://github.com/korpling/ANNIS/issues/458) component normalization fails to generate unique variable name
- [#457](https://github.com/korpling/ANNIS/issues/457) share match: exception when a node is matched twice (as AQL node) 
- [#456](https://github.com/korpling/ANNIS/issues/456) share match: some visualizers don't show the matched nodes

### Enhancements
- [#463](https://github.com/korpling/ANNIS/issues/463) improve indexes for token related queries

## Version 3.4.0-preview6

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/milestones/3.4.0-preview6

### Fixed Bugs
- [#455](https://github.com/korpling/ANNIS/issues/455) can't create reference link (match) for queries with more than one node

## Version 3.4.0-preview5

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-preview5+is%3Aclosed

### Enhancements
- [#454](https://github.com/korpling/ANNIS/issues/454) Allow to share single matches as links for publications
- [#107](https://github.com/korpling/ANNIS/issues/107) Share ANNIS visualizers as web service without front end

## Version 3.4.0-preview4

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-preview4+is%3Aclosed

### Fixed Bugs
- [#450](https://github.com/korpling/ANNIS/issues/450) Extending a context for single match sometimes fails
- [#449](https://github.com/korpling/ANNIS/issues/449) Error message "Corpus Properties does not exists" when logging in after following a corpus link

### Enhancements
- [#448](https://github.com/korpling/ANNIS/issues/448) service update script does not copy the shiro.ini file
- [#105](https://github.com/korpling/ANNIS/issues/105) Hit marking in HTML visualizations

## Version 3.4.0-preview3

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-preview3+is%3Aclosed

### Fixed Bugs
- [#447](https://github.com/korpling/ANNIS/issues/447) language change in virtual keyboard can trigger exception
- [#446](https://github.com/korpling/ANNIS/issues/446) order field empty when search view is re-attached
- [#444](https://github.com/korpling/ANNIS/issues/444) Error message about not having the access rights for a corpus when validating query

### Enhancements
- [#445](https://github.com/korpling/ANNIS/issues/445) Allow to show login window at each startup (per instance configuration)
- [#365](https://github.com/korpling/ANNIS/issues/365) Using a corpus URL for a corpus not in the list should open login window

## Version 3.4.0-preview2

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-preview2+is%3Aclosed

### Fixed Bugs
- [#443](https://github.com/korpling/ANNIS/issues/443) 3.4-0-preview1: query fragment is not longer evaluated
- [#441](https://github.com/korpling/ANNIS/issues/441) annotation existance check fails on annotations without namespace

### Enhancements
- [#442](https://github.com/korpling/ANNIS/issues/442) add "anonymous" group to each new user per default

## Version 3.4.0-preview1

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.4.0-preview1+is%3Aclosed

### Fixed Bugs
- [#437](https://github.com/korpling/ANNIS/issues/437) AQL: in precedence and near operator the "from" value can be large than the "to" value

### Enhancements
- [#440](https://github.com/korpling/ANNIS/issues/440) Make it possible to switch to administration UI easily
- [#439](https://github.com/korpling/ANNIS/issues/439) Easier upgrade for the service
- [#438](https://github.com/korpling/ANNIS/issues/438) allow to filter documents by name in document list
- [#436](https://github.com/korpling/ANNIS/issues/436) positions of semantic errors should be highlighted in query editor
- [#435](https://github.com/korpling/ANNIS/issues/435) Existence validator
- [#426](https://github.com/korpling/ANNIS/issues/426) enhance UI responsivness by using Vaadin push mechanism

## Version 3.3.6

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.3.6+is%3Aclosed

### Fixed Bugs
- [#434](https://github.com/korpling/ANNIS/issues/434) AnnisUser does not implement Serializable
- [#433](https://github.com/korpling/ANNIS/issues/433) Authentifaction errors for result
- [#431](https://github.com/korpling/ANNIS/issues/431) Error in regular expression triggers exception

## Version 3.3.5

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.3.5+is%3Aclosed

### Fixed Bugs
- [#430](https://github.com/korpling/ANNIS/issues/430) Instance font reset in AQL text field
- [#429](https://github.com/korpling/ANNIS/issues/429) exit error codes are uniform and not documented
- [#428](https://github.com/korpling/ANNIS/issues/428) annis-service-distribution.tar.gz can't be read from Python
- [#427](https://github.com/korpling/ANNIS/issues/427) admin UI: sometimes members are removed from group when a new item is added with the popup selector
- [#425](https://github.com/korpling/ANNIS/issues/425) stderr and stdout are not properly closed when annis service can't start
- [#422](https://github.com/korpling/ANNIS/issues/422) regression: AQL lost when query panel hidden


## Version 3.3.4

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.3.4+is%3Aclosed

### Fixed Bugs
- [#424](https://github.com/korpling/ANNIS/issues/424)  Flat query builder disfunctional

## Version 3.3.3

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.3.3+is%3Aclosed

### Fixed Bugs
- [#421](https://github.com/korpling/ANNIS/issues/421) AQL errors not cleared when new query is set from server side
- [#420](https://github.com/korpling/ANNIS/issues/420) Text in AQL editor might get replaced with an older version

## Version 3.3.2

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.3.2+is%3Aclosed

### Fixed Bugs
- [#419](https://github.com/korpling/ANNIS/issues/419) server install documentation says ANNIS requires JDK 7 only, but later versions should work as well
- [#418](https://github.com/korpling/ANNIS/issues/418) Page offset is not reset when executing a new query
- [#417](https://github.com/korpling/ANNIS/issues/417) AQL lost when query panel hidden.
- [#416](https://github.com/korpling/ANNIS/issues/416) Don't replace count result output with validation message

## Version 3.3.1

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.3.1+is%3Aclosed

### Fixed Bugs
- [#415](https://github.com/korpling/ANNIS/issues/415) Adding groups after creating a new user will invalidate password (admin UI)
- [#414](https://github.com/korpling/ANNIS/issues/414) NotSerializableException in authorization cache
- [#413](https://github.com/korpling/ANNIS/issues/413) Text export not working in ANNIS 3.3.0


## Version 3.3.0

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.3.0+is%3Aclosed


### Fixed Bugs
- [#412](https://github.com/korpling/ANNIS/issues/412) Umlaut at beginning or end of annotation name doesn't trigger a parser error
- [#411](https://github.com/korpling/ANNIS/issues/411) Whole UI is scrolled when using "page down"
- [#407](https://github.com/korpling/ANNIS/issues/407) Corpus information hangs, then 504 error
- [#404](https://github.com/korpling/ANNIS/issues/404) HTML visualization config parser error
- [#397](https://github.com/korpling/ANNIS/issues/397) Exception when "{" is part of the query
- [#396](https://github.com/korpling/ANNIS/issues/396) Matrix export slow when multiple corpora selected
- [#395](https://github.com/korpling/ANNIS/issues/395) Embedded font not working in query history
- [#394](https://github.com/korpling/ANNIS/issues/394) Frequencies fails on disjoint AQL
- [#391](https://github.com/korpling/ANNIS/issues/391) Export of metadata fails in CSV and Weka exporters
- [#389](https://github.com/korpling/ANNIS/issues/389) Login window contains ANNIS main interface

### Enhancements
- [#410](https://github.com/korpling/ANNIS/issues/410) Selected tab in main view should not look like a link
- [#408](https://github.com/korpling/ANNIS/issues/408) Moved help messages of exporters into the exporter classes themselves.
- [#398](https://github.com/korpling/ANNIS/issues/398) Added systemd unit file for ANNIS service.
- [#392](https://github.com/korpling/ANNIS/issues/392) Can't set numbers=false and metakeys simultaneously in Grid exporter
- [#385](https://github.com/korpling/ANNIS/issues/385) Syntax highlighting in AQL text editor
- [#378](https://github.com/korpling/ANNIS/issues/378) search for document name
- [#376](https://github.com/korpling/ANNIS/issues/376) Export corpora from database to SaltXML
- [#319](https://github.com/korpling/ANNIS/issues/319) support relANNIS 3.3 format
- [#285](https://github.com/korpling/ANNIS/issues/285) Refactor the current query settings logic
- [#282](https://github.com/korpling/ANNIS/issues/282) Frequency analysis on metadata
- [#268](https://github.com/korpling/ANNIS/issues/268) Limit and random result subsets
- [#78](https://github.com/korpling/ANNIS/issues/78) Farben und Rauten harmonisieren

### Other
- [#409](https://github.com/korpling/ANNIS/issues/409) Corrected reference to old pcc2 query in German. 
- [#388](https://github.com/korpling/ANNIS/issues/388) Embedding visualizations in external web pages - issue#226(1b)


## Version 3.2.3

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.2.3+is%3Aclosed

### Fixed Bugs
- [#399](https://github.com/korpling/ANNIS/issues/399) Firefox 37: no dependency visualization

## Version 3.2.2

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.2.2+is%3Aclosed

### Fixed Bugs
- [#384](https://github.com/korpling/ANNIS/issues/384) user that should be restricted to create new users can gain adminstration rights
- [#381](https://github.com/korpling/ANNIS/issues/381) fullscreen does not work for login window
- [#380](https://github.com/korpling/ANNIS/issues/380) can't import corpus if another corpus has a document with the same name
- [#379](https://github.com/korpling/ANNIS/issues/379) Authorization cache not cleared if new password is set

### Enhancements
- [#383](https://github.com/korpling/ANNIS/issues/383) allow to query the User object of administration API
- [#382](https://github.com/korpling/ANNIS/issues/382) update links to homepage

## Version 3.2.1

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.2.1+is%3Aclosed

### Fixed Bugs
- [#375](https://github.com/korpling/ANNIS/issues/375) admin UI: generated hash is not salted
- [#374](https://github.com/korpling/ANNIS/issues/374) admin UI: upload button available after failed login attempt
- [#373](https://github.com/korpling/ANNIS/issues/373) Highlighting failure in disjunction
- [#372](https://github.com/korpling/ANNIS/issues/372) Disjunction fails depending on order
- [#371](https://github.com/korpling/ANNIS/issues/371) Parallel query leads to spurious context tokens in tree view
- [#370](https://github.com/korpling/ANNIS/issues/370) Metadata in doc browser does not render HTM

## Version 3.2.0

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?q=milestone%3A3.2.0+is%3Aclosed

### Fixed Bugs
- [#368](https://github.com/korpling/ANNIS/issues/368) AQL operator window broken in Query Builder
- [#363](https://github.com/korpling/ANNIS/issues/363) Tutorial doesn't work in an added instance
- [#362](https://github.com/korpling/ANNIS/issues/362) Wrapping in the discourse visualizer fails when line ends in non-maximally underscored element
- [#359](https://github.com/korpling/ANNIS/issues/359) Example query builder does not escape parameters
- [#357](https://github.com/korpling/ANNIS/issues/357) ANNIS locks too many tables if querying multiple corpora
- [#356](https://github.com/korpling/ANNIS/issues/356) arch_dependency visualizer doesn't filter namespace/layer
- [#355](https://github.com/korpling/ANNIS/issues/355) Ignore resolver_vis_map entries with wrong corpus name on import
- [#334](https://github.com/korpling/ANNIS/issues/334) correct the info text for the "TextExporter" and "SimpleTextExporter"
- [#330](https://github.com/korpling/ANNIS/issues/330) "_c" parameter lost if corpus needs login
- [#320](https://github.com/korpling/ANNIS/issues/320) don't add invalid queries to warn.log
- [#311](https://github.com/korpling/ANNIS/issues/311) Corpus names don't allow spaces
- [#291](https://github.com/korpling/ANNIS/issues/291) Reload of restricted corpus URL after logout leads to null pointer exception

### Enhancements
- [#366](https://github.com/korpling/ANNIS/issues/366) feature request: allow deleting via corpus name
- [#354](https://github.com/korpling/ANNIS/issues/354) Issue333 html vis element order
- [#352](https://github.com/korpling/ANNIS/issues/352) configure login-page
- [#351](https://github.com/korpling/ANNIS/issues/351) expiration of accounts
- [#348](https://github.com/korpling/ANNIS/issues/348) Allow to display namespace in grid
- [#347](https://github.com/korpling/ANNIS/issues/347) Provide adminstration interface
- [#346](https://github.com/korpling/ANNIS/issues/346) Allow to copy corpora from existing installation
- [#345](https://github.com/korpling/ANNIS/issues/345) Allow to use different (PostgreSQL) schema
- [#339](https://github.com/korpling/ANNIS/issues/339) Removed progress bar from result view panel
- [#338](https://github.com/korpling/ANNIS/issues/338) arch_dependency visualizer does not scroll
- [#335](https://github.com/korpling/ANNIS/issues/335) Checkbox to disable time-out in Kickstarter
- [#333](https://github.com/korpling/ANNIS/issues/333) HTML visualizer: set element order for annotations w/ identical coverage
- [#318](https://github.com/korpling/ANNIS/issues/318) new "annotext" scheme
- [#317](https://github.com/korpling/ANNIS/issues/317) remove rank entries for continuous spans on import
- [#307](https://github.com/korpling/ANNIS/issues/307) Selective HTML visualizer graph fetching
- [#306](https://github.com/korpling/ANNIS/issues/306) Export metadata also in Text and SimpleText exporters
- [#290](https://github.com/korpling/ANNIS/issues/290) New AQL operator: "near"

### Other
- [#344](https://github.com/korpling/ANNIS/issues/344) Properly seperate matches in matches for nodes and matches for annotation on nodes


## Version 3.1.8

### Fixed Bugs
- [#350](https://github.com/korpling/ANNIS/issues/350) AQL normalization fails for node with two outgoing edge annotations

## Version 3.1.7

We would like to give special thanks to Adriane Boyd and Lari Lampen for providing the bug fixes for the issues #332 and #336.

Changelog is also available on GitHub:
https://github.com/korpling/annis/issues?milestone=17&state=closed

### Fixed Bugs
- [#340](https://github.com/korpling/ANNIS/issues/340) fixed bug in CorefVisualizer. The annotation name is now displayed
- [#336](https://github.com/korpling/ANNIS/issues/336) Existence of directory is not checked when saving corpus properties
- [#332](https://github.com/korpling/ANNIS/issues/332) document browser needs long time to load when there is a larger number of metadata

## Version 3.1.6
Changelog is also available on GitHub:
https://github.com/korpling/annis/issues?milestone=16&state=closed

### Fixed Bugs
- [#325](https://github.com/korpling/ANNIS/issues/325) fetching results (subgraphs) slow
- [#324](https://github.com/korpling/ANNIS/issues/324) Source code contains files with invalid (Windows) file names


## Version 3.1.5
Changelog is also available on GitHub:
https://github.com/korpling/annis/issues?milestone=15&state=closed

### Fixed Bugs
- [#323](https://github.com/korpling/ANNIS/issues/323) css style sheet is not read in HTML visualizer when started from document browser on kickstarter
- [#322](https://github.com/korpling/ANNIS/issues/322) Invalid regex search error is not caught by interface
- [#321](https://github.com/korpling/ANNIS/issues/321) negated metadata queries don't work any longer

## Version 3.1.4
Changelog is also available on GitHub:
https://github.com/korpling/annis/issues?milestone=14&state=closed

### Fixed Bugs
- [#316](https://github.com/korpling/ANNIS/issues/316) OR-queries with different number of nodes don't work any longer

## Version 3.1.3
Changelog is also available on GitHub:
https://github.com/korpling/annis/issues?milestone=13&state=closed

### Fixed Bugs
- [#314](https://github.com/korpling/ANNIS/issues/314) Frequency query needs statistics workaround, too
- [#313](https://github.com/korpling/ANNIS/issues/313) incorrect documentation for "graph" and "binary"
- [#310](https://github.com/korpling/ANNIS/issues/310) corpus.properties is not created if corpus has  "/" in name

### Enhancements
- [#315](https://github.com/korpling/ANNIS/issues/315) avoid joining the corpus table in "find"

### Other
- [#312](https://github.com/korpling/ANNIS/issues/312) Slash in corpus name causes config file not to be found


## Version 3.1.2
Changelog is also available on GitHub:
https://github.com/korpling/annis/issues?milestone=11&state=closed

### Fixed Bugs
- [#309](https://github.com/korpling/ANNIS/issues/309) corpora with token which have the same left or right text boundaries can't be imported

## Version 3.1.1
Changelog is also available on GitHub:
https://github.com/korpling/annis/issues?milestone=11&state=closed

### Fixed Bugs
- [#308](https://github.com/korpling/ANNIS/issues/308) corpus properties are fetched for sub-corpus

## Version 3.1.0
Changelog is also available on GitHub:
https://github.com/korpling/annis/issues?milestone=8&state=closed

### Fixed Bugs
- [#304](https://github.com/korpling/ANNIS/issues/304) Corpus migration on upgrade of kickstarter in windows fails
- [#302](https://github.com/korpling/ANNIS/issues/302) metadata in document browser doesn't work
- [#301](https://github.com/korpling/ANNIS/issues/301) Caching bug with document browser
- [#297](https://github.com/korpling/ANNIS/issues/297) notification when export is finished will be given to wrong user
- [#295](https://github.com/korpling/ANNIS/issues/295) browser fonts are broken
- [#292](https://github.com/korpling/ANNIS/issues/292) Match position in segmentations should count from 1, not 0
- [#287](https://github.com/korpling/ANNIS/issues/287) css styles from HTML vis are appended instead of replaced
- [#286](https://github.com/korpling/ANNIS/issues/286) metadata not sorted and no namespaces in document browser
- [#284](https://github.com/korpling/ANNIS/issues/284) Reference URL settings reset
- [#273](https://github.com/korpling/ANNIS/issues/273) Display error message when importing empty corpora
- [#271](https://github.com/korpling/ANNIS/issues/271) Search for empty values should be allowed
- [#270](https://github.com/korpling/ANNIS/issues/270) RST-Visualizer sentence numbers are shifted by -2
- [#263](https://github.com/korpling/ANNIS/issues/263) Inefficent subgraph retrievial
- [#261](https://github.com/korpling/ANNIS/issues/261) Document Browser produces NullPointerException, if the same document name is added twice to the document table

### Enhancements
- [#305](https://github.com/korpling/ANNIS/issues/305) Global option to disable right-to-left heuristic
- [#299](https://github.com/korpling/ANNIS/issues/299) Single colons for namespaces in grid/kwic  tooltip when hovering over an annotation
- [#289](https://github.com/korpling/ANNIS/issues/289) Embedded fonts don't work in statistics output
- [#288](https://github.com/korpling/ANNIS/issues/288) test and fix CommentHelper.readSDocument()
- [#281](https://github.com/korpling/ANNIS/issues/281) Empty tokens trigger island visualization in KWIC
- [#280](https://github.com/korpling/ANNIS/issues/280) Query builder operator behavior
- [#279](https://github.com/korpling/ANNIS/issues/279) Islands may cause spans not covering any tokens in grid
- [#278](https://github.com/korpling/ANNIS/issues/278) Empty space under arch dependency
- [#274](https://github.com/korpling/ANNIS/issues/274) Configure the doc browser via a json file, not in the corpus.properties file
- [#269](https://github.com/korpling/ANNIS/issues/269) Allow to abort exporters
- [#266](https://github.com/korpling/ANNIS/issues/266) CSV exporter
- [#265](https://github.com/korpling/ANNIS/issues/265) Hidden token annotations should be configurable corpus wide via the corpus.properties
- [#264](https://github.com/korpling/ANNIS/issues/264) corpus.properties files should have a more meaningful name
- [#262](https://github.com/korpling/ANNIS/issues/262) Annis import: measure progress in hours/minutes/seconds instead of milliseconds

### Other
- [#303](https://github.com/korpling/ANNIS/issues/303) What is the syntax for null namespace for metadata to display in document browser?
- [#267](https://github.com/korpling/ANNIS/issues/267) MD5 passwords don't work any longer in 3.1.0-SNAPSHOT

## Version 3.0.1

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?direction=asc&milestone=10&page=1&sort=created&state=closed


### Fixed Bugs
- [#221](https://github.com/korpling/ANNIS/issues/221) Precedence optimization fails when applied to spans which cover more than one token

## Version 3.0.0

**IMPORTANT**
This release changes the default user configuration directory from
/etc/annis/user_config_trunk/
to
/etc/annis/user_config/
Please either update your shiro.ini file to your custom location or
move the folder on your file system to the new default location.

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?direction=asc&milestone=1&page=1&sort=created&state=closed

### Fixed Bugs
- [#158](https://github.com/korpling/ANNIS/issues/158) Exception when using "#c=" fragment
- [#155](https://github.com/korpling/ANNIS/issues/155) Cannot create user specific corpus groups (3.0.0-rc.1)
- [#147](https://github.com/korpling/ANNIS/issues/147) test fails when building service
- [#145](https://github.com/korpling/ANNIS/issues/145) Presence of page annotation creates PDF icon in grid even if there is no PDF registered for the corpus
- [#144](https://github.com/korpling/ANNIS/issues/144) Cancel import in Kickstarter does not work (3.0.0-rc.1)
- [#143](https://github.com/korpling/ANNIS/issues/143) Kickstarter does not start if an older database exists (3.0.0-rc.1)
- [#141](https://github.com/korpling/ANNIS/issues/141) Right-to-left detection in grid visualizer doesn't work
- [#139](https://github.com/korpling/ANNIS/issues/139) example query not always sensitive to default segmentation
- [#137](https://github.com/korpling/ANNIS/issues/137) Match highlighting in KWIC is incorrect/missing in parallel corpus query of non-terminal elements 
- [#126](https://github.com/korpling/ANNIS/issues/126) Hit marking in KWIC for segmentations precedence queries is incorrect

### Enhancements
- [#157](https://github.com/korpling/ANNIS/issues/157) Add CorefVisualizer that is only using the result context
- [#42](https://github.com/korpling/ANNIS/issues/42) user-friendly message in "annis-service import" on duplicate corpus
- [#4](https://github.com/korpling/ANNIS/issues/4) Annotation explorer should also show available document metadata categories


## Version 3.0.0-rc.1

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?milestone=7&state=closed

### Fixed Bugs
- [#138](https://github.com/korpling/ANNIS/issues/138) Bug in arity operator
- [#135](https://github.com/korpling/ANNIS/issues/135) Example query table of the annis-gui should respect the instance config
- [#130](https://github.com/korpling/ANNIS/issues/130) Unable to invoke method click in com.vaadin.shared.ui.button.ButtonServerRpc
- [#129](https://github.com/korpling/ANNIS/issues/129) PDF-Vis should listen to namespaces and node_key
- [#125](https://github.com/korpling/ANNIS/issues/125) Segmentation precedence operator not working correctly
- [#122](https://github.com/korpling/ANNIS/issues/122) Context size and query result page size are no longer configurable in ANNIS3
- [#121](https://github.com/korpling/ANNIS/issues/121) GridExporter keys parameter does nothing
- [#120](https://github.com/korpling/ANNIS/issues/120) HTML visualization doesn't trigger on token annotations
- [#118](https://github.com/korpling/ANNIS/issues/118) font-size in discourse visualization is small (11px) --> change to definition in points? (pt)
- [#115](https://github.com/korpling/ANNIS/issues/115) Highlighting of matched tokens within matched tokens in a second color doesn't always work
- [#114](https://github.com/korpling/ANNIS/issues/114) Instruction anno="value" in HTML vis configuration does not work
- [#112](https://github.com/korpling/ANNIS/issues/112) Prefer query results to example queries.
- [#110](https://github.com/korpling/ANNIS/issues/110) Behavior of default segmentation setting for multiple selected corpora can lead to wrong results
- [#108](https://github.com/korpling/ANNIS/issues/108) UTF-8 encoding not working in Exporters
- [#106](https://github.com/korpling/ANNIS/issues/106) Embedded fonts don't work in corpus explorer and example queries tab

### Enhancements
- [#133](https://github.com/korpling/ANNIS/issues/133) default-text-segmentation -> default-context-segmentation
- [#124](https://github.com/korpling/ANNIS/issues/124) Allow if-missing argument for example queries.
- [#109](https://github.com/korpling/ANNIS/issues/109) Default segmentation for KWIC, search context and context size in relANNIS
- [#80](https://github.com/korpling/ANNIS/issues/80) HTML visualization
- [#57](https://github.com/korpling/ANNIS/issues/57) Corpus specific example queries

### Other
- [#134](https://github.com/korpling/ANNIS/issues/134) use Salt in a faster way
- [#132](https://github.com/korpling/ANNIS/issues/132) metakeys argument should be empty by default
- [#123](https://github.com/korpling/ANNIS/issues/123) Mysterious gap in grid visualizer 
- [#111](https://github.com/korpling/ANNIS/issues/111) FlatQueryBuilder
- [#103](https://github.com/korpling/ANNIS/issues/103) More efficient binary data streaming
- [#76](https://github.com/korpling/ANNIS/issues/76) export metadata only once per match


## Version 3.0.0-alpha.3

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?milestone=6&state=closed

### Fixed Bugs
- [#99](https://github.com/korpling/ANNIS/issues/99) Corpus explorer does not output alignment edges with no annotations
- [#98](https://github.com/korpling/ANNIS/issues/98) Grid is broken in parallel corpora
- [#97](https://github.com/korpling/ANNIS/issues/97) Discourse vis displays multiple texts continuously in parallel corpora
- [#58](https://github.com/korpling/ANNIS/issues/58) Simple text exporter is missing

### Enhancements
- [#93](https://github.com/korpling/ANNIS/issues/93) allow empty PostgreSQL administration password on initialization
- [#91](https://github.com/korpling/ANNIS/issues/91) A list of all metadata available in a corpus
- [#86](https://github.com/korpling/ANNIS/issues/86) Kickstarter Start Script for Mac
- [#66](https://github.com/korpling/ANNIS/issues/66) Arch dependencies based on annotations
- [#63](https://github.com/korpling/ANNIS/issues/63) Administrator-defined embedded fonts
- [#34](https://github.com/korpling/ANNIS/issues/34) Plug-able query builder
- [#9](https://github.com/korpling/ANNIS/issues/9) WEKA: export metadata

### Other
- [#96](https://github.com/korpling/ANNIS/issues/96) Allow to connect to remote databases that use SSL
- [#94](https://github.com/korpling/ANNIS/issues/94) Use asynchronous REST client
- [#90](https://github.com/korpling/ANNIS/issues/90) allow administrator-defined embedded fonts
- [#88](https://github.com/korpling/ANNIS/issues/88) allow to select corpus by URL
- [#85](https://github.com/korpling/ANNIS/issues/85) porting ANNIS to Vaadin7
- [#74](https://github.com/korpling/ANNIS/issues/74) Replace citation with a fragment based approach that allows bookmarking
- [#73](https://github.com/korpling/ANNIS/issues/73) Allow to define corpus sets per instance
- [#72](https://github.com/korpling/ANNIS/issues/72) fix #34 (plugable query builder) and introducing instance configuration
- [#71](https://github.com/korpling/ANNIS/issues/71) restrict metadata selection in WekaExporter
- [#70](https://github.com/korpling/ANNIS/issues/70) replace bigint with integer on most columns
- [#69](https://github.com/korpling/ANNIS/issues/69) Query optimizing: transitive precedence
- [#68](https://github.com/korpling/ANNIS/issues/68) The text id should not be globally unique (only relative to document id

## Version 3.0.0-alpha.2

First public alpha release in the new improved ANNIS 3.0 release series.

Changelog is also available on GitHub:
https://github.com/korpling/ANNIS/issues?milestone=4&state=closed

### Fixed Bugs
- [#31](https://github.com/korpling/ANNIS/issues/31) Regex behavior in Falko metadata is incorrect
- [#12](https://github.com/korpling/ANNIS/issues/12) Staging area tables are not deleted after successfull import
- [#1](https://github.com/korpling/ANNIS/issues/1) Regex bug with initial optional parentheses
- [#2](https://github.com/korpling/ANNIS/issues/2) import: level for dominance edges might be not set

### Enhancements
- [#54](https://github.com/korpling/ANNIS/issues/54) Count number of documents which have tupels matching the query.
- [#53](https://github.com/korpling/ANNIS/issues/53) Sort results by the token order of the matches
- [#52](https://github.com/korpling/ANNIS/issues/52) Vaadin based GUI
- [#33](https://github.com/korpling/ANNIS/issues/33) Re-organize ANNIS manual
- [#32](https://github.com/korpling/ANNIS/issues/32) improve co-existence with Vaadin logging
- [#30](https://github.com/korpling/ANNIS/issues/30) Bug Report button in GUI
- [#22](https://github.com/korpling/ANNIS/issues/22) button for deleting all nodes in query builder
- [#18](https://github.com/korpling/ANNIS/issues/18) Hits should be numbered in result window
- [#16](https://github.com/korpling/ANNIS/issues/16) Results in WEKA should be sorted
- [#13](https://github.com/korpling/ANNIS/issues/13) missing postgres admin password
- [#7](https://github.com/korpling/ANNIS/issues/7) Firefox does not save the login information
- [#8](https://github.com/korpling/ANNIS/issues/8) Configurable edge names for Tiger tree visualizer
- [#3](https://github.com/korpling/ANNIS/issues/3) Allow larger strings in annotation values

### Other
- [#51](https://github.com/korpling/ANNIS/issues/51) fixed package declaration error
- [#48](https://github.com/korpling/ANNIS/issues/48) Improving the speed of the subgraph query
- [#44](https://github.com/korpling/ANNIS/issues/44) Move authentification and authorization from frontend to service
- [#37](https://github.com/korpling/ANNIS/issues/37) Allow to migrate corpora in database initialization


[Unreleased]: https://github.com/korpling/ANNIS/compare/v4.9.5...HEAD
[4.9.5]: https://github.com/korpling/ANNIS/compare/v4.9.4...v4.9.5
[4.9.4]: https://github.com/korpling/ANNIS/compare/v4.9.3...v4.9.4
[4.9.3]: https://github.com/korpling/ANNIS/compare/v4.9.2...v4.9.3
[4.9.2]: https://github.com/korpling/ANNIS/compare/v4.9.1...v4.9.2
[4.9.1]: https://github.com/korpling/ANNIS/compare/v4.9.0...v4.9.1
[4.9.0]: https://github.com/korpling/ANNIS/compare/v4.8.0...v4.9.0
[4.8.0]: https://github.com/korpling/ANNIS/compare/v4.7.1...v4.8.0
[4.7.1]: https://github.com/korpling/ANNIS/compare/v4.7.0...v4.7.1
[4.7.0]: https://github.com/korpling/ANNIS/compare/v4.6.7...v4.7.0
[4.6.7]: https://github.com/korpling/ANNIS/compare/v4.6.6...v4.6.7
[4.6.6]: https://github.com/korpling/ANNIS/compare/v4.6.5...v4.6.6
[4.6.5]: https://github.com/korpling/ANNIS/compare/v4.6.4...v4.6.5
[4.6.4]: https://github.com/korpling/ANNIS/compare/v4.6.3...v4.6.4
[4.6.3]: https://github.com/korpling/ANNIS/compare/v4.6.2...v4.6.3
[4.6.2]: https://github.com/korpling/ANNIS/compare/v4.6.1...v4.6.2
[4.6.1]: https://github.com/korpling/ANNIS/compare/v4.6.0...v4.6.1
[4.6.0]: https://github.com/korpling/ANNIS/compare/v4.5.3...v4.6.0
[4.5.3]: https://github.com/korpling/ANNIS/compare/v4.5.2...v4.5.3
[4.5.2]: https://github.com/korpling/ANNIS/compare/v4.5.1...v4.5.2
[4.5.1]: https://github.com/korpling/ANNIS/compare/v4.5.0...v4.5.1
[4.5.0]: https://github.com/korpling/ANNIS/compare/v4.4.0...v4.5.0
[4.4.0]: https://github.com/korpling/ANNIS/compare/v4.3.1...v4.4.0
[4.3.1]: https://github.com/korpling/ANNIS/compare/v4.3.0...v4.3.1
[4.3.0]: https://github.com/korpling/ANNIS/compare/v4.2.0...v4.3.0
[4.2.0]: https://github.com/korpling/ANNIS/compare/v4.1.4...v4.2.0
[4.1.4]: https://github.com/korpling/ANNIS/compare/v4.1.3...v4.1.4
[4.1.3]: https://github.com/korpling/ANNIS/compare/v4.1.2...v4.1.3
[4.1.2]: https://github.com/korpling/ANNIS/compare/v4.1.1...v4.1.2
[4.1.1]: https://github.com/korpling/ANNIS/compare/v4.1.0...v4.1.1
[4.1.0]: https://github.com/korpling/ANNIS/compare/v4.0.0...v4.1.0
[4.0.0]: https://github.com/korpling/ANNIS/compare/v4.0.0-beta.6...v4.0.0
[4.0.0-beta.6]: https://github.com/korpling/ANNIS/compare/v4.0.0-beta.5...v4.0.0-beta.6
[4.0.0-beta.5]: https://github.com/korpling/ANNIS/compare/v4.0.0-beta.4...v4.0.0-beta.5
[4.0.0-beta.4]: https://github.com/korpling/ANNIS/compare/v4.0.0-beta.3...v4.0.0-beta.4
[4.0.0-beta.3]: https://github.com/korpling/ANNIS/compare/v4.0.0-beta.2...v4.0.0-beta.3
[4.0.0-beta.2]: https://github.com/korpling/ANNIS/compare/v4.0.0-beta.1...v4.0.0-beta.2
