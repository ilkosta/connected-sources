Feature: Provisioning failure -> FAILED + curator notification

  Background:
    * url karate.get('baseUrl')

  Scenario: Permanent failure during provisioning leads to FAILED
    # REQUESTED
    * def payload =
      """
      {
        "producerName": "Bad Co",
        "email": "owner@badco.test",
        "website": "https://badco.test",
        "vatOrFiscalCode": "ITBAD0000000"
      }
      """
    Given path '/onboarding/requests'
    And header Authorization = karate.get('userAuth')
    And request payload
    When method POST
    Then status 202
    * def id = response.id

    # APPROVED
    Given path '/onboarding/requests', id, 'approve'
    And header Authorization = karate.get('curatorAuth')
    When method POST
    Then status 200

    # REGISTRATION con hint che forza failure in test
    * def reg =
      """
      {
        "producerAdminEmail": "admin@badco.test",
        "tenantIdHint": "badco-fail-perm",
        "initialUsers": []
      }
      """
    Given path '/onboarding/requests', id, 'register-producer'
    And header Authorization = karate.get('userAuth')
    And request reg
    When method POST
    Then status 202
    And match response.state == 'PREPARATION'

    # Poll fino a FAILED
    * configure retry = { count: 30, interval: 1000 }
    Given path '/onboarding/requests', id
    And retry until response.state == 'FAILED'
    When method GET
    Then status 200
    And match response.state == 'FAILED'
