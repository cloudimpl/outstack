/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.service;

import com.cloudimpl.outstack.domain.example.Organization;
import com.cloudimpl.outstack.outstack.spring.test.ResourceLoader;
import com.cloudimpl.outstack.outstack.spring.test.data.TableDataMapper;
import com.cloudimpl.outstack.runtime.EntityCommandHandler;
import com.cloudimpl.outstack.runtime.Handler;
import com.cloudimpl.outstack.runtime.domainspec.Entity;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;

import java.util.List;
import java.util.Map;

/**
 * @author nuwan
 */
public class SpringServiceHandlerTestDriver {

    public static ResourceLoader resourceLoader = new ResourceLoader();

    private Handler<Organization> organizationHandler;

    @Given("testing {word} functionality of {word}")
    public void initHandler(String handlerName, String entity) {
        Class<? extends EntityCommandHandler> commandHandlerType = resourceLoader.getMapCmdHandlers().get(handlerName);
        Class<? extends Entity> entityType = resourceLoader.getMapEntities().get(entity);

        System.out.println("handler :" + handlerName + " for entity : " + entity);
    }

    @Given("with organizationData for")
    public void withOrganizationDataFor(DataTable dataTable) {
        List<Map<String, String>> maps = dataTable.asMaps(String.class, String.class);
        TableDataMapper<Organization> dataMapper = new TableDataMapper<>(Organization.class);

        List<Organization> organizations = dataMapper.mapData(maps);

        System.out.println(organizations);
    }
}
