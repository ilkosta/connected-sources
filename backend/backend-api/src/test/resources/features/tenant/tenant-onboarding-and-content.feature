Feature: Tenant onboarding and content creation

  Background:
    * url baseUrl
    * def pingEndpoint = '/api/test/ping'
    * def contentEndpoint = '/api/content'

  Scenario: Tenant is provisioned and can create content
    Given path pingEndpoint
    And header X-Producer-Id = 'producer-karate-content'
    When method post
    Then status 200
    And match response == 'pong'

    Given path contentEndpoint
    And header X-Producer-Id = 'producer-karate-content'
    And request { "title": "Hello Karate", "body": "Test Content" }
    When method post
    Then status 201
    And match response.title == "Hello Karate"
    And match response.body == "Test Content"
