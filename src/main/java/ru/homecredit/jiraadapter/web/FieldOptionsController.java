package ru.homecredit.jiraadapter.web;

import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import ru.homecredit.jiraadapter.dto.FieldOptions;
import ru.homecredit.jiraadapter.dto.request.FieldOptionsRequest;
import ru.homecredit.jiraadapter.dto.response.FieldOptionsResponse;
import ru.homecredit.jiraadapter.service.FieldOptionsService;
import javax.ws.rs.core.Response;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * The core class for handling GET and POST requests to /options endpoint
 */
@Path("/options")
@Named
@Slf4j
public class FieldOptionsController {

    private final FieldOptionsService fieldOptionsService;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * constructor initialises logger and puts the received Jira beans to fields
     */
    @Inject
    public FieldOptionsController(FieldManager fieldManager,
                                  ProjectManager projectManager,
                                  OptionsManager optionsManager,
                                  PluginSettingsFactory pluginSettingsFactory) {
        log.trace("starting FieldOptionsController instance construction");
        log.warn(FieldOptionsController.class.getName());
        fieldOptionsService = new FieldOptionsService(fieldManager,
                                                      projectManager,
                                                      optionsManager,
                                                      pluginSettingsFactory);
    }

    /**
     * GET request is used to receive the list of options for customfield in
     * given context. Context is defined by project key and issue type id
     * the request url for GET request looks like:
     * http://{hostname}/jira/rest/cfoptchange/1.0/options?fieldKey={jira field key}&projKey={
     * jira project key}&issueTypeId={issue type id}
     * http://localhost:2990/jira/rest/cfoptchange/1.0/options?fieldKey=customfield_10000&projKey=test&issueTypeId=10000
     *
     * @param fieldKey - jira customfield key like - customfield_10000
     * @param projectKey - jira project key like TES
     * @param issueTypeId - jira issue type id like 10000
     * @return xml response in format, defined in FieldOptionToXML class
     */
    @GET
    @AnonymousAllowed
    @Produces(MediaType.APPLICATION_JSON)
    public Response doGet(@QueryParam("fieldKey") String fieldKey,
                                           @QueryParam("projectKey") String projectKey,
                                           @QueryParam("issueTypeId") String issueTypeId) {
        log.trace("************* starting doGet method... ************");
        FieldOptions fieldOptions = fieldOptionsService.initializeFieldOptions(
                new FieldOptionsRequest(fieldKey,
                                        projectKey,
                                        issueTypeId));
        String jsonResponse = gson.toJson(new FieldOptionsResponse(fieldOptions));
        log.info(jsonResponse);
        return Response.ok(jsonResponse).build();
    }

    /**
     * method for handling POST request to /options endpoint (adding and
     * enabling/disabling given field option)
     * @param requestBody - string in Json format with request parameters,
     *  same as for GET request with an extra parameter newOption (option value)
     * @return Response object
     */
    @POST
    @AnonymousAllowed
    @Produces(MediaType.APPLICATION_JSON)
    public Response doPost(String requestBody) {
        log.trace("************ starting doPost method... **************");
        FieldOptions fieldOptions = fieldOptionsService.postOption(requestBody);
        return (fieldOptions == null)
            ? javax.ws.rs.core.Response.ok("something goes wrong. Check log file").build()
            : javax.ws.rs.core.Response.ok(gson.toJson(new FieldOptionsResponse(fieldOptions))).build();
    }
}