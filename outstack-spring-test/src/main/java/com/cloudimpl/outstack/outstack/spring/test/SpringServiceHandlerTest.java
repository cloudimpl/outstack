/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cloudimpl.outstack.outstack.spring.test;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 *
 * @author nuwan
 */


@RunWith(Cucumber.class)
@CucumberOptions(
  features = "src/test/feature",
  glue = {"com.cloudimpl.outstack.outstack.spring.test"}
  //plugin = {"json:target/cucumber.json"}
)
public class SpringServiceHandlerTest {

}