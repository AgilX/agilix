package fr.agilix.services.cucumber.stepdefs;

import fr.agilix.services.ServicesApp;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.ResultActions;

import org.springframework.boot.test.context.SpringBootTest;

@WebAppConfiguration
@SpringBootTest
@ContextConfiguration(classes = ServicesApp.class)
public abstract class StepDefs {

    protected ResultActions actions;

}
