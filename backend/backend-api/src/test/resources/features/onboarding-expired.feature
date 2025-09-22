Feature: Provisioning not completed before deadline -> EXPIRED

  Background:
    * url karate.get('baseUrl')

  Scenario: PREPARATION exceeds deadline and becomes EXPIRED
    # REQUESTED
    * def payload =
      """
      {
        "producerName": "Pluto Co",
        "email": "owner@pluto.test",
        "website": "https://pluto.test",
        "vatOrFiscalCode": "ITSLOW0000000"
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

    # REGISTRATION con hint che simula un provisioning che non termina (es. "never-ready")
    * def reg =
      """
      {
        "producerAdminEmail": "admin@pluto.test",
        "tenantIdHint": "pluto-never-ready",
        "initialUsers": []
      }
      """
    Given path '/onboarding/requests', id, 'register-producer'
    And header Authorization = karate.get('userAuth')
    And request reg
    When method POST
    Then status 202
    And match response.state == 'PREPARATION'

    # Poll fino a EXPIRED (deadline accorciata nel profilo test, es. 10s)
    * configure retry = { count: 40, interval: 500 }
    Given path '/onboarding/requests', id
    And retry until response.state == 'EXPIRED'
    When method GET
    Then status 200
    And match response.state == 'EXPIRED'
