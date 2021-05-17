@SignUpFeature
Feature: Sign Up Feature
  Background:
    Given testing CreateOrganization functionality of Organization
    Given with organizationData for
      | orgName         | website            |
      | Xventure Org.   | www.xventure.io    |
      | Microsoft       | www.microsoft.com  |
      | Apple Inc.      | www.apple.com      |

  Scenario: Valid Registration Form Information
    Given testing CreateOrganization functionality of Organization


  