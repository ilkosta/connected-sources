Feature: Curator-approved two-step onboarding (happy path)

  Background:
    * configure url = karate.get('baseUrl')
#    * header Authorization = 'Basic ' + karate.base64('admin:cs123!')

  Scenario: REQUESTED -> APPROVED -> PREPARATION -> ENABLED
    * print '# 1) REQUESTED (utente registrato)'
    * def payload =
    """
    {
      "producerName": "pippo SpA",
      "email": "costantin.giuliodori@studenti.unicam.it",
      "website": "https://pippo.test",
      "vatOrFiscalCode": "ITqazwsxedcrfv"
    }
    """
    Given path '/onboarding/requests'
    And header Authorization = karate.get('userAuth')
    And request payload
    And configure connectTimeout = 500000
    And configure readTimeout = 500000
    When method POST
    Then status 202
    And match response contains { id: '#number', state: 'REQUESTED' }
    * def requestId = response.id

    * print '2) APPROVED (curator)'

    Given path `/onboarding/requests/${requestId}/approve`
    And header Authorization = karate.get('curatorAuth')

    When method POST
    Then status 200
    And match response contains { id: '#(requestId)', state: 'APPROVED' }
    * def token = response.token

    * print '3) REGISTRATION -- riutilizzo il token della resp'
    * def reg =
    """
    {
      "producerAdminEmail": "costantino.giuliodori@gmail.com",
      "tenantIdHint": "pippo-spa",
      "initialUsers": [3]
    }
    """
    Given path '/onboarding/requests', requestId, 'register-producer'
    And param token = token
    And header Authorization = karate.get('userAuth')
    And request reg
    When method POST
    Then status 202
    And match response contains { id: '#(requestId)', state: 'PREPARATION' }

    * print '4) Poll fino a ENABLED (provisioning async)'
#    fino a 30s
    * configure retry = { count: 2, interval: 10000 }
    Given path `/onboarding/requests/${requestId}/status`
    And header Authorization = karate.get('userAuth')
    And retry until response.state == 'ENABLED'
    When method GET
    Then status 200
    And match response contains { id: '#(requestId)', state: 'ENABLED' }
