Feature: Curator-approved two-step onboarding (happy path)

  Background:
    * url karate.get('baseUrl')

  Scenario: REQUESTED -> APPROVED -> PREPARATION -> ENABLED
    # 1) REQUESTED (utente registrato)
    * def payload =
      """
      {
        "producerName": "pippo SpA",
        "email": "owner@pippo.test",
        "website": "https://pippo.test",
        "vatOrFiscalCode": "IT12345678901"
      }
      """
    Given path '/onboarding/requests'
    And header Authorization = karate.get('userAuth')
    And request payload
    When method POST
    Then status 202
    And match response == { id: '#number', state: 'REQUESTED' }
    * def requestId = response.id

    # 2) APPROVED (curator)
    Given path '/onboarding/requests', requestId, 'approve'
    And header Authorization = karate.get('curatorAuth')
    When method POST
    Then status 200
    And match response == { id: '#(requestId)', state: 'APPROVED' }

    # 3) REGISTRATION (link e token sono interni; in test usiamo payload diretto)
    * def reg =
      """
      {
        "producerAdminEmail": "admin@pippo.test",
        "tenantIdHint": "pippo-spa",
        "initialUsers": [101, 102]
      }
      """
    Given path '/onboarding/requests', requestId, 'register-producer'
    And header Authorization = karate.get('userAuth')
    And request reg
    When method POST
    Then status 202
    And match response == { id: '#(requestId)', state: 'PREPARATION' }

    # 4) Poll fino a ENABLED (provisioning async)
    * configure retry = { count: 30, interval: 1000 } # fino a 30s
    Given path '/onboarding/requests', requestId
    And retry until response.state == 'ENABLED'
    When method GET
    Then status 200
    And match response == { id: '#(requestId)', state: 'ENABLED' }
