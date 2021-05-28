/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.domain.example.commands.OrganizationCreateRequest;
import com.cloudimpl.outstack.outstack.spring.test.data.TableDataMapper;
import com.cloudimpl.outstack.outstack.spring.test.runtime.TestContext;
import com.cloudimpl.outstack.runtime.domainspec.DomainEventException;
import com.cloudimpl.outstack.runtime.domainspec.RootEntity;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

/**
 * @author nuwan
 */
public class SpringServiceHandlerTestDriver {

    private TestContext<? extends RootEntity> testContext;

    @Given("testing {word} functionality of {word}")
    public void initHandler(String handlerName, String entity) {
        this.testContext = new TestContext<>();
        testContext.initialize(handlerName, entity);
        System.out.println("handler :" + handlerName + " for entity : " + entity);
    }

    @Given("organization data")
    public void withOrganizationDataFor(DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);

        TableDataMapper<Organization> dataMapper = new TableDataMapper<>(Organization.class);

        List<Organization> entities = dataMapper.mapData(maps);
        for (Organization organization: entities) {
            OrganizationCreateRequest organizationCreateRequest = OrganizationCreateRequest.builder()
                    .withOrgName(organization.getOrgName())
                    .withCommandHandlerName("CreateOrganization")
                    .withVersion("V1")
                    .build();
            testContext.executeCommand(organizationCreateRequest);
        }
    }

    @When("user creates an organization with name {word}")
    public void aboveOrganizationDetailsWhenUserCreatesAnOrganizationWithName(String organizationName) {
        try {
            OrganizationCreateRequest organizationCreateRequest = OrganizationCreateRequest.builder()
                    .withOrgName(organizationName)
                    .withVersion("V1")
                    .withCommandHandlerName("CreateOrganization")
                    .build();
            testContext.executeCommand(organizationCreateRequest);
        } catch (RuntimeException e) {
            testContext.setContextException(e);
        }
    }

    @Then("Request should be rejected")
    public void requestShouldBeRejected() {
        Assert.isTrue(testContext.hasExceptionThrown(DomainEventException.class));
    }

    @When("user creates an organization with name {word} and website {word}")
    public void userCreatesAnOrganizationWithNameAndWebsite(String organizationName, String website) {
        try {
            OrganizationCreateRequest organizationCreateRequest = OrganizationCreateRequest.builder()
                    .withOrgName(organizationName)
                    .withWebsite(website)
                    .withVersion("V1")
                    .withCommandHandlerName("CreateOrganization")
                    .build();
            testContext.executeCommand(organizationCreateRequest);
        } catch (RuntimeException e) {
            testContext.setContextException(e);
        }
    }

    @Then("Request should be successful")
    public void requestShouldBeSuccessful() {
        Assert.isTrue(!testContext.hasExceptionThrown(RuntimeException.class));
    }
}
