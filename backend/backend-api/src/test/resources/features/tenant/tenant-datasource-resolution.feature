Feature: Tenant-aware datasource resolution

  Background:
    * url baseUrl
    * def endpoint = '/api/test/ping'

  Scenario: Request without tenant header should return 400
    Given path endpoint
    When method post
    Then status 400
    And match response contains 'Tenant not set'

  Scenario: Request with new producer ID should return 200 and provision tenant
    Given path endpoint
    And header X-Producer-Id = 'producer-karate-001'
    When method post
    Then status 200
    And match response == 'pong'

  Scenario: Requests to different producers should resolve to distinct datasources
    Given path endpoint
    And header X-Producer-Id = 'tenant-karate-A'
    When method post
    Then status 200
    And match response == 'pong'

    Given path endpoint
    And header X-Producer-Id = 'tenant-karate-B'
    When method post
    Then status 200
    And match response == 'pong'

  Scenario: Check full tenant status with datasource info
    Given path '/api/tenant/status'
    And header X-Producer-Id = 'karate-tenant-status'
    When method get
    Then status 200
    And match response.tenantId == 'karate-tenant-status'
    And match response.dbProduct contains 'SQLite'
    And match response.url contains 'karate-tenant-status'