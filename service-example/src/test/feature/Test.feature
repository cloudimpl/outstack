@SignUpFeature
Feature: Sign Up Feature
  Background:
    Given testing CreateOrganization functionality of Organization
    Given organization data
      | orgName         | website            |
      | orgxventure     | www.xventure.io    |
      | microsoft       | www.microsoft.com  |

  Scenario: Create Organization with Existing Organization Name
    When user creates an organization with name microsoft
    Then Request should be rejected

  Scenario: Create Organization with New Organization Name
    When user creates an organization with name xventure and website www.xventure.org
    Then Request should be successful